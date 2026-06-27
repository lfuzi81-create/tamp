package com.tamp.common.exception;

import com.tamp.common.constants.ErrorCode;

/**
 * 【业务异常类】当业务逻辑出错时，抛出这个异常
 *
 * 它就像一个"错误信号弹"：
 *   - 在 Service 层发现问题 → throw new BizException(ErrorCode.BIZ_PRODUCT_NOT_FOUND)
 *   - GlobalExceptionHandler 捕获到信号 → 自动转成 Result JSON 返回给前端
 *   - 前端收到 { "code": 3000, "message": "产品不存在" }
 *
 * 为什么不直接 return Result.fail()？
 *   因为 Service 层可能被多个 Controller 调用，每个 Controller 的返回类型不同。
 *   用异常可以统一处理，不需要在每个 Service 方法里都写返回错误结果的逻辑。
 *
 * 使用示例：
 *   throw new BizException(ErrorCode.BIZ_PRODUCT_NOT_FOUND);                // 最常用
 *   throw new BizException(ErrorCode.BIZ_PRODUCT_NOT_FOUND, "产品ID不存在"); // 自定义消息
 *   throw new BizException("出错了");                                         // 简单版（不推荐）
 */
public class BizException extends RuntimeException {

    /** 错误码，对应 ErrorCode 枚举里的数字 */
    private final int code;

    /**
     * 方式1：只传消息 —— 错误码默认为 1000（未知错误），一般不推荐用
     */
    public BizException(String message) {
        super(message);
        this.code = ErrorCode.UNKNOWN_ERROR.getCode();
    }

    /**
     * 方式2：手动指定错误码和消息 —— 灵活但不推荐，容易写错
     */
    public BizException(int code, String message) {
        super(message);
        this.code = code;
    }

    /**
     * 方式3：传入 ErrorCode 枚举 —— ✅ 最常用！推荐方式
     * 错误码和消息从枚举里取，不会写错
     *
     * 用法：throw new BizException(ErrorCode.BIZ_PRODUCT_NOT_FOUND);
     * 效果：code=3000, message="产品不存在"
     */
    public BizException(ErrorCode errorCode) {
        super(errorCode.getMessage());
        this.code = errorCode.getCode();
    }

    /**
     * 方式4：传入 ErrorCode + 自定义消息 —— 枚举提供错误码，但消息自己写
     * 适用于：错误码是对的，但需要更具体的提示
     *
     * 用法：throw new BizException(ErrorCode.BIZ_PRODUCT_NOT_FOUND, "产品ID=999不存在");
     * 效果：code=3000, message="产品ID=999不存在"
     */
    public BizException(ErrorCode errorCode, String customMessage) {
        super(customMessage);
        this.code = errorCode.getCode();
    }

    /** 获取错误码 */
    public int getCode() { return code; }
}
