package com.example.expenseapp.service;

import com.example.expenseapp.config.DotenvTestConfig;
import com.example.expenseapp.model.Expense;
import com.example.expenseapp.model.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;

import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ContextConfiguration(initializers = DotenvTestConfig.class)
public class CsvExportServiceTest {

    @Autowired
    private CsvExportService csvExportService;

    private User testUser;
    private List<Expense> testExpenses;

    @BeforeEach
    void setUp() {
        // 建立測試使用者
        testUser = new User();
        testUser.setId(1L);
        testUser.setUsername("testuser");
        testUser.setEmail("test@example.com");
        testUser.setName("Test User");

        // 建立測試支出資料
        testExpenses = new ArrayList<>();
        testExpenses.add(new Expense(testUser, "午餐", BigDecimal.valueOf(120.50), "餐飲", LocalDate.of(2025, 10, 15)));
        testExpenses.add(new Expense(testUser, "捷運", BigDecimal.valueOf(30.00), "交通", LocalDate.of(2025, 10, 15)));
        testExpenses.add(new Expense(testUser, "書籍", BigDecimal.valueOf(450.00), "教育", LocalDate.of(2025, 10, 16)));

        // 設定 ID（模擬資料庫保存後的狀態）
        for (int i = 0; i < testExpenses.size(); i++) {
            testExpenses.get(i).setId((long) (i + 1));
        }
    }

    @Test
    @DisplayName("測試 CSV 匯出基本功能")
    void testExportToCsv_Basic() throws Exception {
        // 匯出 CSV
        byte[] csvBytes = csvExportService.exportExpensesToCsv(testExpenses);
        String csv = new String(csvBytes, StandardCharsets.UTF_8);

        // 驗證包含 UTF-8 BOM
        assertThat(csv).startsWith("\uFEFF");

        // 驗證標題列
        assertThat(csv).contains("ID,標題,金額,分類,日期");

        // 驗證資料列
        assertThat(csv).contains("1,\"午餐\",120.50,\"餐飲\",2025-10-15");
        assertThat(csv).contains("2,\"捷運\",30.00,\"交通\",2025-10-15");
        assertThat(csv).contains("3,\"書籍\",450.00,\"教育\",2025-10-16");
    }

    @Test
    @DisplayName("測試 CSV 匯出 - 空清單")
    void testExportToCsv_EmptyList() throws Exception {
        List<Expense> emptyList = new ArrayList<>();
        byte[] csvBytes = csvExportService.exportExpensesToCsv(emptyList);
        String csv = new String(csvBytes, StandardCharsets.UTF_8);

        // 應該只包含標題列
        assertThat(csv).startsWith("\uFEFF");
        assertThat(csv).contains("ID,標題,金額,分類,日期");
        assertThat(csv.split("\n")).hasSize(2);  // BOM + 標題列 + 換行
    }

    @Test
    @DisplayName("測試 CSV 匯出 - 特殊字元處理（逗號）")
    void testExportToCsv_SpecialCharacters_Comma() throws Exception {
        // 建立包含逗號的支出
        Expense expenseWithComma = new Expense(testUser, "午餐,含飲料", BigDecimal.valueOf(150.00), "餐飲", LocalDate.of(2025, 10, 15));
        expenseWithComma.setId(99L);

        List<Expense> expenses = List.of(expenseWithComma);
        byte[] csvBytes = csvExportService.exportExpensesToCsv(expenses);
        String csv = new String(csvBytes, StandardCharsets.UTF_8);

        // 包含逗號的欄位應該被引號包圍
        assertThat(csv).contains("99,\"午餐,含飲料\",150.00,\"餐飲\",2025-10-15");
    }

    @Test
    @DisplayName("測試 CSV 匯出 - 特殊字元處理（引號）")
    void testExportToCsv_SpecialCharacters_Quote() throws Exception {
        // 建立包含引號的支出
        Expense expenseWithQuote = new Expense(testUser, "\"特價\"商品", BigDecimal.valueOf(200.00), "購物", LocalDate.of(2025, 10, 15));
        expenseWithQuote.setId(88L);

        List<Expense> expenses = List.of(expenseWithQuote);
        byte[] csvBytes = csvExportService.exportExpensesToCsv(expenses);
        String csv = new String(csvBytes, StandardCharsets.UTF_8);

        // 引號應該被轉義為雙引號
        assertThat(csv).contains("88,\"\"\"特價\"\"商品\",200.00,\"購物\",2025-10-15");
    }

    @Test
    @DisplayName("測試 CSV 匯出 - 特殊字元處理（換行符）")
    void testExportToCsv_SpecialCharacters_Newline() throws Exception {
        // 建立包含換行符的支出
        Expense expenseWithNewline = new Expense(testUser, "多行\n描述", BigDecimal.valueOf(100.00), "其他", LocalDate.of(2025, 10, 15));
        expenseWithNewline.setId(77L);

        List<Expense> expenses = List.of(expenseWithNewline);
        byte[] csvBytes = csvExportService.exportExpensesToCsv(expenses);
        String csv = new String(csvBytes, StandardCharsets.UTF_8);

        // 包含換行符的欄位應該被引號包圍
        assertThat(csv).contains("77,\"多行\n描述\",100.00,\"其他\",2025-10-15");
    }

    @Test
    @DisplayName("測試 CSV 匯出 - 中文字元正確編碼")
    void testExportToCsv_ChineseCharacters() throws Exception {
        byte[] bytes = csvExportService.exportExpensesToCsv(testExpenses);

        // 驗證包含 UTF-8 BOM (EF BB BF)
        assertThat(bytes[0]).isEqualTo((byte) 0xEF);
        assertThat(bytes[1]).isEqualTo((byte) 0xBB);
        assertThat(bytes[2]).isEqualTo((byte) 0xBF);

        // 驗證中文字元正確編碼
        String decoded = new String(bytes, StandardCharsets.UTF_8);
        assertThat(decoded).contains("餐飲");
        assertThat(decoded).contains("交通");
        assertThat(decoded).contains("教育");
    }

    @Test
    @DisplayName("測試 CSV 匯出 - 金額格式")
    void testExportToCsv_AmountFormat() throws Exception {
        // 測試不同的金額格式
        List<Expense> expenses = new ArrayList<>();
        expenses.add(new Expense(testUser, "整數金額", BigDecimal.valueOf(100), "測試", LocalDate.of(2025, 10, 15)));
        expenses.add(new Expense(testUser, "小數金額", BigDecimal.valueOf(99.99), "測試", LocalDate.of(2025, 10, 15)));
        expenses.add(new Expense(testUser, "單位小數", BigDecimal.valueOf(50.5), "測試", LocalDate.of(2025, 10, 15)));

        for (int i = 0; i < expenses.size(); i++) {
            expenses.get(i).setId((long) (i + 1));
        }

        byte[] csvBytes = csvExportService.exportExpensesToCsv(expenses);
        String csv = new String(csvBytes, StandardCharsets.UTF_8);

        // 驗證金額格式
        assertThat(csv).contains(",100.00,");  // 整數應顯示為 .00
        assertThat(csv).contains(",99.99,");
        assertThat(csv).contains(",50.50,");   // 單位小數應顯示為 .50
    }

    @Test
    @DisplayName("測試 CSV 匯出 - 日期格式")
    void testExportToCsv_DateFormat() throws Exception {
        List<Expense> expenses = new ArrayList<>();
        expenses.add(new Expense(testUser, "測試", BigDecimal.valueOf(100), "測試", LocalDate.of(2025, 1, 5)));
        expenses.add(new Expense(testUser, "測試", BigDecimal.valueOf(100), "測試", LocalDate.of(2025, 12, 31)));

        for (int i = 0; i < expenses.size(); i++) {
            expenses.get(i).setId((long) (i + 1));
        }

        byte[] csvBytes = csvExportService.exportExpensesToCsv(expenses);
        String csv = new String(csvBytes, StandardCharsets.UTF_8);

        // 驗證日期格式為 yyyy-MM-dd
        assertThat(csv).contains("2025-01-05");
        assertThat(csv).contains("2025-12-31");
    }

    @Test
    @DisplayName("測試 CSV 匯出 - 多筆資料順序")
    void testExportToCsv_MultipleExpenses_Order() throws Exception {
        byte[] csvBytes = csvExportService.exportExpensesToCsv(testExpenses);
        String csv = new String(csvBytes, StandardCharsets.UTF_8);

        String[] lines = csv.split("\n");

        // 驗證行數：BOM + 標題 + 3筆資料 = 4行（最後一行可能有空行）
        assertThat(lines.length).isGreaterThanOrEqualTo(4);

        // 驗證第一筆資料（索引1，因為索引0是標題）
        assertThat(lines[1]).contains("1,\"午餐\",120.50");

        // 驗證第二筆資料
        assertThat(lines[2]).contains("2,\"捷運\",30.00");

        // 驗證第三筆資料
        assertThat(lines[3]).contains("3,\"書籍\",450.00");
    }

    @Test
    @DisplayName("測試 CSV 匯出 - 大量資料")
    void testExportToCsv_LargeDataset() throws Exception {
        // 建立 1000 筆測試資料
        List<Expense> largeExpenses = new ArrayList<>();
        for (int i = 0; i < 1000; i++) {
            Expense expense = new Expense(testUser, "測試支出" + i, BigDecimal.valueOf(100.00), "測試", LocalDate.of(2025, 10, 15));
            expense.setId((long) i);
            largeExpenses.add(expense);
        }

        byte[] csvBytes = csvExportService.exportExpensesToCsv(largeExpenses);
        String csv = new String(csvBytes, StandardCharsets.UTF_8);

        // 驗證包含所有資料
        String[] lines = csv.split("\n");
        assertThat(lines.length).isGreaterThanOrEqualTo(1001);  // 標題 + 1000筆資料

        // 驗證 BOM 存在
        assertThat(csv).startsWith("\uFEFF");
    }
}
