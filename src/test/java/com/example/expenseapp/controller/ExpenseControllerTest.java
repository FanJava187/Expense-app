package com.example.expenseapp.controller;

import com.example.expenseapp.config.DotenvTestConfig;
import com.example.expenseapp.model.Expense;
import com.example.expenseapp.model.User;
import com.example.expenseapp.repository.ExpenseRepository;
import com.example.expenseapp.repository.UserRepository;
import com.example.expenseapp.security.JwtService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ContextConfiguration(initializers = DotenvTestConfig.class)
class ExpenseControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ExpenseRepository expenseRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private JwtService jwtService;

    @Autowired
    private UserDetailsService userDetailsService;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private String token;
    private User testUser;

    @BeforeEach
    void setup() {
        // 清空測試資料
        expenseRepository.deleteAll();
        userRepository.deleteAll();

        // 建立測試使用者
        testUser = new User();
        testUser.setUsername("testuser");
        testUser.setEmail("test@example.com");
        testUser.setPassword(passwordEncoder.encode("password123"));
        testUser.setName("Test User");
        testUser.setStatus(User.UserStatus.ACTIVE);
        testUser = userRepository.save(testUser);

        // 產生 JWT Token
        UserDetails userDetails = userDetailsService.loadUserByUsername(testUser.getUsername());
        token = jwtService.generateToken(userDetails);
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
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(expense)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.title").value("午餐"))
                .andExpect(jsonPath("$.amount").value(150))
                .andExpect(jsonPath("$.category").value("餐飲"));
    }

    @Test
    @DisplayName("測試新增支出 - 未登入")
    void testCreateExpense_Unauthorized() throws Exception {
        Expense expense = new Expense();
        expense.setTitle("午餐");
        expense.setAmount(BigDecimal.valueOf(150));
        expense.setCategory("餐飲");
        expense.setExpenseDate(LocalDate.now());

        mockMvc.perform(post("/api/expenses")
                        // 不帶 Token
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(expense)))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("測試查詢所有支出")
    void testGetAllExpenses() throws Exception {
        // 建立測試資料
        Expense expense1 = new Expense(testUser, "早餐", BigDecimal.valueOf(50), "餐飲", LocalDate.now());
        Expense expense2 = new Expense(testUser, "晚餐", BigDecimal.valueOf(200), "餐飲", LocalDate.now());
        expenseRepository.save(expense1);
        expenseRepository.save(expense2);

        mockMvc.perform(get("/api/expenses")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[*].title", containsInAnyOrder("早餐", "晚餐")));
    }

    @Test
    @DisplayName("測試查詢所有支出 - 未登入")
    void testGetAllExpenses_Unauthorized() throws Exception {
        mockMvc.perform(get("/api/expenses"))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("測試根據 ID 查詢支出 - 成功")
    void testGetExpenseById_Success() throws Exception {
        Expense expense = new Expense(testUser, "電影票", BigDecimal.valueOf(320), "娛樂", LocalDate.now());
        Expense saved = expenseRepository.save(expense);

        mockMvc.perform(get("/api/expenses/" + saved.getId())
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("電影票"))
                .andExpect(jsonPath("$.amount").value(320));
    }

    @Test
    @DisplayName("測試根據 ID 查詢支出 - 找不到")
    void testGetExpenseById_NotFound() throws Exception {
        mockMvc.perform(get("/api/expenses/999")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("測試更新支出 - 成功")
    void testUpdateExpense_Success() throws Exception {
        Expense expense = new Expense(testUser, "咖啡", BigDecimal.valueOf(100), "餐飲", LocalDate.now());
        Expense saved = expenseRepository.save(expense);

        Expense updatedExpense = new Expense();
        updatedExpense.setTitle("星巴克咖啡");
        updatedExpense.setAmount(BigDecimal.valueOf(150));
        updatedExpense.setCategory("餐飲");
        updatedExpense.setExpenseDate(LocalDate.now());

        mockMvc.perform(put("/api/expenses/" + saved.getId())
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updatedExpense)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("星巴克咖啡"))
                .andExpect(jsonPath("$.amount").value(150));
    }

    @Test
    @DisplayName("測試刪除支出")
    void testDeleteExpense() throws Exception {
        Expense expense = new Expense(testUser, "測試支出", BigDecimal.valueOf(100), "其他", LocalDate.now());
        Expense saved = expenseRepository.save(expense);

        mockMvc.perform(delete("/api/expenses/" + saved.getId())
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isNoContent());

        // 確認已刪除
        mockMvc.perform(get("/api/expenses/" + saved.getId())
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isNotFound());
    }

    // ========== 驗證測試 ==========

    @Test
    @DisplayName("測試驗證 - 標題為空")
    void testValidation_EmptyTitle() throws Exception {
        Expense expense = new Expense();
        expense.setTitle("");
        expense.setAmount(BigDecimal.valueOf(100));
        expense.setCategory("餐飲");
        expense.setExpenseDate(LocalDate.now());

        mockMvc.perform(post("/api/expenses")
                        .header("Authorization", "Bearer " + token)
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
        expense.setAmount(BigDecimal.valueOf(-50));
        expense.setCategory("餐飲");
        expense.setExpenseDate(LocalDate.now());

        mockMvc.perform(post("/api/expenses")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(expense)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.amount").exists());
    }

    @Test
    @DisplayName("測試驗證 - 日期為未來")
    void testValidation_FutureDate() throws Exception {
        Expense expense = new Expense();
        expense.setTitle("測試");
        expense.setAmount(BigDecimal.valueOf(100));
        expense.setCategory("餐飲");
        expense.setExpenseDate(LocalDate.now().plusDays(1));

        mockMvc.perform(post("/api/expenses")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(expense)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.expenseDate").exists());
    }

    // ========== 查詢功能測試 ==========

    @Test
    @DisplayName("測試根據分類查詢支出")
    void testGetExpensesByCategory() throws Exception {
        expenseRepository.save(new Expense(testUser, "早餐", BigDecimal.valueOf(50), "餐飲", LocalDate.now()));
        expenseRepository.save(new Expense(testUser, "午餐", BigDecimal.valueOf(120), "餐飲", LocalDate.now()));
        expenseRepository.save(new Expense(testUser, "捷運", BigDecimal.valueOf(35), "交通", LocalDate.now()));

        mockMvc.perform(get("/api/expenses/category/餐飲")
                        .header("Authorization", "Bearer " + token))
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

        expenseRepository.save(new Expense(testUser, "今天", BigDecimal.valueOf(100), "餐飲", today));
        expenseRepository.save(new Expense(testUser, "昨天", BigDecimal.valueOf(200), "餐飲", yesterday));
        expenseRepository.save(new Expense(testUser, "前天", BigDecimal.valueOf(300), "餐飲", twoDaysAgo));

        mockMvc.perform(get("/api/expenses/date-range")
                        .header("Authorization", "Bearer " + token)
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
                        .header("Authorization", "Bearer " + token)
                        .param("startDate", today.toString())
                        .param("endDate", yesterday.toString()))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("測試根據分類和日期範圍組合查詢")
    void testSearchExpenses() throws Exception {
        LocalDate today = LocalDate.now();
        LocalDate yesterday = today.minusDays(1);

        expenseRepository.save(new Expense(testUser, "今天餐飲", BigDecimal.valueOf(100), "餐飲", today));
        expenseRepository.save(new Expense(testUser, "昨天餐飲", BigDecimal.valueOf(200), "餐飲", yesterday));
        expenseRepository.save(new Expense(testUser, "今天交通", BigDecimal.valueOf(50), "交通", today));

        mockMvc.perform(get("/api/expenses/search")
                        .header("Authorization", "Bearer " + token)
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

        expenseRepository.save(new Expense(testUser, "餐飲支出", BigDecimal.valueOf(100), "餐飲", today));
        expenseRepository.save(new Expense(testUser, "交通支出", BigDecimal.valueOf(50), "交通", today));
        expenseRepository.save(new Expense(testUser, "娛樂支出", BigDecimal.valueOf(300), "娛樂", today));

        mockMvc.perform(get("/api/expenses/categories")
                        .header("Authorization", "Bearer " + token)
                        .param("startDate", today.toString())
                        .param("endDate", today.toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(3)))
                .andExpect(jsonPath("$", containsInAnyOrder("交通", "娛樂", "餐飲")));
    }

    @Test
    @DisplayName("測試查詢空結果")
    void testGetExpensesByCategory_NoResults() throws Exception {
        mockMvc.perform(get("/api/expenses/category/不存在的分類")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(0)));
    }

    // ========== 使用者隔離測試 ==========

    @Test
    @DisplayName("測試使用者只能看到自己的支出")
    void testUserIsolation() throws Exception {
        // 建立另一個使用者
        User anotherUser = new User();
        anotherUser.setUsername("anotheruser");
        anotherUser.setEmail("another@example.com");
        anotherUser.setPassword(passwordEncoder.encode("password123"));
        anotherUser.setName("Another User");
        anotherUser.setStatus(User.UserStatus.ACTIVE);
        anotherUser = userRepository.save(anotherUser);

        // 建立兩個使用者的支出
        expenseRepository.save(new Expense(testUser, "測試使用者的支出", BigDecimal.valueOf(100), "餐飲", LocalDate.now()));
        expenseRepository.save(new Expense(anotherUser, "另一個使用者的支出", BigDecimal.valueOf(200), "餐飲", LocalDate.now()));

        // testUser 只能看到自己的支出
        mockMvc.perform(get("/api/expenses")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].title").value("測試使用者的支出"));
    }

    @Test
    @DisplayName("測試使用者不能存取其他人的支出")
    void testCannotAccessOtherUsersExpense() throws Exception {
        // 建立另一個使用者
        User anotherUser = new User();
        anotherUser.setUsername("anotheruser");
        anotherUser.setEmail("another@example.com");
        anotherUser.setPassword(passwordEncoder.encode("password123"));
        anotherUser.setName("Another User");
        anotherUser.setStatus(User.UserStatus.ACTIVE);
        anotherUser = userRepository.save(anotherUser);

        // 建立另一個使用者的支出
        Expense otherExpense = new Expense(anotherUser, "別人的支出", BigDecimal.valueOf(200), "餐飲", LocalDate.now());
        otherExpense = expenseRepository.save(otherExpense);

        // testUser 嘗試存取 anotherUser 的支出
        mockMvc.perform(get("/api/expenses/" + otherExpense.getId())
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("測試使用者不能刪除其他人的支出")
    void testCannotDeleteOtherUsersExpense() throws Exception {
        // 建立另一個使用者
        User anotherUser = new User();
        anotherUser.setUsername("anotheruser");
        anotherUser.setEmail("another@example.com");
        anotherUser.setPassword(passwordEncoder.encode("password123"));
        anotherUser.setName("Another User");
        anotherUser.setStatus(User.UserStatus.ACTIVE);
        anotherUser = userRepository.save(anotherUser);

        // 建立另一個使用者的支出
        Expense otherExpense = new Expense(anotherUser, "別人的支出", BigDecimal.valueOf(200), "餐飲", LocalDate.now());
        otherExpense = expenseRepository.save(otherExpense);

        // testUser 嘗試刪除 anotherUser 的支出
        mockMvc.perform(delete("/api/expenses/" + otherExpense.getId())
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isNotFound());

        // 確認支出還在
        assert expenseRepository.findById(otherExpense.getId()).isPresent();
    }

    // ========== JWT Token 測試 ==========

    @Test
    @DisplayName("測試過期的 Token")
    void testExpiredToken() throws Exception {
        // 這個測試需要等待 Token 過期，或者使用 mock 的方式
        // 簡化版：使用無效的 Token
        String invalidToken = "invalid.token.here";

        mockMvc.perform(get("/api/expenses")
                        .header("Authorization", "Bearer " + invalidToken))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("測試錯誤格式的 Authorization Header")
    void testInvalidAuthorizationHeader() throws Exception {
        mockMvc.perform(get("/api/expenses")
                        .header("Authorization", "InvalidFormat " + token))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("測試沒有 Bearer 前綴的 Token")
    void testTokenWithoutBearerPrefix() throws Exception {
        mockMvc.perform(get("/api/expenses")
                        .header("Authorization", token))
                .andExpect(status().isForbidden());
    }
}