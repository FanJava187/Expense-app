package com.example.expenseapp.dto;

import java.math.BigDecimal;

/**
 * 趨勢圖資料點
 * 用於折線圖或區域圖
 */
public class TrendData {
    private String period;          // 時間點（如 "2025-10-01" 或 "2025-10"）
    private BigDecimal amount;      // 金額
    private Long count;             // 筆數

    public TrendData() {
    }

    public TrendData(String period, BigDecimal amount, Long count) {
        this.period = period;
        this.amount = amount;
        this.count = count;
    }

    // Getters and Setters
    public String getPeriod() {
        return period;
    }

    public void setPeriod(String period) {
        this.period = period;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public Long getCount() {
        return count;
    }

    public void setCount(Long count) {
        this.count = count;
    }
}
