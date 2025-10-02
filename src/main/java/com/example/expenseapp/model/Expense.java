package com.example.expenseapp.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;

import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "expenses")
public class Expense {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "標題不能為空")
    @Size(min = 1, max = 100, message = "標題長度必須在 1-100 字元之間")
    private String title;

    @NotNull(message = "金額不能為空")
    @DecimalMin(value = "0.01", message = "金額必須大於 0")
    @Digits(integer = 10, fraction = 2, message = "金額格式不正確")
    private BigDecimal amount;

    @NotBlank(message = "分類不能為空")
    @Size(max = 50, message = "分類長度不能超過 50 字元")
    private String category;

    @NotNull(message = "日期不能為空")
    @PastOrPresent(message = "日期不能是未來")
    private LocalDate expenseDate;

    public Expense() {
    }

    public Expense(String title, BigDecimal amount, String category, LocalDate expenseDate) {
        this.title = title;
        this.amount = amount;
        this.category = category;
        this.expenseDate = expenseDate;
    }

    // Getters & Setters
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
}