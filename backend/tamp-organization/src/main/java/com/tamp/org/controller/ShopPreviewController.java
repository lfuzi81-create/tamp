package com.tamp.org.controller;

import com.tamp.common.dto.Result;
import com.tamp.org.entity.DecorConfig;
import com.tamp.org.service.DecorConfigService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 店铺公开预览接口 — 无需登录鉴权，供投资人端公开访问店铺装修内容。
 * 注意：此类不标注 @RequireRole，避免被 RoleAuthorizationInterceptor 拦截。
 */
@RestController
@RequestMapping("/api/shop-preview")
public class ShopPreviewController {

    private final DecorConfigService decorConfigService;

    public ShopPreviewController(DecorConfigService decorConfigService) {
        this.decorConfigService = decorConfigService;
    }

    /**
     * 公开获取指定店铺的装修配置（用于投资人端店铺预览页）
     */
    @GetMapping("/{shopId}/decor-config")
    public Result<DecorConfig> getShopDecorConfig(@PathVariable Long shopId) {
        return Result.ok(decorConfigService.getByShopId(shopId));
    }
}
