package com.tamp.common.entity;

import jakarta.persistence.*;
import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedBy;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

/**
 * 【公共实体基类】所有业务实体的"老祖宗"
 *
 * 作用：就像 Excel 表格里每张表都有的"序号"和"创建时间"列一样，
 *       这个基类定义了所有表都需要的公共字段，子类不用重复写。
 *
 * 继承关系：Product extends BaseEntity → Product 自动拥有 id/createdAt/updatedAt 等字段
 *
 * 对应数据库：没有对应的表（@MappedSuperclass 表示"我只是模板，不建表"），
 *            子类（如 Product）建表时，这些字段会自动加到子类的表里
 */
@MappedSuperclass                    // 【标记】我是模板类，不单独建表，字段会合并到子类的表里
@EntityListeners(AuditingEntityListener.class)  // 【监听器】自动填充创建时间/修改时间/创建人/修改人
public abstract class BaseEntity {   // 【抽象类】不能直接用，必须被子类继承

    /**
     * 主键 ID —— 每条数据的唯一编号，数据库自动递增
     * 就像 Excel 的行号，每行都不一样
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)  // 数据库自增（1, 2, 3...）
    private Long id;

    /**
     * 创建时间 —— 数据第一次插入时自动记录
     * @CreatedDate 注解让 Spring 自动填值，不需要手动 set
     * updatable = false 表示：修改数据时不会覆盖这个时间
     */
    @CreatedDate
    @Column(updatable = false)
    private LocalDateTime createdAt;

    /**
     * 最后修改时间 —— 每次更新数据时自动更新
     * @LastModifiedDate 注解让 Spring 自动更新
     */
    @LastModifiedDate
    private LocalDateTime updatedAt;

    /**
     * 创建人 —— 谁创建的这条数据
     * @CreatedBy 注解让 Spring 自动填入当前登录用户的用户名
     * updatable = false 表示：修改数据时不会覆盖创建人
     */
    @CreatedBy
    @Column(length = 64, updatable = false)
    private String createdBy;

    /**
     * 最后修改人 —— 谁最后修改了这条数据
     * @LastModifiedBy 注解让 Spring 自动更新
     */
    @LastModifiedBy
    @Column(length = 64)
    private String updatedBy;

    /**
     * 逻辑删除标记 —— 这是最重要的字段之一！
     *
     * 什么是逻辑删除？
     *   普通删除（物理删除）：DELETE FROM products WHERE id = 1 → 数据真的没了
     *   逻辑删除：UPDATE products SET is_deleted = 1 WHERE id = 1 → 数据还在，只是标记为"已删除"
     *
     * 为什么用逻辑删除？
     *   1. 误删可以恢复（把 1 改回 0 就行了）
     *   2. 保留历史数据，方便审计
     *   3. 外键关联不会断（其他表还引用这条数据）
     *
     * 值的含义：0 = 正常数据，1 = 已删除
     * 默认值 = 0（新建的数据当然是正常的）
     */
    @Column(name = "is_deleted")
    private Integer deleted = 0;

    // ===== 以下是 getter/setter 方法，用于读写字段值 =====

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

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    public String getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(String createdBy) {
        this.createdBy = createdBy;
    }

    public String getUpdatedBy() {
        return updatedBy;
    }

    public void setUpdatedBy(String updatedBy) {
        this.updatedBy = updatedBy;
    }

    public Integer getDeleted() {
        return deleted;
    }

    public void setDeleted(Integer deleted) {
        this.deleted = deleted;
    }
}
