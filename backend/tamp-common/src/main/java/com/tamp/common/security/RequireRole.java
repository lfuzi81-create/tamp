package com.tamp.common.security;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 【角色权限注解】—— 标注在 Controller 的类或方法上，声明"这个接口只允许哪些角色访问"
 *
 * 使用方式：在方法上加 @RequireRole({"SUPER_ADMIN", "PLATFORM_ADMIN"})
 * 效果：只有超级管理员和平台管理员能访问，其他角色会收到 403 错误
 *
 * 可以加在类上（对整个 Controller 生效）：
 *   @RequireRole("SHOP_ADMIN")
 *   public class ShelfController { ... }  → 整个控制器只有店铺管理员能访问
 *
 * 也可以加在方法上（只对单个接口生效）：
 *   @RequireRole({"SUPER_ADMIN", "PLATFORM_ADMIN", "TAMP_ADMIN"})
 *   public Result<Product> createProduct(...) { ... }  → 只有这三种角色能创建产品
 *
 * 角色取值（对应 Role 枚举）：
 *   SUPER_ADMIN    = 超级管理员（最高权限）
 *   PLATFORM_ADMIN = 平台管理员
 *   TAMP_ADMIN     = tamp管理员
 *   SHOP_ADMIN     = 店铺管理员
 *   INVESTOR       = 投资人
 */
@Target({ElementType.METHOD, ElementType.TYPE})   // 可以加在方法上，也可以加在类上
@Retention(RetentionPolicy.RUNTIME)                // 运行时保留（这样拦截器才能读到它）
public @interface RequireRole {

    /**
     * 允许访问的角色列表
     * 只要当前用户的角色在这个列表里，就允许访问
     *
     * 用法：@RequireRole({"SUPER_ADMIN", "PLATFORM_ADMIN"})
     * 意思：超级管理员 或 平台管理员 都能访问（满足其中一个就行）
     */
    String[] value();
}
