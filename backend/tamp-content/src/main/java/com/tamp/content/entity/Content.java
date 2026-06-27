package com.tamp.content.entity;

import com.tamp.common.entity.BaseEntity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

/**
 * 内容表
 */
@Entity
@Table(name = "biz_content")
public class Content extends BaseEntity {

    /**
     * 标题
     */
    @Column(name = "title", nullable = false, length = 256)
    private String title;

    /**
     * 分类ID
     */
    @Column(name = "category_id")
    private Long categoryId;

    /**
     * 类型
     */
    @Column(name = "type", length = 32)
    private String type = "ARTICLE";

    /**
     * 摘要
     */
    @Column(name = "summary", length = 512)
    private String summary;

    /**
     * 封面图
     */
    @Column(name = "cover_image", length = 512)
    private String coverImage;

    /**
     * 内容链接
     */
    @Column(name = "content_url", length = 512)
    private String contentUrl;

    /**
     * 浏览次数
     */
    @Column(name = "view_count")
    private Integer viewCount = 0;

    /**
     * 来源
     */
    @Column(name = "source", length = 128)
    private String source;

    /**
     * 状态（0=已下架，1=已上架）
     */
    @Column(name = "status")
    private Integer status = 1;

    /**
     * 发布时间
     */
    @Column(name = "published_at")
    private LocalDateTime publishedAt;

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
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

    public String getSummary() {
        return summary;
    }

    public void setSummary(String summary) {
        this.summary = summary;
    }

    public String getCoverImage() {
        return coverImage;
    }

    public void setCoverImage(String coverImage) {
        this.coverImage = coverImage;
    }

    public String getContentUrl() {
        return contentUrl;
    }

    public void setContentUrl(String contentUrl) {
        this.contentUrl = contentUrl;
    }

    public Integer getViewCount() {
        return viewCount;
    }

    public void setViewCount(Integer viewCount) {
        this.viewCount = viewCount;
    }

    public String getSource() {
        return source;
    }

    public void setSource(String source) {
        this.source = source;
    }

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }

    public LocalDateTime getPublishedAt() {
        return publishedAt;
    }

    public void setPublishedAt(LocalDateTime publishedAt) {
        this.publishedAt = publishedAt;
    }

    @PrePersist
    void prePersist() {
        if (getDeleted() == null) {
            setDeleted(0);
        }
        if (status == null) {
            status = 1;
        }
    }
}
