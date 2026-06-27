package com.tamp.auth.entity;

import com.tamp.common.entity.BaseEntity;
import jakarta.persistence.*;

/**
 * 用户实体
 */
@Entity
@Table(name = "sys_user")
public class User extends BaseEntity {

    /** 密码（BCrypt 加密） */
    @Column(name = "password", nullable = false, length = 128)
    private String password;

    /** 手机号（即登录账号） */
    @Column(nullable = false, length = 20, unique = true)
    private String phone;

    /** 真实姓名 */
    @Column(name = "real_name", length = 64)
    private String realName;

    /** 角色：SUPER_ADMIN / PLATFORM_ADMIN / OPERATOR / TAMP_ADMIN / SHOP_ADMIN / INVESTOR */
    @Enumerated(EnumType.STRING)
    @Column(name = "role", nullable = false, length = 20)
    private Role role;

    /** 所属家办ID */
    @Column(name = "office_id")
    private Long officeId;

    /** 可见家办范围（运营人员用，逗号分隔多个家办ID） */
    @Column(name = "office_ids", length = 512)
    private String officeIds;

    /** 所属店铺ID（仅 SHOP_ADMIN） */
    @Column(name = "shop_id")
    private Long shopId;

    /** 头像URL */
    @Column(length = 512)
    private String avatar;

    /** 用户状态：0=正常，1=禁用 */
    @Column(name = "status", nullable = false)
    private Integer status = 0;

    /** 是否已修改初始密码：0=否，1=是 */
    @Column(name = "password_changed", nullable = false)
    private Integer passwordChanged = 0;

    /** 最后登录时间 */
    @Column(name = "last_login_time")
    private java.time.LocalDateTime lastLoginTime;

    /** 最后登录IP */
    @Column(name = "last_login_ip", length = 64)
    private String lastLoginIp;

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getRealName() {
        return realName;
    }

    public void setRealName(String realName) {
        this.realName = realName;
    }

    public Role getRole() {
        return role;
    }

    public void setRole(Role role) {
        this.role = role;
    }

    public Long getOfficeId() {
        return officeId;
    }

    public void setOfficeId(Long officeId) {
        this.officeId = officeId;
    }

    public String getOfficeIds() {
        return officeIds;
    }

    public void setOfficeIds(String officeIds) {
        this.officeIds = officeIds;
    }

    public Long getShopId() {
        return shopId;
    }

    public void setShopId(Long shopId) {
        this.shopId = shopId;
    }

    public String getAvatar() {
        return avatar;
    }

    public void setAvatar(String avatar) {
        this.avatar = avatar;
    }

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }

    public Integer getPasswordChanged() {
        return passwordChanged;
    }

    public void setPasswordChanged(Integer passwordChanged) {
        this.passwordChanged = passwordChanged;
    }

    public java.time.LocalDateTime getLastLoginTime() {
        return lastLoginTime;
    }

    public void setLastLoginTime(java.time.LocalDateTime lastLoginTime) {
        this.lastLoginTime = lastLoginTime;
    }

    public String getLastLoginIp() {
        return lastLoginIp;
    }

    public void setLastLoginIp(String lastLoginIp) {
        this.lastLoginIp = lastLoginIp;
    }
}
