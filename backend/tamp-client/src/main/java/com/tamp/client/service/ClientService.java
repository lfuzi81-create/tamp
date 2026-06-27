package com.tamp.client.service;

import com.tamp.client.entity.Client;
import com.tamp.client.entity.ClientAsset;
import com.tamp.client.entity.ClientTag;
import com.tamp.client.entity.ClientTimeline;
import com.tamp.client.repository.ClientAssetRepository;
import com.tamp.client.repository.ClientRepository;
import com.tamp.client.repository.ClientTagRepository;
import com.tamp.client.repository.ClientTimelineRepository;
import com.tamp.common.constants.ErrorCode;
import com.tamp.common.exception.BizException;
import com.tamp.common.util.SecurityUtils;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowCallbackHandler;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Expression;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class ClientService {

    private static final Logger log = LoggerFactory.getLogger(ClientService.class);

    private final ClientRepository clientRepository;
    private final ClientTimelineRepository clientTimelineRepository;
    private final ClientTagRepository clientTagRepository;
    private final ClientAssetRepository clientAssetRepository;
    private final JdbcTemplate jdbcTemplate;
    private final ClientAumSyncService clientAumSyncService;

    public ClientService(ClientRepository clientRepository, ClientTimelineRepository clientTimelineRepository,
                         ClientTagRepository clientTagRepository, ClientAssetRepository clientAssetRepository,
                         JdbcTemplate jdbcTemplate, ClientAumSyncService clientAumSyncService) {
        this.clientRepository = clientRepository;
        this.clientTimelineRepository = clientTimelineRepository;
        this.clientTagRepository = clientTagRepository;
        this.clientAssetRepository = clientAssetRepository;
        this.jdbcTemplate = jdbcTemplate;
        this.clientAumSyncService = clientAumSyncService;
    }

    /**
     * 投资人端「历史访问店铺列表」：按当前登录用户手机号查所有访问过的店铺。
     * 用 JdbcTemplate 直接关联 biz_client + biz_shop，避免 tamp-client 依赖 tamp-organization。
     */
    public List<Map<String, Object>> getMyShops(String phone) {
        return jdbcTemplate.queryForList(
                "SELECT c.shop_id AS shopId, s.name AS shopName, s.office_id AS officeId, " +
                        "       c.updated_at AS lastVisit " +
                        "FROM biz_client c " +
                        "LEFT JOIN biz_shop s ON c.shop_id = s.id " +
                        "WHERE c.phone = ? AND c.is_deleted = 0 " +
                        "ORDER BY c.updated_at DESC",
                phone
        );
    }

    /**
     * 投资人端记录浏览行为
     * 通过 userId 查找投资人手机号，再查找对应的 Client，然后记录行为到时间线
     */
    @Transactional
    public void recordBehavior(Long userId, Long shopId, String actionType, Long targetId, String targetName) {
        // 1. 通过 userId 查找投资人手机号
        String phone = jdbcTemplate.queryForObject(
                "SELECT phone FROM sys_user WHERE id = ?",
                String.class,
                userId
        );
        if (phone == null) {
            log.warn("recordBehavior: userId {} 没有找到手机号", userId);
            return;
        }

        // 2. 查找该手机号在该店铺对应的 Client
        Client client = clientRepository.findByShopIdAndPhoneAndDeleted(shopId, phone, 0).orElse(null);
        if (client == null) {
            // 如果没有找到 Client，则创建一个新的（投资人首次访问该店铺）
            client = new Client();
            client.setPhone(phone);
            client.setShopId(shopId);
            client.setName("投资人-" + phone.substring(phone.length() - 4));
            client.setSource("投资人端浏览");
            client.setStatus(0);
            client = clientRepository.save(client);
            log.info("recordBehavior: 为投资人 {} 创建新客户记录 shopId={}", phone, shopId);
        }

        // 3. 创建时间线记录
        ClientTimeline timeline = new ClientTimeline();
        timeline.setClientId(client.getId());
        timeline.setEventType(actionType);
        timeline.setTitle(resolveBehaviorTitle(actionType));
        timeline.setContent(targetName);
        timeline.setTargetId(targetId);
        timeline.setEventTime(LocalDateTime.now());
        timeline.setCreatedAt(LocalDateTime.now());
        timeline.setCreatedBy("INVESTOR_" + userId);
        clientTimelineRepository.save(timeline);

        incrementTargetViewCount(actionType, targetId);

        // 4. 更新 Client 的 updated_at（用于排序最后访问时间）
        client.setUpdatedAt(LocalDateTime.now());
        clientRepository.save(client);

        log.info("recordBehavior: 记录行为 {} for clientId={} targetId={} targetName={}",
                actionType, client.getId(), targetId, targetName);
    }

    /**
     * 投资人浏览行为同步到实体 view_count（产品/内容）；知识库由 GET /knowledge/articles/{id} 的 countView 负责。
     */
    private void incrementTargetViewCount(String actionType, Long targetId) {
        if (targetId == null) {
            return;
        }
        if ("VIEW_PRODUCT".equals(actionType)) {
            jdbcTemplate.update(
                    "UPDATE biz_product SET view_count = COALESCE(view_count, 0) + 1 WHERE id = ? AND is_deleted = 0",
                    targetId
            );
        } else if ("VIEW_CONTENT".equals(actionType)) {
            jdbcTemplate.update(
                    "UPDATE biz_content SET view_count = COALESCE(view_count, 0) + 1 WHERE id = ? AND is_deleted = 0",
                    targetId
            );
        }
    }

    private String resolveBehaviorTitle(String actionType) {
        if ("VIEW_PRODUCT".equals(actionType)) {
            return "浏览产品";
        }
        if ("VIEW_KNOWLEDGE".equals(actionType)) {
            return "浏览知识库";
        }
        return "浏览内容";
    }

    /**
     * 按店铺可见的已授权资产重算客户 AUM，并同步店铺 total_aum。
     */
    @Transactional
    public void refreshClientAum(Long clientId) {
        Client client = clientRepository.findByIdAndDeleted(clientId, 0).orElse(null);
        if (client == null || client.getShopId() == null) {
            return;
        }
        Long investorId = resolveInvestorIdByPhone(client.getPhone());
        AuthorizedAssetSummary summary = queryAuthorizedAssetSummary(client.getId(), investorId, client.getShopId());
        clientAumSyncService.syncClientAum(client.getId(), summary.totalAmount());
    }

    private record AuthorizedAssetSummary(int count, BigDecimal totalAmount) {}

    /**
     * 统计对指定客户/店铺可见的已授权资产数量与金额（与资产列表、AUM 重算规则一致）。
     */
    private AuthorizedAssetSummary queryAuthorizedAssetSummary(Long clientId, Long investorId, Long shopId) {
        if (investorId != null && shopId != null) {
            String shopIdStr = shopId.toString();
            return jdbcTemplate.queryForObject(
                    """
                    SELECT COUNT(*), COALESCE(SUM(amount), 0) FROM biz_client_asset ca
                    WHERE ca.is_deleted = 0 AND ca.is_authorized = 1
                      AND (
                        (
                          (ca.auth_scope IS NULL OR TRIM(ca.auth_scope) = '')
                          AND ca.client_id = ?
                        )
                        OR (
                          TRIM(COALESCE(ca.auth_scope, '')) != ''
                          AND SUBSTRING_INDEX(TRIM(ca.auth_scope), ',', 1) = ?
                          AND (
                            ca.client_id = ?
                            OR (ca.investor_id = ? AND ca.investor_id IS NOT NULL)
                          )
                        )
                      )
                    """,
                    (rs, rowNum) -> new AuthorizedAssetSummary(
                            rs.getInt(1),
                            rs.getBigDecimal(2) != null ? rs.getBigDecimal(2) : BigDecimal.ZERO
                    ),
                    clientId,
                    shopIdStr,
                    clientId,
                    investorId
            );
        }
        return jdbcTemplate.queryForObject(
                """
                SELECT COUNT(*), COALESCE(SUM(amount), 0) FROM biz_client_asset
                WHERE is_deleted = 0 AND is_authorized = 1 AND client_id = ?
                """,
                (rs, rowNum) -> new AuthorizedAssetSummary(
                        rs.getInt(1),
                        rs.getBigDecimal(2) != null ? rs.getBigDecimal(2) : BigDecimal.ZERO
                ),
                clientId
        );
    }

    @Transactional
    public void refreshClientAumForInvestor(Long investorId) {
        List<String> phones = jdbcTemplate.query(
                "SELECT phone FROM sys_user WHERE id = ? AND is_deleted = 0 LIMIT 1",
                (rs, rowNum) -> rs.getString("phone"),
                investorId
        );
        if (phones.isEmpty() || !StringUtils.hasText(phones.get(0))) {
            return;
        }
        for (Client client : clientRepository.findByPhoneAndDeletedOrderByUpdatedAtDesc(phones.get(0), 0)) {
            refreshClientAum(client.getId());
        }
    }

    public List<Map<String, Object>> listTimelineEnriched(Long clientId) {
        Client client = getClient(clientId);
        String displayName = resolveClientDisplayName(client);
        return clientTimelineRepository.findByClientIdOrderByCreatedAtDesc(clientId).stream()
                .map(t -> {
                    Map<String, Object> item = new HashMap<>();
                    item.put("id", t.getId());
                    item.put("clientId", t.getClientId());
                    item.put("eventType", t.getEventType());
                    item.put("title", t.getTitle());
                    item.put("content", t.getContent());
                    item.put("targetId", t.getTargetId());
                    item.put("eventTime", t.getEventTime());
                    item.put("createdAt", t.getCreatedAt());
                    item.put("createdBy", t.getCreatedBy());
                    item.put("clientName", displayName);
                    return item;
                })
                .toList();
    }

    /**
     * 投资人端获取个人资料：从 sys_user 读 real_name(昵称)、avatar(头像)、phone
     */
    public Map<String, Object> getInvestorProfile(Long userId) {
        List<Map<String, Object>> rows = jdbcTemplate.queryForList(
                "SELECT real_name, avatar, phone FROM sys_user WHERE id = ? AND is_deleted = 0",
                userId
        );
        if (rows.isEmpty()) {
            Map<String, Object> empty = new HashMap<>();
            empty.put("nickname", "");
            empty.put("avatarUrl", "");
            empty.put("phone", "");
            return empty;
        }
        Map<String, Object> row = rows.get(0);
        Map<String, Object> profile = new HashMap<>();
        profile.put("nickname", row.getOrDefault("real_name", ""));
        profile.put("avatarUrl", row.getOrDefault("avatar", ""));
        profile.put("phone", row.getOrDefault("phone", ""));
        return profile;
    }

    /**
     * 投资人端更新个人资料：更新 sys_user 的 real_name 和 avatar 字段，
     * 并同步所有关联 biz_client 记录的 name，保证三端显示一致。
     */
    @Transactional
    public void updateInvestorProfile(Long userId, String nickname, String avatarUrl) {
        log.info("[updateInvestorProfile] 开始更新用户资料: userId={}, nickname={}, hasAvatar={}",
                userId, nickname, avatarUrl != null && !avatarUrl.isBlank());

        try {
            StringBuilder sql = new StringBuilder("UPDATE sys_user SET updated_at = NOW()");
            java.util.List<Object> params = new java.util.ArrayList<>();
            if (nickname != null && !nickname.isBlank()) {
                sql.append(", real_name = ?");
                params.add(nickname);
                log.debug("  → 添加昵称更新: {}", nickname);
            }
            if (avatarUrl != null && !avatarUrl.isBlank()) {
                sql.append(", avatar = ?");
                params.add(avatarUrl);
                log.debug("  → 添加头像更新: {}...", avatarUrl.substring(0, Math.min(50, avatarUrl.length())));
            }
            sql.append(" WHERE id = ? AND is_deleted = 0");
            params.add(userId);

            String finalSql = sql.toString();
            log.info("[updateInvestorProfile] 执行SQL: {}, 参数数量: {}", finalSql, params.size());

            int rows = jdbcTemplate.update(finalSql, params.toArray());

            log.info("[updateInvestorProfile] 更新完成，影响行数: {}", rows);

            if (rows == 0) {
                log.warn("[updateInvestorProfile] ⚠️ 未更新任何行！可能原因: userId={} 不存在或已删除", userId);
                throw new RuntimeException("用户不存在或已被删除");
            }

            if (nickname != null && !nickname.isBlank()) {
                String phone = jdbcTemplate.queryForObject(
                        "SELECT phone FROM sys_user WHERE id = ? AND is_deleted = 0",
                        String.class,
                        userId
                );
                if (phone != null) {
                    int synced = jdbcTemplate.update(
                            "UPDATE biz_client SET name = ?, updated_at = NOW() WHERE phone = ? AND is_deleted = 0",
                            nickname,
                            phone
                    );
                    log.info("[updateInvestorProfile] 同步 biz_client.name, phone={}, 影响行数={}", phone, synced);
                }
            }

        } catch (Exception e) {
            log.error("[updateInvestorProfile] ❌ 更新失败! userId={}, error={}", userId, e.getMessage(), e);
            throw new RuntimeException("更新个人资料失败: " + e.getMessage(), e);
        }
    }

    public Page<Client> listClients(String keyword, Long officeId, Long shopId, String tag, Pageable pageable) {
        String role = SecurityUtils.getCurrentUserRole();

        if ("INVESTOR".equals(role)) {
            return Page.empty(pageable);
        }

        final Long finalOfficeId = officeId;
        final Long finalShopId = shopId;
        final String finalKeyword = keyword;
        final String finalTag = tag;
        final List<Long> currentUserOfficeIds = SecurityUtils.getCurrentUserOfficeIds();
        final Long currentShopId = SecurityUtils.getCurrentShopId();

        List<Long> taggedClientIds = null;
        if (StringUtils.hasText(finalTag)) {
            taggedClientIds = clientTagRepository.findClientIdsByTagName(finalTag);
            if (taggedClientIds.isEmpty()) {
                return Page.empty(pageable);
            }
        }
        final List<Long> finalTaggedClientIds = taggedClientIds;

        Specification<Client> spec = (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            predicates.add(cb.equal(root.get("deleted"), 0));

            // 数据范围过滤
            if (currentUserOfficeIds != null && !currentUserOfficeIds.isEmpty()) {
                predicates.add(root.get("officeId").in(currentUserOfficeIds));
            }
            if ("SHOP_ADMIN".equals(role) && currentShopId != null) {
                predicates.add(cb.equal(root.get("shopId"), currentShopId));
            }

            // 手动筛选条件
            if (StringUtils.hasText(finalKeyword)) {
                predicates.add(cb.or(
                        cb.like(cb.lower(root.get("name")), "%" + finalKeyword.toLowerCase() + "%"),
                        cb.like(cb.lower(root.get("phone")), "%" + finalKeyword.toLowerCase() + "%")
                ));
            }
            if (finalOfficeId != null) {
                predicates.add(cb.equal(root.get("officeId"), finalOfficeId));
            }
            if (finalShopId != null) {
                predicates.add(cb.equal(root.get("shopId"), finalShopId));
            }
            if (finalTaggedClientIds != null) {
                predicates.add(root.get("id").in(finalTaggedClientIds));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };
        return clientRepository.findAll(spec, pageable).map(this::enrichClientForList);
    }

    private Client enrichClientForList(Client client) {
        enrichClientDisplayName(client);
        enrichClientAuthSummary(client);
        return client;
    }

    /**
     * 聚合客户授权状态与 AUM：与资产明细使用同一套可见性规则，避免列表 aumTotal 与授权资产不一致。
     */
    public void enrichClientAuthSummary(Client client) {
        if (client == null || client.getId() == null) {
            return;
        }
        BigDecimal storedAum = client.getAumTotal();
        Long investorId = resolveInvestorIdByPhone(client.getPhone());
        Long shopId = client.getShopId();
        AuthorizedAssetSummary summary = queryAuthorizedAssetSummary(client.getId(), investorId, shopId);

        client.setAuthorizedCount(summary.count());
        client.setAuthorizedStatus(summary.count() > 0 ? "authorized" : "unauthorized");
        client.setAumTotal(summary.totalAmount());

        if (shopId != null && aumNeedsSync(storedAum, summary.totalAmount())) {
            try {
                clientAumSyncService.syncClientAum(client.getId(), summary.totalAmount());
            } catch (Exception e) {
                log.warn("sync client aum failed clientId={}: {}", client.getId(), e.getMessage());
            }
        }
    }

    private boolean aumNeedsSync(BigDecimal stored, BigDecimal computed) {
        BigDecimal storedNorm = stored != null ? stored : BigDecimal.ZERO;
        BigDecimal computedNorm = computed != null ? computed : BigDecimal.ZERO;
        return storedNorm.compareTo(computedNorm) != 0;
    }

    /**
     * 按授权资产实时汇总店铺 AUM（主归属店规则：auth_scope 首店计入，其余店不计）。
     */
    public BigDecimal computeShopAuthorizedAum(Long shopId) {
        if (shopId == null) {
            return BigDecimal.ZERO;
        }
        String shopIdStr = shopId.toString();
        BigDecimal total = jdbcTemplate.queryForObject(
                """
                SELECT COALESCE(SUM(ca.amount), 0) FROM biz_client_asset ca
                WHERE ca.is_deleted = 0 AND ca.is_authorized = 1
                  AND (
                    (
                      (ca.auth_scope IS NULL OR TRIM(ca.auth_scope) = '')
                      AND EXISTS (
                        SELECT 1 FROM biz_client c
                        WHERE c.id = ca.client_id AND c.shop_id = ? AND c.is_deleted = 0
                      )
                    )
                    OR (
                      TRIM(COALESCE(ca.auth_scope, '')) != ''
                      AND SUBSTRING_INDEX(TRIM(ca.auth_scope), ',', 1) = ?
                      AND (
                        EXISTS (
                          SELECT 1 FROM biz_client c
                          WHERE c.id = ca.client_id AND c.shop_id = ? AND c.is_deleted = 0
                        )
                        OR (
                          ca.investor_id IS NOT NULL
                          AND EXISTS (
                            SELECT 1 FROM biz_client c
                            INNER JOIN sys_user u ON u.phone = c.phone AND u.id = ca.investor_id AND u.is_deleted = 0
                            WHERE c.shop_id = ? AND c.is_deleted = 0
                          )
                        )
                      )
                    )
                  )
                """,
                BigDecimal.class,
                shopId,
                shopIdStr,
                shopId,
                shopId
        );
        return total != null ? total : BigDecimal.ZERO;
    }

    /**
     * 按授权资产实时汇总家办 AUM（仅计入主归属店落在该家办下的资产）。
     */
    public BigDecimal computeOfficeAuthorizedAum(Long officeId) {
        if (officeId == null) {
            return BigDecimal.ZERO;
        }
        BigDecimal total = jdbcTemplate.queryForObject(
                """
                SELECT COALESCE(SUM(ca.amount), 0) FROM biz_client_asset ca
                WHERE ca.is_deleted = 0 AND ca.is_authorized = 1
                  AND (
                    (
                      (ca.auth_scope IS NULL OR TRIM(ca.auth_scope) = '')
                      AND EXISTS (
                        SELECT 1 FROM biz_client c
                        WHERE c.id = ca.client_id AND c.office_id = ? AND c.is_deleted = 0
                      )
                    )
                    OR (
                      TRIM(COALESCE(ca.auth_scope, '')) != ''
                      AND EXISTS (
                        SELECT 1 FROM biz_shop s
                        WHERE s.office_id = ? AND s.is_deleted = 0
                          AND s.id = CAST(SUBSTRING_INDEX(TRIM(ca.auth_scope), ',', 1) AS UNSIGNED)
                      )
                    )
                  )
                """,
                BigDecimal.class,
                officeId,
                officeId
        );
        return total != null ? total : BigDecimal.ZERO;
    }

    /**
     * 平台总 AUM：全部已授权资产金额之和（每条资产只计一次）。
     */
    public BigDecimal computePlatformAuthorizedAum() {
        BigDecimal total = jdbcTemplate.queryForObject(
                """
                SELECT COALESCE(SUM(amount), 0) FROM biz_client_asset
                WHERE is_deleted = 0 AND is_authorized = 1
                """,
                BigDecimal.class
        );
        return total != null ? total : BigDecimal.ZERO;
    }

    /**
     * 优先使用投资人 sys_user.real_name 作为客户展示名，解决三端姓名不同步问题。
     */
    public Client enrichClientDisplayName(Client client) {
        if (client == null || !StringUtils.hasText(client.getPhone())) {
            return client;
        }
        try {
            List<String> names = jdbcTemplate.query(
                    "SELECT COALESCE(NULLIF(TRIM(u.real_name), ''), NULLIF(TRIM(ip.nickname), '')) AS display_name "
                            + "FROM sys_user u "
                            + "LEFT JOIN biz_investor_profile ip ON ip.user_id = u.id AND ip.is_deleted = 0 "
                            + "WHERE u.phone = ? AND u.role = 'INVESTOR' AND u.is_deleted = 0 "
                            + "LIMIT 1",
                    (rs, rowNum) -> rs.getString(1),
                    client.getPhone()
            );
            if (!names.isEmpty() && StringUtils.hasText(names.get(0))) {
                client.setName(names.get(0));
            }
        } catch (Exception e) {
            log.debug("enrichClientDisplayName failed for phone={}: {}", client.getPhone(), e.getMessage());
        }
        return client;
    }

    public String resolveClientDisplayName(Client client) {
        if (client == null) {
            return "客户";
        }
        enrichClientDisplayName(client);
        return StringUtils.hasText(client.getName()) ? client.getName() : "客户";
    }

    public List<String> getAllTagNames() {
        return clientTagRepository.findAllDistinctTagNames();
    }

    public List<ClientTag> getClientTags(Long clientId) {
        getClient(clientId);
        return clientTagRepository.findByClientIdOrderByCreatedAtDesc(clientId);
    }

    public Client getClient(Long id) {
        Client client = clientRepository.findByIdAndDeleted(id, 0)
                .orElseThrow(() -> new BizException(ErrorCode.BIZ_CLIENT_NOT_FOUND));
        enrichClientDisplayName(client);
        enrichClientAuthSummary(client);
        return client;
    }

    @Transactional
    public Client createClient(Client client) {
        client.setDeleted(0);
        return clientRepository.save(client);
    }

    @Transactional
    public Client updateClient(Long id, Client client) {
        Client existing = getClient(id);
        if (client.getName() != null) existing.setName(client.getName());
        if (client.getPhone() != null) existing.setPhone(client.getPhone());
        if (client.getGender() != null) existing.setGender(client.getGender());
        if (client.getEmail() != null) existing.setEmail(client.getEmail());
        if (client.getCompany() != null) existing.setCompany(client.getCompany());
        if (client.getPosition() != null) existing.setPosition(client.getPosition());
        if (client.getAumTotal() != null) existing.setAumTotal(client.getAumTotal());
        if (client.getSource() != null) existing.setSource(client.getSource());
        if (client.getRemark() != null) existing.setRemark(client.getRemark());
        if (client.getShopId() != null) existing.setShopId(client.getShopId());
        if (client.getOfficeId() != null) existing.setOfficeId(client.getOfficeId());
        if (client.getStatus() != null) existing.setStatus(client.getStatus());
        return clientRepository.save(existing);
    }

    @Transactional
    public void deleteClient(Long id) {
        Client client = getClient(id);
        client.setDeleted(1);
        clientRepository.save(client);
    }

    public List<ClientTimeline> listTimeline(Long clientId) {
        getClient(clientId);
        return clientTimelineRepository.findByClientIdOrderByCreatedAtDesc(clientId);
    }

    @Transactional
    public ClientTimeline addTimeline(ClientTimeline timeline) {
        getClient(timeline.getClientId());
        timeline.setCreatedAt(LocalDateTime.now());
        return clientTimelineRepository.save(timeline);
    }

    public Page<ClientAsset> listAssets(Long clientId, Pageable pageable) {
        Client client = getClient(clientId);
        Long shopId = client.getShopId();
        String phone = client.getPhone();

        Long investorId = resolveInvestorIdByPhone(phone);
        if (investorId == null || shopId == null) {
            Page<ClientAsset> page = clientAssetRepository.findByClientIdAndDeleted(clientId, 0, pageable);
            enrichAssetsWithProductNames(page.getContent());
            return page;
        }

        String shopIdStr = shopId.toString();
        Specification<ClientAsset> spec = (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            predicates.add(cb.equal(root.get("deleted"), 0));

            Expression<String> trimmedScope = cb.function("TRIM", String.class, root.get("authScope"));
            Expression<String> primaryShop = cb.function(
                    "SUBSTRING_INDEX",
                    String.class,
                    trimmedScope,
                    cb.literal(","),
                    cb.literal(1)
            );
            Predicate emptyScope = cb.or(
                    cb.isNull(root.get("authScope")),
                    cb.equal(trimmedScope, "")
            );
            Predicate primaryMatchesShop = cb.equal(primaryShop, shopIdStr);

            Predicate byClientId = cb.and(
                    cb.equal(root.get("clientId"), clientId),
                    cb.or(emptyScope, primaryMatchesShop)
            );
            Predicate byInvestorAuth = cb.and(
                    cb.equal(root.get("investorId"), investorId),
                    cb.equal(root.get("isAuthorized"), 1),
                    cb.not(emptyScope),
                    primaryMatchesShop
            );
            predicates.add(cb.or(byClientId, byInvestorAuth));
            return cb.and(predicates.toArray(new Predicate[0]));
        };
        Page<ClientAsset> page = clientAssetRepository.findAll(spec, pageable);
        enrichAssetsWithProductNames(page.getContent());
        return page;
    }

    private void enrichAssetsWithProductNames(List<ClientAsset> assets) {
        if (assets == null || assets.isEmpty()) {
            return;
        }
        Set<Long> productIds = assets.stream()
                .map(ClientAsset::getProductId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());
        if (productIds.isEmpty()) {
            return;
        }
        String placeholders = productIds.stream().map(id -> "?").collect(Collectors.joining(","));
        Map<Long, String> nameMap = new HashMap<>();
        jdbcTemplate.query(
                "SELECT id, name FROM biz_product WHERE is_deleted = 0 AND id IN (" + placeholders + ")",
                (RowCallbackHandler) rs -> nameMap.put(rs.getLong("id"), rs.getString("name")),
                productIds.toArray()
        );
        for (ClientAsset asset : assets) {
            if (asset.getProductId() != null) {
                asset.setProductName(nameMap.get(asset.getProductId()));
            }
        }
    }

    private Long resolveInvestorIdByPhone(String phone) {
        if (!StringUtils.hasText(phone)) {
            return null;
        }
        List<Long> ids = jdbcTemplate.query(
                "SELECT id FROM sys_user WHERE phone = ? AND role = 'INVESTOR' AND is_deleted = 0 LIMIT 1",
                (rs, rowNum) -> rs.getLong("id"),
                phone
        );
        return ids.isEmpty() ? null : ids.get(0);
    }

    @Transactional
    public ClientAsset addAsset(Long clientId, ClientAsset asset) {
        getClient(clientId);
        asset.setClientId(clientId);
        asset.setDeleted(0);
        return clientAssetRepository.save(asset);
    }

    @Transactional
    public ClientAsset updateAsset(Long clientId, Long assetId, ClientAsset asset) {
        ClientAsset existing = clientAssetRepository.findByIdAndDeleted(assetId, 0)
                .orElseThrow(() -> new BizException(ErrorCode.BIZ_ASSET_NOT_FOUND));
        if (!isAssetVisibleToClient(clientId, existing)) {
            throw new BizException(ErrorCode.BIZ_ASSET_NOT_FOUND);
        }
        existing.setName(asset.getName());
        existing.setType(asset.getType());
        existing.setAmount(asset.getAmount());
        if (asset.getIsAuthorized() != null) {
            existing.setIsAuthorized(asset.getIsAuthorized());
        }
        if (existing.getClientId() == null) {
            existing.setClientId(clientId);
        }
        return clientAssetRepository.save(existing);
    }

    @Transactional
    public void deleteAsset(Long clientId, Long assetId) {
        ClientAsset asset = clientAssetRepository.findByIdAndDeleted(assetId, 0)
                .orElseThrow(() -> new BizException(ErrorCode.BIZ_ASSET_NOT_FOUND));
        if (!isAssetVisibleToClient(clientId, asset)) {
            throw new BizException(ErrorCode.BIZ_ASSET_NOT_FOUND);
        }
        asset.setDeleted(1);
        clientAssetRepository.save(asset);
    }

    /**
     * 判断资产是否对指定客户可见（含投资人授权资产）
     */
    private boolean isAssetVisibleToClient(Long clientId, ClientAsset asset) {
        if (clientId.equals(asset.getClientId())) {
            return true;
        }
        Client client = clientRepository.findById(clientId)
                .filter(c -> c.getDeleted() == null || c.getDeleted() == 0)
                .orElse(null);
        if (client == null) {
            return false;
        }
        Long investorId = resolveInvestorIdByPhone(client.getPhone());
        Long shopId = client.getShopId();
        if (investorId == null || shopId == null || !investorId.equals(asset.getInvestorId())) {
            return false;
        }
        if (asset.getIsAuthorized() == null || asset.getIsAuthorized() != 1) {
            return false;
        }
        String shopIdStr = shopId.toString();
        String scope = asset.getAuthScope();
        if (!StringUtils.hasText(scope)) {
            return false;
        }
        return scope.equals(shopIdStr)
                || scope.startsWith(shopIdStr + ",")
                || scope.contains("," + shopIdStr + ",")
                || scope.endsWith("," + shopIdStr);
    }

    public List<ClientTag> listTags(Long clientId) {
        getClient(clientId);
        return clientTagRepository.findByClientIdOrderByCreatedAtDesc(clientId);
    }

    @Transactional
    public void addTag(Long clientId, String tagName, String tagColor) {
        getClient(clientId);
        ClientTag tag = new ClientTag();
        tag.setClientId(clientId);
        tag.setTagName(tagName);
        tag.setTagColor(tagColor != null ? tagColor : "#1890ff");
        tag.setCreatedAt(LocalDateTime.now());
        clientTagRepository.save(tag);
    }

    @Transactional
    public void deleteTag(Long clientId, Long tagId) {
        ClientTag tag = clientTagRepository.findById(tagId)
                .orElseThrow(() -> new BizException(ErrorCode.NOT_FOUND));
        if (!tag.getClientId().equals(clientId)) {
            throw new BizException(ErrorCode.NOT_FOUND);
        }
        clientTagRepository.delete(tag);
    }
}
