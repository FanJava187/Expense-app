package com.example.expenseapp.service;

import com.example.expenseapp.dto.*;
import com.example.expenseapp.exception.ResourceNotFoundException;
import com.example.expenseapp.model.Expense;
import com.example.expenseapp.model.User;
import com.example.expenseapp.repository.ExpenseRepository;
import com.example.expenseapp.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class ChartService {

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
     * 取得每日趨勢資料（指定月份）
     */
    public List<TrendData> getDailyTrend(int year, int month) {
        User user = getCurrentUser();
        YearMonth yearMonth = YearMonth.of(year, month);
        LocalDate startDate = yearMonth.atDay(1);
        LocalDate endDate = yearMonth.atEndOfMonth();

        List<Expense> expenses = expenseRepository.findByUserAndExpenseDateBetween(user, startDate, endDate);

        // 按日期分組
        Map<LocalDate, List<Expense>> groupedByDate = expenses.stream()
                .collect(Collectors.groupingBy(Expense::getExpenseDate));

        List<TrendData> trendData = new ArrayList<>();

        // 填充每一天的資料（包含沒有支出的日期）
        for (LocalDate date = startDate; !date.isAfter(endDate); date = date.plusDays(1)) {
            List<Expense> dayExpenses = groupedByDate.getOrDefault(date, Collections.emptyList());
            BigDecimal dayTotal = dayExpenses.stream()
                    .map(Expense::getAmount)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

            trendData.add(new TrendData(
                    date.toString(),
                    dayTotal,
                    (long) dayExpenses.size()
            ));
        }

        return trendData;
    }

    /**
     * 取得月度趨勢資料（指定年份）
     */
    public List<TrendData> getMonthlyTrend(int year) {
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

        List<TrendData> trendData = new ArrayList<>();

        // 填充每個月的資料（包含沒有支出的月份）
        for (int m = 1; m <= 12; m++) {
            String monthKey = String.format("%04d-%02d", year, m);
            List<Expense> monthExpenses = groupedByMonth.getOrDefault(monthKey, Collections.emptyList());
            BigDecimal monthTotal = monthExpenses.stream()
                    .map(Expense::getAmount)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

            trendData.add(new TrendData(
                    monthKey,
                    monthTotal,
                    (long) monthExpenses.size()
            ));
        }

        return trendData;
    }

    /**
     * 取得分類佔比圓餅圖資料
     */
    public List<PieChartData> getCategoryPieChart(LocalDate startDate, LocalDate endDate) {
        User user = getCurrentUser();
        List<Expense> expenses = expenseRepository.findByUserAndExpenseDateBetween(user, startDate, endDate);

        if (expenses.isEmpty()) {
            return new ArrayList<>();
        }

        // 計算總金額
        BigDecimal totalAmount = expenses.stream()
                .map(Expense::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        // 按分類分組
        Map<String, List<Expense>> groupedByCategory = expenses.stream()
                .collect(Collectors.groupingBy(Expense::getCategory));

        List<PieChartData> pieData = groupedByCategory.entrySet().stream()
                .map(entry -> {
                    String category = entry.getKey();
                    List<Expense> categoryExpenses = entry.getValue();

                    BigDecimal categoryTotal = categoryExpenses.stream()
                            .map(Expense::getAmount)
                            .reduce(BigDecimal.ZERO, BigDecimal::add);

                    Double percentage = totalAmount.compareTo(BigDecimal.ZERO) > 0
                            ? categoryTotal.divide(totalAmount, 4, RoundingMode.HALF_UP)
                            .multiply(BigDecimal.valueOf(100))
                            .doubleValue()
                            : 0.0;

                    return new PieChartData(
                            category,
                            categoryTotal,
                            percentage,
                            (long) categoryExpenses.size()
                    );
                })
                .sorted(Comparator.comparing(PieChartData::getValue).reversed())
                .collect(Collectors.toList());

        return pieData;
    }

    /**
     * 取得月度比較資料（最近 N 個月）
     */
    public ComparisonData getMonthlyComparison(int months) {
        User user = getCurrentUser();
        LocalDate endDate = LocalDate.now();
        LocalDate startDate = endDate.minusMonths(months - 1).withDayOfMonth(1);

        List<Expense> expenses = expenseRepository.findByUserAndExpenseDateBetween(
                user, startDate, YearMonth.from(endDate).atEndOfMonth());

        // 按年月分組
        Map<String, List<Expense>> groupedByMonth = expenses.stream()
                .collect(Collectors.groupingBy(expense -> {
                    LocalDate date = expense.getExpenseDate();
                    return String.format("%04d-%02d", date.getYear(), date.getMonthValue());
                }));

        List<String> labels = new ArrayList<>();
        List<BigDecimal> amounts = new ArrayList<>();
        List<Long> counts = new ArrayList<>();

        // 填充每個月的資料
        YearMonth currentMonth = YearMonth.from(startDate);
        YearMonth lastMonth = YearMonth.from(endDate);

        while (!currentMonth.isAfter(lastMonth)) {
            String monthKey = String.format("%04d-%02d", currentMonth.getYear(), currentMonth.getMonthValue());
            List<Expense> monthExpenses = groupedByMonth.getOrDefault(monthKey, Collections.emptyList());

            BigDecimal monthTotal = monthExpenses.stream()
                    .map(Expense::getAmount)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

            labels.add(monthKey);
            amounts.add(monthTotal);
            counts.add((long) monthExpenses.size());

            currentMonth = currentMonth.plusMonths(1);
        }

        return new ComparisonData(labels, amounts, counts);
    }

    /**
     * 取得 Top N 最大筆支出
     */
    public List<TopExpenseItem> getTopExpenses(LocalDate startDate, LocalDate endDate, int limit) {
        User user = getCurrentUser();
        List<Expense> expenses = expenseRepository.findByUserAndExpenseDateBetween(user, startDate, endDate);

        List<TopExpenseItem> topItems = expenses.stream()
                .sorted(Comparator.comparing(Expense::getAmount).reversed())
                .limit(limit)
                .map(expense -> new TopExpenseItem(
                        expense.getId(),
                        expense.getTitle(),
                        expense.getAmount(),
                        expense.getCategory(),
                        expense.getExpenseDate(),
                        null  // rank 會在後面設定
                ))
                .collect(Collectors.toList());

        // 設定排名
        for (int i = 0; i < topItems.size(); i++) {
            topItems.get(i).setRank(i + 1);
        }

        return topItems;
    }

    /**
     * 取得分類比較資料（指定日期範圍）
     */
    public ComparisonData getCategoryComparison(LocalDate startDate, LocalDate endDate) {
        User user = getCurrentUser();
        List<Expense> expenses = expenseRepository.findByUserAndExpenseDateBetween(user, startDate, endDate);

        // 按分類分組
        Map<String, List<Expense>> groupedByCategory = expenses.stream()
                .collect(Collectors.groupingBy(Expense::getCategory));

        List<String> labels = new ArrayList<>();
        List<BigDecimal> amounts = new ArrayList<>();
        List<Long> counts = new ArrayList<>();

        // 按金額排序
        groupedByCategory.entrySet().stream()
                .sorted((e1, e2) -> {
                    BigDecimal sum1 = e1.getValue().stream()
                            .map(Expense::getAmount)
                            .reduce(BigDecimal.ZERO, BigDecimal::add);
                    BigDecimal sum2 = e2.getValue().stream()
                            .map(Expense::getAmount)
                            .reduce(BigDecimal.ZERO, BigDecimal::add);
                    return sum2.compareTo(sum1);  // 降序
                })
                .forEach(entry -> {
                    String category = entry.getKey();
                    List<Expense> categoryExpenses = entry.getValue();

                    BigDecimal categoryTotal = categoryExpenses.stream()
                            .map(Expense::getAmount)
                            .reduce(BigDecimal.ZERO, BigDecimal::add);

                    labels.add(category);
                    amounts.add(categoryTotal);
                    counts.add((long) categoryExpenses.size());
                });

        return new ComparisonData(labels, amounts, counts);
    }
}
