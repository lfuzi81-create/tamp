package com.tamp.client.service;

/**
 * 授权范围规则：多店授权时 AUM 仅计入 auth_scope 中的第一个店铺（主归属店）。
 */
public final class AuthScopeRules {

    private AuthScopeRules() {
    }

    public static String primaryShopId(String authScope) {
        if (authScope == null || authScope.isBlank()) {
            return null;
        }
        String first = authScope.split(",", -1)[0].trim();
        return first.isEmpty() ? null : first;
    }

    public static boolean isPrimaryShop(String authScope, Long shopId) {
        if (shopId == null) {
            return false;
        }
        String primary = primaryShopId(authScope);
        if (primary == null) {
            return true;
        }
        return primary.equals(shopId.toString());
    }
}
