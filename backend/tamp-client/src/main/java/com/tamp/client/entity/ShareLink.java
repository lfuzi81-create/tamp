package com.tamp.client.entity;

import com.tamp.common.entity.BaseEntity;

import jakarta.persistence.*;

/**
 * MGM分享链接表
 */
@Entity
@Table(name = "biz_share_link")
public class ShareLink extends BaseEntity {

    /**
     * 短码
     */
    @Column(name = "short_code", nullable = false, unique = true, length = 32)
    private String shortCode;

    /**
     * 分享者用户ID
     */
    @Column(name = "sharer_user_id", nullable = false)
    private Long sharerUserId;

    /**
     * 分享者家办ID
     */
    @Column(name = "sharer_office_id")
    private Long sharerOfficeId;

    /**
     * 目标类型：product/content/page
     */
    @Column(name = "target_type", length = 20)
    private String targetType;

    /**
     * 目标ID
     */
    @Column(name = "target_id")
    private Long targetId;

    /**
     * 目标名称
     */
    @Column(name = "target_name", length = 200)
    private String targetName;

    /**
     * 点击次数
     */
    @Column(name = "click_count")
    private Integer clickCount = 0;

    public String getShortCode() {
        return shortCode;
    }

    public void setShortCode(String shortCode) {
        this.shortCode = shortCode;
    }

    public Long getSharerUserId() {
        return sharerUserId;
    }

    public void setSharerUserId(Long sharerUserId) {
        this.sharerUserId = sharerUserId;
    }

    public Long getSharerOfficeId() {
        return sharerOfficeId;
    }

    public void setSharerOfficeId(Long sharerOfficeId) {
        this.sharerOfficeId = sharerOfficeId;
    }

    public String getTargetType() {
        return targetType;
    }

    public void setTargetType(String targetType) {
        this.targetType = targetType;
    }

    public Long getTargetId() {
        return targetId;
    }

    public void setTargetId(Long targetId) {
        this.targetId = targetId;
    }

    public String getTargetName() {
        return targetName;
    }

    public void setTargetName(String targetName) {
        this.targetName = targetName;
    }

    public Integer getClickCount() {
        return clickCount;
    }

    public void setClickCount(Integer clickCount) {
        this.clickCount = clickCount;
    }
}
