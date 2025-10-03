package com.example.expenseapp.service;

import com.example.expenseapp.exception.ResourceNotFoundException;
import com.example.expenseapp.model.Expense;
import com.example.expenseapp.model.User;
import com.example.expenseapp.repository.ExpenseRepository;
import com.example.expenseapp.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

@Service
public class ExpenseService {

    @Autowired
    private ExpenseRepository expenseRepository;

    @Autowired
    private UserRepository userRepository;

    private User getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("找不到目前使用者"));
    }

    public List<Expense> getAllExpenses() {
        User user = getCurrentUser();
        return expenseRepository.findByUserOrderByExpenseDateDesc(user);
    }

    public Expense getExpenseById(Long id) {
        User user = getCurrentUser();
        return expenseRepository.findByIdAndUser(id, user)
                .orElseThrow(() -> new ResourceNotFoundException("找不到 ID 為 " + id + " 的支出紀錄"));
    }

    public Expense createExpense(Expense expense) {
        User user = getCurrentUser();
        expense.setUser(user);
        return expenseRepository.save(expense);
    }

    public Expense updateExpense(Long id, Expense expenseDetails) {
        User user = getCurrentUser();
        Expense expense = expenseRepository.findByIdAndUser(id, user)
                .orElseThrow(() -> new ResourceNotFoundException("找不到 ID 為 " + id + " 的支出紀錄"));

        expense.setTitle(expenseDetails.getTitle());
        expense.setAmount(expenseDetails.getAmount());
        expense.setCategory(expenseDetails.getCategory());
        expense.setExpenseDate(expenseDetails.getExpenseDate());

        return expenseRepository.save(expense);
    }

    public void deleteExpense(Long id) {
        User user = getCurrentUser();
        Expense expense = expenseRepository.findByIdAndUser(id, user)
                .orElseThrow(() -> new ResourceNotFoundException("找不到 ID 為 " + id + " 的支出紀錄"));
        expenseRepository.delete(expense);
    }

    public List<Expense> getExpensesByCategory(String category) {
        User user = getCurrentUser();
        return expenseRepository.findByUserAndCategory(user, category);
    }

    public List<Expense> getExpensesByDateRange(LocalDate startDate, LocalDate endDate) {
        User user = getCurrentUser();
        return expenseRepository.findByUserAndExpenseDateBetween(user, startDate, endDate);
    }

    public List<Expense> getExpensesByCategoryAndDateRange(String category, LocalDate startDate, LocalDate endDate) {
        User user = getCurrentUser();
        return expenseRepository.findByUserAndCategoryAndDateRange(user, category, startDate, endDate);
    }

    public List<String> getCategoriesByDateRange(LocalDate startDate, LocalDate endDate) {
        User user = getCurrentUser();
        return expenseRepository.findDistinctCategoriesByUserAndDateRange(user, startDate, endDate);
    }
}