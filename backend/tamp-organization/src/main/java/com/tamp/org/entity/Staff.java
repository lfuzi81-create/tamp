package com.tamp.org.entity;

import com.tamp.common.entity.BaseEntity;

import jakarta.persistence.*;
import java.time.LocalDate;

/**
 * 人员表
 */
@Entity
@Table(name = "biz_staff")
public class Staff extends BaseEntity {

    /**
     * 姓名
     */
    @Column(name = "name", nullable = false, length = 64)
    private String name;

    /**
     * 手机号
     */
    @Column(name = "phone", length = 20)
    private String phone;

    /**
     * 邮箱
     */
    @Column(name = "email", length = 128)
    private String email;

    /**
     * 角色类型
     */
    @Column(name = "role_type", nullable = false, length = 32)
    private String roleType = "MANAGER";

    /**
     * 家办ID
     */
    @Column(name = "office_id")
    private Long officeId;

    /**
     * 店铺ID
     */
    @Column(name = "shop_id")
    private Long shopId;

    /**
     * 状态（0在职 1离职）
     */
    @Column(name = "status")
    private Integer status;

    /**
     * 入职日期
     */
    @Column(name = "join_date")
    private LocalDate joinDate;

    /**
     * 备注
     */
    @Column(name = "remark", columnDefinition = "TEXT")
    private String remark;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getRoleType() {
        return roleType;
    }

    public void setRoleType(String roleType) {
        this.roleType = roleType;
    }

    public Long getOfficeId() {
        return officeId;
    }

    public void setOfficeId(Long officeId) {
        this.officeId = officeId;
    }

    public Long getShopId() {
        return shopId;
    }

    public void setShopId(Long shopId) {
        this.shopId = shopId;
    }

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }

    public LocalDate getJoinDate() {
        return joinDate;
    }

    public void setJoinDate(LocalDate joinDate) {
        this.joinDate = joinDate;
    }

    public String getRemark() {
        return remark;
    }

    public void setRemark(String remark) {
        this.remark = remark;
    }
}
