package com.example.expenseapp.service;

import com.example.expenseapp.dto.BudgetRequest;
import com.example.expenseapp.dto.BudgetResponse;
import com.example.expenseapp.exception.ResourceNotFoundException;
import com.example.expenseapp.model.Budget;
import com.example.expenseapp.model.Expense;
import com.example.expenseapp.model.User;
import com.example.expenseapp.repository.BudgetRepository;
import com.example.expenseapp.repository.ExpenseRepository;
import com.example.expenseapp.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.List;

@Service
public class BudgetService {

    @Autowired
    private BudgetRepository budgetRepository;

    @Autowired
    private ExpenseRepository expenseRepository;

    @Autowired
    private UserRepository userRepository;

    /**
     * 取得當前登入的使用者
     */
    private User getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("找不到目前使用者"));
    }

    /**
     * 建立預算
     */
    @Transactional
    public BudgetResponse createBudget(BudgetRequest request) {
        User user = getCurrentUser();

        // 驗證預算類型
        Budget.BudgetType budgetType;
        try {
            budgetType = Budget.BudgetType.valueOf(request.getBudgetType().toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("無效的預算類型：" + request.getBudgetType());
        }

        // 如果是分類預算，category 必須有值
        if (budgetType == Budget.BudgetType.CATEGORY && (request.getCategory() == null || request.getCategory().trim().isEmpty())) {
            throw new IllegalArgumentException("分類預算必須指定分類");
        }

        // 檢查是否已存在相同的預算
        if (budgetType == Budget.BudgetType.MONTHLY) {
            budgetRepository.findMonthlyBudget(user, request.getYear(), request.getMonth())
                    .ifPresent(b -> {
                        throw new IllegalArgumentException(
                                String.format("已存在 %d 年 %d 月的月度預算", request.getYear(), request.getMonth()));
                    });
        } else {
            budgetRepository.findCategoryBudget(user, request.getYear(), request.getMonth(), request.getCategory())
                    .ifPresent(b -> {
                        throw new IllegalArgumentException(
                                String.format("已存在 %d 年 %d 月分類「%s」的預算",
                                        request.getYear(), request.getMonth(), request.getCategory()));
                    });
        }

        // 建立預算
        Budget budget = new Budget();
        budget.setUser(user);
        budget.setBudgetType(budgetType);
        budget.setCategory(budgetType == Budget.BudgetType.CATEGORY ? request.getCategory() : null);
        budget.setAmount(request.getAmount());
        budget.setYear(request.getYear());
        budget.setMonth(request.getMonth());

        Budget savedBudget = budgetRepository.save(budget);

        // 計算已使用金額並回傳
        return buildBudgetResponse(savedBudget);
    }

    /**
     * 更新預算
     */
    @Transactional
    public BudgetResponse updateBudget(Long id, BudgetRequest request) {
        User user = getCurrentUser();
        Budget budget = budgetRepository.findByIdAndUser(id, user)
                .orElseThrow(() -> new ResourceNotFoundException("找不到 ID 為 " + id + " 的預算"));

        // 只允許更新金額
        budget.setAmount(request.getAmount());
        Budget updatedBudget = budgetRepository.save(budget);

        return buildBudgetResponse(updatedBudget);
    }

    /**
     * 刪除預算
     */
    @Transactional
    public void deleteBudget(Long id) {
        User user = getCurrentUser();
        Budget budget = budgetRepository.findByIdAndUser(id, user)
                .orElseThrow(() -> new ResourceNotFoundException("找不到 ID 為 " + id + " 的預算"));
        budgetRepository.delete(budget);
    }

    /**
     * 取得特定預算
     */
    public BudgetResponse getBudget(Long id) {
        User user = getCurrentUser();
        Budget budget = budgetRepository.findByIdAndUser(id, user)
                .orElseThrow(() -> new ResourceNotFoundException("找不到 ID 為 " + id + " 的預算"));
        return buildBudgetResponse(budget);
    }

    /**
     * 取得指定年月的所有預算
     */
    public List<BudgetResponse> getBudgetsByMonth(Integer year, Integer month) {
        User user = getCurrentUser();
        List<Budget> budgets = budgetRepository.findByUserAndYearAndMonth(user, year, month);

        List<BudgetResponse> responses = new ArrayList<>();
        for (Budget budget : budgets) {
            responses.add(buildBudgetResponse(budget));
        }
        return responses;
    }

    /**
     * 取得當月所有預算
     */
    public List<BudgetResponse> getCurrentMonthBudgets() {
        LocalDate now = LocalDate.now();
        return getBudgetsByMonth(now.getYear(), now.getMonthValue());
    }

    /**
     * 建立 BudgetResponse（包含已使用金額等計算）
     */
    private BudgetResponse buildBudgetResponse(Budget budget) {
        User user = budget.getUser();
        YearMonth yearMonth = YearMonth.of(budget.getYear(), budget.getMonth());
        LocalDate startDate = yearMonth.atDay(1);
        LocalDate endDate = yearMonth.atEndOfMonth();

        // 計算已使用金額
        BigDecimal spent;
        if (budget.getBudgetType() == Budget.BudgetType.MONTHLY) {
            // 月度預算：計算該月所有支出
            List<Expense> expenses = expenseRepository.findByUserAndExpenseDateBetween(user, startDate, endDate);
            spent = expenses.stream()
                    .map(Expense::getAmount)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
        } else {
            // 分類預算：計算該月該分類的支出
            List<Expense> expenses = expenseRepository.findByUserAndCategoryAndDateRange(
                    user, budget.getCategory(), startDate, endDate);
            spent = expenses.stream()
                    .map(Expense::getAmount)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
        }

        // 計算剩餘金額
        BigDecimal remaining = budget.getAmount().subtract(spent);

        // 計算使用百分比
        double percentage = budget.getAmount().compareTo(BigDecimal.ZERO) > 0
                ? spent.divide(budget.getAmount(), 4, RoundingMode.HALF_UP)
                .multiply(BigDecimal.valueOf(100))
                .doubleValue()
                : 0.0;

        return new BudgetResponse(
                budget.getId(),
                budget.getBudgetType().name(),
                budget.getCategory(),
                budget.getAmount(),
                budget.getYear(),
                budget.getMonth(),
                spent,
                remaining,
                percentage
        );
    }
}
