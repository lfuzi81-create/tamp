package com.tamp.org.controller;

import com.tamp.org.entity.Staff;
import com.tamp.org.service.StaffService;
import com.tamp.common.dto.PageResult;
import com.tamp.common.dto.Result;
import com.tamp.common.security.RequireRole;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/staff")
@RequireRole({"SUPER_ADMIN", "PLATFORM_ADMIN", "TAMP_ADMIN"})
public class StaffController {

    private final StaffService staffService;

    public StaffController(StaffService staffService) {
        this.staffService = staffService;
    }

    @GetMapping
    public Result<PageResult<Staff>> listStaff(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String roleType,
            @RequestParam(required = false) Long officeId,
            Pageable pageable) {
        Page<Staff> page = staffService.listStaff(keyword, roleType, officeId, pageable);
        return Result.ok(PageResult.of(page));
    }

    @GetMapping("/office/{officeId}")
    public Result<PageResult<Staff>> listByOffice(
            @PathVariable Long officeId,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Integer status,
            Pageable pageable) {
        Page<Staff> page = staffService.listByOffice(officeId, keyword, status, pageable);
        return Result.ok(PageResult.of(page));
    }

    @GetMapping("/shop/{shopId}")
    @RequireRole({"SHOP_ADMIN", "SUPER_ADMIN", "PLATFORM_ADMIN", "TAMP_ADMIN"})
    public Result<PageResult<Staff>> listByShop(@PathVariable Long shopId, Pageable pageable) {
        Page<Staff> page = staffService.listByShop(shopId, pageable);
        return Result.ok(PageResult.of(page));
    }

    @PostMapping
    @RequireRole({"SUPER_ADMIN", "PLATFORM_ADMIN", "TAMP_ADMIN", "SHOP_ADMIN"})
    public Result<Staff> createStaff(@RequestBody Staff staff) {
        return Result.ok(staffService.createStaff(staff));
    }

    @PutMapping("/{id}")
    @RequireRole({"SUPER_ADMIN", "PLATFORM_ADMIN", "TAMP_ADMIN", "SHOP_ADMIN"})
    public Result<Staff> updateStaff(@PathVariable Long id, @RequestBody Staff staff) {
        return Result.ok(staffService.updateStaff(id, staff));
    }

    @PutMapping("/{id}/status")
    @RequireRole({"SUPER_ADMIN", "PLATFORM_ADMIN", "TAMP_ADMIN", "SHOP_ADMIN"})
    public Result<Void> toggleStaffStatus(@PathVariable Long id) {
        staffService.toggleStaffStatus(id);
        return Result.ok();
    }

    @DeleteMapping("/{id}")
    @RequireRole({"SUPER_ADMIN", "PLATFORM_ADMIN", "TAMP_ADMIN", "SHOP_ADMIN"})
    public Result<Void> deleteStaff(@PathVariable Long id) {
        staffService.deleteStaff(id);
        return Result.ok();
    }
}
