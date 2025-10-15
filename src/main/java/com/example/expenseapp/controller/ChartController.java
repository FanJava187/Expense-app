package com.example.expenseapp.controller;

import com.example.expenseapp.dto.*;
import com.example.expenseapp.service.ChartService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/charts")
@Tag(name = "圖表資料 API", description = "提供各種圖表所需的資料")
public class ChartController {

    @Autowired
    private ChartService chartService;

    @Operation(summary = "取得每日趨勢資料", description = "取得指定月份的每日支出趨勢（用於折線圖）")
    @GetMapping("/trend/daily")
    public ResponseEntity<List<TrendData>> getDailyTrend(
            @Parameter(description = "年份", example = "2025") @RequestParam int year,
            @Parameter(description = "月份", example = "10") @RequestParam int month) {
        List<TrendData> data = chartService.getDailyTrend(year, month);
        return ResponseEntity.ok(data);
    }

    @Operation(summary = "取得月度趨勢資料", description = "取得指定年份的每月支出趨勢（用於折線圖）")
    @GetMapping("/trend/monthly")
    public ResponseEntity<List<TrendData>> getMonthlyTrend(
            @Parameter(description = "年份", example = "2025") @RequestParam int year) {
        List<TrendData> data = chartService.getMonthlyTrend(year);
        return ResponseEntity.ok(data);
    }

    @Operation(summary = "取得分類圓餅圖資料", description = "取得指定日期範圍內各分類的支出佔比（用於圓餅圖）")
    @GetMapping("/pie/category")
    public ResponseEntity<List<PieChartData>> getCategoryPieChart(
            @Parameter(description = "開始日期", example = "2025-10-01")
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @Parameter(description = "結束日期", example = "2025-10-31")
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {

        if (startDate.isAfter(endDate)) {
            return ResponseEntity.badRequest().build();
        }

        List<PieChartData> data = chartService.getCategoryPieChart(startDate, endDate);
        return ResponseEntity.ok(data);
    }

    @Operation(summary = "取得月度比較資料", description = "取得最近 N 個月的支出比較（用於長條圖）")
    @GetMapping("/comparison/monthly")
    public ResponseEntity<ComparisonData> getMonthlyComparison(
            @Parameter(description = "月份數量", example = "6")
            @RequestParam(defaultValue = "6") int months) {

        if (months < 1 || months > 24) {
            return ResponseEntity.badRequest().build();
        }

        ComparisonData data = chartService.getMonthlyComparison(months);
        return ResponseEntity.ok(data);
    }

    @Operation(summary = "取得分類比較資料", description = "取得指定日期範圍內各分類的支出比較（用於長條圖）")
    @GetMapping("/comparison/category")
    public ResponseEntity<ComparisonData> getCategoryComparison(
            @Parameter(description = "開始日期", example = "2025-10-01")
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @Parameter(description = "結束日期", example = "2025-10-31")
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {

        if (startDate.isAfter(endDate)) {
            return ResponseEntity.badRequest().build();
        }

        ComparisonData data = chartService.getCategoryComparison(startDate, endDate);
        return ResponseEntity.ok(data);
    }

    @Operation(summary = "取得 Top N 最大筆支出", description = "取得指定日期範圍內金額最大的 N 筆支出（用於排行榜）")
    @GetMapping("/top-expenses")
    public ResponseEntity<List<TopExpenseItem>> getTopExpenses(
            @Parameter(description = "開始日期", example = "2025-10-01")
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @Parameter(description = "結束日期", example = "2025-10-31")
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @Parameter(description = "數量限制", example = "10")
            @RequestParam(defaultValue = "10") int limit) {

        if (startDate.isAfter(endDate)) {
            return ResponseEntity.badRequest().build();
        }

        if (limit < 1 || limit > 100) {
            return ResponseEntity.badRequest().build();
        }

        List<TopExpenseItem> data = chartService.getTopExpenses(startDate, endDate, limit);
        return ResponseEntity.ok(data);
    }
}
