package com.tamp.user.controller;

import com.tamp.user.entity.Permission;
import com.tamp.user.service.PermissionService;
import com.tamp.common.dto.Result;
import com.tamp.common.security.RequireRole;
import com.tamp.common.util.SecurityUtils;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 权限管理控制器
 */
@RestController
@RequestMapping("/api/permissions")
public class PermissionController {

    private final PermissionService permissionService;

    public PermissionController(PermissionService permissionService) {
        this.permissionService = permissionService;
    }

    /**
     * 权限列表（查询自己的权限，或管理员查询指定用户的权限）
     */
    @GetMapping("")
    public Result<Object> listPermissions(
            @RequestParam(required = false) Long userId,
            @RequestParam(required = false) String scope) {
        if ("role".equals(scope)) {
            return Result.ok(permissionService.getRolePermissions());
        }
        if (userId == null) {
            userId = SecurityUtils.getCurrentUserId();
        }
        return Result.ok(permissionService.listPermissions(userId));
    }

    /**
     * 更新单个权限
     */
    @PutMapping("/{id}")
    @RequireRole({"SUPER_ADMIN", "PLATFORM_ADMIN"})
    public Result<Void> updatePermission(@PathVariable Long id,
                                         @RequestBody UpdatePermissionRequest request) {
        Long currentUserId = SecurityUtils.getCurrentUserId();
        permissionService.updatePermissionById(currentUserId, id, request.getAllowed());
        return Result.ok();
    }

    /**
     * 批量更新角色权限模板
     */
    @PutMapping("")
    @RequireRole({"SUPER_ADMIN", "PLATFORM_ADMIN"})
    public Result<Void> updateRolePermissions(@RequestBody RolePermissionUpdateRequest request) {
        if ("role".equals(request.getScope()) && request.getPermissions() != null) {
            permissionService.updateRolePermissions(request.getPermissions());
        }
        return Result.ok();
    }

    // ==================== 请求 DTO ====================

    public static class UpdatePermissionRequest {
        private Boolean allowed;

        public Boolean getAllowed() {
            return allowed;
        }

        public void setAllowed(Boolean allowed) {
            this.allowed = allowed;
        }
    }

    public static class RolePermissionUpdateRequest {
        private String scope;
        private Map<String, Map<String, Boolean>> permissions;

        public String getScope() {
            return scope;
        }

        public void setScope(String scope) {
            this.scope = scope;
        }

        public Map<String, Map<String, Boolean>> getPermissions() {
            return permissions;
        }

        public void setPermissions(Map<String, Map<String, Boolean>> permissions) {
            this.permissions = permissions;
        }
    }
}
