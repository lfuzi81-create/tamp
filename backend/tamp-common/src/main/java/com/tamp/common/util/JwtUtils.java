package com.tamp.common.util;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * JWT 工具类 — Token 生成、解析、校验
 */
public final class JwtUtils {

    private static final Logger log = LoggerFactory.getLogger(JwtUtils.class);

    private JwtUtils() {}

    /**
     * 生成签名密钥
     */
    public static SecretKey getSigningKey(String secret) {
        byte[] keyBytes = secret.getBytes(StandardCharsets.UTF_8);
        return Keys.hmacShaKeyFor(keyBytes);
    }

    /**
     * 生成 Token
     *
     * @param subject   用户标识（通常是 userId）
     * @param claims    额外载荷（角色、用户名等）
     * @param secret    签名密钥
     * @param expiration 过期时间（毫秒）
     * @return JWT Token 字符串
     */
    public static String generateToken(String subject, Map<String, Object> claims,
                                        String secret, long expiration) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + expiration);

        SecretKey key = getSigningKey(secret);

        var builder = Jwts.builder()
                .subject(subject)
                .issuedAt(now)
                .expiration(expiryDate)
                .signWith(key);

        if (claims != null && !claims.isEmpty()) {
            builder.claims(claims);
        }

        return builder.compact();
    }

    /**
     * 生成 Token（简化版，仅含 subject）
     */
    public static String generateToken(String subject, String secret, long expiration) {
        return generateToken(subject, null, secret, expiration);
    }

    /**
     * 解析 Token 获取 Claims
     *
     * @param token  JWT Token
     * @param secret 签名密钥
     * @return Claims 对象
     * @throws io.jsonwebtoken.JwtException Token 无效或过期时抛出
     */
    public static Claims parseToken(String token, String secret) {
        SecretKey key = getSigningKey(secret);
        return Jwts.parser()
                .verifyWith(key)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    /**
     * 从 Token 中获取用户标识（subject）
     */
    public static String getSubject(String token, String secret) {
        return parseToken(token, secret).getSubject();
    }

    /**
     * 从 Token 中获取指定 Claim 值
     */
    public static Object getClaim(String token, String secret, String claimName) {
        return parseToken(token, secret).get(claimName);
    }

    /**
     * 校验 Token 是否有效（未过期且签名正确）
     */
    public static boolean validateToken(String token, String secret) {
        try {
            parseToken(token, secret);
            return true;
        } catch (Exception e) {
            log.debug("Token 校验失败: {}", e.getMessage());
            return false;
        }
    }

    /**
     * 检查 Token 是否即将过期（剩余有效期 < thresholdMillis）
     */
    public static boolean isTokenExpiringSoon(String token, String secret, long thresholdMillis) {
        try {
            Claims claims = parseToken(token, secret);
            Date expiration = claims.getExpiration();
            long remaining = expiration.getTime() - System.currentTimeMillis();
            return remaining < thresholdMillis && remaining > 0;
        } catch (Exception e) {
            return true; // 解析失败视为需要刷新
        }
    }

    /**
     * 构建 Token 载荷（通用方法）
     */
    public static Map<String, Object> buildClaims(Long userId, String phone, String role, Long officeId, Long shopId) {
        return buildClaims(userId, phone, role, officeId, shopId, null);
    }

    /**
     * 构建 Token 载荷（含多家办可见范围）
     *
     * @param officeIds 逗号分隔的家办ID列表（OPERATOR 角色可访问多家办）
     */
    public static Map<String, Object> buildClaims(Long userId, String phone, String role,
                                                   Long officeId, Long shopId, String officeIds) {
        Map<String, Object> claims = new HashMap<>(8);
        claims.put("userId", userId);
        claims.put("phone", phone);
        claims.put("role", role);
        if (officeId != null) {
            claims.put("officeId", officeId);
        }
        if (shopId != null) {
            claims.put("shopId", shopId);
        }
        if (officeIds != null && !officeIds.isBlank()) {
            claims.put("officeIds", officeIds);
        }
        return claims;
    }
}
