package com.tamp.org.entity;

import com.tamp.common.entity.BaseEntity;

import jakarta.persistence.*;

/**
 * 家办选品表
 */
@Entity
@Table(name = "biz_office_selection", uniqueConstraints = {
    @UniqueConstraint(columnNames = {"office_id", "item_type", "item_id"})
})
public class OfficeSelection extends BaseEntity {

    /**
     * 家办ID
     */
    @Column(name = "office_id", nullable = false)
    private Long officeId;

    /**
     * 项目类型：PRODUCT/CONTENT
     */
    @Column(name = "item_type", nullable = false, length = 20)
    private String itemType;

    /**
     * 项目ID
     */
    @Column(name = "item_id", nullable = false)
    private Long itemId;

    /**
     * 排序序号
     */
    @Column(name = "sort_order")
    private Integer sortOrder = 0;

    /**
     * 是否推荐
     */
    @Column(name = "is_recommended")
    private Boolean isRecommended = false;

    public Long getOfficeId() {
        return officeId;
    }

    public void setOfficeId(Long officeId) {
        this.officeId = officeId;
    }

    public String getItemType() {
        return itemType;
    }

    public void setItemType(String itemType) {
        this.itemType = itemType;
    }

    public Long getItemId() {
        return itemId;
    }

    public void setItemId(Long itemId) {
        this.itemId = itemId;
    }

    public Integer getSortOrder() {
        return sortOrder;
    }

    public void setSortOrder(Integer sortOrder) {
        this.sortOrder = sortOrder;
    }

    public Boolean getIsRecommended() {
        return isRecommended;
    }

    public void setIsRecommended(Boolean isRecommended) {
        this.isRecommended = isRecommended;
    }
}
