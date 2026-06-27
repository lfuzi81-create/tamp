package com.tamp.common.exception;

import com.tamp.common.constants.ErrorCode;
import com.tamp.common.dto.Result;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.validation.BindException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.NoHandlerFoundException;

import java.util.stream.Collectors;

import org.springframework.security.access.AccessDeniedException;

/**
 * 【全局异常处理器】统一拦截所有异常，转成 Result JSON 返回给前端
 *
 * 它就像一个"万能客服"：
 *   - 不管代码哪里出了错（Service 抛异常、参数校验失败、没权限...）
 *   - 都会被这个处理器拦截
 *   - 统一转成 { "code": xxx, "message": "xxx" } 的 JSON 格式返回
 *
 * 好处：
 *   - 开发者不需要在每个 Controller 里写 try-catch
 *   - 前端收到的错误格式永远一致，方便统一处理
 *   - 错误码和消息不会遗漏
 *
 * 处理顺序（从上到下，越具体越先匹配）：
 *   1. BizException        → 业务异常（我们主动抛的）
 *   2. 参数校验异常         → @Valid 校验不通过
 *   3. 认证异常             → 没登录
 *   4. 权限异常             → 没权限
 *   5. Exception            → 兜底：所有其他未知异常
 */
@RestControllerAdvice   // 【标记】我是全局异常处理器，会拦截所有 Controller 的异常
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    /**
     * ① 业务异常 —— 最常见的异常，我们在 Service 里主动抛的
     *
     * 触发场景：throw new BizException(ErrorCode.BIZ_PRODUCT_NOT_FOUND)
     * 返回示例：{ "code": 3000, "message": "产品不存在" }
     * HTTP 状态码：200（业务错误不算服务器错误，用 200 + code 区分）
     */
    @ExceptionHandler(BizException.class)
    public Result<Void> handleBizException(BizException e) {
        log.warn("业务异常: code={}, msg={}", e.getCode(), e.getMessage());  // 记录警告日志
        return Result.fail(e.getCode(), e.getMessage());                     // 转成 Result 返回
    }

    /**
     * ② 参数校验异常 —— 前端传的 JSON 数据不符合 @Valid 校验规则
     *
     * 触发场景：@RequestBody @Valid ProductDTO，但 name 字段为空（@NotBlank 校验失败）
     * 返回示例：{ "code": 1001, "message": "产品名称不能为空;风险等级不能为空" }
     * HTTP 状态码：400（客户端传了错误数据）
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Result<Void> handleValidationException(MethodArgumentNotValidException e) {
        // 把所有字段的校验错误拼成一句话，用分号隔开
        String message = e.getBindingResult().getFieldErrors().stream()
                .map(FieldError::getDefaultMessage)
                .collect(Collectors.joining("; "));
        log.warn("参数校验失败: {}", message);
        return Result.fail(ErrorCode.PARAM_INVALID.getCode(), message);
    }

    /**
     * ③ 参数绑定异常 —— 表单提交时参数格式不对
     * 和 ② 类似，只是触发场景不同（②是JSON，③是表单）
     */
    @ExceptionHandler(BindException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Result<Void> handleBindException(BindException e) {
        String message = e.getFieldErrors().stream()
                .map(FieldError::getDefaultMessage)
                .collect(Collectors.joining("; "));
        log.warn("参数绑定失败: {}", message);
        return Result.fail(ErrorCode.PARAM_INVALID.getCode(), message);
    }

    /**
     * ④ 非法参数异常 —— 代码里主动抛的参数错误
     * 触发场景：throw new IllegalArgumentException("ID不能为负数")
     */
    @ExceptionHandler(IllegalArgumentException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Result<Void> handleIllegalArgument(IllegalArgumentException e) {
        log.warn("非法参数: {}", e.getMessage());
        return Result.fail(ErrorCode.PARAM_INVALID.getCode(), e.getMessage());
    }

    /**
     * ⑤ 未认证异常 —— 没登录就访问需要登录的接口
     *
     * 触发场景：没带 token 访问 /api/products
     * 返回示例：{ "code": 1004, "message": "未登录或Token已过期" }
     * HTTP 状态码：401
     */
    @ExceptionHandler({org.springframework.security.core.AuthenticationException.class})
    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    public Result<Void> handleAuthenticationException(Exception e) {
        log.warn("认证失败: {}", e.getMessage());
        return Result.fail(ErrorCode.UNAUTHORIZED);
    }

    /**
     * ⑥ 无权限异常 —— 登录了但角色不够
     *
     * 触发场景：店铺管理员访问只有总部管理员才能用的接口
     * 返回示例：{ "code": 1003, "message": "无权限访问" }
     * HTTP 状态码：403
     */
    @ExceptionHandler(org.springframework.security.access.AccessDeniedException.class)
    @ResponseStatus(HttpStatus.FORBIDDEN)
    public Result<Void> handleAccessDeniedException(AccessDeniedException e) {
        log.warn("权限不足: {}", e.getMessage());
        return Result.fail(ErrorCode.FORBIDDEN);
    }

    /**
     * ⑦ 缺少必填参数 —— 前端没传 @RequestParam(required=true) 的参数
     *
     * 触发场景：接口声明了 @RequestParam String startDate，但前端没传
     * 返回示例：{ "code": 1001, "message": "缺少必填参数: startDate" }
     * HTTP 状态码：400
     */
    @ExceptionHandler(org.springframework.web.bind.MissingServletRequestParameterException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Result<Void> handleMissingParam(org.springframework.web.bind.MissingServletRequestParameterException e) {
        String message = "缺少必填参数: " + e.getParameterName();
        log.warn("缺少必填参数: {}", e.getParameterName());
        return Result.fail(ErrorCode.PARAM_INVALID.getCode(), message);
    }

    /**
     * ⑧ 参数类型不匹配 —— 路径变量或请求参数类型转换失败
     *
     * 触发场景：/api/offices/{id} 传了 /api/offices/abc，但 id 是 Long 类型
     * 返回示例：{ "code": 1001, "message": "参数类型不匹配: id 应为 Long" }
     * HTTP 状态码：400
     */
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Result<Void> handleTypeMismatch(MethodArgumentTypeMismatchException e) {
        String paramName = e.getName();
        String requiredType = e.getRequiredType() != null ? e.getRequiredType().getSimpleName() : "未知";
        String message = "参数类型不匹配: " + paramName + " 应为 " + requiredType;
        log.warn("参数类型不匹配: {} (期望 {})", paramName, requiredType);
        return Result.fail(ErrorCode.PARAM_INVALID.getCode(), message);
    }

    /**
     * ⑨ 接口不存在 —— 请求的 URL 没有对应的 Controller 方法
     *
     * 触发场景：前端调用了 /api/nonexistent，后端没有这个接口
     * 返回示例：{ "code": 1002, "message": "接口不存在" }
     * HTTP 状态码：404
     */
    @ExceptionHandler(NoHandlerFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public Result<Void> handleNotFound(NoHandlerFoundException e) {
        log.warn("接口不存在: {} {}", e.getHttpMethod(), e.getRequestURL());
        return Result.fail(ErrorCode.NOT_FOUND.getCode(), "接口不存在: " + e.getRequestURL());
    }

    /**
     * ⑩ 兜底异常处理 —— 捕获所有上面没匹配到的异常
     *
     * 触发场景：数据库连接断开、空指针异常、数组越界...任何意料之外的错误
     * 返回示例：{ "code": 1000, "message": "未知错误" }
     * HTTP 状态码：500
     *
     * 注意：不会把真实错误信息返回给前端（防止泄露技术细节），只记录在服务器日志里
     */
    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public Result<Void> handleException(Exception e) {
        log.error("系统内部异常", e);  // 记录完整错误堆栈，方便开发排查
        return Result.fail(ErrorCode.UNKNOWN_ERROR);
    }
}
