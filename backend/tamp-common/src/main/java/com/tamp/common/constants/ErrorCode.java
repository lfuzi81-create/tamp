package com.tamp.common.constants;

/**
 * 【业务错误码枚举】系统里所有可能的错误，都定义在这里
 *
 * 为什么要集中管理？
 *   1. 不会写错 —— 用 ErrorCode.BIZ_PRODUCT_NOT_FOUND 代替手写 3000 和 "产品不存在"
 *   2. 方便搜索 —— 想知道 3000 是什么错误，来这里一查就知道
 *   3. 前后端对齐 —— 前端也有一份对应的错误码定义，知道每个 code 该怎么处理
 *
 * 编号规则（像电话区号一样，不同段代表不同类别）：
 *   0        = 成功
 *   1000-1999 = 通用错误（参数不对、没登录、没权限...）
 *   2000-2999 = 认证相关错误（密码错误、验证码过期...）
 *   3000-3999 = 业务相关错误（产品不存在、客户手机号重复...）
 */
public enum ErrorCode {

    // ===== 通用错误 (1000-1999) =====
    // 这些错误任何接口都可能发生，跟具体业务无关

    SUCCESS(0, "操作成功"),                              // 万事大吉
    UNKNOWN_ERROR(1000, "未知错误"),                      // 兜底：出了意料之外的错
    PARAM_INVALID(1001, "参数校验失败"),                   // 前端传的参数不对（比如必填字段没填）
    NOT_FOUND(1002, "资源不存在"),                        // 请求的东西找不到
    FORBIDDEN(1003, "无权限访问"),                        // 你没权限看这个
    UNAUTHORIZED(1004, "未登录或Token已过期"),             // 没登录，或者登录过期了
    TOKEN_EXPIRED(1005, "Token已过期"),                   // Token 过期（需要重新登录）
    TOKEN_INVALID(1006, "无效的Token"),                   // Token 被篡改或格式不对
    METHOD_NOT_ALLOWED(1007, "请求方法不允许"),            // 比如接口要求 POST，你用了 GET
    TOO_MANY_REQUESTS(1008, "请求过于频繁，请稍后再试"),   // 请求太频繁，被限流了
    AUTH_SMS_LOGIN_NOT_ALLOWED(1010, "该账号不支持验证码登录"),  // 只有投资人才能短信登录

    // ===== 认证相关 (2000-2999) =====
    // 这些错误只发生在登录/注册/改密码的场景

    AUTH_LOGIN_FAILED(2000, "用户名或密码错误"),           // 密码输错了
    AUTH_USER_DISABLED(2001, "用户已被禁用"),              // 账号被管理员禁用了
    AUTH_USER_LOCKED(2002, "用户已被锁定"),                // 密码输错太多次，账号被锁
    AUTH_SMS_SEND_FAILED(2003, "短信发送失败，请稍后重试"), // 短信网关出问题
    AUTH_SMS_CODE_EXPIRED(2004, "验证码已过期"),           // 验证码超过5分钟失效了
    AUTH_SMS_CODE_WRONG(2005, "验证码错误"),               // 验证码输错了
    AUTH_SMS_TOO_FREQUENT(2006, "短信发送过于频繁"),       // 60秒内只能发一次
    AUTH_PASSWORD_NOT_CHANGED(2007, "首次登录请先修改密码"), // 初始密码必须改
    AUTH_OLD_PASSWORD_WRONG(2008, "原密码错误"),           // 改密码时旧密码输错
    AUTH_PHONE_EXISTS(2009, "手机号已被注册"),             // 手机号重复
    AUTH_USERNAME_EXISTS(2010, "用户名已存在"),            // 用户名重复

    // ===== 业务相关 (3000-3999) =====
    // 这些错误跟具体业务有关，每个模块有自己的错误码段

    BIZ_PRODUCT_NOT_FOUND(3000, "产品不存在"),             // 查的产品 ID 找不到
    BIZ_PRODUCT_OFFLINE(3001, "产品已下架"),               // 产品被下架了，不能操作
    BIZ_CONTENT_NOT_FOUND(3002, "内容不存在"),             // 查的内容 ID 找不到
    BIZ_CLIENT_NOT_FOUND(3003, "客户不存在"),              // 查的客户 ID 找不到
    BIZ_CLIENT_DUPLICATE(3004, "客户手机号重复"),          // 同一店铺下手机号不能重复
    BIZ_OFFICE_NOT_FOUND(3005, "家办不存在"),              // 查的家办 ID 找不到
    BIZ_SHOP_NOT_FOUND(3006, "店铺不存在"),                // 查的店铺 ID 找不到
    BIZ_STAFF_NOT_FOUND(3007, "人员不存在"),               // 查的员工 ID 找不到
    BIZ_KNOWLEDGE_NOT_FOUND(3008, "知识文章不存在"),       // 查的知识文章 ID 找不到
    BIZ_ASSET_NOT_FOUND(3009, "资产记录不存在"),           // 查的资产 ID 找不到
    BIZ_SHELF_ITEM_EXISTS(3010, "该产品已在货架中"),       // 不能重复添加
    BIZ_SHELF_CONTENT_EXISTS(3011, "该内容已在推荐中"),    // 不能重复添加
    BIZ_CATEGORY_HAS_PRODUCTS(3012, "分类下存在产品/内容，无法删除"),  // 有关联数据不能删
    BIZ_OPERATION_FAILED(3013, "操作失败"),                // 通用操作失败
    BIZ_DECOR_CONFIG_NOT_FOUND(3014, "装修配置不存在"),    // 店铺装修配置找不到
    BIZ_SHOP_DUPLICATE(3015, "同一家办下已存在同名店铺"),  // 店铺名称重复
    BIZ_SHOP_ADMIN_REQUIRED(3016, "创建店铺必须指定管理员"); // 缺少管理员

    // ===== 字段定义 =====

    private final int code;       // 错误码数字
    private final String message; // 错误提示文字

    /**
     * 构造方法 —— 每个枚举值创建时传入 code 和 message
     * 比如 BIZ_PRODUCT_NOT_FOUND(3000, "产品不存在")
     * 就相当于创建了 code=3000, message="产品不存在" 的一个实例
     */
    ErrorCode(int code, String message) {
        this.code = code;
        this.message = message;
    }

    /** 获取错误码数字 */
    public int getCode() {
        return code;
    }

    /** 获取错误提示文字 */
    public String getMessage() {
        return message;
    }
}
