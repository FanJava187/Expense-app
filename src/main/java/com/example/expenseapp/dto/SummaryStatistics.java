package com.example.expenseapp.dto;

import java.math.BigDecimal;

/**
 * 總覽統計資料傳輸物件
 */
public class SummaryStatistics {
    private BigDecimal totalAmount;      // 總支出金額
    private Long totalCount;             // 總筆數
    private BigDecimal averageAmount;    // 平均金額
    private BigDecimal maxAmount;        // 最大單筆金額
    private BigDecimal minAmount;        // 最小單筆金額

    public SummaryStatistics() {
    }

    public SummaryStatistics(BigDecimal totalAmount, Long totalCount, BigDecimal averageAmount,
                           BigDecimal maxAmount, BigDecimal minAmount) {
        this.totalAmount = totalAmount;
        this.totalCount = totalCount;
        this.averageAmount = averageAmount;
        this.maxAmount = maxAmount;
        this.minAmount = minAmount;
    }

    // Getters and Setters
    public BigDecimal getTotalAmount() {
        return totalAmount;
    }

    public void setTotalAmount(BigDecimal totalAmount) {
        this.totalAmount = totalAmount;
    }

    public Long getTotalCount() {
        return totalCount;
    }

    public void setTotalCount(Long totalCount) {
        this.totalCount = totalCount;
    }

    public BigDecimal getAverageAmount() {
        return averageAmount;
    }

    public void setAverageAmount(BigDecimal averageAmount) {
        this.averageAmount = averageAmount;
    }

    public BigDecimal getMaxAmount() {
        return maxAmount;
    }

    public void setMaxAmount(BigDecimal maxAmount) {
        this.maxAmount = maxAmount;
    }

    public BigDecimal getMinAmount() {
        return minAmount;
    }

    public void setMinAmount(BigDecimal minAmount) {
        this.minAmount = minAmount;
    }
}
