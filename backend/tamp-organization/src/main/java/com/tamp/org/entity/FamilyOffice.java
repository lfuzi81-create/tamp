package com.tamp.org.entity;

import com.tamp.common.entity.BaseEntity;

import jakarta.persistence.*;
import java.math.BigDecimal;

/**
 * 家办表
 */
@Entity
@Table(name = "biz_family_office")
public class FamilyOffice extends BaseEntity {

    /**
     * 家办名称
     */
    @Column(name = "name", nullable = false, length = 128)
    private String name;

    /**
     * 联系人
     */
    @Column(name = "contact_person", length = 64)
    private String contactPerson;

    /**
     * 联系电话
     */
    @Column(name = "contact_phone", length = 20)
    private String contactPhone;

    /**
     * 简介
     */
    @Column(name = "intro", columnDefinition = "TEXT")
    private String intro;

    /**
     * 成员数量
     */
    @Column(name = "member_count")
    private Integer memberCount = 0;

    /**
     * Logo链接
     */
    @Column(name = "logo_url", length = 512)
    private String logoUrl;

    /**
     * 状态（0启用 1停用）
     */
    @Column(name = "status")
    private Integer status;

    /**
     * 总资产规模
     */
    @Column(name = "total_aum", precision = 18, scale = 2)
    private BigDecimal totalAum = new BigDecimal("0");

    /**
     * 客户数量
     */
    @Column(name = "client_count")
    private Integer clientCount = 0;

    /**
     * 店铺数量
     */
    @Column(name = "shop_count")
    private Integer shopCount = 0;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getContactPerson() {
        return contactPerson;
    }

    public void setContactPerson(String contactPerson) {
        this.contactPerson = contactPerson;
    }

    public String getContactPhone() {
        return contactPhone;
    }

    public void setContactPhone(String contactPhone) {
        this.contactPhone = contactPhone;
    }

    public String getIntro() {
        return intro;
    }

    public void setIntro(String intro) {
        this.intro = intro;
    }

    public Integer getMemberCount() {
        return memberCount;
    }

    public void setMemberCount(Integer memberCount) {
        this.memberCount = memberCount;
    }

    public String getLogoUrl() {
        return logoUrl;
    }

    public void setLogoUrl(String logoUrl) {
        this.logoUrl = logoUrl;
    }

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }

    public BigDecimal getTotalAum() {
        return totalAum;
    }

    public void setTotalAum(BigDecimal totalAum) {
        this.totalAum = totalAum;
    }

    public Integer getClientCount() {
        return clientCount;
    }

    public void setClientCount(Integer clientCount) {
        this.clientCount = clientCount;
    }

    public Integer getShopCount() {
        return shopCount;
    }

    public void setShopCount(Integer shopCount) {
        this.shopCount = shopCount;
    }
}
