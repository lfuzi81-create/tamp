package com.tamp.auth.service;

/**
 * 登录结果 DTO
 */
public record LoginResult(
        String accessToken,
        String refreshToken,
        boolean needChangePassword,
        Long userId,
        String phone,
        String role,
        String realName,
        String avatar,
        Long officeId,
        Long shopId
) {}
