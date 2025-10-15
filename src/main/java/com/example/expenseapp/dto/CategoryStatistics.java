package com.example.expenseapp.dto;

import java.math.BigDecimal;

/**
 * 分類統計資料傳輸物件
 */
public class CategoryStatistics {
    private String category;           // 分類名稱
    private BigDecimal totalAmount;    // 該分類總金額
    private Long count;                // 該分類筆數
    private BigDecimal percentage;     // 占總支出的百分比

    public CategoryStatistics() {
    }

    public CategoryStatistics(String category, BigDecimal totalAmount, Long count) {
        this.category = category;
        this.totalAmount = totalAmount;
        this.count = count;
    }

    public CategoryStatistics(String category, BigDecimal totalAmount, Long count, BigDecimal percentage) {
        this.category = category;
        this.totalAmount = totalAmount;
        this.count = count;
        this.percentage = percentage;
    }

    // Getters and Setters
    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
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

    public BigDecimal getPercentage() {
        return percentage;
    }

    public void setPercentage(BigDecimal percentage) {
        this.percentage = percentage;
    }
}
