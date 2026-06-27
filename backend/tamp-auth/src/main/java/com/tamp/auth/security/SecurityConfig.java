package com.tamp.auth.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

/**
 * Spring Security 配置
 */
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private final JwtAuthFilter jwtAuthFilter;

    public SecurityConfig(JwtAuthFilter jwtAuthFilter) {
        this.jwtAuthFilter = jwtAuthFilter;
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            // 关闭 CSRF（使用 Stateless JWT 天然免疫）
            .csrf(csrf -> csrf.disable())

            // 无状态 Session（不使用 Session 存储认证状态）
            .sessionManagement(session ->
                    session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))

            // 异常处理入口点（返回 JSON 格式 401）
            .exceptionHandling(exception -> exception
                    .authenticationEntryPoint((request, response, authException) -> {
                        response.setStatus(401);
                        response.setContentType("application/json;charset=UTF-8");
                        response.getWriter().write("{\"code\":1004,\"message\":\"未登录或Token已过期\"}");
                    })
                    .accessDeniedHandler((request, response, accessDeniedException) -> {
                        response.setStatus(403);
                        response.setContentType("application/json;charset=UTF-8");
                        response.getWriter().write("{\"code\":1003,\"message\":\"无权限访问\"}");
                    })
            )

            // 权限规则配置
            .authorizeHttpRequests(auth -> auth
                    // ===== 公开接口（无需认证）=====
                    .requestMatchers("/api/auth/login",
                                     "/api/auth/sms-login",
                                     "/api/auth/sms-code",
                                     "/api/auth/reset-password",
                                     "/api/shop-preview/**").permitAll()

                    // ===== 静态资源（前端开发时允许）=====
                    .requestMatchers("/error").permitAll()

                    // ===== 其他所有接口需要认证 =====
                    .anyRequest().authenticated()
            )

            // 在用户名密码过滤器之前添加 JWT 过滤器
            .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
