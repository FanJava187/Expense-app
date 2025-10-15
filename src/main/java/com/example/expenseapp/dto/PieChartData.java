package com.example.expenseapp.dto;

import java.math.BigDecimal;

/**
 * 圓餅圖資料
 * 用於顯示分類佔比
 */
public class PieChartData {
    private String label;           // 標籤（分類名稱）
    private BigDecimal value;       // 數值（金額）
    private Double percentage;      // 百分比
    private Long count;             // 筆數

    public PieChartData() {
    }

    public PieChartData(String label, BigDecimal value, Double percentage, Long count) {
        this.label = label;
        this.value = value;
        this.percentage = percentage;
        this.count = count;
    }

    // Getters and Setters
    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public BigDecimal getValue() {
        return value;
    }

    public void setValue(BigDecimal value) {
        this.value = value;
    }

    public Double getPercentage() {
        return percentage;
    }

    public void setPercentage(Double percentage) {
        this.percentage = percentage;
    }

    public Long getCount() {
        return count;
    }

    public void setCount(Long count) {
        this.count = count;
    }
}
