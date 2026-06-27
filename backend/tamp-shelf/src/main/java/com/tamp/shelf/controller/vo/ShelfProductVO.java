package com.tamp.shelf.controller.vo;

import java.math.BigDecimal;

/**
 * 货架产品项 VO
 */
public class ShelfProductVO {

    private Long id;
    private Long shelfItemId;
    private Long shopId;
    private Long itemId;
    private String itemType;
    private Integer sortOrder;
    private Integer isTop;
    private String tags;
    private ProductInfo product;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getShelfItemId() { return shelfItemId; }
    public void setShelfItemId(Long shelfItemId) { this.shelfItemId = shelfItemId; }
    public Long getShopId() { return shopId; }
    public void setShopId(Long shopId) { this.shopId = shopId; }
    public Long getItemId() { return itemId; }
    public void setItemId(Long itemId) { this.itemId = itemId; }
    public String getItemType() { return itemType; }
    public void setItemType(String itemType) { this.itemType = itemType; }
    public Integer getSortOrder() { return sortOrder; }
    public void setSortOrder(Integer sortOrder) { this.sortOrder = sortOrder; }
    public Integer getIsTop() { return isTop; }
    public void setIsTop(Integer isTop) { this.isTop = isTop; }
    public String getTags() { return tags; }
    public void setTags(String tags) { this.tags = tags; }
    public ProductInfo getProduct() { return product; }
    public void setProduct(ProductInfo product) { this.product = product; }

    /**
     * 产品摘要信息
     */
    public static class ProductInfo {
        private Long id;
        private String name;
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
        private String attr1;
        private String attr2;
        private Integer viewCount;

        public Long getId() { return id; }
        public void setId(Long id) { this.id = id; }
        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
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
        public String getAttr1() { return attr1; }
        public void setAttr1(String attr1) { this.attr1 = attr1; }
        public String getAttr2() { return attr2; }
        public void setAttr2(String attr2) { this.attr2 = attr2; }
        public Integer getViewCount() { return viewCount; }
        public void setViewCount(Integer viewCount) { this.viewCount = viewCount; }
    }
}
