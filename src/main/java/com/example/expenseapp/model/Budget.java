package com.example.expenseapp.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * 預算實體
 * 支援兩種預算類型：
 * 1. MONTHLY - 月度總預算
 * 2. CATEGORY - 特定分類的預算
 */
@Entity
@Table(name = "budgets", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"user_id", "budget_type", "category", "year", "month"})
})
public class Budget {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @NotNull(message = "預算類型不能為空")
    @Enumerated(EnumType.STRING)
    @Column(name = "budget_type", nullable = false, length = 20)
    private BudgetType budgetType;

    @Column(name = "category", length = 50)
    private String category;  // 分類預算時必填，月度預算時為 null

    @NotNull(message = "預算金額不能為空")
    @DecimalMin(value = "0.01", message = "預算金額必須大於 0")
    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal amount;

    @NotNull(message = "年份不能為空")
    @Column(nullable = false)
    private Integer year;

    @NotNull(message = "月份不能為空")
    @Column(nullable = false)
    private Integer month;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDate createdAt;

    @Column(name = "updated_at")
    private LocalDate updatedAt;

    // 預算類型枚舉
    public enum BudgetType {
        MONTHLY,    // 月度總預算
        CATEGORY    // 分類預算
    }

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDate.now();
        updatedAt = LocalDate.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDate.now();
    }

    // Constructors
    public Budget() {
    }

    public Budget(User user, BudgetType budgetType, String category, BigDecimal amount, Integer year, Integer month) {
        this.user = user;
        this.budgetType = budgetType;
        this.category = category;
        this.amount = amount;
        this.year = year;
        this.month = month;
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public BudgetType getBudgetType() {
        return budgetType;
    }

    public void setBudgetType(BudgetType budgetType) {
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

    public LocalDate getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDate createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDate getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDate updatedAt) {
        this.updatedAt = updatedAt;
    }
}
