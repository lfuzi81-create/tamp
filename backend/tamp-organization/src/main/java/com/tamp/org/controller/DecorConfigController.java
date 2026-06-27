package com.tamp.org.controller;

import com.tamp.common.dto.Result;
import com.tamp.common.security.RequireRole;
import com.tamp.common.util.SecurityUtils;
import com.tamp.org.entity.DecorConfig;
import com.tamp.org.service.DecorConfigService;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/decor-config")
@RequireRole({"SHOP_ADMIN", "SUPER_ADMIN", "PLATFORM_ADMIN", "TAMP_ADMIN", "INVESTOR"})
public class DecorConfigController {

    private final DecorConfigService decorConfigService;

    public DecorConfigController(DecorConfigService decorConfigService) {
        this.decorConfigService = decorConfigService;
    }

    /**
     * 投资人端获取当前家办的装修配置
     *
     * shopId 来源优先级（防伪造）：
     * 1. JWT 内的 shopId（SecurityUtils.getCurrentShopId，登录时签发，不可伪造）
     * 2. X-Shop-Id 请求头（仅当 JWT 无 shopId 时回退，向后兼容非投资人端）
     * 3. office_id 兜底（无 shopId 场景）
     */
    @GetMapping("/investor")
    public Result<DecorConfig> getInvestorDecorConfig(
            @RequestHeader(value = "X-Shop-Id", required = false) Long headerShopId) {
        // 优先用 JWT 中的 shopId（防伪造），其次回退到请求头
        Long shopId = SecurityUtils.getCurrentShopId();
        if (shopId == null) {
            shopId = headerShopId;
        }
        if (shopId != null) {
            return Result.ok(decorConfigService.getByShopId(shopId));
        }
        Long officeId = SecurityUtils.getCurrentOfficeId();
        if (officeId == null) {
            return Result.ok(null);
        }
        return Result.ok(decorConfigService.getByOfficeId(officeId));
    }

    @GetMapping("/shop/{shopId}")
    public Result<DecorConfig> getByShopId(@PathVariable Long shopId) {
        return Result.ok(decorConfigService.getByShopId(shopId));
    }

    @GetMapping("/office/{officeId}")
    public Result<DecorConfig> getByOfficeId(@PathVariable Long officeId) {
        return Result.ok(decorConfigService.getByOfficeId(officeId));
    }

    @PostMapping
    @RequireRole({"SHOP_ADMIN", "SUPER_ADMIN", "PLATFORM_ADMIN", "TAMP_ADMIN"})
    public Result<DecorConfig> create(@RequestBody DecorConfig config) {
        return Result.ok(decorConfigService.createOrUpdate(config));
    }

    @PutMapping("/{id}")
    @RequireRole({"SHOP_ADMIN", "SUPER_ADMIN", "PLATFORM_ADMIN", "TAMP_ADMIN"})
    public Result<DecorConfig> update(@PathVariable Long id, @RequestBody DecorConfig config) {
        config.setId(id);
        return Result.ok(decorConfigService.createOrUpdate(config));
    }

    @PutMapping("/{id}/modules")
    @RequireRole({"SHOP_ADMIN", "SUPER_ADMIN", "PLATFORM_ADMIN", "TAMP_ADMIN"})
    public Result<DecorConfig> updateModules(@PathVariable Long id, @RequestBody String modulesJson) {
        return Result.ok(decorConfigService.updateModules(id, modulesJson));
    }

    @PutMapping("/{id}/navigation")
    @RequireRole({"SHOP_ADMIN", "SUPER_ADMIN", "PLATFORM_ADMIN", "TAMP_ADMIN"})
    public Result<DecorConfig> updateNavigation(@PathVariable Long id, @RequestBody String navigationJson) {
        return Result.ok(decorConfigService.updateNavigation(id, navigationJson));
    }

    @PutMapping("/{id}/shelf-selections")
    @RequireRole({"SHOP_ADMIN", "SUPER_ADMIN", "PLATFORM_ADMIN", "TAMP_ADMIN"})
    public Result<DecorConfig> updateShelfSelections(@PathVariable Long id, @RequestBody String selectionsJson) {
        return Result.ok(decorConfigService.updateShelfSelections(id, selectionsJson));
    }
}
