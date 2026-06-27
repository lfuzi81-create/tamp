package com.tamp.org.controller;

import com.tamp.org.entity.FamilyOffice;
import com.tamp.org.entity.Shop;
import com.tamp.org.controller.vo.OfficeDetailVO;
import com.tamp.org.service.OrganizationService;
import com.tamp.common.dto.PageResult;
import com.tamp.common.dto.Result;
import com.tamp.common.security.RequireRole;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequireRole({"SUPER_ADMIN", "PLATFORM_ADMIN", "TAMP_ADMIN"})
public class OrganizationController {

    private final OrganizationService organizationService;

    public OrganizationController(OrganizationService organizationService) {
        this.organizationService = organizationService;
    }

    // ===== 家办接口 =====

    @GetMapping("/api/offices")
    public Result<PageResult<FamilyOffice>> listOffices(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Integer status,
            Pageable pageable) {
        Page<FamilyOffice> page = organizationService.listOffices(keyword, status, pageable);
        return Result.ok(PageResult.of(page));
    }

    @GetMapping("/api/offices/{id}")
    public Result<FamilyOffice> getOffice(@PathVariable Long id) {
        return Result.ok(organizationService.getOffice(id));
    }

    @GetMapping("/api/offices/{id}/detail")
    public Result<OfficeDetailVO> getOfficeDetail(@PathVariable Long id) {
        return Result.ok(organizationService.getOfficeDetail(id));
    }

    @PostMapping("/api/offices")
    public Result<FamilyOffice> createOffice(@RequestBody FamilyOffice office) {
        return Result.ok(organizationService.createOffice(office));
    }

    @PutMapping("/api/offices/{id}")
    public Result<FamilyOffice> updateOffice(@PathVariable Long id, @RequestBody FamilyOffice office) {
        return Result.ok(organizationService.updateOffice(id, office));
    }

    @PutMapping("/api/offices/{id}/status")
    public Result<Void> toggleOfficeStatus(@PathVariable Long id) {
        organizationService.toggleOfficeStatus(id);
        return Result.ok();
    }

    @GetMapping("/api/offices/{id}/shops")
    public Result<List<Shop>> listShopsByOffice(@PathVariable Long id) {
        return Result.ok(organizationService.listShopsByOffice(id));
    }

    // ===== 店铺接口 =====

    @GetMapping("/api/shops")
    public Result<PageResult<Shop>> listShops(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Long officeId,
            @RequestParam(required = false) Integer status,
            Pageable pageable) {
        Page<Shop> page = organizationService.listShops(keyword, officeId, status, pageable);
        return Result.ok(PageResult.of(page));
    }

    @GetMapping("/api/shops/{id}")
    public Result<Shop> getShop(@PathVariable Long id) {
        return Result.ok(organizationService.getShop(id));
    }

    @PostMapping("/api/shops")
    public Result<Shop> createShop(@RequestBody Shop shop) {
        return Result.ok(organizationService.createShop(shop));
    }

    @PutMapping("/api/shops/{id}")
    public Result<Shop> updateShop(@PathVariable Long id, @RequestBody Shop shop) {
        return Result.ok(organizationService.updateShop(id, shop));
    }

    @PutMapping("/api/shops/{id}/status")
    public Result<Void> toggleShopStatus(@PathVariable Long id) {
        organizationService.toggleShopStatus(id);
        return Result.ok();
    }
}
