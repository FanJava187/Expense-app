package com.example.expenseapp.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Top N 支出項目
 * 用於排行榜
 */
public class TopExpenseItem {
    private Long id;
    private String title;
    private BigDecimal amount;
    private String category;
    private LocalDate expenseDate;
    private Integer rank;           // 排名

    public TopExpenseItem() {
    }

    public TopExpenseItem(Long id, String title, BigDecimal amount, String category, LocalDate expenseDate, Integer rank) {
        this.id = id;
        this.title = title;
        this.amount = amount;
        this.category = category;
        this.expenseDate = expenseDate;
        this.rank = rank;
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public LocalDate getExpenseDate() {
        return expenseDate;
    }

    public void setExpenseDate(LocalDate expenseDate) {
        this.expenseDate = expenseDate;
    }

    public Integer getRank() {
        return rank;
    }

    public void setRank(Integer rank) {
        this.rank = rank;
    }
}
