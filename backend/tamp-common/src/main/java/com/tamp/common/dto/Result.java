package com.tamp.common.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import java.time.LocalDateTime;

/**
 * 【统一响应封装】所有 API 接口返回给前端的数据格式
 *
 * 不管是成功还是失败，后端都返回这个格式，前端只需要看 code 就知道成功还是失败：
 *
 * 成功示例：{ "code": 0, "message": "操作成功", "data": { 产品列表... }, "timestamp": "2026-06-22T10:00:00" }
 * 失败示例：{ "code": 3000, "message": "产品不存在", "data": null, "timestamp": "2026-06-22T10:00:00" }
 *
 * code 的含义：
 *   0     = 成功
 *   1004  = 没登录或 token 过期
 *   1003  = 没权限
 *   3000+ = 各种业务错误（产品不存在、客户不存在...）
 *   详见 ErrorCode.java
 *
 * <T> 是泛型，意思是 data 里可以放任何类型的数据：
 *   Result<Product>     → data 里是一个产品对象
 *   Result<List<...>>   → data 里是一个列表
 *   Result<Void>        → data 为空（比如删除操作，不需要返回数据）
 */
@JsonInclude(JsonInclude.Include.NON_NULL)  // 字段为 null 时不输出，让 JSON 更简洁
public class Result<T> {

    /** 错误码：0=成功，非0=失败（具体值见 ErrorCode.java） */
    private int code;

    /** 提示信息：成功时"操作成功"，失败时显示具体错误原因 */
    private String message;

    /** 响应数据：成功时有值，失败时通常为 null */
    private T data;

    /** 时间戳：接口响应的时间，方便排查问题 */
    private LocalDateTime timestamp;

    public Result() {
        this.timestamp = LocalDateTime.now();  // 自动记录当前时间
    }

    // ===== getter/setter =====

    public int getCode() { return code; }
    public void setCode(int code) { this.code = code; }
    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }
    public T getData() { return data; }
    public void setData(T data) { this.data = data; }
    public LocalDateTime getTimestamp() { return timestamp; }

    // ===== 静态工厂方法 —— 创建 Result 对象的快捷方式 =====

    /**
     * 成功（无数据）—— 用于删除、更新等不需要返回数据的操作
     * 用法：return Result.ok();
     * 返回：{ "code": 0, "message": "操作成功" }
     */
    public static <T> Result<T> ok() {
        Result<T> result = new Result<>();
        result.setCode(0);
        result.setMessage("操作成功");
        return result;
    }

    /**
     * 成功（带数据）—— 最常用的方式
     * 用法：return Result.ok(product);
     * 返回：{ "code": 0, "message": "操作成功", "data": { 产品对象 } }
     */
    public static <T> Result<T> ok(T data) {
        Result<T> result = new Result<>();
        result.setCode(0);
        result.setMessage("操作成功");
        result.setData(data);
        return result;
    }

    /**
     * 成功（自定义消息 + 数据）—— 需要返回特殊提示信息时用
     * 用法：return Result.ok("创建成功", newProduct);
     */
    public static <T> Result<T> ok(String message, T data) {
        Result<T> result = new Result<>();
        result.setCode(0);
        result.setMessage(message);
        result.setData(data);
        return result;
    }

    /**
     * 失败（手动指定错误码和消息）—— 一般不用，优先用下面的 fail(ErrorCode) 方式
     * 用法：return Result.fail(1004, "Token已过期");
     */
    public static <T> Result<T> fail(int code, String message) {
        Result<T> result = new Result<>();
        result.setCode(code);
        result.setMessage(message);
        return result;
    }

    /**
     * 失败（使用 ErrorCode 枚举）—— 推荐方式！错误码和消息集中管理，不会写错
     * 用法：return Result.fail(ErrorCode.BIZ_PRODUCT_NOT_FOUND);
     * 返回：{ "code": 3000, "message": "产品不存在" }
     */
    public static <T> Result<T> fail(com.tamp.common.constants.ErrorCode errorCode) {
        Result<T> result = new Result<>();
        result.setCode(errorCode.getCode());
        result.setMessage(errorCode.getMessage());
        return result;
    }
}
