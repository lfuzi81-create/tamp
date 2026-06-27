package com.tamp.common.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.tamp.common.util.SecurityUtils;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/**
 * 【角色权限拦截器】—— 在请求到达 Controller 之前，检查用户角色是否有权限
 *
 * 工作流程：
 *   1. 请求进来 → JwtAuthFilter 先解析 token，把用户角色存到 SecurityContext
 *   2. 请求继续 → 本拦截器检查 Controller 上有没有 @RequireRole 注解
 *   3. 如果有注解 → 取出当前用户角色，看是否在允许列表里
 *   4. 在列表里 → 放行，请求到达 Controller
 *   5. 不在列表里 → 返回 403 错误，请求被拦截
 *   6. 没有注解 → 放行（不需要特殊角色的接口，所有登录用户都能访问）
 *
 * 就像小区门禁：
 *   - 没有门禁卡 → 进不来（JwtAuthFilter 负责检查）
 *   - 有门禁卡但权限不够 → 只能进公共区域（本拦截器负责检查）
 *   - 权限足够 → 可以进特定房间
 */
@Component   // 【标记】我是 Spring 组件，会被自动注册为拦截器
public class RoleAuthorizationInterceptor implements HandlerInterceptor {

    private final ObjectMapper objectMapper = new ObjectMapper();  // JSON 序列化工具

    /**
     * 在 Controller 方法执行之前调用
     *
     * 返回 true  = 放行（继续执行 Controller）
     * 返回 false = 拦截（不执行 Controller，直接返回错误）
     */
    @Override
    public boolean preHandle(HttpServletRequest request,
                             HttpServletResponse response,
                             Object handler) throws Exception {
        // 如果请求的不是 Controller 方法（比如静态资源），直接放行
        if (!(handler instanceof HandlerMethod handlerMethod)) {
            return true;
        }

        // 第1步：先看方法上有没有 @RequireRole 注解
        RequireRole requireRole = handlerMethod.getMethodAnnotation(RequireRole.class);
        if (requireRole == null) {
            // 方法上没有，再看类上有没有（比如 ShelfController 整个类都标注了 @RequireRole("SHOP_ADMIN")）
            requireRole = handlerMethod.getBeanType().getAnnotation(RequireRole.class);
        }

        // 如果方法上和类上都没有 @RequireRole 注解，说明不需要角色校验，直接放行
        if (requireRole == null) {
            return true;
        }

        // 第2步：获取当前用户的角色
        String currentRole = SecurityUtils.getCurrentUserRole();
        if (currentRole == null) {
            // 没有角色信息（不应该发生，因为 JwtAuthFilter 已经校验过了）
            writeForbidden(response);
            return false;
        }

        // 第3步：检查当前角色是否在允许列表里
        // 比如 @RequireRole({"SUPER_ADMIN", "PLATFORM_ADMIN"})
        // 如果当前角色是 "SHOP_ADMIN"，不在列表里，就拒绝
        boolean allowed = Arrays.asList(requireRole.value()).contains(currentRole);
        if (!allowed) {
            writeForbidden(response);
            return false;
        }

        // 角色匹配，放行
        return true;
    }

    /**
     * 返回 403 无权限的 JSON 响应
     *
     * 返回格式：{ "code": 1003, "message": "无权限访问" }
     * 前端收到后可以弹出提示"您没有权限执行此操作"
     */
    private void writeForbidden(HttpServletResponse response) throws Exception {
        response.setStatus(HttpServletResponse.SC_FORBIDDEN);  // HTTP 403
        response.setContentType("application/json;charset=UTF-8");

        Map<String, Object> body = new HashMap<>();
        body.put("code", 1003);
        body.put("message", "无权限访问");
        response.getWriter().write(objectMapper.writeValueAsString(body));
    }
}
