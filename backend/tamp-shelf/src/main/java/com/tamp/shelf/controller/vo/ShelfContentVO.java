package com.tamp.shelf.controller.vo;

/**
 * 货架内容项 VO
 */
public class ShelfContentVO {

    private Long id;
    private Long shelfItemId;
    private Long shopId;
    private Long itemId;
    private String itemType;
    private Integer sortOrder;
    private Integer isTop;
    private String tags;
    private ContentInfo content;

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
    public ContentInfo getContent() { return content; }
    public void setContent(ContentInfo content) { this.content = content; }

    /**
     * 内容摘要信息
     */
    public static class ContentInfo {
        private Long id;
        private String title;
        private String type;
        private String summary;
        private String coverImage;
        private String contentUrl;
        private Integer viewCount;
        private String source;
        private Integer status;
        private java.time.LocalDateTime createdAt;
        private java.time.LocalDateTime updatedAt;
        private java.time.LocalDateTime publishedAt;

        public Long getId() { return id; }
        public void setId(Long id) { this.id = id; }
        public String getTitle() { return title; }
        public void setTitle(String title) { this.title = title; }
        public String getType() { return type; }
        public void setType(String type) { this.type = type; }
        public String getSummary() { return summary; }
        public void setSummary(String summary) { this.summary = summary; }
        public String getCoverImage() { return coverImage; }
        public void setCoverImage(String coverImage) { this.coverImage = coverImage; }
        public String getContentUrl() { return contentUrl; }
        public void setContentUrl(String contentUrl) { this.contentUrl = contentUrl; }
        public Integer getViewCount() { return viewCount; }
        public void setViewCount(Integer viewCount) { this.viewCount = viewCount; }
        public String getSource() { return source; }
        public void setSource(String source) { this.source = source; }
        public Integer getStatus() { return status; }
        public void setStatus(Integer status) { this.status = status; }
        public java.time.LocalDateTime getCreatedAt() { return createdAt; }
        public void setCreatedAt(java.time.LocalDateTime createdAt) { this.createdAt = createdAt; }
        public java.time.LocalDateTime getUpdatedAt() { return updatedAt; }
        public void setUpdatedAt(java.time.LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
        public java.time.LocalDateTime getPublishedAt() { return publishedAt; }
        public void setPublishedAt(java.time.LocalDateTime publishedAt) { this.publishedAt = publishedAt; }
    }
}
