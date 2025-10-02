package com.example.expenseapp.controller;

import com.example.expenseapp.model.Expense;
import com.example.expenseapp.repository.ExpenseRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class ExpenseControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ExpenseRepository expenseRepository;

    @Autowired
    private ObjectMapper objectMapper;

    @BeforeEach
    void setup() {
        expenseRepository.deleteAll();
    }

    // ========== 基本 CRUD 測試 ==========

    @Test
    @DisplayName("測試新增支出 - 成功")
    void testCreateExpense_Success() throws Exception {
        Expense expense = new Expense();
        expense.setTitle("午餐");
        expense.setAmount(BigDecimal.valueOf(150));
        expense.setCategory("餐飲");
        expense.setExpenseDate(LocalDate.now());

        mockMvc.perform(post("/api/expenses")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(expense)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.title").value("午餐"))
                .andExpect(jsonPath("$.amount").value(150))
                .andExpect(jsonPath("$.category").value("餐飲"));
    }

    @Test
    @DisplayName("測試查詢所有支出")
    void testGetAllExpenses() throws Exception {
        // 準備測試資料
        Expense expense1 = new Expense("早餐", BigDecimal.valueOf(50), "餐飲", LocalDate.now());
        Expense expense2 = new Expense("晚餐", BigDecimal.valueOf(200), "餐飲", LocalDate.now());
        expenseRepository.save(expense1);
        expenseRepository.save(expense2);

        mockMvc.perform(get("/api/expenses"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].title").value("早餐"))
                .andExpect(jsonPath("$[1].title").value("晚餐"));
    }

    @Test
    @DisplayName("測試根據 ID 查詢支出 - 成功")
    void testGetExpenseById_Success() throws Exception {
        Expense expense = new Expense("電影票", BigDecimal.valueOf(320), "娛樂", LocalDate.now());
        Expense saved = expenseRepository.save(expense);

        mockMvc.perform(get("/api/expenses/" + saved.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("電影票"))
                .andExpect(jsonPath("$.amount").value(320));
    }

    @Test
    @DisplayName("測試根據 ID 查詢支出 - 找不到")
    void testGetExpenseById_NotFound() throws Exception {
        mockMvc.perform(get("/api/expenses/999"))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("測試更新支出 - 成功")
    void testUpdateExpense_Success() throws Exception {
        Expense expense = new Expense("咖啡", BigDecimal.valueOf(100), "餐飲", LocalDate.now());
        Expense saved = expenseRepository.save(expense);

        Expense updatedExpense = new Expense();
        updatedExpense.setTitle("星巴克咖啡");
        updatedExpense.setAmount(BigDecimal.valueOf(150));
        updatedExpense.setCategory("餐飲");
        updatedExpense.setExpenseDate(LocalDate.now());

        mockMvc.perform(put("/api/expenses/" + saved.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updatedExpense)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("星巴克咖啡"))
                .andExpect(jsonPath("$.amount").value(150));
    }

    @Test
    @DisplayName("測試刪除支出")
    void testDeleteExpense() throws Exception {
        Expense expense = new Expense("測試支出", BigDecimal.valueOf(100), "其他", LocalDate.now());
        Expense saved = expenseRepository.save(expense);

        mockMvc.perform(delete("/api/expenses/" + saved.getId()))
                .andExpect(status().isNoContent());

        // 確認已刪除
        mockMvc.perform(get("/api/expenses/" + saved.getId()))
                .andExpect(status().isNotFound());
    }

    // ========== 驗證測試 ==========

    @Test
    @DisplayName("測試驗證 - 標題為空")
    void testValidation_EmptyTitle() throws Exception {
        Expense expense = new Expense();
        expense.setTitle("");  // 空標題
        expense.setAmount(BigDecimal.valueOf(100));
        expense.setCategory("餐飲");
        expense.setExpenseDate(LocalDate.now());

        mockMvc.perform(post("/api/expenses")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(expense)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.title").exists());
    }

    @Test
    @DisplayName("測試驗證 - 金額為負數")
    void testValidation_NegativeAmount() throws Exception {
        Expense expense = new Expense();
        expense.setTitle("測試");
        expense.setAmount(BigDecimal.valueOf(-50));  // 負數金額
        expense.setCategory("餐飲");
        expense.setExpenseDate(LocalDate.now());

        mockMvc.perform(post("/api/expenses")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(expense)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.amount").exists());
    }

    @Test
    @DisplayName("測試驗證 - 金額為零")
    void testValidation_ZeroAmount() throws Exception {
        Expense expense = new Expense();
        expense.setTitle("測試");
        expense.setAmount(BigDecimal.ZERO);  // 零金額
        expense.setCategory("餐飲");
        expense.setExpenseDate(LocalDate.now());

        mockMvc.perform(post("/api/expenses")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(expense)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("測試驗證 - 分類為空")
    void testValidation_EmptyCategory() throws Exception {
        Expense expense = new Expense();
        expense.setTitle("測試");
        expense.setAmount(BigDecimal.valueOf(100));
        expense.setCategory("");  // 空分類
        expense.setExpenseDate(LocalDate.now());

        mockMvc.perform(post("/api/expenses")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(expense)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.category").exists());
    }

    @Test
    @DisplayName("測試驗證 - 日期為未來")
    void testValidation_FutureDate() throws Exception {
        Expense expense = new Expense();
        expense.setTitle("測試");
        expense.setAmount(BigDecimal.valueOf(100));
        expense.setCategory("餐飲");
        expense.setExpenseDate(LocalDate.now().plusDays(1));  // 未來日期

        mockMvc.perform(post("/api/expenses")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(expense)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.expenseDate").exists());
    }

    // ========== 查詢功能測試 ==========

    @Test
    @DisplayName("測試根據分類查詢支出")
    void testGetExpensesByCategory() throws Exception {
        // 準備不同分類的資料
        expenseRepository.save(new Expense("早餐", BigDecimal.valueOf(50), "餐飲", LocalDate.now()));
        expenseRepository.save(new Expense("午餐", BigDecimal.valueOf(120), "餐飲", LocalDate.now()));
        expenseRepository.save(new Expense("捷運", BigDecimal.valueOf(35), "交通", LocalDate.now()));

        mockMvc.perform(get("/api/expenses/category/餐飲"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].category").value("餐飲"))
                .andExpect(jsonPath("$[1].category").value("餐飲"));
    }

    @Test
    @DisplayName("測試根據日期範圍查詢支出")
    void testGetExpensesByDateRange() throws Exception {
        LocalDate today = LocalDate.now();
        LocalDate yesterday = today.minusDays(1);
        LocalDate twoDaysAgo = today.minusDays(2);

        // 準備不同日期的資料
        expenseRepository.save(new Expense("今天", BigDecimal.valueOf(100), "餐飲", today));
        expenseRepository.save(new Expense("昨天", BigDecimal.valueOf(200), "餐飲", yesterday));
        expenseRepository.save(new Expense("前天", BigDecimal.valueOf(300), "餐飲", twoDaysAgo));

        // 查詢昨天到今天的資料
        mockMvc.perform(get("/api/expenses/date-range")
                        .param("startDate", yesterday.toString())
                        .param("endDate", today.toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)));
    }

    @Test
    @DisplayName("測試日期範圍查詢 - 開始日期晚於結束日期")
    void testGetExpensesByDateRange_InvalidRange() throws Exception {
        LocalDate today = LocalDate.now();
        LocalDate yesterday = today.minusDays(1);

        mockMvc.perform(get("/api/expenses/date-range")
                        .param("startDate", today.toString())
                        .param("endDate", yesterday.toString()))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("測試根據分類和日期範圍組合查詢")
    void testSearchExpenses() throws Exception {
        LocalDate today = LocalDate.now();
        LocalDate yesterday = today.minusDays(1);

        // 準備測試資料
        expenseRepository.save(new Expense("今天餐飲", BigDecimal.valueOf(100), "餐飲", today));
        expenseRepository.save(new Expense("昨天餐飲", BigDecimal.valueOf(200), "餐飲", yesterday));
        expenseRepository.save(new Expense("今天交通", BigDecimal.valueOf(50), "交通", today));

        mockMvc.perform(get("/api/expenses/search")
                        .param("category", "餐飲")
                        .param("startDate", yesterday.toString())
                        .param("endDate", today.toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[*].category", everyItem(is("餐飲"))));
    }

    @Test
    @DisplayName("測試取得日期範圍內的所有分類")
    void testGetCategoriesByDateRange() throws Exception {
        LocalDate today = LocalDate.now();

        // 準備不同分類的資料
        expenseRepository.save(new Expense("餐飲支出", BigDecimal.valueOf(100), "餐飲", today));
        expenseRepository.save(new Expense("交通支出", BigDecimal.valueOf(50), "交通", today));
        expenseRepository.save(new Expense("娛樂支出", BigDecimal.valueOf(300), "娛樂", today));

        mockMvc.perform(get("/api/expenses/categories")
                        .param("startDate", today.toString())
                        .param("endDate", today.toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(3)))
                .andExpect(jsonPath("$", containsInAnyOrder("交通", "娛樂", "餐飲")));
    }

    @Test
    @DisplayName("測試查詢空結果")
    void testGetExpensesByCategory_NoResults() throws Exception {
        mockMvc.perform(get("/api/expenses/category/不存在的分類"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(0)));
    }
}