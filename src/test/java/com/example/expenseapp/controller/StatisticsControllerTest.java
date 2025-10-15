package com.example.expenseapp.controller;

import com.example.expenseapp.config.DotenvTestConfig;
import com.example.expenseapp.model.Expense;
import com.example.expenseapp.model.User;
import com.example.expenseapp.repository.ExpenseRepository;
import com.example.expenseapp.repository.UserRepository;
import com.example.expenseapp.security.JwtService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.hamcrest.Matchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
@ContextConfiguration(initializers = DotenvTestConfig.class)
public class StatisticsControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ExpenseRepository expenseRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtService jwtService;

    @Autowired
    private UserDetailsService userDetailsService;

    private User testUser;
    private String token;

    @BeforeEach
    void setUp() {
        // 清理資料
        expenseRepository.deleteAll();
        userRepository.deleteAll();

        // 建立測試使用者
        testUser = new User();
        testUser.setUsername("stattest");
        testUser.setEmail("stattest@example.com");
        testUser.setPassword(passwordEncoder.encode("password123"));
        testUser.setName("Statistics Test User");
        testUser.setStatus(User.UserStatus.ACTIVE);
        testUser = userRepository.save(testUser);

        // 生成 JWT Token
        UserDetails userDetails = userDetailsService.loadUserByUsername(testUser.getUsername());
        token = jwtService.generateToken(userDetails);

        // 建立測試資料
        createTestExpenses();
    }

    private void createTestExpenses() {
        // 10月份的測試資料
        expenseRepository.save(new Expense(testUser, "早餐", BigDecimal.valueOf(80), "餐飲", LocalDate.of(2025, 10, 1)));
        expenseRepository.save(new Expense(testUser, "午餐", BigDecimal.valueOf(150), "餐飲", LocalDate.of(2025, 10, 1)));
        expenseRepository.save(new Expense(testUser, "捷運", BigDecimal.valueOf(30), "交通", LocalDate.of(2025, 10, 2)));
        expenseRepository.save(new Expense(testUser, "晚餐", BigDecimal.valueOf(200), "餐飲", LocalDate.of(2025, 10, 3)));
        expenseRepository.save(new Expense(testUser, "電影", BigDecimal.valueOf(320), "娛樂", LocalDate.of(2025, 10, 5)));
        expenseRepository.save(new Expense(testUser, "公車", BigDecimal.valueOf(15), "交通", LocalDate.of(2025, 10, 5)));
        expenseRepository.save(new Expense(testUser, "書籍", BigDecimal.valueOf(450), "教育", LocalDate.of(2025, 10, 10)));

        // 9月份的測試資料
        expenseRepository.save(new Expense(testUser, "餐費", BigDecimal.valueOf(120), "餐飲", LocalDate.of(2025, 9, 15)));
        expenseRepository.save(new Expense(testUser, "計程車", BigDecimal.valueOf(180), "交通", LocalDate.of(2025, 9, 20)));
    }

    @Test
    @DisplayName("測試總覽統計")
    void testSummaryStatistics() throws Exception {
        mockMvc.perform(get("/api/statistics/summary")
                        .param("startDate", "2025-10-01")
                        .param("endDate", "2025-10-31")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalAmount").value(1245.0))  // 80+150+30+200+320+15+450
                .andExpect(jsonPath("$.totalCount").value(7))
                .andExpect(jsonPath("$.averageAmount").value(177.86))
                .andExpect(jsonPath("$.maxAmount").value(450.0))
                .andExpect(jsonPath("$.minAmount").value(15.0));
    }

    @Test
    @DisplayName("測試總覽統計 - 無資料")
    void testSummaryStatistics_NoData() throws Exception {
        mockMvc.perform(get("/api/statistics/summary")
                        .param("startDate", "2025-11-01")
                        .param("endDate", "2025-11-30")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalAmount").value(0))
                .andExpect(jsonPath("$.totalCount").value(0))
                .andExpect(jsonPath("$.averageAmount").value(0));
    }

    @Test
    @DisplayName("測試分類統計")
    void testCategoryStatistics() throws Exception {
        mockMvc.perform(get("/api/statistics/category")
                        .param("startDate", "2025-10-01")
                        .param("endDate", "2025-10-31")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(4)))  // 4個分類：餐飲、交通、娛樂、教育
                .andExpect(jsonPath("$[0].category").value("教育"))  // 按金額降序，教育最高(450)
                .andExpect(jsonPath("$[0].totalAmount").value(450.0))
                .andExpect(jsonPath("$[0].count").value(1))
                .andExpect(jsonPath("$[0].percentage").value(36.14))  // 450/1245*100
                .andExpect(jsonPath("$[1].category").value("餐飲"))  // 第二高：餐飲(430)
                .andExpect(jsonPath("$[1].totalAmount").value(430.0))
                .andExpect(jsonPath("$[1].count").value(3));
    }

    @Test
    @DisplayName("測試分類統計 - 無資料")
    void testCategoryStatistics_NoData() throws Exception {
        mockMvc.perform(get("/api/statistics/category")
                        .param("startDate", "2025-11-01")
                        .param("endDate", "2025-11-30")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(0)));
    }

    @Test
    @DisplayName("測試月度統計")
    void testMonthlyStatistics() throws Exception {
        mockMvc.perform(get("/api/statistics/monthly")
                        .param("year", "2025")
                        .param("month", "10")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(4)))  // 4天有資料：1, 2, 3, 5, 10
                .andExpect(jsonPath("$[0].period").value("2025-10-01"))
                .andExpect(jsonPath("$[0].totalAmount").value(230.0))  // 80+150
                .andExpect(jsonPath("$[0].count").value(2));
    }

    @Test
    @DisplayName("測試月度統計 - 無資料")
    void testMonthlyStatistics_NoData() throws Exception {
        mockMvc.perform(get("/api/statistics/monthly")
                        .param("year", "2025")
                        .param("month", "11")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(0)));
    }

    @Test
    @DisplayName("測試年度統計")
    void testYearlyStatistics() throws Exception {
        mockMvc.perform(get("/api/statistics/yearly")
                        .param("year", "2025")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))  // 2個月有資料：9月、10月
                .andExpect(jsonPath("$[0].period").value("2025-09"))
                .andExpect(jsonPath("$[0].totalAmount").value(300.0))  // 120+180
                .andExpect(jsonPath("$[0].count").value(2))
                .andExpect(jsonPath("$[1].period").value("2025-10"))
                .andExpect(jsonPath("$[1].totalAmount").value(1245.0));
    }

    @Test
    @DisplayName("測試年度統計 - 無資料")
    void testYearlyStatistics_NoData() throws Exception {
        mockMvc.perform(get("/api/statistics/yearly")
                        .param("year", "2024")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(0)));
    }

    @Test
    @DisplayName("測試當月統計")
    void testCurrentMonthStatistics() throws Exception {
        mockMvc.perform(get("/api/statistics/current-month")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk());
        // 結果會根據當前月份變化，所以只測試狀態碼
    }

    @Test
    @DisplayName("測試當年統計")
    void testCurrentYearStatistics() throws Exception {
        mockMvc.perform(get("/api/statistics/current-year")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk());
        // 結果會根據當前年份變化，所以只測試狀態碼
    }

    @Test
    @DisplayName("測試未登入存取統計 API")
    void testStatistics_Unauthorized() throws Exception {
        mockMvc.perform(get("/api/statistics/summary")
                        .param("startDate", "2025-10-01")
                        .param("endDate", "2025-10-31"))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("測試使用者資料隔離")
    void testUserIsolation() throws Exception {
        // 建立另一個使用者
        User anotherUser = new User();
        anotherUser.setUsername("another");
        anotherUser.setEmail("another@example.com");
        anotherUser.setPassword(passwordEncoder.encode("password123"));
        anotherUser.setName("Another User");
        anotherUser.setStatus(User.UserStatus.ACTIVE);
        anotherUser = userRepository.save(anotherUser);

        // 為另一個使用者新增支出
        expenseRepository.save(new Expense(anotherUser, "其他使用者支出", BigDecimal.valueOf(9999), "測試", LocalDate.of(2025, 10, 15)));

        // 測試原使用者的統計不受影響
        mockMvc.perform(get("/api/statistics/summary")
                        .param("startDate", "2025-10-01")
                        .param("endDate", "2025-10-31")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalAmount").value(1245.0))  // 不包含另一個使用者的9999
                .andExpect(jsonPath("$.totalCount").value(7));
    }
}
