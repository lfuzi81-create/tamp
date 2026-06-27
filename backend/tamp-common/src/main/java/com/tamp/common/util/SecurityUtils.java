package com.tamp.common.util;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * 安全上下文工具类 — 从 SecurityContext 提取当前用户信息
 */
public final class SecurityUtils {

    private SecurityUtils() {}

    /**
     * 获取当前用户ID
     */
    public static Long getCurrentUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication instanceof UsernamePasswordAuthenticationToken) {
            Object principal = authentication.getPrincipal();
            if (principal instanceof Long) {
                return (Long) principal;
            }
            if (principal instanceof Integer) {
                return ((Integer) principal).longValue();
            }
            try {
                return Long.parseLong(String.valueOf(principal));
            } catch (NumberFormatException e) {
                return null;
            }
        }
        return null;
    }

    /**
     * 获取当前用户角色
     */
    public static String getCurrentUserRole() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication instanceof UsernamePasswordAuthenticationToken) {
            Object details = authentication.getDetails();
            if (details instanceof Map) {
                @SuppressWarnings("unchecked")
                Map<String, Object> detailMap = (Map<String, Object>) details;
                Object role = detailMap.get("role");
                return role != null ? role.toString() : null;
            }
        }
        return null;
    }

    /**
     * 获取当前用户手机号
     */
    public static String getCurrentPhone() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication instanceof UsernamePasswordAuthenticationToken) {
            Object details = authentication.getDetails();
            if (details instanceof Map) {
                @SuppressWarnings("unchecked")
                Map<String, Object> detailMap = (Map<String, Object>) details;
                Object phone = detailMap.get("phone");
                return phone != null ? phone.toString() : null;
            }
        }
        return null;
    }

    /**
     * 获取当前用户所属家办ID
     */
    public static Long getCurrentUserOfficeId() {
        return getCurrentOfficeId();
    }

    /**
     * 获取当前用户所属家办ID（短方法名）
     */
    public static Long getCurrentOfficeId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication instanceof UsernamePasswordAuthenticationToken) {
            Object details = authentication.getDetails();
            if (details instanceof Map) {
                @SuppressWarnings("unchecked")
                Map<String, Object> detailMap = (Map<String, Object>) details;
                Object officeId = detailMap.get("officeId");
                if (officeId instanceof Long) {
                    return (Long) officeId;
                }
                if (officeId instanceof Integer) {
                    return ((Integer) officeId).longValue();
                }
                if (officeId != null) {
                    try {
                        return Long.parseLong(officeId.toString());
                    } catch (NumberFormatException e) {
                        return null;
                    }
                }
            }
        }
        return null;
    }

    /**
     * 获取当前用户所属店铺ID
     */
    public static Long getCurrentUserShopId() {
        return getCurrentShopId();
    }

    /**
     * 获取当前用户所属店铺ID（短方法名）
     */
    public static Long getCurrentShopId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication instanceof UsernamePasswordAuthenticationToken) {
            Object details = authentication.getDetails();
            if (details instanceof Map) {
                @SuppressWarnings("unchecked")
                Map<String, Object> detailMap = (Map<String, Object>) details;
                Object shopId = detailMap.get("shopId");
                if (shopId instanceof Long) {
                    return (Long) shopId;
                }
                if (shopId instanceof Integer) {
                    return ((Integer) shopId).longValue();
                }
                if (shopId != null) {
                    try {
                        return Long.parseLong(shopId.toString());
                    } catch (NumberFormatException e) {
                        return null;
                    }
                }
            }
        }
        return null;
    }

    /**
     * 是否为超级管理员
     */
    public static boolean isSuperAdmin() {
        return "SUPER_ADMIN".equals(getCurrentUserRole());
    }

    /**
     * 是否为平台管理员
     */
    public static boolean isPlatformAdmin() {
        return "PLATFORM_ADMIN".equals(getCurrentUserRole());
    }

    /**
     * 是否为tamp管理员
     */
    public static boolean isTampAdmin() {
        return "TAMP_ADMIN".equals(getCurrentUserRole());
    }

    /**
     * 获取当前用户可见的家办ID列表
     *
     * 对于 OPERATOR 角色，返回 officeIds 字段解析出的列表；
     * 对于 TAMP_ADMIN 角色，返回单个 officeId 包装为列表；
     * 对于 SUPER_ADMIN/PLATFORM_ADMIN 返回 null（表示全部可见）。
     */
    public static List<Long> getCurrentUserOfficeIds() {
        String role = getCurrentUserRole();
        if ("SUPER_ADMIN".equals(role) || "PLATFORM_ADMIN".equals(role)) {
            return null;
        }

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (!(authentication instanceof UsernamePasswordAuthenticationToken)) {
            return null;
        }
        Object details = authentication.getDetails();
        if (!(details instanceof Map)) {
            return null;
        }

        @SuppressWarnings("unchecked")
        Map<String, Object> detailMap = (Map<String, Object>) details;

        List<Long> result = new ArrayList<>();

        Object officeIdsRaw = detailMap.get("officeIds");
        if (officeIdsRaw instanceof String && !((String) officeIdsRaw).isBlank()) {
            for (String s : ((String) officeIdsRaw).split(",")) {
                try {
                    result.add(Long.parseLong(s.trim()));
                } catch (NumberFormatException ignored) {}
            }
        }

        Object officeId = detailMap.get("officeId");
        if (officeId != null) {
            Long oid = null;
            if (officeId instanceof Long) {
                oid = (Long) officeId;
            } else if (officeId instanceof Integer) {
                oid = ((Integer) officeId).longValue();
            } else {
                try {
                    oid = Long.parseLong(officeId.toString());
                } catch (NumberFormatException ignored) {}
            }
            if (oid != null && !result.contains(oid)) {
                result.add(oid);
            }
        }

        return result.isEmpty() ? null : result;
    }
}
