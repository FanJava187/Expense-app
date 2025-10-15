package com.example.expenseapp.service;

import com.example.expenseapp.dto.CategoryStatistics;
import com.example.expenseapp.dto.SummaryStatistics;
import com.example.expenseapp.model.Expense;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * Excel 匯出服務
 * 使用 Apache POI 生成豐富格式的 Excel 檔案
 */
@Service
public class ExcelExportService {

    @Autowired
    private StatisticsService statisticsService;

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    /**
     * 匯出支出到 Excel（包含多個工作表）
     */
    public byte[] exportExpensesToExcel(List<Expense> expenses, LocalDate startDate, LocalDate endDate) throws IOException {
        Workbook workbook = new XSSFWorkbook();

        try {
            // 工作表 1: 支出明細
            createExpenseSheet(workbook, expenses);

            // 工作表 2: 統計摘要
            createSummarySheet(workbook, startDate, endDate);

            // 工作表 3: 分類統計
            createCategorySheet(workbook, startDate, endDate);

            // 寫入輸出流
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            workbook.write(outputStream);
            return outputStream.toByteArray();

        } finally {
            workbook.close();
        }
    }

    /**
     * 建立支出明細工作表
     */
    private void createExpenseSheet(Workbook workbook, List<Expense> expenses) {
        Sheet sheet = workbook.createSheet("支出明細");

        // 設定欄寬
        sheet.setColumnWidth(0, 3000);  // ID
        sheet.setColumnWidth(1, 8000);  // 標題
        sheet.setColumnWidth(2, 4000);  // 金額
        sheet.setColumnWidth(3, 5000);  // 分類
        sheet.setColumnWidth(4, 4000);  // 日期

        // 建立樣式
        CellStyle headerStyle = createHeaderStyle(workbook);
        CellStyle dataStyle = createDataStyle(workbook);
        CellStyle currencyStyle = createCurrencyStyle(workbook);
        CellStyle dateStyle = createDateStyle(workbook);

        // 標題列
        Row headerRow = sheet.createRow(0);
        String[] headers = {"ID", "標題", "金額", "分類", "日期"};
        for (int i = 0; i < headers.length; i++) {
            Cell cell = headerRow.createCell(i);
            cell.setCellValue(headers[i]);
            cell.setCellStyle(headerStyle);
        }

        // 資料列
        int rowNum = 1;
        for (Expense expense : expenses) {
            Row row = sheet.createRow(rowNum++);

            Cell cell0 = row.createCell(0);
            cell0.setCellValue(expense.getId());
            cell0.setCellStyle(dataStyle);

            Cell cell1 = row.createCell(1);
            cell1.setCellValue(expense.getTitle());
            cell1.setCellStyle(dataStyle);

            Cell cell2 = row.createCell(2);
            cell2.setCellValue(expense.getAmount().doubleValue());
            cell2.setCellStyle(currencyStyle);

            Cell cell3 = row.createCell(3);
            cell3.setCellValue(expense.getCategory());
            cell3.setCellStyle(dataStyle);

            Cell cell4 = row.createCell(4);
            cell4.setCellValue(expense.getExpenseDate().format(DATE_FORMATTER));
            cell4.setCellStyle(dateStyle);
        }

        // 合計列
        if (!expenses.isEmpty()) {
            Row totalRow = sheet.createRow(rowNum);
            Cell totalLabelCell = totalRow.createCell(1);
            totalLabelCell.setCellValue("總計");
            totalLabelCell.setCellStyle(headerStyle);

            Cell totalAmountCell = totalRow.createCell(2);
            totalAmountCell.setCellFormula("SUM(C2:C" + rowNum + ")");
            totalAmountCell.setCellStyle(currencyStyle);
        }
    }

    /**
     * 建立統計摘要工作表
     */
    private void createSummarySheet(Workbook workbook, LocalDate startDate, LocalDate endDate) {
        Sheet sheet = workbook.createSheet("統計摘要");

        // 設定欄寬
        sheet.setColumnWidth(0, 6000);
        sheet.setColumnWidth(1, 5000);

        CellStyle headerStyle = createHeaderStyle(workbook);
        CellStyle dataStyle = createDataStyle(workbook);
        CellStyle currencyStyle = createCurrencyStyle(workbook);

        // 標題
        Row titleRow = sheet.createRow(0);
        Cell titleCell = titleRow.createCell(0);
        titleCell.setCellValue("統計摘要");
        titleCell.setCellStyle(headerStyle);
        sheet.addMergedRegion(new CellRangeAddress(0, 0, 0, 1));

        // 日期範圍
        Row dateRow = sheet.createRow(1);
        Cell dateLabelCell = dateRow.createCell(0);
        dateLabelCell.setCellValue("統計期間");
        dateLabelCell.setCellStyle(dataStyle);
        Cell dateValueCell = dateRow.createCell(1);
        dateValueCell.setCellValue(startDate.format(DATE_FORMATTER) + " ~ " + endDate.format(DATE_FORMATTER));
        dateValueCell.setCellStyle(dataStyle);

        // 取得統計資料
        SummaryStatistics stats = statisticsService.getSummaryStatistics(startDate, endDate);

        // 統計項目
        int rowNum = 3;
        String[][] statsData = {
                {"總支出金額", stats.getTotalAmount().toString()},
                {"總筆數", stats.getTotalCount().toString()},
                {"平均金額", stats.getAverageAmount().toString()},
                {"最大金額", stats.getMaxAmount().toString()},
                {"最小金額", stats.getMinAmount().toString()}
        };

        for (String[] data : statsData) {
            Row row = sheet.createRow(rowNum++);
            Cell labelCell = row.createCell(0);
            labelCell.setCellValue(data[0]);
            labelCell.setCellStyle(dataStyle);

            Cell valueCell = row.createCell(1);
            if (data[0].contains("筆數")) {
                valueCell.setCellValue(data[1]);
                valueCell.setCellStyle(dataStyle);
            } else {
                valueCell.setCellValue(Double.parseDouble(data[1]));
                valueCell.setCellStyle(currencyStyle);
            }
        }
    }

    /**
     * 建立分類統計工作表
     */
    private void createCategorySheet(Workbook workbook, LocalDate startDate, LocalDate endDate) {
        Sheet sheet = workbook.createSheet("分類統計");

        // 設定欄寬
        sheet.setColumnWidth(0, 5000);  // 分類
        sheet.setColumnWidth(1, 5000);  // 金額
        sheet.setColumnWidth(2, 3000);  // 筆數
        sheet.setColumnWidth(3, 4000);  // 百分比

        CellStyle headerStyle = createHeaderStyle(workbook);
        CellStyle dataStyle = createDataStyle(workbook);
        CellStyle currencyStyle = createCurrencyStyle(workbook);
        CellStyle percentStyle = createPercentStyle(workbook);

        // 標題列
        Row headerRow = sheet.createRow(0);
        String[] headers = {"分類", "金額", "筆數", "佔比"};
        for (int i = 0; i < headers.length; i++) {
            Cell cell = headerRow.createCell(i);
            cell.setCellValue(headers[i]);
            cell.setCellStyle(headerStyle);
        }

        // 取得分類統計
        List<CategoryStatistics> categoryStats = statisticsService.getCategoryStatistics(startDate, endDate);

        // 資料列
        int rowNum = 1;
        for (CategoryStatistics stat : categoryStats) {
            Row row = sheet.createRow(rowNum++);

            Cell cell0 = row.createCell(0);
            cell0.setCellValue(stat.getCategory());
            cell0.setCellStyle(dataStyle);

            Cell cell1 = row.createCell(1);
            cell1.setCellValue(stat.getTotalAmount().doubleValue());
            cell1.setCellStyle(currencyStyle);

            Cell cell2 = row.createCell(2);
            cell2.setCellValue(stat.getCount());
            cell2.setCellStyle(dataStyle);

            Cell cell3 = row.createCell(3);
            cell3.setCellValue(stat.getPercentage().doubleValue() / 100);  // POI 百分比格式需要小數
            cell3.setCellStyle(percentStyle);
        }
    }

    /**
     * 建立標題樣式
     */
    private CellStyle createHeaderStyle(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        Font font = workbook.createFont();
        font.setBold(true);
        font.setFontHeightInPoints((short) 12);
        style.setFont(font);
        style.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
        style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        style.setBorderTop(BorderStyle.THIN);
        style.setBorderBottom(BorderStyle.THIN);
        style.setBorderLeft(BorderStyle.THIN);
        style.setBorderRight(BorderStyle.THIN);
        style.setAlignment(HorizontalAlignment.CENTER);
        style.setVerticalAlignment(VerticalAlignment.CENTER);
        return style;
    }

    /**
     * 建立資料樣式
     */
    private CellStyle createDataStyle(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        style.setBorderTop(BorderStyle.THIN);
        style.setBorderBottom(BorderStyle.THIN);
        style.setBorderLeft(BorderStyle.THIN);
        style.setBorderRight(BorderStyle.THIN);
        style.setVerticalAlignment(VerticalAlignment.CENTER);
        return style;
    }

    /**
     * 建立貨幣樣式
     */
    private CellStyle createCurrencyStyle(Workbook workbook) {
        CellStyle style = createDataStyle(workbook);
        DataFormat format = workbook.createDataFormat();
        style.setDataFormat(format.getFormat("#,##0.00"));
        return style;
    }

    /**
     * 建立日期樣式
     */
    private CellStyle createDateStyle(Workbook workbook) {
        CellStyle style = createDataStyle(workbook);
        style.setAlignment(HorizontalAlignment.CENTER);
        return style;
    }

    /**
     * 建立百分比樣式
     */
    private CellStyle createPercentStyle(Workbook workbook) {
        CellStyle style = createDataStyle(workbook);
        DataFormat format = workbook.createDataFormat();
        style.setDataFormat(format.getFormat("0.00%"));
        return style;
    }
}
