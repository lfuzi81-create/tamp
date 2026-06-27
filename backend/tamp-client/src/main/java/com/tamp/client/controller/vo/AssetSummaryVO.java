package com.tamp.client.controller.vo;

import java.util.List;

/**
 * 资产总览 VO
 */
public class AssetSummaryVO {

    private Double totalValue;
    private List<TypeDistribution> typeDistribution;
    private long totalCount;

    public Double getTotalValue() { return totalValue; }
    public void setTotalValue(Double totalValue) { this.totalValue = totalValue; }
    public List<TypeDistribution> getTypeDistribution() { return typeDistribution; }
    public void setTypeDistribution(List<TypeDistribution> typeDistribution) { this.typeDistribution = typeDistribution; }
    public long getTotalCount() { return totalCount; }
    public void setTotalCount(long totalCount) { this.totalCount = totalCount; }

    /**
     * 资产类型分布项
     */
    public static class TypeDistribution {
        private String type;
        private Double amount;

        public String getType() { return type; }
        public void setType(String type) { this.type = type; }
        public Double getAmount() { return amount; }
        public void setAmount(Double amount) { this.amount = amount; }
    }
}
