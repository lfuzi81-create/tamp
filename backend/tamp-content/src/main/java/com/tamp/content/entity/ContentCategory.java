package com.tamp.content.entity;

import com.tamp.common.entity.BaseEntity;

import jakarta.persistence.*;

/**
 * 内容分类表
 */
@Entity
@Table(name = "biz_content_category")
public class ContentCategory extends BaseEntity {

    /**
     * 分类名称
     */
    @Column(name = "name", nullable = false, length = 64)
    private String name;

    /**
     * 排序顺序
     */
    @Column(name = "sort_order")
    private Integer sortOrder = 0;

    /**
     * 状态
     */
    @Column(name = "status", columnDefinition = "TINYINT")
    private Integer status = 1;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Integer getSortOrder() {
        return sortOrder;
    }

    public void setSortOrder(Integer sortOrder) {
        this.sortOrder = sortOrder;
    }

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }

    @PrePersist
    void prePersist() {
        if (sortOrder == null) {
            sortOrder = 0;
        }
        if (status == null) {
            status = 1;
        }
        if (getDeleted() == null) {
            setDeleted(0);
        }
    }
}
