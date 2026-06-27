package com.tamp.client.service;

import com.tamp.client.controller.vo.AssetSummaryVO;
import com.tamp.client.entity.Client;
import com.tamp.client.entity.ClientAsset;
import com.tamp.client.repository.ClientAssetRepository;
import com.tamp.client.repository.ClientRepository;
import com.tamp.common.constants.ErrorCode;
import com.tamp.common.exception.BizException;
import com.tamp.common.util.SecurityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.util.*;

/**
 * 资产管理服务 — 投资人端
 */
@Service
public class AssetService {

    private static final Logger log = LoggerFactory.getLogger(AssetService.class);

    private final ClientAssetRepository assetRepository;
    private final ClientRepository clientRepository;
    private final ClientService clientService;
    private final JdbcTemplate jdbcTemplate;

    public AssetService(ClientAssetRepository assetRepository,
                        ClientRepository clientRepository,
                        ClientService clientService,
                        JdbcTemplate jdbcTemplate) {
        this.assetRepository = assetRepository;
        this.clientRepository = clientRepository;
        this.clientService = clientService;
        this.jdbcTemplate = jdbcTemplate;
    }

    /**
     * 我的资产列表
     */
    public Page<ClientAsset> listMyAssets(Long investorId, Pageable pageable) {
        // INVESTOR 只能查看自己的资产
        String role = SecurityUtils.getCurrentUserRole();
        if ("INVESTOR".equals(role)) {
            Long currentUserId = SecurityUtils.getCurrentUserId();
            if (currentUserId != null && !currentUserId.equals(investorId)) {
                throw new BizException(ErrorCode.FORBIDDEN);
            }
        }
        return assetRepository.findByInvestorIdAndDeleted(investorId, 0, pageable);
    }

    /**
     * 查询单条资产详情（校验归属当前投资人）
     */
    public ClientAsset getAsset(Long investorId, Long id) {
        String role = SecurityUtils.getCurrentUserRole();
        if ("INVESTOR".equals(role)) {
            Long currentUserId = SecurityUtils.getCurrentUserId();
            if (currentUserId != null && !currentUserId.equals(investorId)) {
                throw new BizException(ErrorCode.FORBIDDEN);
            }
        }
        ClientAsset asset = assetRepository.findByIdAndDeleted(id, 0)
                .orElseThrow(() -> new BizException(ErrorCode.BIZ_ASSET_NOT_FOUND));
        if (!investorId.equals(asset.getInvestorId())) {
            throw new BizException(ErrorCode.FORBIDDEN);
        }
        return asset;
    }

    /**
     * 资产总览
     */
    public AssetSummaryVO getAssetSummary(Long investorId) {
        String role = SecurityUtils.getCurrentUserRole();
        if ("INVESTOR".equals(role)) {
            Long currentUserId = SecurityUtils.getCurrentUserId();
            if (currentUserId != null && !currentUserId.equals(investorId)) {
                throw new BizException(ErrorCode.FORBIDDEN);
            }
        }
        Page<ClientAsset> allAssets = assetRepository.findByInvestorIdAndDeleted(investorId, 0,
                org.springframework.data.domain.PageRequest.of(0, Integer.MAX_VALUE));

        Double totalValue = assetRepository.sumAmountByInvestorIdAndDeleted(investorId, 0);
        if (totalValue == null) {
            totalValue = 0.0;
        }

        Map<String, Double> typeSumMap = new LinkedHashMap<>();
        for (ClientAsset asset : allAssets) {
            typeSumMap.merge(asset.getType(), asset.getAmount().doubleValue(), Double::sum);
        }

        List<AssetSummaryVO.TypeDistribution> typeDistribution = new ArrayList<>();
        for (Map.Entry<String, Double> entry : typeSumMap.entrySet()) {
            AssetSummaryVO.TypeDistribution item = new AssetSummaryVO.TypeDistribution();
            item.setType(entry.getKey());
            item.setAmount(entry.getValue());
            typeDistribution.add(item);
        }

        AssetSummaryVO summary = new AssetSummaryVO();
        summary.setTotalValue(totalValue);
        summary.setTypeDistribution(typeDistribution);
        summary.setTotalCount(allAssets.getTotalElements());
        return summary;
    }

    /**
     * 上传/录入资产
     */
    @Transactional
    public ClientAsset createAsset(Long investorId, ClientAsset asset) {
        // 校验金额不能为负数
        if (asset.getAmount() != null && asset.getAmount().compareTo(BigDecimal.ZERO) < 0) {
            throw new BizException(ErrorCode.PARAM_INVALID, "资产金额不能为负数");
        }
        asset.setInvestorId(investorId);
        asset.setDeleted(0);
        if (StringUtils.hasText(asset.getAuthScope())) {
            asset.setIsAuthorized(1);
        }
        linkAssetToClient(investorId, asset);
        ClientAsset saved = assetRepository.save(asset);
        clientService.refreshClientAumForInvestor(investorId);
        return saved;
    }

    /**
     * 编辑资产
     */
    @Transactional
    public ClientAsset updateAsset(Long investorId, Long id, ClientAsset asset) {
        ClientAsset existing = assetRepository.findById(id)
                .orElseThrow(() -> new BizException(ErrorCode.BIZ_ASSET_NOT_FOUND));

        if (!investorId.equals(existing.getInvestorId())) {
            throw new BizException(ErrorCode.FORBIDDEN);
        }

        // 校验金额不能为负数
        if (asset.getAmount() != null && asset.getAmount().compareTo(BigDecimal.ZERO) < 0) {
            throw new BizException(ErrorCode.PARAM_INVALID, "资产金额不能为负数");
        }

        existing.setName(asset.getName());
        existing.setType(asset.getType());
        existing.setProductId(asset.getProductId());
        existing.setAmount(asset.getAmount());
        existing.setRiskLevel(asset.getRiskLevel());
        existing.setPurchaseDate(asset.getPurchaseDate());
        existing.setMaturityDate(asset.getMaturityDate());
        existing.setExpectedReturn(asset.getExpectedReturn());
        existing.setInstitution(asset.getInstitution());
        existing.setRemark(asset.getRemark());
        if (StringUtils.hasText(asset.getAuthScope())) {
            existing.setIsAuthorized(1);
            linkAssetToClient(investorId, existing);
        }
        ClientAsset saved = assetRepository.save(existing);
        clientService.refreshClientAumForInvestor(investorId);
        return saved;
    }

    /**
     * 删除资产
     */
    @Transactional
    public void deleteAsset(Long investorId, Long id) {
        ClientAsset asset = assetRepository.findById(id)
                .orElseThrow(() -> new BizException(ErrorCode.BIZ_ASSET_NOT_FOUND));

        if (!investorId.equals(asset.getInvestorId())) {
            throw new BizException(ErrorCode.FORBIDDEN);
        }

        asset.setDeleted(1);
        assetRepository.save(asset);
        clientService.refreshClientAumForInvestor(investorId);
    }

    /**
     * 单项授权设置
     */
    @Transactional
    public void updateAssetAuth(Long investorId, Long id, Boolean isAuthorized, String authScope) {
        ClientAsset asset = assetRepository.findById(id)
                .orElseThrow(() -> new BizException(ErrorCode.BIZ_ASSET_NOT_FOUND));

        if (!investorId.equals(asset.getInvestorId())) {
            throw new BizException(ErrorCode.FORBIDDEN);
        }

        asset.setIsAuthorized(isAuthorized ? 1 : 0);
        asset.setAuthScope(authScope);
        linkAssetToClient(investorId, asset);
        assetRepository.save(asset);
        clientService.refreshClientAumForInvestor(investorId);
    }

    /**
     * 全局一键授权开关
     */
    @Transactional
    public void toggleGlobalAuth(Long investorId, Boolean authorized) {
        Page<ClientAsset> assets = assetRepository.findByInvestorIdAndDeleted(investorId, 0,
                org.springframework.data.domain.PageRequest.of(0, Integer.MAX_VALUE));

        for (ClientAsset asset : assets) {
            asset.setIsAuthorized(authorized ? 1 : 0);
            linkAssetToClient(investorId, asset);
            assetRepository.save(asset);
        }
        clientService.refreshClientAumForInvestor(investorId);
    }

    /**
     * 将投资人授权的资产关联到主归属店铺的客户记录（auth_scope 首店）。
     */
    private void linkAssetToClient(Long investorId, ClientAsset asset) {
        if (asset.getIsAuthorized() == null || asset.getIsAuthorized() != 1
                || !StringUtils.hasText(asset.getAuthScope())) {
            return;
        }

        String phone = resolvePhone(investorId);
        if (phone == null) {
            return;
        }

        String primaryShopStr = AuthScopeRules.primaryShopId(asset.getAuthScope());
        if (primaryShopStr == null) {
            return;
        }
        try {
            Long shopId = Long.parseLong(primaryShopStr);
            Optional<Client> client = clientRepository.findByShopIdAndPhoneAndDeleted(shopId, phone, 0);
            if (client.isPresent()) {
                asset.setClientId(client.get().getId());
                log.debug("linkAssetToClient: asset {} -> clientId {} (primaryShopId={})",
                        asset.getId(), client.get().getId(), shopId);
            } else {
                // 如果没有找到 Client，则创建一个新的（投资人授权资产时未访问过店铺）
                Client newClient = new Client();
                newClient.setPhone(phone);
                newClient.setShopId(shopId);
                newClient.setName("投资人-" + phone.substring(phone.length() - 4));
                newClient.setSource("投资人端资产授权");
                newClient.setStatus(0);
                newClient = clientRepository.save(newClient);
                asset.setClientId(newClient.getId());
                log.info("linkAssetToClient: 为投资人 {} 创建新客户记录 shopId={}, assetId={}", phone, shopId, asset.getId());
            }
        } catch (NumberFormatException e) {
            log.warn("linkAssetToClient: invalid primary shopId in authScope: {}", primaryShopStr);
        }
    }

    private String resolvePhone(Long investorId) {
        List<String> phones = jdbcTemplate.query(
                "SELECT phone FROM sys_user WHERE id = ? AND is_deleted = 0 LIMIT 1",
                (rs, rowNum) -> rs.getString("phone"),
                investorId
        );
        return phones.isEmpty() ? null : phones.get(0);
    }
}
