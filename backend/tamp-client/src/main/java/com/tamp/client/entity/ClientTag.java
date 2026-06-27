package com.tamp.client.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

/**
 * 客户标签表
 */
@Entity
@Table(name = "biz_client_tag")
public class ClientTag {

    /**
     * 主键ID
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    /**
     * 客户ID
     */
    @Column(name = "client_id", nullable = false)
    private Long clientId;

    /**
     * 标签名称
     */
    @Column(name = "tag_name", nullable = false, length = 64)
    private String tagName;

    /**
     * 标签颜色
     */
    @Column(name = "tag_color", length = 16)
    private String tagColor = "#1890ff";

    /**
     * 创建时间
     */
    @Column(name = "created_at")
    private LocalDateTime createdAt;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getClientId() {
        return clientId;
    }

    public void setClientId(Long clientId) {
        this.clientId = clientId;
    }

    public String getTagName() {
        return tagName;
    }

    public void setTagName(String tagName) {
        this.tagName = tagName;
    }

    public String getTagColor() {
        return tagColor;
    }

    public void setTagColor(String tagColor) {
        this.tagColor = tagColor;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}
