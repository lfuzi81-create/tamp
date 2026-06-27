package com.tamp.auth.entity;

/**
 * 用户角色枚举 — 对应多端用户类型
 */
public enum Role {

    SUPER_ADMIN("超级管理员", "super"),
    PLATFORM_ADMIN("平台管理员", "platform"),
    OPERATOR("运营人员", "ops"),
    TAMP_ADMIN("家办管理员", "tamp"),
    SHOP_ADMIN("店铺管理员", "shop"),
    INVESTOR("投资人", "investor");

    private final String description;
    private final String prefix;

    Role(String description, String prefix) {
        this.description = description;
        this.prefix = prefix;
    }

    public String getDescription() {
        return description;
    }

    public String getPrefix() {
        return prefix;
    }
}
