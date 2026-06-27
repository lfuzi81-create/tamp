package com.tamp.client.entity;

import com.tamp.common.entity.BaseEntity;

import jakarta.persistence.*;

/**
 * 分享点击记录表
 */
@Entity
@Table(name = "biz_share_click")
public class ShareClick extends BaseEntity {

    /**
     * 分享链接ID
     */
    @Column(name = "share_id", nullable = false)
    private Long shareId;

    /**
     * 访问者用户ID（可能为空）
     */
    @Column(name = "visitor_user_id")
    private Long visitorUserId;

    /**
     * 访问者手机号
     */
    @Column(name = "visitor_phone", length = 20)
    private String visitorPhone;

    /**
     * 来源家办ID
     */
    @Column(name = "source_office_id")
    private Long sourceOfficeId;

    /**
     * IP地址
     */
    @Column(name = "ip_address", length = 50)
    private String ipAddress;

    /**
     * 浏览器UA
     */
    @Column(name = "user_agent", length = 500)
    private String userAgent;

    public Long getShareId() {
        return shareId;
    }

    public void setShareId(Long shareId) {
        this.shareId = shareId;
    }

    public Long getVisitorUserId() {
        return visitorUserId;
    }

    public void setVisitorUserId(Long visitorUserId) {
        this.visitorUserId = visitorUserId;
    }

    public String getVisitorPhone() {
        return visitorPhone;
    }

    public void setVisitorPhone(String visitorPhone) {
        this.visitorPhone = visitorPhone;
    }

    public Long getSourceOfficeId() {
        return sourceOfficeId;
    }

    public void setSourceOfficeId(Long sourceOfficeId) {
        this.sourceOfficeId = sourceOfficeId;
    }

    public String getIpAddress() {
        return ipAddress;
    }

    public void setIpAddress(String ipAddress) {
        this.ipAddress = ipAddress;
    }

    public String getUserAgent() {
        return userAgent;
    }

    public void setUserAgent(String userAgent) {
        this.userAgent = userAgent;
    }
}
