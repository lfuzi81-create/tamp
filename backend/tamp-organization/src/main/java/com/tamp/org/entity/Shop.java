package com.tamp.org.entity;

import com.tamp.common.entity.BaseEntity;

import jakarta.persistence.*;
import java.math.BigDecimal;

/**
 * 店铺表
 */
@Entity
@Table(name = "biz_shop")
public class Shop extends BaseEntity {

    /**
     * 店铺名称
     */
    @Column(name = "name", nullable = false, length = 128)
    private String name;

    /**
     * 家办ID
     */
    @Column(name = "office_id", nullable = false)
    private Long officeId;

    /**
     * 负责人姓名
     */
    @Column(name = "manager_name", length = 64)
    private String managerName;

    /**
     * 负责人电话
     */
    @Column(name = "manager_phone", length = 20)
    private String managerPhone;

    /**
     * 地址
     */
    @Column(name = "address", length = 256)
    private String address;

    /**
     * 状态（0启用 1停用）
     */
    @Column(name = "status")
    private Integer status;

    /**
     * 客户数量
     */
    @Column(name = "client_count")
    private Integer clientCount = 0;

    /**
     * 总资产规模
     */
    @Column(name = "total_aum", precision = 18, scale = 2)
    private BigDecimal totalAum = new BigDecimal("0");

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Long getOfficeId() {
        return officeId;
    }

    public void setOfficeId(Long officeId) {
        this.officeId = officeId;
    }

    public String getManagerName() {
        return managerName;
    }

    public void setManagerName(String managerName) {
        this.managerName = managerName;
    }

    public String getManagerPhone() {
        return managerPhone;
    }

    public void setManagerPhone(String managerPhone) {
        this.managerPhone = managerPhone;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }

    public Integer getClientCount() {
        return clientCount;
    }

    public void setClientCount(Integer clientCount) {
        this.clientCount = clientCount;
    }

    public BigDecimal getTotalAum() {
        return totalAum;
    }

    public void setTotalAum(BigDecimal totalAum) {
        this.totalAum = totalAum;
    }
}
