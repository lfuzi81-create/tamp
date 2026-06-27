package com.tamp.user.entity;

import com.tamp.common.entity.BaseEntity;
import jakarta.persistence.*;

@Entity
@Table(name = "biz_role_permission_template",
        uniqueConstraints = @UniqueConstraint(columnNames = {"role", "resource_code"}))
public class RolePermissionTemplate extends BaseEntity {

    @Column(name = "role", nullable = false, length = 32)
    private String role;

    @Column(name = "resource_code", nullable = false, length = 64)
    private String resourceCode;

    @Column(name = "allowed", columnDefinition = "TINYINT")
    private Integer allowed = 1;

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
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
}
