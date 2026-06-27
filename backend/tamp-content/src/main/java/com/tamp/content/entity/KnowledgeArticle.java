package com.tamp.content.entity;

import com.tamp.common.entity.BaseEntity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

/**
 * 知识库文章表
 */
@Entity
@Table(name = "biz_knowledge_article")
public class KnowledgeArticle extends BaseEntity {

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
     * 正文内容
     */
    @Column(name = "content_text", columnDefinition = "LONGTEXT")
    private String contentText;

    /**
     * 附件链接
     */
    @Column(name = "attachment_url", length = 512)
    private String attachmentUrl;

    /**
     * 浏览次数
     */
    @Column(name = "view_count")
    private Integer viewCount = 0;

    /**
     * 状态（0发布 1草稿）
     */
    @Column(name = "status")
    private Integer status;

    /**
     * 是否更新（用于红点提示）
     */
    @Column(name = "is_updated", columnDefinition = "TINYINT")
    private Integer isUpdated = 0;

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

    public String getContentText() {
        return contentText;
    }

    public void setContentText(String contentText) {
        this.contentText = contentText;
    }

    public String getAttachmentUrl() {
        return attachmentUrl;
    }

    public void setAttachmentUrl(String attachmentUrl) {
        this.attachmentUrl = attachmentUrl;
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

    public Integer getIsUpdated() {
        return isUpdated;
    }

    public void setIsUpdated(Integer isUpdated) {
        this.isUpdated = isUpdated;
    }

    public LocalDateTime getPublishedAt() {
        return publishedAt;
    }

    public void setPublishedAt(LocalDateTime publishedAt) {
        this.publishedAt = publishedAt;
    }
}
