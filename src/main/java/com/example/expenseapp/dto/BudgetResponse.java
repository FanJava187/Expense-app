package com.example.expenseapp.dto;

import com.example.expenseapp.model.Budget;

import java.math.BigDecimal;

/**
 * 預算回應 DTO
 */
public class BudgetResponse {
    private Long id;
    private String budgetType;      // MONTHLY 或 CATEGORY
    private String category;        // 分類預算時的分類名稱
    private BigDecimal amount;      // 預算金額
    private Integer year;
    private Integer month;
    private BigDecimal spent;       // 已使用金額
    private BigDecimal remaining;   // 剩餘金額
    private Double percentage;      // 使用百分比

    public BudgetResponse() {
    }

    public BudgetResponse(Long id, String budgetType, String category, BigDecimal amount,
                         Integer year, Integer month, BigDecimal spent, BigDecimal remaining, Double percentage) {
        this.id = id;
        this.budgetType = budgetType;
        this.category = category;
        this.amount = amount;
        this.year = year;
        this.month = month;
        this.spent = spent;
        this.remaining = remaining;
        this.percentage = percentage;
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getBudgetType() {
        return budgetType;
    }

    public void setBudgetType(String budgetType) {
        this.budgetType = budgetType;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public Integer getYear() {
        return year;
    }

    public void setYear(Integer year) {
        this.year = year;
    }

    public Integer getMonth() {
        return month;
    }

    public void setMonth(Integer month) {
        this.month = month;
    }

    public BigDecimal getSpent() {
        return spent;
    }

    public void setSpent(BigDecimal spent) {
        this.spent = spent;
    }

    public BigDecimal getRemaining() {
        return remaining;
    }

    public void setRemaining(BigDecimal remaining) {
        this.remaining = remaining;
    }

    public Double getPercentage() {
        return percentage;
    }

    public void setPercentage(Double percentage) {
        this.percentage = percentage;
    }
}
