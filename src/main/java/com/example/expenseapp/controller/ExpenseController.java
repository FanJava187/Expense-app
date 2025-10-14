package com.example.expenseapp.controller;

import com.example.expenseapp.model.Expense;
import com.example.expenseapp.service.ExpenseService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/expenses")
@Tag(name = "Expense API", description = "管理支出紀錄的 API")
public class ExpenseController {

    private final ExpenseService expenseService;

    public ExpenseController(ExpenseService expenseService) {
        this.expenseService = expenseService;
    }

    @Operation(summary = "取得所有支出紀錄（支援分頁）",
               description = "查詢當前使用者的所有支出紀錄，支援分頁和排序")
    @GetMapping
    public ResponseEntity<Page<Expense>> getAllExpenses(
            @Parameter(description = "頁碼（從 0 開始）", example = "0")
            @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "每頁筆數", example = "20")
            @RequestParam(defaultValue = "20") int size,
            @Parameter(description = "排序欄位", example = "expenseDate")
            @RequestParam(defaultValue = "expenseDate") String sortBy,
            @Parameter(description = "排序方向（asc 或 desc）", example = "desc")
            @RequestParam(defaultValue = "desc") String sortDirection) {

        // 建立排序物件
        Sort.Direction direction = sortDirection.equalsIgnoreCase("asc")
                ? Sort.Direction.ASC
                : Sort.Direction.DESC;
        Sort sort = Sort.by(direction, sortBy);

        // 建立分頁物件
        Pageable pageable = PageRequest.of(page, size, sort);

        // 查詢資料
        Page<Expense> expenses = expenseService.getAllExpenses(pageable);
        return ResponseEntity.ok(expenses);
    }

    @Operation(summary = "依 ID 查詢支出紀錄")
    @GetMapping("/{id}")
    public ResponseEntity<Expense> getExpenseById(@PathVariable Long id) {
        Expense expense = expenseService.getExpenseById(id);
        return ResponseEntity.ok(expense);
    }

    @Operation(summary = "新增支出紀錄")
    @PostMapping
    public ResponseEntity<Expense> createExpense(@Valid @RequestBody Expense expense) {
        Expense created = expenseService.createExpense(expense);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @Operation(summary = "更新支出紀錄")
    @PutMapping("/{id}")
    public ResponseEntity<Expense> updateExpense(
            @PathVariable Long id,
            @Valid @RequestBody Expense expenseDetails) {
        Expense updatedExpense = expenseService.updateExpense(id, expenseDetails);
        return ResponseEntity.ok(updatedExpense);
    }

    @Operation(summary = "刪除支出紀錄")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteExpense(@PathVariable Long id) {
        expenseService.deleteExpense(id);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "根據分類查詢支出", description = "查詢特定分類的所有支出紀錄")
    @GetMapping("/category/{category}")
    public ResponseEntity<List<Expense>> getExpensesByCategory(
            @Parameter(description = "支出分類", example = "食物")
            @PathVariable String category) {
        List<Expense> expenses = expenseService.getExpensesByCategory(category);
        return ResponseEntity.ok(expenses);
    }

    @Operation(summary = "根據日期範圍查詢支出", description = "查詢指定日期範圍內的所有支出紀錄")
    @GetMapping("/date-range")
    public ResponseEntity<List<Expense>> getExpensesByDateRange(
            @Parameter(description = "開始日期", example = "2025-09-01")
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @Parameter(description = "結束日期", example = "2025-09-30")
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {

        if (startDate.isAfter(endDate)) {
            return ResponseEntity.badRequest().build();
        }

        List<Expense> expenses = expenseService.getExpensesByDateRange(startDate, endDate);
        return ResponseEntity.ok(expenses);
    }

    @Operation(summary = "根據分類和日期範圍查詢支出", description = "查詢特定分類在指定日期範圍內的支出紀錄")
    @GetMapping("/search")
    public ResponseEntity<List<Expense>> searchExpenses(
            @Parameter(description = "支出分類", example = "食物")
            @RequestParam String category,
            @Parameter(description = "開始日期", example = "2025-09-01")
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @Parameter(description = "結束日期", example = "2025-09-30")
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {

        if (startDate.isAfter(endDate)) {
            return ResponseEntity.badRequest().build();
        }

        List<Expense> expenses = expenseService.getExpensesByCategoryAndDateRange(category, startDate, endDate);
        return ResponseEntity.ok(expenses);
    }

    @Operation(summary = "取得日期範圍內的所有分類", description = "取得指定日期範圍內使用過的所有分類")
    @GetMapping("/categories")
    public ResponseEntity<List<String>> getCategoriesByDateRange(
            @Parameter(description = "開始日期", example = "2025-09-01")
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @Parameter(description = "結束日期", example = "2025-09-30")
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {

        if (startDate.isAfter(endDate)) {
            return ResponseEntity.badRequest().build();
        }

        List<String> categories = expenseService.getCategoriesByDateRange(startDate, endDate);
        return ResponseEntity.ok(categories);
    }

    /**
     * 處理驗證錯誤
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Map<String, String> handleValidationExceptions(MethodArgumentNotValidException ex) {
        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getAllErrors().forEach((error) -> {
            String fieldName = ((FieldError) error).getField();
            String errorMessage = error.getDefaultMessage();
            errors.put(fieldName, errorMessage);
        });
        return errors;
    }
}