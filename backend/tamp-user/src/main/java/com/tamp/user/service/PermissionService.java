package com.tamp.user.service;

import com.tamp.auth.entity.Role;
import com.tamp.auth.entity.User;
import com.tamp.auth.repository.UserRepository;
import com.tamp.user.entity.Permission;
import com.tamp.user.entity.RolePermissionTemplate;
import com.tamp.user.repository.PermissionRepository;
import com.tamp.user.repository.RolePermissionTemplateRepository;
import com.tamp.common.constants.ErrorCode;
import com.tamp.common.exception.BizException;
import com.tamp.common.util.SecurityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * 权限管理服务
 */
@Service
public class PermissionService {

    private static final Logger log = LoggerFactory.getLogger(PermissionService.class);

    private static final String[] PERMISSION_GROUP_CODES = {
            "dashboard",
            "staff_management",
            "tamp_management",
            "product_library",
            "content_library",
            "knowledge_library",
            "client_management",
            "permission_management",
            "system_config"
    };

    private final PermissionRepository permissionRepository;
    private final RolePermissionTemplateRepository rolePermissionTemplateRepository;
    private final UserRepository userRepository;

    public PermissionService(PermissionRepository permissionRepository,
                             RolePermissionTemplateRepository rolePermissionTemplateRepository,
                             UserRepository userRepository) {
        this.permissionRepository = permissionRepository;
        this.rolePermissionTemplateRepository = rolePermissionTemplateRepository;
        this.userRepository = userRepository;
    }

    /**
     * 查询用户权限列表
     */
    public List<Permission> listPermissions(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BizException(ErrorCode.NOT_FOUND));
        if (user.getRole() == Role.SUPER_ADMIN) {
            return getAllPermissionsAsAllowed(userId);
        }
        Map<String, Boolean> defaults = DEFAULT_ROLE_PERMISSIONS.getOrDefault(
                user.getRole().name(), new LinkedHashMap<>()
        );
        Map<String, Permission> permissionMap = new LinkedHashMap<>();
        for (Map.Entry<String, Boolean> entry : defaults.entrySet()) {
            Permission permission = new Permission();
            permission.setUserId(userId);
            permission.setResourceCode(entry.getKey());
            permission.setGroupCode(entry.getKey());
            permission.setIsGroupSwitch(1);
            permission.setAllowed(entry.getValue() ? 1 : 0);
            permissionMap.put(entry.getKey(), permission);
        }
        for (Permission permission : permissionRepository.findByUserId(userId)) {
            permissionMap.put(permission.getResourceCode(), permission);
        }
        return new ArrayList<>(permissionMap.values());
    }

    /**
     * 返回全部权限组为允许
     */
    public List<Permission> getAllPermissionsAsAllowed(Long userId) {
        List<Permission> permissions = new ArrayList<>();
        for (String groupCode : PERMISSION_GROUP_CODES) {
            Permission permission = new Permission();
            permission.setUserId(userId);
            permission.setResourceCode(groupCode);
            permission.setGroupCode(groupCode);
            permission.setIsGroupSwitch(1);
            permission.setAllowed(1);
            permissions.add(permission);
        }
        return permissions;
    }

    /**
     * 更新权限
     */
    @Transactional
    public void updatePermission(String resourceCode, Boolean allowed) {
        Long currentUserId = SecurityUtils.getCurrentUserId();
        Permission permission = permissionRepository.findByUserId(currentUserId).stream()
                .filter(p -> resourceCode.equals(p.getResourceCode()))
                .findFirst()
                .orElseGet(() -> {
                    Permission p = new Permission();
                    p.setUserId(currentUserId);
                    p.setResourceCode(resourceCode);
                    return p;
                });

        permission.setAllowed(allowed ? 1 : 0);
        permissionRepository.save(permission);
    }

    /**
     * 按权限ID更新权限
     */
    @Transactional
    public void updatePermissionById(Long userId, Long permissionId, Boolean allowed) {
        Permission permission = permissionRepository.findById(permissionId)
                .orElseThrow(() -> new BizException(ErrorCode.NOT_FOUND));

        if (!permission.getUserId().equals(userId)) {
            throw new BizException(ErrorCode.FORBIDDEN);
        }

        permission.setAllowed(allowed ? 1 : 0);
        permissionRepository.save(permission);
    }

    private static final Map<String, Map<String, Boolean>> DEFAULT_ROLE_PERMISSIONS = new LinkedHashMap<>();

    static {
        Map<String, Boolean> superAdminPerms = new LinkedHashMap<>();
        for (String code : PERMISSION_GROUP_CODES) {
            superAdminPerms.put(code, true);
        }
        DEFAULT_ROLE_PERMISSIONS.put("SUPER_ADMIN", superAdminPerms);

        Map<String, Boolean> platformAdminPerms = new LinkedHashMap<>();
        platformAdminPerms.put("dashboard", true);
        platformAdminPerms.put("staff_management", true);
        platformAdminPerms.put("tamp_management", true);
        platformAdminPerms.put("product_library", true);
        platformAdminPerms.put("content_library", true);
        platformAdminPerms.put("knowledge_library", true);
        platformAdminPerms.put("client_management", true);
        platformAdminPerms.put("permission_management", false);
        platformAdminPerms.put("system_config", true);
        DEFAULT_ROLE_PERMISSIONS.put("PLATFORM_ADMIN", platformAdminPerms);

        Map<String, Boolean> tampAdminPerms = new LinkedHashMap<>();
        tampAdminPerms.put("dashboard", true);
        tampAdminPerms.put("staff_management", true);
        tampAdminPerms.put("tamp_management", true);
        tampAdminPerms.put("product_library", false);
        tampAdminPerms.put("content_library", false);
        tampAdminPerms.put("knowledge_library", false);
        tampAdminPerms.put("client_management", true);
        tampAdminPerms.put("permission_management", false);
        tampAdminPerms.put("system_config", false);
        DEFAULT_ROLE_PERMISSIONS.put("TAMP_ADMIN", tampAdminPerms);

        Map<String, Boolean> operatorPerms = new LinkedHashMap<>();
        operatorPerms.put("dashboard", true);
        operatorPerms.put("staff_management", false);
        operatorPerms.put("tamp_management", false);
        operatorPerms.put("product_library", true);
        operatorPerms.put("content_library", true);
        operatorPerms.put("knowledge_library", true);
        operatorPerms.put("client_management", true);
        operatorPerms.put("permission_management", false);
        operatorPerms.put("system_config", false);
        DEFAULT_ROLE_PERMISSIONS.put("OPERATOR", operatorPerms);
    }

    private static final Map<String, String> MODULE_TO_RESOURCE_CODE = new LinkedHashMap<>();

    static {
        MODULE_TO_RESOURCE_CODE.put("dashboard", "dashboard");
        MODULE_TO_RESOURCE_CODE.put("product", "product_library");
        MODULE_TO_RESOURCE_CODE.put("content", "content_library");
        MODULE_TO_RESOURCE_CODE.put("knowledge", "knowledge_library");
        MODULE_TO_RESOURCE_CODE.put("staff", "staff_management");
        MODULE_TO_RESOURCE_CODE.put("permission", "permission_management");
        MODULE_TO_RESOURCE_CODE.put("tamp", "tamp_management");
        MODULE_TO_RESOURCE_CODE.put("client", "client_management");
    }

    public Map<String, Map<String, Boolean>> getRolePermissions() {
        List<RolePermissionTemplate> templates = rolePermissionTemplateRepository
                .findByRoleIn(List.of("PLATFORM_ADMIN", "TAMP_ADMIN"));

        Map<String, Map<String, Boolean>> result = new LinkedHashMap<>();

        Map<String, Boolean> superAdminPerms = new LinkedHashMap<>();
        for (String code : PERMISSION_GROUP_CODES) {
            superAdminPerms.put(code, true);
        }
        result.put("SUPER_ADMIN", superAdminPerms);

        for (String role : List.of("PLATFORM_ADMIN", "TAMP_ADMIN")) {
            Map<String, Boolean> defaults = DEFAULT_ROLE_PERMISSIONS.getOrDefault(role, new LinkedHashMap<>());
            Map<String, Boolean> rolePerms = new LinkedHashMap<>(defaults);

            for (RolePermissionTemplate t : templates) {
                if (role.equals(t.getRole())) {
                    rolePerms.put(t.getResourceCode(), t.getAllowed() != null && t.getAllowed() == 1);
                }
            }
            result.put(role, rolePerms);
        }

        return result;
    }

    @Transactional
    public void updateRolePermissions(Map<String, Map<String, Boolean>> permissions) {
        for (Map.Entry<String, Map<String, Boolean>> roleEntry : permissions.entrySet()) {
            String role = roleEntry.getKey();
            if ("SUPER_ADMIN".equals(role)) continue;

            Map<String, Boolean> perms = roleEntry.getValue();
            List<RolePermissionTemplate> existing = rolePermissionTemplateRepository.findByRole(role);
            Map<String, RolePermissionTemplate> existingMap = new LinkedHashMap<>();
            for (RolePermissionTemplate t : existing) {
                existingMap.put(t.getResourceCode(), t);
            }

            for (Map.Entry<String, Boolean> permEntry : perms.entrySet()) {
                String resourceCode = permEntry.getKey();
                Boolean allowed = permEntry.getValue();

                RolePermissionTemplate template = existingMap.get(resourceCode);
                if (template == null) {
                    template = new RolePermissionTemplate();
                    template.setRole(role);
                    template.setResourceCode(resourceCode);
                }
                template.setAllowed(allowed ? 1 : 0);
                rolePermissionTemplateRepository.save(template);
            }
        }
    }
}
