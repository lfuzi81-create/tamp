package com.tamp.auth.service;

import io.jsonwebtoken.Claims;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * JWT Token 服务 — 生成、校验、黑名单管理
 */
@Service
public class JwtTokenService {

    private static final Logger log = LoggerFactory.getLogger(JwtTokenService.class);

    @Value("${tamp.jwt.secret}")
    private String secret;

    @Value("${tamp.jwt.expiration}")
    private long expiration;

    @Value("${tamp.jwt.refresh-expiration}")
    private long refreshExpiration;

    private static final String TOKEN_BLACKLIST_PREFIX = "jwt:blacklist:";
    private static final String REFRESH_TOKEN_PREFIX = "jwt:refresh:";

    private final StringRedisTemplate redisTemplate;

    public JwtTokenService(StringRedisTemplate redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    /**
     * 生成访问 Token
     */
    public String generateAccessToken(Long userId, String phone, String role,
                                       Long officeId, Long shopId) {
        return generateAccessToken(userId, phone, role, officeId, shopId, null);
    }

    /**
     * 生成访问 Token（含多家办可见范围）
     */
    public String generateAccessToken(Long userId, String phone, String role,
                                       Long officeId, Long shopId, String officeIds) {
        Map<String, Object> claims = com.tamp.common.util.JwtUtils.buildClaims(
                userId, phone, role, officeId, shopId, officeIds);
        return com.tamp.common.util.JwtUtils.generateToken(userId.toString(), claims, secret, expiration);
    }

    /**
     * 生成刷新 Token（存入 Redis）
     */
    public String generateRefreshToken(String userId) {
        String refreshToken = com.tamp.common.util.JwtUtils.generateToken(userId + ":refresh", null, secret, refreshExpiration);
        // 存入 Redis，用于后续刷新校验
        redisTemplate.opsForValue().set(
                REFRESH_TOKEN_PREFIX + userId,
                refreshToken,
                refreshExpiration,
                TimeUnit.MILLISECONDS
        );
        return refreshToken;
    }

    /**
     * 刷新 Token
     *
     * @param oldAccessToken 旧的访问 Token
     * @return 新的 Token 对象 [accessToken, refreshToken]
     */
    public String[] refreshTokens(String oldAccessToken) {
        Claims claims = com.tamp.common.util.JwtUtils.parseToken(oldAccessToken, secret);
        String userId = claims.getSubject();
        String phone = (String) claims.get("phone");
        String role = (String) claims.get("role");
        Number officeIdNum = (Number) claims.get("officeId");
        Number shopIdNum = (Number) claims.get("shopId");
        String officeIds = (String) claims.get("officeIds");

        Long officeId = officeIdNum != null ? officeIdNum.longValue() : null;
        Long shopId = shopIdNum != null ? shopIdNum.longValue() : null;

        // 将旧 Token 加入黑名单
        blacklistToken(oldAccessToken);

        String newAccessToken = generateAccessToken(Long.parseLong(userId), phone, role, officeId, shopId, officeIds);
        String newRefreshToken = generateRefreshToken(userId);

        return new String[]{newAccessToken, newRefreshToken};
    }

    /**
     * 将 Token 加入黑名单（登出时调用）
     */
    public void blacklistToken(String token) {
        try {
            Claims claims = com.tamp.common.util.JwtUtils.parseToken(token, secret);
            long remaining = claims.getExpiration().getTime() - System.currentTimeMillis();
            if (remaining > 0) {
                redisTemplate.opsForValue().set(
                        TOKEN_BLACKLIST_PREFIX + token,
                        "1",
                        remaining,
                        TimeUnit.MILLISECONDS
                );
            }
        } catch (Exception e) {
            log.warn("将 Token 加入黑名单失败: {}", e.getMessage());
        }
    }

    /**
     * 检查 Token 是否在黑名单中
     */
    public boolean isBlacklisted(String token) {
        return Boolean.TRUE.equals(redisTemplate.hasKey(TOKEN_BLACKLIST_PREFIX + token));
    }

    /**
     * 解析 Token 获取 Claims
     */
    public Claims parseToken(String token) {
        return com.tamp.common.util.JwtUtils.parseToken(token, secret);
    }

    /**
     * 校验 Token 是否有效且未被拉黑
     */
    public boolean validateToken(String token) {
        if (!com.tamp.common.util.JwtUtils.validateToken(token, secret)) {
            return false;
        }
        return !isBlacklisted(token);
    }

    /**
     * 检查 Token 是否即将过期（剩余 < 30 分钟）
     */
    public boolean isExpiringSoon(String token) {
        return com.tamp.common.util.JwtUtils.isTokenExpiringSoon(token, secret, 1800000L);
    }
}
