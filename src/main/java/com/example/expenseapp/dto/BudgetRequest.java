package com.example.expenseapp.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

/**
 * 預算請求 DTO
 */
public class BudgetRequest {

    @NotNull(message = "預算類型不能為空")
    private String budgetType;  // MONTHLY 或 CATEGORY

    private String category;    // 分類預算時必填

    @NotNull(message = "預算金額不能為空")
    @DecimalMin(value = "0.01", message = "預算金額必須大於 0")
    private BigDecimal amount;

    @NotNull(message = "年份不能為空")
    @Min(value = 2000, message = "年份不能小於 2000")
    @Max(value = 2100, message = "年份不能大於 2100")
    private Integer year;

    @NotNull(message = "月份不能為空")
    @Min(value = 1, message = "月份必須在 1-12 之間")
    @Max(value = 12, message = "月份必須在 1-12 之間")
    private Integer month;

    public BudgetRequest() {
    }

    public BudgetRequest(String budgetType, String category, BigDecimal amount, Integer year, Integer month) {
        this.budgetType = budgetType;
        this.category = category;
        this.amount = amount;
        this.year = year;
        this.month = month;
    }

    // Getters and Setters
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
}
