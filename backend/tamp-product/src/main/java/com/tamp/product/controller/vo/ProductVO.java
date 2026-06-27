package com.tamp.product.controller.vo;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 产品列表 VO
 */
public class ProductVO {

    private Long id;
    private String name;
    private Long categoryId;
    private String type;
    private String description;
    private BigDecimal aumMin;
    private BigDecimal aumMax;
    private String expectedReturn;
    private Integer riskLevel;
    private String duration;
    private Integer status;
    private String coverImage;
    private String detailUrl;
    private String purchaseUrl;
    private String attr1;
    private String attr2;
    private Integer viewCount;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private Integer shelfCount;
    private BigDecimal clientAum;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public Long getCategoryId() { return categoryId; }
    public void setCategoryId(Long categoryId) { this.categoryId = categoryId; }
    public String getType() { return type; }
    public void setType(String type) { this.type = type; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public BigDecimal getAumMin() { return aumMin; }
    public void setAumMin(BigDecimal aumMin) { this.aumMin = aumMin; }
    public BigDecimal getAumMax() { return aumMax; }
    public void setAumMax(BigDecimal aumMax) { this.aumMax = aumMax; }
    public String getExpectedReturn() { return expectedReturn; }
    public void setExpectedReturn(String expectedReturn) { this.expectedReturn = expectedReturn; }
    public Integer getRiskLevel() { return riskLevel; }
    public void setRiskLevel(Integer riskLevel) { this.riskLevel = riskLevel; }
    public String getDuration() { return duration; }
    public void setDuration(String duration) { this.duration = duration; }
    public Integer getStatus() { return status; }
    public void setStatus(Integer status) { this.status = status; }
    public String getCoverImage() { return coverImage; }
    public void setCoverImage(String coverImage) { this.coverImage = coverImage; }
    public String getDetailUrl() { return detailUrl; }
    public void setDetailUrl(String detailUrl) { this.detailUrl = detailUrl; }
    public String getPurchaseUrl() { return purchaseUrl; }
    public void setPurchaseUrl(String purchaseUrl) { this.purchaseUrl = purchaseUrl; }
    public String getAttr1() { return attr1; }
    public void setAttr1(String attr1) { this.attr1 = attr1; }
    public String getAttr2() { return attr2; }
    public void setAttr2(String attr2) { this.attr2 = attr2; }
    public Integer getViewCount() { return viewCount; }
    public void setViewCount(Integer viewCount) { this.viewCount = viewCount; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
    public Integer getShelfCount() { return shelfCount; }
    public void setShelfCount(Integer shelfCount) { this.shelfCount = shelfCount; }
    public BigDecimal getClientAum() { return clientAum; }
    public void setClientAum(BigDecimal clientAum) { this.clientAum = clientAum; }
}
