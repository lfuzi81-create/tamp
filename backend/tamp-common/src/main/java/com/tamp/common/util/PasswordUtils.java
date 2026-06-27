package com.tamp.common.util;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

/**
 * 密码工具类 — BCrypt 加密/校验
 */
public final class PasswordUtils {

    private static final BCryptPasswordEncoder ENCODER = new BCryptPasswordEncoder();

    private PasswordUtils() {}

    /**
     * 加密明文密码
     *
     * @param rawPassword 明文密码
     * @return BCrypt 哈希值
     */
    public static String encode(String rawPassword) {
        return ENCODER.encode(rawPassword);
    }

    /**
     * 校验密码是否匹配
     *
     * @param rawPassword     明文密码
     * @param encodedPassword 已加密的密码
     * @return 是否匹配
     */
    public static boolean matches(String rawPassword, String encodedPassword) {
        return ENCODER.matches(rawPassword, encodedPassword);
    }
}
