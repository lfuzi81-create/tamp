package com.tamp.shelf.entity;

import com.tamp.common.entity.BaseEntity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

/**
 * 货架项表
 */
@Entity
@Table(name = "biz_shelf_item",
        uniqueConstraints = @UniqueConstraint(columnNames = {"shop_id", "item_type", "item_id"}))
public class ShelfItem extends BaseEntity {

    /**
     * 店铺ID
     */
    @Column(name = "shop_id", nullable = false)
    private Long shopId;

    /**
     * 项类型（PRODUCT/CONTENT）
     */
    @Column(name = "item_type", nullable = false, length = 16)
    private String itemType;

    /**
     * 项ID
     */
    @Column(name = "item_id", nullable = false)
    private Long itemId;

    /**
     * 排序顺序
     */
    @Column(name = "sort_order")
    private Integer sortOrder = 0;

    /**
     * 是否置顶
     */
    @Column(name = "is_top", columnDefinition = "TINYINT")
    private Integer isTop = 0;

    /**
     * 置顶时间（用于排序，最新置顶排在最前）
     */
    @Column(name = "top_time")
    private LocalDateTime topTime;

    /**
     * 标签（逗号分隔）
     */
    @Column(name = "tags", length = 256)
    private String tags;

    /**
     * 添加时间
     */
    @Column(name = "added_at")
    private LocalDateTime addedAt;

    public Long getShopId() {
        return shopId;
    }

    public void setShopId(Long shopId) {
        this.shopId = shopId;
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

    public Integer getIsTop() {
        return isTop;
    }

    public void setIsTop(Integer isTop) {
        this.isTop = isTop;
    }

    public LocalDateTime getTopTime() {
        return topTime;
    }

    public void setTopTime(LocalDateTime topTime) {
        this.topTime = topTime;
    }

    public String getTags() {
        return tags;
    }

    public void setTags(String tags) {
        this.tags = tags;
    }

    public LocalDateTime getAddedAt() {
        return addedAt;
    }

    public void setAddedAt(LocalDateTime addedAt) {
        this.addedAt = addedAt;
    }
}
