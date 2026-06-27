package com.tamp.auth.service;

import com.tamp.auth.entity.Role;
import com.tamp.auth.entity.User;
import com.tamp.auth.repository.UserRepository;
import com.tamp.client.entity.Client;
import com.tamp.client.repository.ClientRepository;
import com.tamp.common.constants.ErrorCode;
import com.tamp.common.exception.BizException;
import com.tamp.common.util.PasswordUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;

/**
 * 认证业务服务
 */
@Service
public class AuthService {

    private static final Logger log = LoggerFactory.getLogger(AuthService.class);

    private static final String SMS_CODE_KEY_PREFIX = "sms:code:";

    private final UserRepository userRepository;
    private final JwtTokenService jwtTokenService;
    private final StringRedisTemplate redisTemplate;
    private final ClientRepository clientRepository;
    private final JdbcTemplate jdbcTemplate;

    /**
     * 开发环境万能验证码（仅当配置非空时生效，生产环境留空即可禁用）
     */
    @Value("${tamp.sms.dev-bypass-code:}")
    private String devBypassCode;

    public AuthService(UserRepository userRepository,
                       JwtTokenService jwtTokenService,
                       StringRedisTemplate redisTemplate,
                       ClientRepository clientRepository,
                       JdbcTemplate jdbcTemplate) {
        this.userRepository = userRepository;
        this.jwtTokenService = jwtTokenService;
        this.redisTemplate = redisTemplate;
        this.clientRepository = clientRepository;
        this.jdbcTemplate = jdbcTemplate;
    }

    /**
     * 密码登录
     *
     * @param phone    手机号
     * @param password 明文密码
     * @return 登录结果 [token, refreshToken, needChangePassword]
     */
    public LoginResult loginByPassword(String phone, String password) {
        User user = userRepository.findByPhone(phone)
                .orElseThrow(() -> new BizException(ErrorCode.AUTH_LOGIN_FAILED));

        checkUserStatus(user);

        if (!PasswordUtils.matches(password, user.getPassword())) {
            throw new BizException(ErrorCode.AUTH_LOGIN_FAILED);
        }

        return doLogin(user);
    }

    /**
     * 手机号+验证码登录
     *
     * @param phone   手机号
     * @param smsCode 验证码
     * @param shopId  投资人端定向访问的店铺 ID（可空，非投资人端登录时为 null）
     * @return 登录结果
     */
    @Transactional
    public LoginResult loginBySms(String phone, String smsCode, Long shopId) {
        verifySmsCode(phone, smsCode);

        // 投资人端语义：验证码通过即登录/注册。手机号不存在则自动注册为 INVESTOR
        User user = userRepository.findByPhone(phone).orElseGet(() -> {
            User u = new User();
            u.setPhone(phone);
            u.setRole(Role.INVESTOR);
            // 短信注册用户无密码概念，置一个占位哈希；passwordChanged=1 避免被强制改密码
            u.setPassword(PasswordUtils.encode("INVESTOR_SMS_REGISTERED_PLACEHOLDER"));
            u.setPasswordChanged(1);
            u.setStatus(0);
            log.info("[投资人端] 新用户注册 | phone={}", phone);
            return userRepository.save(u);
        });

        if (user.getRole() != Role.INVESTOR) {
            throw new BizException(ErrorCode.AUTH_SMS_LOGIN_NOT_ALLOWED);
        }

        checkUserStatus(user);

        // 投资人端定向访问店铺：绑定到 shopId（查/创建 client 记录）
        if (shopId != null) {
            bindClientToShop(user, phone, shopId);
            // 更新 user 的 shopId 为本次访问的店（用于 JWT 生成和后续接口鉴权）
            user.setShopId(shopId);
            userRepository.save(user);
        }

        return doLogin(user);
    }

    /**
     * 绑定投资人到店铺：若该 (shopId, phone) 尚无 client 记录则创建一条。
     * 同一手机号可在多个店各有独立 client 记录（一对多）。
     * 用 JdbcTemplate 直接查 biz_shop 表拿 office_id，避免 tamp-auth 依赖 tamp-organization（循环依赖）。
     */
    private void bindClientToShop(User user, String phone, Long shopId) {
        if (clientRepository.findByShopIdAndPhoneAndDeleted(shopId, phone, 0).isPresent()) {
            // 已存在该店该手机的 client 记录，跳过
            return;
        }
        // 直接查 biz_shop 表拿 office_id，避免引入 tamp-organization 依赖
        java.util.List<Map<String, Object>> rows = jdbcTemplate.queryForList(
                "SELECT id, office_id FROM biz_shop WHERE id = ? AND is_deleted = 0", shopId);
        if (rows.isEmpty()) {
            throw new BizException(ErrorCode.BIZ_SHOP_NOT_FOUND);
        }
        Long officeId = null;
        Object officeIdVal = rows.get(0).get("office_id");
        if (officeIdVal instanceof Number) {
            officeId = ((Number) officeIdVal).longValue();
        }
        Client client = new Client();
        client.setName(generateDefaultNickname(phone));
        client.setPhone(phone);
        client.setShopId(shopId);
        client.setOfficeId(officeId);
        client.setStatus(0);
        client.setSource("投资人端自助注册");
        clientRepository.save(client);
        log.info("[投资人端] 创建 client 记录 | phone={}, shopId={}, officeId={}",
                phone, shopId, officeId);
    }

    /**
     * 生成默认昵称（中性，提示用户修改）
     * 使用手机号后4位作为标识，避免产生误导性名称
     */
    private String generateDefaultNickname(String phone) {
        // 使用手机号后4位作为默认昵称前缀
        // 格式："投资人" + 后4位，简单明了，提示这是默认值
        String suffix = phone.substring(phone.length() - 4);
        String nickname = "投资人" + suffix;

        log.info("[generateDefaultNickname] phone={} → 默认昵称={}", phone, nickname);
        log.info("提示: 投资人可在个人中心修改此昵称");

        return nickname;
    }

    /**
     * 执行登录逻辑（生成 Token）
     */
    private LoginResult doLogin(User user) {
        String accessToken = jwtTokenService.generateAccessToken(
                user.getId(), user.getPhone(),
                user.getRole().name(), user.getOfficeId(), user.getShopId(),
                user.getOfficeIds()
        );
        String refreshToken = jwtTokenService.generateRefreshToken(user.getId().toString());

        // 更新最后登录信息
        user.setLastLoginTime(java.time.LocalDateTime.now());
        userRepository.save(user);

        boolean needChangePassword = user.getPasswordChanged() == 0;

        return new LoginResult(accessToken, refreshToken, needChangePassword,
                user.getId(), user.getPhone(), user.getRole().name(),
                user.getRealName(), user.getAvatar(), user.getOfficeId(), user.getShopId());
    }

    /**
     * 修改密码
     */
    @Transactional
    public void changePassword(Long userId, String oldPassword, String newPassword) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BizException(ErrorCode.UNKNOWN_ERROR));

        if (!PasswordUtils.matches(oldPassword, user.getPassword())) {
            throw new BizException(ErrorCode.AUTH_OLD_PASSWORD_WRONG);
        }

        user.setPassword(PasswordUtils.encode(newPassword));
        user.setPasswordChanged(1);
        userRepository.save(user);
    }

    /**
     * 找回密码（通过手机号+验证码重置）
     */
    @Transactional
    public void resetPassword(String phone, String smsCode, String newPassword) {
        verifySmsCode(phone, smsCode);

        User user = userRepository.findByPhone(phone)
                .orElseThrow(() -> new BizException(ErrorCode.AUTH_PHONE_EXISTS));

        user.setPassword(PasswordUtils.encode(newPassword));
        user.setPasswordChanged(1);
        userRepository.save(user);
    }

    /**
     * 登出（将 Token 加入黑名单）
     */
    public void logout(String token) {
        jwtTokenService.blacklistToken(token);
    }

    /**
     * 刷新 Token
     */
    public String[] refreshTokens(String oldToken) {
        return jwtTokenService.refreshTokens(oldToken);
    }

    /**
     * 根据 ID 查找用户
     */
    public User findById(Long userId) {
        return userRepository.findById(userId).orElse(null);
    }

    /**
     * 分页查询用户列表
     */
    public Page<User> findUsers(Pageable pageable) {
        return userRepository.findAll(pageable);
    }

    /**
     * 校验短信验证码（校验后删除，防重放）
     * <p>
     * 开发便利：当配置了 {@code tamp.sms.dev-bypass-code} 时，输入该万能验证码可直接通过，
     * 无需真正发送短信。生产环境应将该配置留空以禁用此能力。
     *
     * @param phone   手机号
     * @param smsCode 用户输入的验证码
     */
    private void verifySmsCode(String phone, String smsCode) {
        if (devBypassCode != null && !devBypassCode.isBlank()
                && devBypassCode.equals(smsCode)) {
            log.warn("[DEV] 使用万能验证码登录 | phone={}", phone);
            redisTemplate.delete(SMS_CODE_KEY_PREFIX + phone);
            return;
        }

        String key = SMS_CODE_KEY_PREFIX + phone;
        String cachedCode = redisTemplate.opsForValue().get(key);

        if (cachedCode == null) {
            throw new BizException(ErrorCode.AUTH_SMS_CODE_EXPIRED);
        }

        if (!cachedCode.equals(smsCode)) {
            throw new BizException(ErrorCode.AUTH_SMS_CODE_WRONG);
        }

        redisTemplate.delete(key);
    }

    /**
     * 检查用户状态
     */
    private void checkUserStatus(User user) {
        if (user.getStatus() == 1) {
            throw new BizException(ErrorCode.AUTH_USER_DISABLED);
        }
    }
}
