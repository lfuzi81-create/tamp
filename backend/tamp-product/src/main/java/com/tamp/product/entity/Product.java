package com.tamp.product.entity;

import com.tamp.common.entity.BaseEntity;

import jakarta.persistence.*;
import java.math.BigDecimal;

/**
 * 【产品实体】—— 对应数据库里的 biz_product 表
 *
 * 作用：定义"产品"这个业务对象有哪些字段，每个字段对应数据库表的哪一列
 * 就像定义一张 Excel 表的列头
 *
 * 继承 BaseEntity 意味着自动拥有：id / createdAt / updatedAt / createdBy / updatedBy / deleted
 * 不需要重复写这些公共字段
 *
 * 对应数据库表：biz_product
 * 对应前端页面：总部端-产品库、店铺端-产品池、投资人端-产品浏览
 */
@Entity                                       // 【标记】这是一个 JPA 实体，对应数据库的一张表
@Table(name = "biz_product",                  // 对应的表名是 biz_product（biz_ 前缀表示业务表）
        indexes = @Index(name = "idx_category_id", columnList = "category_id"))  // 在 category_id 列上建索引，加速按分类查询
public class Product extends BaseEntity {     // 继承 BaseEntity，自动拥有 id/createdAt/deleted 等公共字段

    /** 产品名称，如"稳健增值1号" */
    @Column(name = "name", nullable = false, length = 128)  // 对应列名 name，不能为空，最长128字
    private String name;

    /** 所属分类ID，关联 biz_product_category 表的 id */
    @Column(name = "category_id")
    private Long categoryId;

    /**
     * 产品类型，固定值枚举：
     *   FIXED   = 固收类
     *   EQUITY  = 权益类
     *   OVERSEA = 海外类
     *   OTHER   = 其他
     */
    @Column(name = "type", nullable = false, length = 32)
    private String type = "FIXED";            // 默认值"FIXED"（固收类）

    /** 产品描述，大段文字 */
    @Column(name = "description", columnDefinition = "TEXT")  // TEXT 类型，可以存很长的文字
    private String description;

    /** 最低投资金额（万元），如 100.00 表示100万起投 */
    @Column(name = "aum_min", precision = 18, scale = 2)  // 18位数字，2位小数
    private BigDecimal aumMin;

    /** 最高投资金额（万元） */
    @Column(name = "aum_max", precision = 18, scale = 2)
    private BigDecimal aumMax;

    /** 预期收益率，如"6%-8%"，存的是文字不是数字 */
    @Column(name = "expected_return", length = 32)
    private String expectedReturn;

    /**
     * 风险等级，1-5 级：
     *   1 = 低风险（R1）
     *   2 = 中低风险（R2）
     *   3 = 中风险（R3）
     *   4 = 中高风险（R4）
     *   5 = 高风险（R5）
     */
    @Column(name = "risk_level")
    private Integer riskLevel;

    /** 投资期限，如"12个月"、"3年" */
    @Column(name = "duration", length = 64)
    private String duration;

    /** 自定义属性1，如"R2 中低风险"、"R4 中高风险"，由管理员自由填写 */
    @Column(name = "attr_1", length = 128)
    private String attr1;

    /** 自定义属性2，如"1年期"、"5年封闭期"，由管理员自由填写 */
    @Column(name = "attr_2", length = 128)
    private String attr2;

    /** 浏览量 */
    @Column(name = "view_count")
    private Integer viewCount;

    /**
     * 上下架状态：
     *   0 = 上架（用户可见）
     *   1 = 下架（用户不可见，但数据还在）
     */
    @Column(name = "status")
    private Integer status;

    /** 封面图片的 URL 地址 */
    @Column(name = "cover_image", length = 512)
    private String coverImage;

    /** 产品详情页的链接地址 */
    @Column(name = "detail_url", length = 512)
    private String detailUrl;

    /** 三方购买链接 */
    @Column(name = "purchase_url", length = 512)
    private String purchaseUrl;

    // ===== getter/setter 方法 =====
    // 这些方法让其他代码可以读取和修改上面的字段值
    // 比如 product.getName() 获取产品名称，product.setName("新产品") 修改名称

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Long getCategoryId() {
        return categoryId;
    }

    public void setCategoryId(Long categoryId) {
        this.categoryId = categoryId;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public BigDecimal getAumMin() {
        return aumMin;
    }

    public void setAumMin(BigDecimal aumMin) {
        this.aumMin = aumMin;
    }

    public BigDecimal getAumMax() {
        return aumMax;
    }

    public void setAumMax(BigDecimal aumMax) {
        this.aumMax = aumMax;
    }

    public String getExpectedReturn() {
        return expectedReturn;
    }

    public void setExpectedReturn(String expectedReturn) {
        this.expectedReturn = expectedReturn;
    }

    public Integer getRiskLevel() {
        return riskLevel;
    }

    public void setRiskLevel(Integer riskLevel) {
        this.riskLevel = riskLevel;
    }

    public String getDuration() {
        return duration;
    }

    public void setDuration(String duration) {
        this.duration = duration;
    }

    public String getAttr1() {
        return attr1;
    }

    public void setAttr1(String attr1) {
        this.attr1 = attr1;
    }

    public String getAttr2() {
        return attr2;
    }

    public void setAttr2(String attr2) {
        this.attr2 = attr2;
    }

    public Integer getViewCount() {
        return viewCount;
    }

    public void setViewCount(Integer viewCount) {
        this.viewCount = viewCount;
    }

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }

    public String getCoverImage() {
        return coverImage;
    }

    public void setCoverImage(String coverImage) {
        this.coverImage = coverImage;
    }

    public String getDetailUrl() {
        return detailUrl;
    }

    public void setDetailUrl(String detailUrl) {
        this.detailUrl = detailUrl;
    }

    public String getPurchaseUrl() {
        return purchaseUrl;
    }

    public void setPurchaseUrl(String purchaseUrl) {
        this.purchaseUrl = purchaseUrl;
    }
}
