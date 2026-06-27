package com.tamp.client.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

/**
 * 客户时间线表
 * 注意：此表无 updatedBy/is_deleted，只有 createdAt 和 id
 */
@Entity
@Table(name = "biz_client_timeline")
public class ClientTimeline {

    /**
     * 主键ID
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    /**
     * 创建时间
     */
    @Column(name = "created_at")
    private LocalDateTime createdAt;

    /**
     * 客户ID
     */
    @Column(name = "client_id", nullable = false)
    private Long clientId;

    /**
     * 事件类型
     */
    @Column(name = "event_type", nullable = false, length = 32)
    private String eventType = "CALL";

    /**
     * 标题
     */
    @Column(name = "title", nullable = false, length = 256)
    private String title;

    /**
     * 内容
     */
    @Column(name = "content", columnDefinition = "TEXT")
    private String content;

    /**
     * 行为目标 ID（产品/内容/知识文章）
     */
    @Column(name = "target_id")
    private Long targetId;

    /**
     * 事件时间
     */
    @Column(name = "event_time")
    private LocalDateTime eventTime;

    /**
     * 操作人
     */
    @Column(name = "created_by", length = 64)
    private String createdBy;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public Long getClientId() {
        return clientId;
    }

    public void setClientId(Long clientId) {
        this.clientId = clientId;
    }

    public String getEventType() {
        return eventType;
    }

    public void setEventType(String eventType) {
        this.eventType = eventType;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public Long getTargetId() {
        return targetId;
    }

    public void setTargetId(Long targetId) {
        this.targetId = targetId;
    }

    public LocalDateTime getEventTime() {
        return eventTime;
    }

    public void setEventTime(LocalDateTime eventTime) {
        this.eventTime = eventTime;
    }

    public String getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(String createdBy) {
        this.createdBy = createdBy;
    }
}
