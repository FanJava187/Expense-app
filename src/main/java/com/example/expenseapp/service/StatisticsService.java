package com.example.expenseapp.service;

import com.example.expenseapp.dto.CategoryStatistics;
import com.example.expenseapp.dto.PeriodStatistics;
import com.example.expenseapp.dto.SummaryStatistics;
import com.example.expenseapp.model.Expense;
import com.example.expenseapp.model.User;
import com.example.expenseapp.repository.ExpenseRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class StatisticsService {

    @Autowired
    private ExpenseRepository expenseRepository;

    /**
     * 取得當前登入的使用者
     */
    private User getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        return (User) userDetails;
    }

    /**
     * 取得總覽統計（指定日期範圍）
     */
    public SummaryStatistics getSummaryStatistics(LocalDate startDate, LocalDate endDate) {
        User user = getCurrentUser();
        List<Expense> expenses = expenseRepository.findByUserAndExpenseDateBetween(user, startDate, endDate);

        if (expenses.isEmpty()) {
            return new SummaryStatistics(
                    BigDecimal.ZERO,
                    0L,
                    BigDecimal.ZERO,
                    BigDecimal.ZERO,
                    BigDecimal.ZERO
            );
        }

        BigDecimal totalAmount = expenses.stream()
                .map(Expense::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        long count = expenses.size();

        BigDecimal averageAmount = totalAmount.divide(
                BigDecimal.valueOf(count),
                2,
                RoundingMode.HALF_UP
        );

        BigDecimal maxAmount = expenses.stream()
                .map(Expense::getAmount)
                .max(BigDecimal::compareTo)
                .orElse(BigDecimal.ZERO);

        BigDecimal minAmount = expenses.stream()
                .map(Expense::getAmount)
                .min(BigDecimal::compareTo)
                .orElse(BigDecimal.ZERO);

        return new SummaryStatistics(totalAmount, count, averageAmount, maxAmount, minAmount);
    }

    /**
     * 取得分類統計（指定日期範圍）
     */
    public List<CategoryStatistics> getCategoryStatistics(LocalDate startDate, LocalDate endDate) {
        User user = getCurrentUser();
        List<Expense> expenses = expenseRepository.findByUserAndExpenseDateBetween(user, startDate, endDate);

        if (expenses.isEmpty()) {
            return new ArrayList<>();
        }

        // 計算總金額（用於計算百分比）
        BigDecimal totalAmount = expenses.stream()
                .map(Expense::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        // 按分類分組統計
        Map<String, List<Expense>> groupedByCategory = expenses.stream()
                .collect(Collectors.groupingBy(Expense::getCategory));

        List<CategoryStatistics> statistics = groupedByCategory.entrySet().stream()
                .map(entry -> {
                    String category = entry.getKey();
                    List<Expense> categoryExpenses = entry.getValue();

                    BigDecimal categoryTotal = categoryExpenses.stream()
                            .map(Expense::getAmount)
                            .reduce(BigDecimal.ZERO, BigDecimal::add);

                    Long count = (long) categoryExpenses.size();

                    // 計算百分比
                    BigDecimal percentage = totalAmount.compareTo(BigDecimal.ZERO) > 0
                            ? categoryTotal.divide(totalAmount, 4, RoundingMode.HALF_UP)
                            .multiply(BigDecimal.valueOf(100))
                            .setScale(2, RoundingMode.HALF_UP)
                            : BigDecimal.ZERO;

                    return new CategoryStatistics(category, categoryTotal, count, percentage);
                })
                .sorted(Comparator.comparing(CategoryStatistics::getTotalAmount).reversed())
                .collect(Collectors.toList());

        return statistics;
    }

    /**
     * 取得月度統計（指定年月的每日統計）
     */
    public List<PeriodStatistics> getMonthlyStatistics(int year, int month) {
        User user = getCurrentUser();

        YearMonth yearMonth = YearMonth.of(year, month);
        LocalDate startDate = yearMonth.atDay(1);
        LocalDate endDate = yearMonth.atEndOfMonth();

        List<Expense> expenses = expenseRepository.findByUserAndExpenseDateBetween(user, startDate, endDate);

        // 按日期分組
        Map<LocalDate, List<Expense>> groupedByDate = expenses.stream()
                .collect(Collectors.groupingBy(Expense::getExpenseDate));

        List<PeriodStatistics> statistics = groupedByDate.entrySet().stream()
                .map(entry -> {
                    String period = entry.getKey().toString(); // 格式: "2025-10-14"
                    BigDecimal dailyTotal = entry.getValue().stream()
                            .map(Expense::getAmount)
                            .reduce(BigDecimal.ZERO, BigDecimal::add);
                    Long count = (long) entry.getValue().size();

                    return new PeriodStatistics(period, dailyTotal, count);
                })
                .sorted(Comparator.comparing(PeriodStatistics::getPeriod))
                .collect(Collectors.toList());

        return statistics;
    }

    /**
     * 取得年度統計（指定年份的每月統計）
     */
    public List<PeriodStatistics> getYearlyStatistics(int year) {
        User user = getCurrentUser();

        LocalDate startDate = LocalDate.of(year, 1, 1);
        LocalDate endDate = LocalDate.of(year, 12, 31);

        List<Expense> expenses = expenseRepository.findByUserAndExpenseDateBetween(user, startDate, endDate);

        // 按年月分組
        Map<String, List<Expense>> groupedByMonth = expenses.stream()
                .collect(Collectors.groupingBy(expense -> {
                    LocalDate date = expense.getExpenseDate();
                    return String.format("%04d-%02d", date.getYear(), date.getMonthValue());
                }));

        List<PeriodStatistics> statistics = groupedByMonth.entrySet().stream()
                .map(entry -> {
                    String period = entry.getKey(); // 格式: "2025-10"
                    BigDecimal monthlyTotal = entry.getValue().stream()
                            .map(Expense::getAmount)
                            .reduce(BigDecimal.ZERO, BigDecimal::add);
                    Long count = (long) entry.getValue().size();

                    return new PeriodStatistics(period, monthlyTotal, count);
                })
                .sorted(Comparator.comparing(PeriodStatistics::getPeriod))
                .collect(Collectors.toList());

        return statistics;
    }

    /**
     * 取得當月統計（便利方法）
     */
    public List<PeriodStatistics> getCurrentMonthStatistics() {
        LocalDate now = LocalDate.now();
        return getMonthlyStatistics(now.getYear(), now.getMonthValue());
    }

    /**
     * 取得當年統計（便利方法）
     */
    public List<PeriodStatistics> getCurrentYearStatistics() {
        LocalDate now = LocalDate.now();
        return getYearlyStatistics(now.getYear());
    }
}
