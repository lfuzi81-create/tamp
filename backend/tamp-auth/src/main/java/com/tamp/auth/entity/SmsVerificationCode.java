package com.tamp.auth.entity;

import com.tamp.common.entity.BaseEntity;
import jakarta.persistence.*;
import java.time.LocalDateTime;

/**
 * 短信验证码表
 */
@Entity
@Table(name = "sms_verification_code")
public class SmsVerificationCode extends BaseEntity {

    /**
     * 手机号
     */
    @Column(name = "phone", nullable = false, length = 20)
    private String phone;

    /**
     * 验证码
     */
    @Column(name = "code", nullable = false, length = 8)
    private String code;

    /**
     * 类型
     */
    @Column(name = "type", nullable = false, length = 16)
    private String type = "LOGIN";

    /**
     * 过期时间
     */
    @Column(name = "expire_at")
    private LocalDateTime expireAt;

    /**
     * 是否已使用
     */
    @Column(name = "used", columnDefinition = "TINYINT")
    private Integer used = 0;

    /**
     * IP地址
     */
    @Column(name = "ip_address", length = 64)
    private String ipAddress;

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public LocalDateTime getExpireAt() {
        return expireAt;
    }

    public void setExpireAt(LocalDateTime expireAt) {
        this.expireAt = expireAt;
    }

    public Integer getUsed() {
        return used;
    }

    public void setUsed(Integer used) {
        this.used = used;
    }

    public String getIpAddress() {
        return ipAddress;
    }

    public void setIpAddress(String ipAddress) {
        this.ipAddress = ipAddress;
    }
}
