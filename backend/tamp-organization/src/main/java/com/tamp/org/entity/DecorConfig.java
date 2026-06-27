package com.tamp.org.entity;

import com.tamp.common.entity.BaseEntity;

import jakarta.persistence.*;

/**
 * 店铺装修配置表
 */
@Entity
@Table(name = "biz_decor_config")
public class DecorConfig extends BaseEntity {

    /**
     * 所属家办ID
     */
    @Column(name = "office_id", nullable = false)
    private Long officeId;

    /**
     * 所属店铺ID
     */
    @Column(name = "shop_id", nullable = false)
    private Long shopId;

    /**
     * 品牌显示名称，如"华港家族办公室"
     */
    @Column(name = "display_name", length = 128)
    private String displayName;

    /**
     * 品牌介绍描述
     */
    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    /**
     * 品牌 Logo URL（也可能是 base64 DataURL）
     */
    @Column(name = "logo_url", columnDefinition = "TEXT")
    private String logoUrl;

    /**
     * JSON: 模块显隐和排序配置 {brand_header:{visible:true,sort:1},...}
     */
    @Column(name = "modules", columnDefinition = "TEXT")
    private String modules;

    /**
     * JSON: 底部导航配置 {items:[...],sort_order:[...]}
     */
    @Column(name = "navigation", columnDefinition = "TEXT")
    private String navigation;

    /**
     * 主题色值，如 #C4954A
     */
    @Column(name = "primary_color", length = 16)
    private String primaryColor;

    /**
     * JSON: 货架选品 {featured_products:[...],recommended_contents:[...]}
     */
    @Column(name = "shelf_selections", columnDefinition = "TEXT")
    private String shelfSelections;

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

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getLogoUrl() {
        return logoUrl;
    }

    public void setLogoUrl(String logoUrl) {
        this.logoUrl = logoUrl;
    }

    public String getModules() {
        return modules;
    }

    public void setModules(String modules) {
        this.modules = modules;
    }

    public String getNavigation() {
        return navigation;
    }

    public void setNavigation(String navigation) {
        this.navigation = navigation;
    }

    public String getPrimaryColor() {
        return primaryColor;
    }

    public void setPrimaryColor(String primaryColor) {
        this.primaryColor = primaryColor;
    }

    public String getShelfSelections() {
        return shelfSelections;
    }

    public void setShelfSelections(String shelfSelections) {
        this.shelfSelections = shelfSelections;
    }
}
