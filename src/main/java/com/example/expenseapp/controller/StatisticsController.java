package com.example.expenseapp.controller;

import com.example.expenseapp.dto.CategoryStatistics;
import com.example.expenseapp.dto.PeriodStatistics;
import com.example.expenseapp.dto.SummaryStatistics;
import com.example.expenseapp.service.StatisticsService;
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
@RequestMapping("/api/statistics")
@Tag(name = "統計 API", description = "支出統計相關操作")
public class StatisticsController {

    @Autowired
    private StatisticsService statisticsService;

    @Operation(summary = "取得總覽統計",
            description = "取得指定日期範圍的總覽統計資料，包含總金額、總筆數、平均值、最大值、最小值")
    @GetMapping("/summary")
    public ResponseEntity<SummaryStatistics> getSummaryStatistics(
            @Parameter(description = "開始日期", example = "2025-10-01")
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @Parameter(description = "結束日期", example = "2025-10-31")
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {

        SummaryStatistics statistics = statisticsService.getSummaryStatistics(startDate, endDate);
        return ResponseEntity.ok(statistics);
    }

    @Operation(summary = "取得分類統計",
            description = "取得指定日期範圍內各分類的支出統計，包含金額、筆數和占比")
    @GetMapping("/category")
    public ResponseEntity<List<CategoryStatistics>> getCategoryStatistics(
            @Parameter(description = "開始日期", example = "2025-10-01")
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @Parameter(description = "結束日期", example = "2025-10-31")
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {

        List<CategoryStatistics> statistics = statisticsService.getCategoryStatistics(startDate, endDate);
        return ResponseEntity.ok(statistics);
    }

    @Operation(summary = "取得月度統計",
            description = "取得指定年月的每日支出統計")
    @GetMapping("/monthly")
    public ResponseEntity<List<PeriodStatistics>> getMonthlyStatistics(
            @Parameter(description = "年份", example = "2025")
            @RequestParam int year,
            @Parameter(description = "月份（1-12）", example = "10")
            @RequestParam int month) {

        List<PeriodStatistics> statistics = statisticsService.getMonthlyStatistics(year, month);
        return ResponseEntity.ok(statistics);
    }

    @Operation(summary = "取得年度統計",
            description = "取得指定年份的每月支出統計")
    @GetMapping("/yearly")
    public ResponseEntity<List<PeriodStatistics>> getYearlyStatistics(
            @Parameter(description = "年份", example = "2025")
            @RequestParam int year) {

        List<PeriodStatistics> statistics = statisticsService.getYearlyStatistics(year);
        return ResponseEntity.ok(statistics);
    }

    @Operation(summary = "取得當月統計",
            description = "取得當前月份的每日支出統計")
    @GetMapping("/current-month")
    public ResponseEntity<List<PeriodStatistics>> getCurrentMonthStatistics() {
        List<PeriodStatistics> statistics = statisticsService.getCurrentMonthStatistics();
        return ResponseEntity.ok(statistics);
    }

    @Operation(summary = "取得當年統計",
            description = "取得當前年份的每月支出統計")
    @GetMapping("/current-year")
    public ResponseEntity<List<PeriodStatistics>> getCurrentYearStatistics() {
        List<PeriodStatistics> statistics = statisticsService.getCurrentYearStatistics();
        return ResponseEntity.ok(statistics);
    }
}
