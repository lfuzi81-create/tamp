package com.tamp.client.entity;

import com.tamp.common.entity.BaseEntity;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * 资产表
 */
@Entity
@Table(name = "biz_client_asset")
public class ClientAsset extends BaseEntity {

    /**
     * 客户ID
     */
    @Column(name = "client_id")
    private Long clientId;

    /**
     * 投资人ID
     */
    @Column(name = "investor_id")
    private Long investorId;

    /**
     * 资产名称
     */
    @Column(name = "name", nullable = false, length = 128)
    private String name;

    /**
     * 资产类型
     */
    @Column(name = "type", nullable = false, length = 32)
    private String type;

    /**
     * 关联店铺产品 ID（投资人从货架选品时填写）
     */
    @Column(name = "product_id")
    private Long productId;

    /**
     * 金额
     */
    @Column(name = "amount", precision = 18, scale = 2)
    private BigDecimal amount = new BigDecimal("0");

    /**
     * 风险等级
     */
    @Column(name = "risk_level")
    private Integer riskLevel;

    /**
     * 购买日期
     */
    @Column(name = "purchase_date")
    private LocalDate purchaseDate;

    /**
     * 到期日期
     */
    @Column(name = "maturity_date")
    private LocalDate maturityDate;

    /**
     * 预期收益率
     */
    @Column(name = "expected_return", length = 32)
    private String expectedReturn;

    /**
     * 机构
     */
    @Column(name = "institution", length = 128)
    private String institution;

    /**
     * 是否授权
     */
    @Column(name = "is_authorized", columnDefinition = "TINYINT")
    private Integer isAuthorized = 0;

    /**
     * 授权范围
     */
    @Column(name = "auth_scope", columnDefinition = "TEXT")
    private String authScope;

    /**
     * 备注
     */
    @Column(name = "remark", columnDefinition = "TEXT")
    private String remark;

    /**
     * 关联产品名称（非持久化，列表展示用）
     */
    @Transient
    private String productName;

    public Long getClientId() {
        return clientId;
    }

    public void setClientId(Long clientId) {
        this.clientId = clientId;
    }

    public Long getInvestorId() {
        return investorId;
    }

    public void setInvestorId(Long investorId) {
        this.investorId = investorId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public Long getProductId() {
        return productId;
    }

    public void setProductId(Long productId) {
        this.productId = productId;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public Integer getRiskLevel() {
        return riskLevel;
    }

    public void setRiskLevel(Integer riskLevel) {
        this.riskLevel = riskLevel;
    }

    public LocalDate getPurchaseDate() {
        return purchaseDate;
    }

    public void setPurchaseDate(LocalDate purchaseDate) {
        this.purchaseDate = purchaseDate;
    }

    public LocalDate getMaturityDate() {
        return maturityDate;
    }

    public void setMaturityDate(LocalDate maturityDate) {
        this.maturityDate = maturityDate;
    }

    public String getExpectedReturn() {
        return expectedReturn;
    }

    public void setExpectedReturn(String expectedReturn) {
        this.expectedReturn = expectedReturn;
    }

    public String getInstitution() {
        return institution;
    }

    public void setInstitution(String institution) {
        this.institution = institution;
    }

    public Integer getIsAuthorized() {
        return isAuthorized;
    }

    public void setIsAuthorized(Integer isAuthorized) {
        this.isAuthorized = isAuthorized;
    }

    public String getAuthScope() {
        return authScope;
    }

    public void setAuthScope(String authScope) {
        this.authScope = authScope;
    }

    public String getRemark() {
        return remark;
    }

    public void setRemark(String remark) {
        this.remark = remark;
    }

    public String getProductName() {
        return productName;
    }

    public void setProductName(String productName) {
        this.productName = productName;
    }
}
