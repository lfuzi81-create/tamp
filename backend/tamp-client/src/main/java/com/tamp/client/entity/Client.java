package com.tamp.client.entity;

import com.tamp.common.entity.BaseEntity;

import jakarta.persistence.*;
import java.math.BigDecimal;

/**
 * 客户表
 */
@Entity
@Table(name = "biz_client")
public class Client extends BaseEntity {

    /**
     * 客户姓名
     */
    @Column(name = "name", nullable = false, length = 64)
    private String name;

    /**
     * 手机号
     */
    @Column(name = "phone", length = 20)
    private String phone;

    /**
     * 性别
     */
    @Column(name = "gender", columnDefinition = "TINYINT")
    private Integer gender;

    /**
     * 邮箱
     */
    @Column(name = "email", length = 128)
    private String email;

    /**
     * 公司
     */
    @Column(name = "company", length = 128)
    private String company;

    /**
     * 职位
     */
    @Column(name = "position", length = 64)
    private String position;

    /**
     * 总资产规模
     */
    @Column(name = "aum_total", precision = 18, scale = 2)
    private BigDecimal aumTotal = new BigDecimal("0");

    /**
     * 来源
     */
    @Column(name = "source", length = 64)
    private String source;

    /**
     * 备注
     */
    @Column(name = "remark", columnDefinition = "TEXT")
    private String remark;

    /**
     * 店铺ID
     */
    @Column(name = "shop_id")
    private Long shopId;

    /**
     * 家办ID
     */
    @Column(name = "office_id")
    private Long officeId;

    /**
     * 状态（0活跃 1流失）
     */
    @Column(name = "status")
    private Integer status;

    /** 授权状态（authorized / unauthorized），列表接口聚合，非 DB 字段 */
    @Transient
    private String authorizedStatus;

    /** 已授权资产项数，列表接口聚合，非 DB 字段 */
    @Transient
    private Integer authorizedCount;

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

    public Integer getGender() {
        return gender;
    }

    public void setGender(Integer gender) {
        this.gender = gender;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getCompany() {
        return company;
    }

    public void setCompany(String company) {
        this.company = company;
    }

    public String getPosition() {
        return position;
    }

    public void setPosition(String position) {
        this.position = position;
    }

    public BigDecimal getAumTotal() {
        return aumTotal;
    }

    public void setAumTotal(BigDecimal aumTotal) {
        this.aumTotal = aumTotal;
    }

    public String getSource() {
        return source;
    }

    public void setSource(String source) {
        this.source = source;
    }

    public String getRemark() {
        return remark;
    }

    public void setRemark(String remark) {
        this.remark = remark;
    }

    public Long getShopId() {
        return shopId;
    }

    public void setShopId(Long shopId) {
        this.shopId = shopId;
    }

    public Long getOfficeId() {
        return officeId;
    }

    public void setOfficeId(Long officeId) {
        this.officeId = officeId;
    }

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }

    public String getAuthorizedStatus() {
        return authorizedStatus;
    }

    public void setAuthorizedStatus(String authorizedStatus) {
        this.authorizedStatus = authorizedStatus;
    }

    public Integer getAuthorizedCount() {
        return authorizedCount;
    }

    public void setAuthorizedCount(Integer authorizedCount) {
        this.authorizedCount = authorizedCount;
    }
}
