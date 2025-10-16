package com.example.expenseapp.controller;

import com.example.expenseapp.config.DotenvTestConfig;
import com.example.expenseapp.model.Budget;
import com.example.expenseapp.model.Expense;
import com.example.expenseapp.model.User;
import com.example.expenseapp.repository.BudgetRepository;
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
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
@ContextConfiguration(initializers = DotenvTestConfig.class)
public class BudgetControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private BudgetRepository budgetRepository;

    @Autowired
    private ExpenseRepository expenseRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtService jwtService;

    @Autowired
    private UserDetailsService userDetailsService;

    @Autowired
    private ObjectMapper objectMapper;

    private User testUser;
    private String token;

    @BeforeEach
    void setUp() {
        // 清理資料
        budgetRepository.deleteAll();
        expenseRepository.deleteAll();
        userRepository.deleteAll();

        // 建立測試使用者
        testUser = new User();
        testUser.setUsername("budgettest");
        testUser.setEmail("budgettest@example.com");
        testUser.setPassword(passwordEncoder.encode("password123"));
        testUser.setName("Budget Test User");
        testUser.setStatus(User.UserStatus.ACTIVE);
        testUser = userRepository.save(testUser);

        // 生成 JWT Token
        UserDetails userDetails = userDetailsService.loadUserByUsername(testUser.getUsername());
        token = jwtService.generateToken(userDetails);
    }

    @Test
    @DisplayName("測試建立月度預算")
    void testCreateMonthlyBudget() throws Exception {
        Map<String, Object> request = new HashMap<>();
        request.put("budgetType", "MONTHLY");
        request.put("amount", 10000.00);
        request.put("year", 2025);
        request.put("month", 10);

        mockMvc.perform(post("/api/budgets")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.budgetType").value("MONTHLY"))
                .andExpect(jsonPath("$.amount").value(10000.0))
                .andExpect(jsonPath("$.year").value(2025))
                .andExpect(jsonPath("$.month").value(10))
                .andExpect(jsonPath("$.category").isEmpty())
                .andExpect(jsonPath("$.spent").value(0.0))
                .andExpect(jsonPath("$.remaining").value(10000.0))
                .andExpect(jsonPath("$.percentage").value(0.0));
    }

    @Test
    @DisplayName("測試建立分類預算")
    void testCreateCategoryBudget() throws Exception {
        Map<String, Object> request = new HashMap<>();
        request.put("budgetType", "CATEGORY");
        request.put("category", "餐飲");
        request.put("amount", 3000.00);
        request.put("year", 2025);
        request.put("month", 10);

        mockMvc.perform(post("/api/budgets")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.budgetType").value("CATEGORY"))
                .andExpect(jsonPath("$.category").value("餐飲"))
                .andExpect(jsonPath("$.amount").value(3000.0))
                .andExpect(jsonPath("$.spent").value(0.0))
                .andExpect(jsonPath("$.remaining").value(3000.0));
    }

    @Test
    @DisplayName("測試建立預算 - 缺少必要欄位")
    void testCreateBudget_MissingFields() throws Exception {
        Map<String, Object> request = new HashMap<>();
        request.put("budgetType", "MONTHLY");
        // 缺少 amount, year, month

        mockMvc.perform(post("/api/budgets")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("測試建立預算 - 金額為負數")
    void testCreateBudget_NegativeAmount() throws Exception {
        Map<String, Object> request = new HashMap<>();
        request.put("budgetType", "MONTHLY");
        request.put("amount", -100.00);
        request.put("year", 2025);
        request.put("month", 10);

        mockMvc.perform(post("/api/budgets")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("測試建立預算 - 重複的預算")
    void testCreateBudget_Duplicate() throws Exception {
        // 先建立一個預算
        Budget budget = new Budget();
        budget.setUser(testUser);
        budget.setBudgetType(Budget.BudgetType.MONTHLY);
        budget.setAmount(BigDecimal.valueOf(10000));
        budget.setYear(2025);
        budget.setMonth(10);
        budgetRepository.save(budget);

        // 嘗試建立相同的預算
        Map<String, Object> request = new HashMap<>();
        request.put("budgetType", "MONTHLY");
        request.put("amount", 12000.00);
        request.put("year", 2025);
        request.put("month", 10);

        mockMvc.perform(post("/api/budgets")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict());
    }

    @Test
    @DisplayName("測試查詢預算 - 根據 ID")
    void testGetBudgetById() throws Exception {
        // 建立測試預算
        Budget budget = new Budget();
        budget.setUser(testUser);
        budget.setBudgetType(Budget.BudgetType.MONTHLY);
        budget.setAmount(BigDecimal.valueOf(10000));
        budget.setYear(2025);
        budget.setMonth(10);
        budget = budgetRepository.save(budget);

        mockMvc.perform(get("/api/budgets/" + budget.getId())
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(budget.getId()))
                .andExpect(jsonPath("$.budgetType").value("MONTHLY"))
                .andExpect(jsonPath("$.amount").value(10000.0));
    }

    @Test
    @DisplayName("測試查詢預算 - 不存在的 ID")
    void testGetBudgetById_NotFound() throws Exception {
        mockMvc.perform(get("/api/budgets/99999")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("測試查詢預算 - 根據年月")
    void testGetBudgetsByYearAndMonth() throws Exception {
        // 建立多個預算
        Budget monthlyBudget = new Budget();
        monthlyBudget.setUser(testUser);
        monthlyBudget.setBudgetType(Budget.BudgetType.MONTHLY);
        monthlyBudget.setAmount(BigDecimal.valueOf(10000));
        monthlyBudget.setYear(2025);
        monthlyBudget.setMonth(10);
        budgetRepository.save(monthlyBudget);

        Budget categoryBudget = new Budget();
        categoryBudget.setUser(testUser);
        categoryBudget.setBudgetType(Budget.BudgetType.CATEGORY);
        categoryBudget.setCategory("餐飲");
        categoryBudget.setAmount(BigDecimal.valueOf(3000));
        categoryBudget.setYear(2025);
        categoryBudget.setMonth(10);
        budgetRepository.save(categoryBudget);

        mockMvc.perform(get("/api/budgets")
                        .param("year", "2025")
                        .param("month", "10")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].budgetType").value("MONTHLY"))
                .andExpect(jsonPath("$[1].budgetType").value("CATEGORY"));
    }

    @Test
    @DisplayName("測試查詢當月預算")
    void testGetCurrentMonthBudgets() throws Exception {
        // 建立當月預算
        LocalDate now = LocalDate.now();
        Budget budget = new Budget();
        budget.setUser(testUser);
        budget.setBudgetType(Budget.BudgetType.MONTHLY);
        budget.setAmount(BigDecimal.valueOf(10000));
        budget.setYear(now.getYear());
        budget.setMonth(now.getMonthValue());
        budgetRepository.save(budget);

        mockMvc.perform(get("/api/budgets/current")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)));
    }

    @Test
    @DisplayName("測試預算自動計算 - 已使用金額")
    void testBudgetCalculation_Spent() throws Exception {
        // 建立預算
        Budget budget = new Budget();
        budget.setUser(testUser);
        budget.setBudgetType(Budget.BudgetType.MONTHLY);
        budget.setAmount(BigDecimal.valueOf(10000));
        budget.setYear(2025);
        budget.setMonth(10);
        budget = budgetRepository.save(budget);

        // 新增支出
        expenseRepository.save(new Expense(testUser, "午餐", BigDecimal.valueOf(150), "餐飲", LocalDate.of(2025, 10, 15)));
        expenseRepository.save(new Expense(testUser, "晚餐", BigDecimal.valueOf(200), "餐飲", LocalDate.of(2025, 10, 16)));
        expenseRepository.save(new Expense(testUser, "捷運", BigDecimal.valueOf(50), "交通", LocalDate.of(2025, 10, 17)));

        mockMvc.perform(get("/api/budgets/" + budget.getId())
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.spent").value(400.0))  // 150 + 200 + 50
                .andExpect(jsonPath("$.remaining").value(9600.0))  // 10000 - 400
                .andExpect(jsonPath("$.percentage").value(4.0));  // 400/10000*100
    }

    @Test
    @DisplayName("測試分類預算自動計算")
    void testCategoryBudgetCalculation() throws Exception {
        // 建立分類預算
        Budget budget = new Budget();
        budget.setUser(testUser);
        budget.setBudgetType(Budget.BudgetType.CATEGORY);
        budget.setCategory("餐飲");
        budget.setAmount(BigDecimal.valueOf(3000));
        budget.setYear(2025);
        budget.setMonth(10);
        budget = budgetRepository.save(budget);

        // 新增支出（只有餐飲分類應該被計算）
        expenseRepository.save(new Expense(testUser, "午餐", BigDecimal.valueOf(150), "餐飲", LocalDate.of(2025, 10, 15)));
        expenseRepository.save(new Expense(testUser, "晚餐", BigDecimal.valueOf(200), "餐飲", LocalDate.of(2025, 10, 16)));
        expenseRepository.save(new Expense(testUser, "捷運", BigDecimal.valueOf(50), "交通", LocalDate.of(2025, 10, 17)));

        mockMvc.perform(get("/api/budgets/" + budget.getId())
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.spent").value(350.0))  // 只計算餐飲：150 + 200
                .andExpect(jsonPath("$.remaining").value(2650.0))
                .andExpect(jsonPath("$.percentage").value(closeTo(11.67, 0.01)));
    }

    @Test
    @DisplayName("測試更新預算")
    void testUpdateBudget() throws Exception {
        // 建立預算
        Budget budget = new Budget();
        budget.setUser(testUser);
        budget.setBudgetType(Budget.BudgetType.MONTHLY);
        budget.setAmount(BigDecimal.valueOf(10000));
        budget.setYear(2025);
        budget.setMonth(10);
        budget = budgetRepository.save(budget);

        // 更新預算
        Map<String, Object> request = new HashMap<>();
        request.put("budgetType", "MONTHLY");
        request.put("amount", 12000.00);
        request.put("year", 2025);
        request.put("month", 10);

        mockMvc.perform(put("/api/budgets/" + budget.getId())
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.amount").value(12000.0));
    }

    @Test
    @DisplayName("測試刪除預算")
    void testDeleteBudget() throws Exception {
        // 建立預算
        Budget budget = new Budget();
        budget.setUser(testUser);
        budget.setBudgetType(Budget.BudgetType.MONTHLY);
        budget.setAmount(BigDecimal.valueOf(10000));
        budget.setYear(2025);
        budget.setMonth(10);
        budget = budgetRepository.save(budget);

        mockMvc.perform(delete("/api/budgets/" + budget.getId())
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isNoContent());

        // 驗證已刪除
        mockMvc.perform(get("/api/budgets/" + budget.getId())
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("測試未登入存取預算 API")
    void testBudget_Unauthorized() throws Exception {
        mockMvc.perform(get("/api/budgets/current"))
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

        // 為另一個使用者建立預算
        Budget anotherBudget = new Budget();
        anotherBudget.setUser(anotherUser);
        anotherBudget.setBudgetType(Budget.BudgetType.MONTHLY);
        anotherBudget.setAmount(BigDecimal.valueOf(5000));
        anotherBudget.setYear(2025);
        anotherBudget.setMonth(10);
        anotherBudget = budgetRepository.save(anotherBudget);

        // 測試原使用者無法存取另一個使用者的預算
        mockMvc.perform(get("/api/budgets/" + anotherBudget.getId())
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isForbidden());

        // 測試原使用者的當月預算查詢不包含另一個使用者的預算
        mockMvc.perform(get("/api/budgets")
                        .param("year", "2025")
                        .param("month", "10")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(0)));  // 原使用者沒有預算
    }
}
