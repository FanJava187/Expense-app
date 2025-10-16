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

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
@ContextConfiguration(initializers = DotenvTestConfig.class)
public class ChartControllerTest {

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
        testUser.setUsername("charttest");
        testUser.setEmail("charttest@example.com");
        testUser.setPassword(passwordEncoder.encode("password123"));
        testUser.setName("Chart Test User");
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

        // 8月份的測試資料
        expenseRepository.save(new Expense(testUser, "午餐", BigDecimal.valueOf(100), "餐飲", LocalDate.of(2025, 8, 10)));
    }

    @Test
    @DisplayName("測試每日趨勢圖")
    void testGetDailyTrend() throws Exception {
        mockMvc.perform(get("/api/charts/trend/daily")
                        .param("year", "2025")
                        .param("month", "10")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(greaterThan(0))))
                .andExpect(jsonPath("$[0].period").value("2025-10-01"))
                .andExpect(jsonPath("$[0].amount").value(230.0))  // 80 + 150
                .andExpect(jsonPath("$[0].count").value(2))
                .andExpect(jsonPath("$[1].period").value("2025-10-02"))
                .andExpect(jsonPath("$[1].amount").value(30.0))
                .andExpect(jsonPath("$[1].count").value(1));
    }

    @Test
    @DisplayName("測試每日趨勢圖 - 包含無資料的日期")
    void testGetDailyTrend_IncludesZeroDays() throws Exception {
        mockMvc.perform(get("/api/charts/trend/daily")
                        .param("year", "2025")
                        .param("month", "10")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[3].period").value("2025-10-04"))
                .andExpect(jsonPath("$[3].amount").value(0.0))  // 10/4 無資料
                .andExpect(jsonPath("$[3].count").value(0));
    }

    @Test
    @DisplayName("測試月度趨勢圖")
    void testGetMonthlyTrend() throws Exception {
        mockMvc.perform(get("/api/charts/trend/monthly")
                        .param("year", "2025")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(12)))  // 應該包含全年 12 個月
                .andExpect(jsonPath("$[7].period").value("2025-08"))  // 8月（索引7）
                .andExpect(jsonPath("$[7].amount").value(100.0))
                .andExpect(jsonPath("$[7].count").value(1))
                .andExpect(jsonPath("$[8].period").value("2025-09"))  // 9月
                .andExpect(jsonPath("$[8].amount").value(300.0))
                .andExpect(jsonPath("$[8].count").value(2))
                .andExpect(jsonPath("$[9].period").value("2025-10"))  // 10月
                .andExpect(jsonPath("$[9].amount").value(1245.0));
    }

    @Test
    @DisplayName("測試月度趨勢圖 - 包含無資料的月份")
    void testGetMonthlyTrend_IncludesZeroMonths() throws Exception {
        mockMvc.perform(get("/api/charts/trend/monthly")
                        .param("year", "2025")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].period").value("2025-01"))
                .andExpect(jsonPath("$[0].amount").value(0.0))  // 1月無資料
                .andExpect(jsonPath("$[0].count").value(0));
    }

    @Test
    @DisplayName("測試分類圓餅圖")
    void testGetCategoryPieChart() throws Exception {
        mockMvc.perform(get("/api/charts/pie/category")
                        .param("startDate", "2025-10-01")
                        .param("endDate", "2025-10-31")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(4)))  // 4個分類
                // 第一個應該是教育(450)或餐飲(430),按金額降序排列
                .andExpect(jsonPath("$[0].count").isNumber());
    }

    @Test
    @DisplayName("測試分類圓餅圖 - 無資料")
    void testGetCategoryPieChart_NoData() throws Exception {
        mockMvc.perform(get("/api/charts/pie/category")
                        .param("startDate", "2025-11-01")
                        .param("endDate", "2025-11-30")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(0)));
    }

    @Test
    @DisplayName("測試月度比較圖")
    void testGetMonthlyComparison() throws Exception {
        mockMvc.perform(get("/api/charts/comparison/monthly")
                        .param("months", "3")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.labels", hasSize(3)))
                .andExpect(jsonPath("$.amounts", hasSize(3)))
                .andExpect(jsonPath("$.counts", hasSize(3)));
    }

    @Test
    @DisplayName("測試月度比較圖 - 預設 6 個月")
    void testGetMonthlyComparison_Default() throws Exception {
        mockMvc.perform(get("/api/charts/comparison/monthly")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.labels", hasSize(6)))
                .andExpect(jsonPath("$.amounts", hasSize(6)))
                .andExpect(jsonPath("$.counts", hasSize(6)));
    }

    @Test
    @DisplayName("測試分類比較圖")
    void testGetCategoryComparison() throws Exception {
        mockMvc.perform(get("/api/charts/comparison/category")
                        .param("startDate", "2025-10-01")
                        .param("endDate", "2025-10-31")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.labels", hasSize(4)))  // 4個分類
                .andExpect(jsonPath("$.amounts", hasSize(4)))
                .andExpect(jsonPath("$.counts", hasSize(4)))
                .andExpect(jsonPath("$.labels[0]").value("教育"))  // 按金額降序
                .andExpect(jsonPath("$.amounts[0]").value(450.0))
                .andExpect(jsonPath("$.counts[0]").value(1));
    }

    @Test
    @DisplayName("測試分類比較圖 - 無資料")
    void testGetCategoryComparison_NoData() throws Exception {
        mockMvc.perform(get("/api/charts/comparison/category")
                        .param("startDate", "2025-11-01")
                        .param("endDate", "2025-11-30")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.labels", hasSize(0)))
                .andExpect(jsonPath("$.amounts", hasSize(0)))
                .andExpect(jsonPath("$.counts", hasSize(0)));
    }

    @Test
    @DisplayName("測試 Top 排行榜")
    void testGetTopExpenses() throws Exception {
        mockMvc.perform(get("/api/charts/top-expenses")
                        .param("startDate", "2025-10-01")
                        .param("endDate", "2025-10-31")
                        .param("limit", "3")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(3)))
                .andExpect(jsonPath("$[0].rank").value(1))
                .andExpect(jsonPath("$[0].title").value("書籍"))
                .andExpect(jsonPath("$[0].amount").value(450.0))
                .andExpect(jsonPath("$[0].category").value("教育"))
                .andExpect(jsonPath("$[1].rank").value(2))
                .andExpect(jsonPath("$[1].title").value("電影"))
                .andExpect(jsonPath("$[1].amount").value(320.0));
    }

    @Test
    @DisplayName("測試 Top 排行榜 - 預設 10 筆")
    void testGetTopExpenses_Default() throws Exception {
        mockMvc.perform(get("/api/charts/top-expenses")
                        .param("startDate", "2025-10-01")
                        .param("endDate", "2025-10-31")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(7)));  // 10月只有7筆資料
    }

    @Test
    @DisplayName("測試 Top 排行榜 - 無資料")
    void testGetTopExpenses_NoData() throws Exception {
        mockMvc.perform(get("/api/charts/top-expenses")
                        .param("startDate", "2025-11-01")
                        .param("endDate", "2025-11-30")
                        .param("limit", "10")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(0)));
    }

    @Test
    @DisplayName("測試未登入存取圖表 API")
    void testChart_Unauthorized() throws Exception {
        mockMvc.perform(get("/api/charts/trend/daily")
                        .param("year", "2025")
                        .param("month", "10"))
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

        // 測試原使用者的圖表不受影響
        mockMvc.perform(get("/api/charts/pie/category")
                        .param("startDate", "2025-10-01")
                        .param("endDate", "2025-10-31")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(4)))  // 不包含「測試」分類
                .andExpect(jsonPath("$[*].label", not(hasItem("測試"))));
    }

    @Test
    @DisplayName("測試圖表資料排序 - 分類圓餅圖按金額降序")
    void testCategoryPieChart_SortedByAmount() throws Exception {
        mockMvc.perform(get("/api/charts/pie/category")
                        .param("startDate", "2025-10-01")
                        .param("endDate", "2025-10-31")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(4)))
                // 驗證第一個分類的金額最高（教育450）
                .andExpect(jsonPath("$[0].label").value("教育"))
                .andExpect(jsonPath("$[0].value").value(450));
    }
}
