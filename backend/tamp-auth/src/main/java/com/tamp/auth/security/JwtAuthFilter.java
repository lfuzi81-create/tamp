package com.tamp.auth.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.tamp.auth.service.JwtTokenService;
import com.tamp.common.dto.Result;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * JWT 认证过滤器 — 拦截请求、解析 Token、注入 SecurityContext
 */
@Component
public class JwtAuthFilter extends OncePerRequestFilter {

    private final JwtTokenService jwtTokenService;
    private final ObjectMapper objectMapper;

    public JwtAuthFilter(JwtTokenService jwtTokenService, ObjectMapper objectMapper) {
        this.jwtTokenService = jwtTokenService;
        this.objectMapper = objectMapper;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        String token = extractToken(request);

        if (StringUtils.hasText(token)) {
            try {
                // 校验 Token 有效性及黑名单
                if (jwtTokenService.validateToken(token)) {
                    var claims = jwtTokenService.parseToken(token);

                    Long userId = Long.parseLong(claims.getSubject());
                    String phone = (String) claims.get("phone");
                    String role = (String) claims.get("role");
                    Object officeIdClaim = claims.get("officeId");
                    Object shopIdClaim = claims.get("shopId");
                    String officeIds = (String) claims.get("officeIds");

                    Long officeId = null;
                    if (officeIdClaim != null) {
                        if (officeIdClaim instanceof Number) {
                            officeId = ((Number) officeIdClaim).longValue();
                        } else {
                            try {
                                officeId = Long.parseLong(officeIdClaim.toString());
                            } catch (NumberFormatException ignored) {}
                        }
                    }

                    Long shopId = null;
                    if (shopIdClaim != null) {
                        if (shopIdClaim instanceof Number) {
                            shopId = ((Number) shopIdClaim).longValue();
                        } else {
                            try {
                                shopId = Long.parseLong(shopIdClaim.toString());
                            } catch (NumberFormatException ignored) {}
                        }
                    }

                    // 构建权限列表
                    List<SimpleGrantedAuthority> authorities =
                            Collections.singletonList(new SimpleGrantedAuthority("ROLE_" + role));

                    // 将角色、officeId、shopId、officeIds、phone 存入 details，供 SecurityUtils 提取
                    Map<String, Object> details = new HashMap<>();
                    details.put("role", role);
                    details.put("phone", phone);
                    if (officeId != null) {
                        details.put("officeId", officeId);
                    }
                    if (shopId != null) {
                        details.put("shopId", shopId);
                    }
                    if (officeIds != null && !officeIds.isBlank()) {
                        details.put("officeIds", officeIds);
                    }

                    // 创建认证对象并设置到上下文
                    UsernamePasswordAuthenticationToken authentication =
                            new UsernamePasswordAuthenticationToken(userId, null, authorities);
                    authentication.setDetails(details);
                    SecurityContextHolder.getContext().setAuthentication(authentication);
                } else {
                    // Token 无效或已过期，返回 401
                    handleUnauthorized(response, "Token无效或已过期");
                    return;
                }
            } catch (Exception e) {
                // Token 解析异常，返回 401
                handleUnauthorized(response, "Token解析失败");
                return;
            }
        }

        filterChain.doFilter(request, response);
    }

    /**
     * 从请求头提取 Bearer Token
     */
    private String extractToken(HttpServletRequest request) {
        String header = request.getHeader("Authorization");
        if (StringUtils.hasText(header) && header.startsWith("Bearer ")) {
            return header.substring(7);
        }
        return null;
    }

    /**
     * 返回 401 未认证响应
     */
    private void handleUnauthorized(HttpServletResponse response, String message) throws IOException {
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType("application/json;charset=UTF-8");
        Result<Void> result = Result.fail(1004, message);
        response.getWriter().write(objectMapper.writeValueAsString(result));
    }
}
