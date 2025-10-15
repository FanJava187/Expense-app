package com.example.expenseapp.dto;

import java.math.BigDecimal;
import java.util.List;

/**
 * 比較圖資料
 * 用於多月份或多分類的比較長條圖
 */
public class ComparisonData {
    private List<String> labels;        // X 軸標籤（如月份或分類）
    private List<BigDecimal> amounts;   // Y 軸數值（金額）
    private List<Long> counts;          // 筆數

    public ComparisonData() {
    }

    public ComparisonData(List<String> labels, List<BigDecimal> amounts, List<Long> counts) {
        this.labels = labels;
        this.amounts = amounts;
        this.counts = counts;
    }

    // Getters and Setters
    public List<String> getLabels() {
        return labels;
    }

    public void setLabels(List<String> labels) {
        this.labels = labels;
    }

    public List<BigDecimal> getAmounts() {
        return amounts;
    }

    public void setAmounts(List<BigDecimal> amounts) {
        this.amounts = amounts;
    }

    public List<Long> getCounts() {
        return counts;
    }

    public void setCounts(List<Long> counts) {
        this.counts = counts;
    }
}
