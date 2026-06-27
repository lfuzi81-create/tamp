package com.tamp.auth.controller;

import com.tamp.auth.entity.Role;
import com.tamp.auth.service.AuthService;
import com.tamp.auth.service.LoginResult;
import com.tamp.common.dto.Result;
import com.tamp.common.exception.BizException;
import com.tamp.common.sms.SmsService;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;

/**
 * 认证控制器 — 登录/登出/改密/找回密码/Token刷新
 */
@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private static final Logger log = LoggerFactory.getLogger(AuthController.class);

    private static final String SMS_CODE_KEY_PREFIX = "sms:code:";
    private static final String SMS_LOCK_KEY_PREFIX = "sms:lock:";

    private static final long SMS_CODE_TTL_SECONDS = 300;
    private static final long SMS_LOCK_TTL_SECONDS = 60;

    private final AuthService authService;
    private final StringRedisTemplate redisTemplate;
    private final SmsService smsService;

    public AuthController(AuthService authService, StringRedisTemplate redisTemplate, SmsService smsService) {
        this.authService = authService;
        this.redisTemplate = redisTemplate;
        this.smsService = smsService;
    }

    /**
     * 密码登录
     */
    @PostMapping("/login")
    public Result<Map<String, Object>> loginByPassword(@RequestBody LoginRequest request) {
        LoginResult result = authService.loginByPassword(request.getPhone(), request.getPassword());

        Map<String, Object> data = new java.util.HashMap<>();
        data.put("accessToken", result.accessToken());
        data.put("refreshToken", result.refreshToken());
        data.put("needChangePassword", result.needChangePassword());

        Map<String, Object> userInfo = new java.util.HashMap<>();
        userInfo.put("userId", result.userId());
        userInfo.put("phone", result.phone());
        userInfo.put("role", result.role());
        userInfo.put("realName", result.realName());
        userInfo.put("avatar", result.avatar());
        userInfo.put("officeId", result.officeId() != null ? result.officeId() : "");
        userInfo.put("shopId", result.shopId() != null ? result.shopId() : "");
        data.put("userInfo", userInfo);

        return Result.ok(data);
    }

    /**
     * 短信验证码登录
     */
    @PostMapping("/sms-login")
    public Result<Map<String, Object>> loginBySms(@RequestBody SmsLoginRequest request) {
        LoginResult result = authService.loginBySms(request.getPhone(), request.getSmsCode(), request.getShopId());

        Map<String, Object> data = new java.util.HashMap<>();
        data.put("accessToken", result.accessToken());
        data.put("refreshToken", result.refreshToken());
        data.put("needChangePassword", result.needChangePassword());

        Map<String, Object> userInfo = new java.util.HashMap<>();
        userInfo.put("userId", result.userId());
        userInfo.put("phone", result.phone());
        userInfo.put("role", result.role());
        userInfo.put("realName", result.realName());
        userInfo.put("avatar", result.avatar());
        userInfo.put("officeId", result.officeId() != null ? result.officeId() : "");
        userInfo.put("shopId", result.shopId() != null ? result.shopId() : "");
        data.put("userInfo", userInfo);

        return Result.ok(data);
    }

    /**
     * 发送短信验证码
     */
    @PostMapping("/sms-code")
    public Result<Void> sendSmsCode(@RequestBody SendSmsRequest request) {
        String phone = request.getPhone();

        String lockKey = SMS_LOCK_KEY_PREFIX + phone;
        if (Boolean.TRUE.equals(redisTemplate.hasKey(lockKey))) {
            throw new BizException(com.tamp.common.constants.ErrorCode.AUTH_SMS_TOO_FREQUENT);
        }

        String code = String.format("%06d", ThreadLocalRandom.current().nextInt(1000000));

        redisTemplate.opsForValue().set(SMS_CODE_KEY_PREFIX + phone, code,
                SMS_CODE_TTL_SECONDS, TimeUnit.SECONDS);
        redisTemplate.opsForValue().set(lockKey, "1",
                SMS_LOCK_TTL_SECONDS, TimeUnit.SECONDS);

        smsService.sendVerificationCode(phone, code);

        return Result.ok();
    }

    /**
     * 修改密码
     */
    @PutMapping("/change-password")
    public Result<Void> changePassword(@RequestBody ChangePasswordRequest request) {
        Long userId = getCurrentUserId();
        authService.changePassword(userId, request.getOldPassword(), request.getNewPassword());
        return Result.ok();
    }

    /**
     * 找回密码（手机号+验证码重置）
     */
    @PostMapping("/reset-password")
    public Result<Void> resetPassword(@RequestBody ResetPasswordRequest request) {
        authService.resetPassword(request.getPhone(), request.getSmsCode(), request.getNewPassword());
        return Result.ok();
    }

    /**
     * 登出
     */
    @PostMapping("/logout")
    public Result<Void> logout(@RequestHeader(value = "Authorization", required = false) String authHeader) {
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String token = authHeader.substring(7);
            authService.logout(token);
        }
        return Result.ok();
    }

    /**
     * 刷新 Token
     */
    @PostMapping("/refresh-token")
    public Result<Map<String, String>> refreshToken(@RequestBody RefreshTokenRequest request) {
        String[] tokens = authService.refreshTokens(request.getRefreshToken());
        return Result.ok(Map.of(
                "accessToken", tokens[0],
                "refreshToken", tokens[1]
        ));
    }

    // ==================== 内部方法 ====================

    /**
     * 从 SecurityContext 获取当前登录用户 ID
     */
    private Long getCurrentUserId() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !(auth.getPrincipal() instanceof Long userId)) {
            throw new BizException(com.tamp.common.constants.ErrorCode.UNAUTHORIZED);
        }
        return userId;
    }

    // ==================== 请求 DTO ====================

    public static class LoginRequest {
        @NotBlank(message = "手机号不能为空")
        private String phone;

        @NotBlank(message = "密码不能为空")
        @Size(min = 6, max = 32, message = "密码长度需在6-32位之间")
        private String password;

        public String getPhone() {
            return phone;
        }

        public void setPhone(String phone) {
            this.phone = phone;
        }

        public String getPassword() {
            return password;
        }

        public void setPassword(String password) {
            this.password = password;
        }
    }

    public static class SmsLoginRequest {
        @NotBlank(message = "手机号不能为空")
        private String phone;

        @NotBlank(message = "验证码不能为空")
        private String smsCode;

        /** 投资人端定向访问店铺时传，非投资人端登录不传 */
        private Long shopId;

        public String getPhone() {
            return phone;
        }

        public void setPhone(String phone) {
            this.phone = phone;
        }

        public String getSmsCode() {
            return smsCode;
        }

        public void setSmsCode(String smsCode) {
            this.smsCode = smsCode;
        }

        public Long getShopId() {
            return shopId;
        }

        public void setShopId(Long shopId) {
            this.shopId = shopId;
        }
    }

    public static class SendSmsRequest {
        @NotBlank(message = "手机号不能为空")
        private String phone;

        public String getPhone() {
            return phone;
        }

        public void setPhone(String phone) {
            this.phone = phone;
        }
    }

    public static class ChangePasswordRequest {
        @NotBlank(message = "原密码不能为空")
        private String oldPassword;

        @NotBlank(message = "新密码不能为空")
        @Size(min = 6, max = 32, message = "新密码长度需在6-32位之间")
        private String newPassword;

        public String getOldPassword() {
            return oldPassword;
        }

        public void setOldPassword(String oldPassword) {
            this.oldPassword = oldPassword;
        }

        public String getNewPassword() {
            return newPassword;
        }

        public void setNewPassword(String newPassword) {
            this.newPassword = newPassword;
        }
    }

    public static class ResetPasswordRequest {
        @NotBlank(message = "手机号不能为空")
        private String phone;

        @NotBlank(message = "验证码不能为空")
        private String smsCode;

        @NotBlank(message = "新密码不能为空")
        @Size(min = 6, max = 32, message = "新密码长度需在6-32位之间")
        private String newPassword;

        public String getPhone() {
            return phone;
        }

        public void setPhone(String phone) {
            this.phone = phone;
        }

        public String getSmsCode() {
            return smsCode;
        }

        public void setSmsCode(String smsCode) {
            this.smsCode = smsCode;
        }

        public String getNewPassword() {
            return newPassword;
        }

        public void setNewPassword(String newPassword) {
            this.newPassword = newPassword;
        }
    }

    public static class RefreshTokenRequest {
        @NotBlank(message = "刷新Token不能为空")
        private String refreshToken;

        public String getRefreshToken() {
            return refreshToken;
        }

        public void setRefreshToken(String refreshToken) {
            this.refreshToken = refreshToken;
        }
    }
}
