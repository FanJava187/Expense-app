package com.example.expenseapp.service;

import com.example.expenseapp.model.Expense;
import com.example.expenseapp.repository.ExpenseRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Service
public class ExpenseService {
    private final ExpenseRepository expenseRepository;

    public ExpenseService(ExpenseRepository expenseRepository) {
        this.expenseRepository = expenseRepository;
    }

    public List<Expense> getAllExpenses() {
        return expenseRepository.findAll();
    }

    public Optional<Expense> getExpenseById(Long id) {
        return expenseRepository.findById(id);
    }

    public Expense createExpense(Expense expense) {
        return expenseRepository.save(expense);
    }

    public Expense updateExpense(Long id, Expense expenseDetails) {
        return expenseRepository.findById(id).map(expense -> {
            expense.setTitle(expenseDetails.getTitle());
            expense.setAmount(expenseDetails.getAmount());
            expense.setCategory(expenseDetails.getCategory());
            expense.setExpenseDate(expenseDetails.getExpenseDate());
            return expenseRepository.save(expense);
        }).orElseThrow(() -> new RuntimeException("找不到 ID 為 " + id + " 的支出紀錄"));
    }

    public void deleteExpense(Long id) {
        expenseRepository.deleteById(id);
    }

    /**
     * 根據分類查詢支出
     */
    public List<Expense> getExpensesByCategory(String category) {
        return expenseRepository.findByCategory(category);
    }

    /**
     * 根據日期範圍查詢支出
     */
    public List<Expense> getExpensesByDateRange(LocalDate startDate, LocalDate endDate) {
        return expenseRepository.findByExpenseDateBetween(startDate, endDate);
    }

    /**
     * 根據分類和日期範圍查詢支出
     */
    public List<Expense> getExpensesByCategoryAndDateRange(String category, LocalDate startDate, LocalDate endDate) {
        return expenseRepository.findByCategoryAndDateRange(category, startDate, endDate);
    }

    /**
     * 取得指定日期範圍內的所有分類
     */
    public List<String> getCategoriesByDateRange(LocalDate startDate, LocalDate endDate) {
        return expenseRepository.findDistinctCategoriesByDateRange(startDate, endDate);
    }
}