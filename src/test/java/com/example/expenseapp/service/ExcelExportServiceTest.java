package com.example.expenseapp.service;

import com.example.expenseapp.config.DotenvTestConfig;
import com.example.expenseapp.model.Expense;
import com.example.expenseapp.model.User;
import com.example.expenseapp.repository.UserRepository;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.ContextConfiguration;

import java.io.ByteArrayInputStream;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ContextConfiguration(initializers = DotenvTestConfig.class)
public class ExcelExportServiceTest {

    @Autowired
    private ExcelExportService excelExportService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private com.example.expenseapp.repository.ExpenseRepository expenseRepository;

    private User testUser;
    private List<Expense> testExpenses;

    @BeforeEach
    void setUp() {
        // 建立測試使用者（使用唯一名稱避免衝突）
        testUser = new User();
        testUser.setUsername("exceltestuser" + System.currentTimeMillis());
        testUser.setEmail("exceltest" + System.currentTimeMillis() + "@example.com");
        testUser.setName("Excel Test User");
        testUser.setPassword("password");
        testUser.setStatus(User.UserStatus.ACTIVE);
        testUser = userRepository.save(testUser);

        // 設置 SecurityContext
        UsernamePasswordAuthenticationToken authentication =
            new UsernamePasswordAuthenticationToken(testUser.getUsername(), null, new ArrayList<>());
        SecurityContextHolder.getContext().setAuthentication(authentication);

        // 建立測試支出資料並保存到資料庫 (使用過去的日期以符合 @PastOrPresent 驗證)
        testExpenses = new ArrayList<>();
        testExpenses.add(expenseRepository.save(new Expense(testUser, "午餐", BigDecimal.valueOf(120.50), "餐飲", LocalDate.of(2025, 10, 10))));
        testExpenses.add(expenseRepository.save(new Expense(testUser, "捷運", BigDecimal.valueOf(30.00), "交通", LocalDate.of(2025, 10, 10))));
        testExpenses.add(expenseRepository.save(new Expense(testUser, "晚餐", BigDecimal.valueOf(200.00), "餐飲", LocalDate.of(2025, 10, 11))));
        testExpenses.add(expenseRepository.save(new Expense(testUser, "書籍", BigDecimal.valueOf(450.00), "教育", LocalDate.of(2025, 10, 12))));
    }

    @AfterEach
    void tearDown() {
        // 清理資料
        SecurityContextHolder.clearContext();
        if (testUser != null && testUser.getId() != null) {
            // Expenses will be cascaded deleted when user is deleted
            userRepository.deleteById(testUser.getId());
        }
    }

    @Test
    @DisplayName("測試 Excel 匯出基本功能")
    void testExportToExcel_Basic() throws Exception {
        // 匯出 Excel
        byte[] excelData = excelExportService.exportExpensesToExcel(testExpenses, LocalDate.of(2025, 10, 1), LocalDate.of(2025, 10, 31));

        // 驗證資料不為空
        assertThat(excelData).isNotNull();
        assertThat(excelData.length).isGreaterThan(0);

        // 解析 Excel 檔案
        try (Workbook workbook = new XSSFWorkbook(new ByteArrayInputStream(excelData))) {
            // 驗證工作表數量
            assertThat(workbook.getNumberOfSheets()).isEqualTo(3);

            // 驗證工作表名稱
            assertThat(workbook.getSheetName(0)).isEqualTo("支出明細");
            assertThat(workbook.getSheetName(1)).isEqualTo("統計摘要");
            assertThat(workbook.getSheetName(2)).isEqualTo("分類統計");
        }
    }

    @Test
    @DisplayName("測試 Excel 匯出 - 工作表1（支出明細）")
    void testExportToExcel_Sheet1_ExpenseDetails() throws Exception {
        byte[] excelData = excelExportService.exportExpensesToExcel(testExpenses, LocalDate.of(2025, 10, 1), LocalDate.of(2025, 10, 31));

        try (Workbook workbook = new XSSFWorkbook(new ByteArrayInputStream(excelData))) {
            Sheet sheet = workbook.getSheetAt(0);

            // 驗證標題列
            Row headerRow = sheet.getRow(0);
            assertThat(getCellValue(headerRow.getCell(0))).isEqualTo("ID");
            assertThat(getCellValue(headerRow.getCell(1))).isEqualTo("標題");
            assertThat(getCellValue(headerRow.getCell(2))).isEqualTo("金額");
            assertThat(getCellValue(headerRow.getCell(3))).isEqualTo("分類");
            assertThat(getCellValue(headerRow.getCell(4))).isEqualTo("日期");

            // 驗證資料列
            Row dataRow1 = sheet.getRow(1);
            assertThat(getCellValue(dataRow1.getCell(0))).isEqualTo(String.valueOf(testExpenses.get(0).getId()));
            assertThat(getCellValue(dataRow1.getCell(1))).isEqualTo("午餐");
            assertThat(dataRow1.getCell(2).getNumericCellValue()).isEqualTo(120.50);
            assertThat(getCellValue(dataRow1.getCell(3))).isEqualTo("餐飲");

            // 驗證總計列（最後一行）
            Row totalRow = sheet.getRow(5);  // 標題 + 4筆資料 + 總計
            assertThat(getCellValue(totalRow.getCell(1))).isEqualTo("總計");
            // 驗證總計使用公式
            assertThat(totalRow.getCell(2).getCellType()).isEqualTo(CellType.FORMULA);
        }
    }

    @Test
    @DisplayName("測試 Excel 匯出 - 工作表2（統計摘要）")
    void testExportToExcel_Sheet2_SummaryStatistics() throws Exception {
        byte[] excelData = excelExportService.exportExpensesToExcel(testExpenses, LocalDate.of(2025, 10, 1), LocalDate.of(2025, 10, 31));

        try (Workbook workbook = new XSSFWorkbook(new ByteArrayInputStream(excelData))) {
            Sheet sheet = workbook.getSheetAt(1);

            // 驗證統計期間（Row 1）
            assertThat(getCellValue(sheet.getRow(1).getCell(0))).isEqualTo("統計期間");

            // 驗證統計項目（從 Row 3 開始）
            assertThat(getCellValue(sheet.getRow(3).getCell(0))).isEqualTo("總支出金額");
            assertThat(getCellValue(sheet.getRow(4).getCell(0))).isEqualTo("總筆數");
            assertThat(getCellValue(sheet.getRow(5).getCell(0))).isEqualTo("平均金額");
            assertThat(getCellValue(sheet.getRow(6).getCell(0))).isEqualTo("最大金額");
            assertThat(getCellValue(sheet.getRow(7).getCell(0))).isEqualTo("最小金額");

            // 驗證統計值
            assertThat(sheet.getRow(3).getCell(1).getNumericCellValue()).isEqualTo(800.50);  // 120.50+30+200+450
            assertThat(getCellValue(sheet.getRow(4).getCell(1))).isEqualTo("4");
            assertThat(sheet.getRow(5).getCell(1).getNumericCellValue()).isCloseTo(200.125, org.assertj.core.data.Offset.offset(0.01));  // 800.50/4 (允許些微誤差)
            assertThat(sheet.getRow(6).getCell(1).getNumericCellValue()).isEqualTo(450.0);
            assertThat(sheet.getRow(7).getCell(1).getNumericCellValue()).isEqualTo(30.0);
        }
    }

    @Test
    @DisplayName("測試 Excel 匯出 - 工作表3（分類統計）")
    void testExportToExcel_Sheet3_CategoryStatistics() throws Exception {
        byte[] excelData = excelExportService.exportExpensesToExcel(testExpenses, LocalDate.of(2025, 10, 1), LocalDate.of(2025, 10, 31));

        try (Workbook workbook = new XSSFWorkbook(new ByteArrayInputStream(excelData))) {
            Sheet sheet = workbook.getSheetAt(2);

            // 驗證標題列
            Row headerRow = sheet.getRow(0);
            assertThat(getCellValue(headerRow.getCell(0))).isEqualTo("分類");
            assertThat(getCellValue(headerRow.getCell(1))).isEqualTo("金額");
            assertThat(getCellValue(headerRow.getCell(2))).isEqualTo("筆數");
            assertThat(getCellValue(headerRow.getCell(3))).isEqualTo("佔比");

            // 驗證有資料列（應該有3個分類：餐飲、交通、教育）
            assertThat(sheet.getPhysicalNumberOfRows()).isGreaterThanOrEqualTo(4);  // 標題 + 3個分類

            // 驗證有資料列存在且格式正確（不強制順序）
            if (sheet.getPhysicalNumberOfRows() > 1) {
                Row dataRow = sheet.getRow(1);
                // 驗證第一行有分類名稱
                assertThat(getCellValue(dataRow.getCell(0))).isNotEmpty();
                // 驗證金額欄位是數字
                assertThat(dataRow.getCell(1).getCellType()).isEqualTo(CellType.NUMERIC);
                // 驗證筆數欄位是數字
                assertThat(dataRow.getCell(2).getCellType()).isEqualTo(CellType.NUMERIC);
                // 驗證佔比欄位是數字（百分比格式）
                assertThat(dataRow.getCell(3).getCellType()).isEqualTo(CellType.NUMERIC);
            }
        }
    }

    @Test
    @DisplayName("測試 Excel 匯出 - 空清單")
    void testExportToExcel_EmptyList() throws Exception {
        List<Expense> emptyList = new ArrayList<>();
        byte[] excelData = excelExportService.exportExpensesToExcel(emptyList, LocalDate.of(2025, 10, 1), LocalDate.of(2025, 10, 31));

        try (Workbook workbook = new XSSFWorkbook(new ByteArrayInputStream(excelData))) {
            // 應該仍然有3個工作表
            assertThat(workbook.getNumberOfSheets()).isEqualTo(3);

            // 工作表1應該只有標題列
            Sheet sheet1 = workbook.getSheetAt(0);
            assertThat(sheet1.getPhysicalNumberOfRows()).isEqualTo(1);  // 只有標題

            // 工作表2的統計標題應該存在
            Sheet sheet2 = workbook.getSheetAt(1);
            assertThat(sheet2.getPhysicalNumberOfRows()).isGreaterThanOrEqualTo(1);

            // 工作表3至少有標題列（但可能有統計資料，因為統計查詢資料庫）
            Sheet sheet3 = workbook.getSheetAt(2);
            assertThat(sheet3.getPhysicalNumberOfRows()).isGreaterThanOrEqualTo(1);
        }
    }

    @Test
    @DisplayName("測試 Excel 匯出 - 樣式設定")
    void testExportToExcel_Styling() throws Exception {
        byte[] excelData = excelExportService.exportExpensesToExcel(testExpenses, LocalDate.of(2025, 10, 1), LocalDate.of(2025, 10, 31));

        try (Workbook workbook = new XSSFWorkbook(new ByteArrayInputStream(excelData))) {
            Sheet sheet = workbook.getSheetAt(0);
            Row headerRow = sheet.getRow(0);

            // 驗證標題列有特殊樣式（灰底、粗體）
            CellStyle headerStyle = headerRow.getCell(0).getCellStyle();
            assertThat(headerStyle).isNotNull();

            // 驗證標題列字體粗體
            Font headerFont = workbook.getFontAt(headerStyle.getFontIndex());
            assertThat(headerFont.getBold()).isTrue();

            // 驗證資料列有邊框
            Row dataRow = sheet.getRow(1);
            CellStyle dataStyle = dataRow.getCell(0).getCellStyle();
            assertThat(dataStyle.getBorderTop()).isNotEqualTo(BorderStyle.NONE);
        }
    }

    @Test
    @DisplayName("測試 Excel 匯出 - 貨幣格式")
    void testExportToExcel_CurrencyFormat() throws Exception {
        byte[] excelData = excelExportService.exportExpensesToExcel(testExpenses, LocalDate.of(2025, 10, 1), LocalDate.of(2025, 10, 31));

        try (Workbook workbook = new XSSFWorkbook(new ByteArrayInputStream(excelData))) {
            Sheet sheet = workbook.getSheetAt(0);
            Row dataRow = sheet.getRow(1);

            // 驗證金額欄位有貨幣格式
            Cell amountCell = dataRow.getCell(2);
            assertThat(amountCell.getCellType()).isEqualTo(CellType.NUMERIC);

            // 驗證格式字串包含千分位或小數點
            String formatString = amountCell.getCellStyle().getDataFormatString();
            assertThat(formatString).containsAnyOf("#,##0.00", "0.00");
        }
    }

    @Test
    @DisplayName("測試 Excel 匯出 - 百分比格式")
    void testExportToExcel_PercentageFormat() throws Exception {
        byte[] excelData = excelExportService.exportExpensesToExcel(testExpenses, LocalDate.of(2025, 10, 1), LocalDate.of(2025, 10, 31));

        try (Workbook workbook = new XSSFWorkbook(new ByteArrayInputStream(excelData))) {
            Sheet sheet = workbook.getSheetAt(2);  // 分類統計工作表

            if (sheet.getPhysicalNumberOfRows() > 1) {
                Row dataRow = sheet.getRow(1);
                Cell percentCell = dataRow.getCell(3);  // 佔比欄位

                // 驗證百分比格式
                assertThat(percentCell.getCellType()).isEqualTo(CellType.NUMERIC);
                String formatString = percentCell.getCellStyle().getDataFormatString();
                assertThat(formatString).containsAnyOf("0.00%", "%");
            }
        }
    }

    @Test
    @DisplayName("測試 Excel 匯出 - 中文字元")
    void testExportToExcel_ChineseCharacters() throws Exception {
        byte[] excelData = excelExportService.exportExpensesToExcel(testExpenses, LocalDate.of(2025, 10, 1), LocalDate.of(2025, 10, 31));

        try (Workbook workbook = new XSSFWorkbook(new ByteArrayInputStream(excelData))) {
            Sheet sheet = workbook.getSheetAt(0);

            // 驗證中文標題正確顯示
            assertThat(getCellValue(sheet.getRow(0).getCell(1))).isEqualTo("標題");
            assertThat(getCellValue(sheet.getRow(0).getCell(3))).isEqualTo("分類");

            // 驗證中文資料正確顯示
            assertThat(getCellValue(sheet.getRow(1).getCell(1))).isEqualTo("午餐");
            assertThat(getCellValue(sheet.getRow(1).getCell(3))).isEqualTo("餐飲");
        }
    }

    @Test
    @DisplayName("測試 Excel 匯出 - 大量資料")
    void testExportToExcel_LargeDataset() throws Exception {
        // 建立 1000 筆測試資料
        List<Expense> largeExpenses = new ArrayList<>();
        for (int i = 0; i < 1000; i++) {
            Expense expense = new Expense(testUser, "測試支出" + i, BigDecimal.valueOf(100.00), "測試", LocalDate.of(2025, 10, 15));
            expense.setId((long) i);
            largeExpenses.add(expense);
        }

        byte[] excelData = excelExportService.exportExpensesToExcel(largeExpenses, LocalDate.of(2025, 10, 1), LocalDate.of(2025, 10, 31));

        try (Workbook workbook = new XSSFWorkbook(new ByteArrayInputStream(excelData))) {
            Sheet sheet = workbook.getSheetAt(0);

            // 驗證行數：標題 + 1000筆資料 + 總計
            assertThat(sheet.getPhysicalNumberOfRows()).isEqualTo(1002);

            // 驗證最後一筆資料
            Row lastDataRow = sheet.getRow(1000);
            assertThat(getCellValue(lastDataRow.getCell(1))).isEqualTo("測試支出999");
        }
    }

    @Test
    @DisplayName("測試 Excel 匯出 - 日期範圍資訊")
    void testExportToExcel_DateRangeInfo() throws Exception {
        LocalDate startDate = LocalDate.of(2025, 10, 1);
        LocalDate endDate = LocalDate.of(2025, 10, 31);

        byte[] excelData = excelExportService.exportExpensesToExcel(testExpenses, startDate, endDate);

        try (Workbook workbook = new XSSFWorkbook(new ByteArrayInputStream(excelData))) {
            Sheet sheet = workbook.getSheetAt(1);  // 統計摘要

            // 驗證日期範圍資訊在 Row 1 的第二個儲存格
            Row dateRow = sheet.getRow(1);
            String dateRange = getCellValue(dateRow.getCell(1));
            assertThat(dateRange).contains("2025-10-01");
            assertThat(dateRange).contains("2025-10-31");
        }
    }

    // 輔助方法：取得儲存格值（字串）
    private String getCellValue(Cell cell) {
        if (cell == null) {
            return "";
        }
        switch (cell.getCellType()) {
            case STRING:
                return cell.getStringCellValue();
            case NUMERIC:
                return String.valueOf((int) cell.getNumericCellValue());
            case BOOLEAN:
                return String.valueOf(cell.getBooleanCellValue());
            case FORMULA:
                return cell.getCellFormula();
            default:
                return "";
        }
    }
}
