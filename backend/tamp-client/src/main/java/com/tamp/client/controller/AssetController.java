package com.tamp.client.controller;

import com.tamp.client.controller.vo.AssetSummaryVO;
import com.tamp.client.entity.ClientAsset;
import com.tamp.client.service.AssetService;
import com.tamp.common.dto.PageResult;
import com.tamp.common.dto.Result;
import com.tamp.common.security.RequireRole;
import com.tamp.common.util.SecurityUtils;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.*;

/**
 * 资产管理控制器 — 投资人端
 */
@RestController
@RequestMapping("/api/assets")
@RequireRole("INVESTOR")
public class AssetController {

    private final AssetService assetService;

    public AssetController(AssetService assetService) {
        this.assetService = assetService;
    }

    /**
     * 我的资产列表
     */
    @GetMapping("")
    public Result<PageResult<ClientAsset>> listMyAssets(Pageable pageable) {
        Long investorId = SecurityUtils.getCurrentUserId();
        return Result.ok(PageResult.of(assetService.listMyAssets(investorId, pageable)));
    }

    /**
     * 单条资产详情
     */
    @GetMapping("/{id}")
    public Result<ClientAsset> getAsset(@PathVariable Long id) {
        Long investorId = SecurityUtils.getCurrentUserId();
        return Result.ok(assetService.getAsset(investorId, id));
    }

    /**
     * 资产总览
     */
    @GetMapping("/summary")
    public Result<AssetSummaryVO> getAssetSummary() {
        Long investorId = SecurityUtils.getCurrentUserId();
        return Result.ok(assetService.getAssetSummary(investorId));
    }

    /**
     * 上传/录入资产
     */
    @PostMapping("")
    public Result<ClientAsset> createAsset(@RequestBody ClientAsset asset) {
        Long investorId = SecurityUtils.getCurrentUserId();
        return Result.ok(assetService.createAsset(investorId, asset));
    }

    /**
     * 编辑资产
     */
    @PutMapping("/{id}")
    public Result<ClientAsset> updateAsset(@PathVariable Long id,
                                           @RequestBody ClientAsset asset) {
        Long investorId = SecurityUtils.getCurrentUserId();
        return Result.ok(assetService.updateAsset(investorId, id, asset));
    }

    /**
     * 删除资产
     */
    @DeleteMapping("/{id}")
    public Result<Void> deleteAsset(@PathVariable Long id) {
        Long investorId = SecurityUtils.getCurrentUserId();
        assetService.deleteAsset(investorId, id);
        return Result.ok();
    }

    /**
     * 单项授权设置
     */
    @PutMapping("/{id}/auth")
    public Result<Void> updateAssetAuth(@PathVariable Long id,
                                        @RequestBody AuthRequest request) {
        Long investorId = SecurityUtils.getCurrentUserId();
        assetService.updateAssetAuth(investorId, id, request.getIsAuthorized(), request.getAuthScope());
        return Result.ok();
    }

    /**
     * 全局一键授权开关
     */
    @PutMapping("/global-auth")
    public Result<Void> toggleGlobalAuth(@RequestBody GlobalAuthRequest request) {
        Long investorId = SecurityUtils.getCurrentUserId();
        assetService.toggleGlobalAuth(investorId, request.getAuthorized());
        return Result.ok();
    }

    // ==================== 请求 DTO ====================

    public static class AuthRequest {
        private Boolean isAuthorized;
        private String authScope;

        public Boolean getIsAuthorized() {
            return isAuthorized;
        }

        public void setIsAuthorized(Boolean isAuthorized) {
            this.isAuthorized = isAuthorized;
        }

        public String getAuthScope() {
            return authScope;
        }

        public void setAuthScope(String authScope) {
            this.authScope = authScope;
        }
    }

    public static class GlobalAuthRequest {
        private Boolean authorized;

        public Boolean getAuthorized() {
            return authorized;
        }

        public void setAuthorized(Boolean authorized) {
            this.authorized = authorized;
        }
    }
}
