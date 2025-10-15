package com.example.expenseapp.controller;

import com.example.expenseapp.dto.BudgetRequest;
import com.example.expenseapp.dto.BudgetResponse;
import com.example.expenseapp.service.BudgetService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/budgets")
@Tag(name = "預算管理 API", description = "管理月度預算和分類預算")
public class BudgetController {

    @Autowired
    private BudgetService budgetService;

    @Operation(summary = "建立預算", description = "建立月度預算或分類預算")
    @PostMapping
    public ResponseEntity<BudgetResponse> createBudget(@Valid @RequestBody BudgetRequest request) {
        BudgetResponse response = budgetService.createBudget(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @Operation(summary = "更新預算", description = "更新預算金額")
    @PutMapping("/{id}")
    public ResponseEntity<BudgetResponse> updateBudget(
            @Parameter(description = "預算 ID") @PathVariable Long id,
            @Valid @RequestBody BudgetRequest request) {
        BudgetResponse response = budgetService.updateBudget(id, request);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "刪除預算", description = "刪除指定的預算")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteBudget(
            @Parameter(description = "預算 ID") @PathVariable Long id) {
        budgetService.deleteBudget(id);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "取得預算詳情", description = "取得指定預算的詳細資訊，包含已使用金額")
    @GetMapping("/{id}")
    public ResponseEntity<BudgetResponse> getBudget(
            @Parameter(description = "預算 ID") @PathVariable Long id) {
        BudgetResponse response = budgetService.getBudget(id);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "取得指定月份的所有預算", description = "取得指定年月的所有預算（包含月度預算和所有分類預算）")
    @GetMapping
    public ResponseEntity<List<BudgetResponse>> getBudgetsByMonth(
            @Parameter(description = "年份", example = "2025") @RequestParam Integer year,
            @Parameter(description = "月份", example = "10") @RequestParam Integer month) {
        List<BudgetResponse> responses = budgetService.getBudgetsByMonth(year, month);
        return ResponseEntity.ok(responses);
    }

    @Operation(summary = "取得當月所有預算", description = "取得當前月份的所有預算（便利方法）")
    @GetMapping("/current")
    public ResponseEntity<List<BudgetResponse>> getCurrentMonthBudgets() {
        List<BudgetResponse> responses = budgetService.getCurrentMonthBudgets();
        return ResponseEntity.ok(responses);
    }
}
