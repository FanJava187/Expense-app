package com.example.expenseapp.dto;

import java.math.BigDecimal;

/**
 * 時間週期統計資料傳輸物件
 * 用於月度統計（每日）和年度統計（每月）
 */
public class PeriodStatistics {
    private String period;             // 時間週期（例如: "2025-10-14" 或 "2025-10"）
    private BigDecimal totalAmount;    // 該週期總金額
    private Long count;                // 該週期筆數

    public PeriodStatistics() {
    }

    public PeriodStatistics(String period, BigDecimal totalAmount, Long count) {
        this.period = period;
        this.totalAmount = totalAmount;
        this.count = count;
    }

    // Getters and Setters
    public String getPeriod() {
        return period;
    }

    public void setPeriod(String period) {
        this.period = period;
    }

    public BigDecimal getTotalAmount() {
        return totalAmount;
    }

    public void setTotalAmount(BigDecimal totalAmount) {
        this.totalAmount = totalAmount;
    }

    public Long getCount() {
        return count;
    }

    public void setCount(Long count) {
        this.count = count;
    }
}
