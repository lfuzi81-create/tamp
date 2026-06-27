package com.tamp.user.entity;

import com.tamp.common.entity.BaseEntity;

import jakarta.persistence.*;

/**
 * 权限表
 */
@Entity
@Table(name = "biz_permission",
        uniqueConstraints = @UniqueConstraint(columnNames = {"user_id", "resource_code"}))
public class Permission extends BaseEntity {

    /**
     * 用户ID
     */
    @Column(name = "user_id", nullable = false)
    private Long userId;

    /**
     * 资源编码
     */
    @Column(name = "resource_code", nullable = false, length = 64)
    private String resourceCode;

    /**
     * 是否允许
     */
    @Column(name = "allowed", columnDefinition = "TINYINT")
    private Integer allowed = 1;

    /**
     * 权限组编码
     */
    @Column(name = "group_code", length = 64)
    private String groupCode;

    /**
     * 是否为组总开关 0否 1是
     */
    @Column(name = "is_group_switch")
    private Integer isGroupSwitch;

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public String getResourceCode() {
        return resourceCode;
    }

    public void setResourceCode(String resourceCode) {
        this.resourceCode = resourceCode;
    }

    public Integer getAllowed() {
        return allowed;
    }

    public void setAllowed(Integer allowed) {
        this.allowed = allowed;
    }

    public String getGroupCode() {
        return groupCode;
    }

    public void setGroupCode(String groupCode) {
        this.groupCode = groupCode;
    }

    public Integer getIsGroupSwitch() {
        return isGroupSwitch;
    }

    public void setIsGroupSwitch(Integer isGroupSwitch) {
        this.isGroupSwitch = isGroupSwitch;
    }
}
