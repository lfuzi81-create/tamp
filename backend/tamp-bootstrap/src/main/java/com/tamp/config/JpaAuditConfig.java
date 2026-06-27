package com.tamp.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.domain.AuditorAware;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Optional;

/**
 * JPA 审计配置 — 提供 createdBy / updatedBy 的值
 */
@Configuration
public class JpaAuditConfig {

    @Bean
    public AuditorAware<String> auditorProvider() {
        return () -> {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            if (authentication == null || !authentication.isAuthenticated()) {
                return Optional.of("system");
            }
            Object principal = authentication.getPrincipal();
            if (principal instanceof String && "anonymousUser".equals(principal)) {
                return Optional.of("system");
            }
            return Optional.of(String.valueOf(authentication.getPrincipal()));
        };
    }
}
