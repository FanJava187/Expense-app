package com.example.expenseapp.service;

import com.example.expenseapp.model.Expense;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * CSV 匯出服務
 */
@Service
public class CsvExportService {

    private static final String CSV_HEADER = "ID,標題,金額,分類,日期\n";
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    /**
     * 將支出列表匯出為 CSV 格式的位元組陣列
     *
     * @param expenses 支出列表
     * @return CSV 格式的位元組陣列
     * @throws IOException 如果寫入過程發生錯誤
     */
    public byte[] exportExpensesToCsv(List<Expense> expenses) throws IOException {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

        try (Writer writer = new OutputStreamWriter(outputStream, StandardCharsets.UTF_8)) {
            // 寫入 UTF-8 BOM，讓 Excel 能正確識別編碼
            writer.write('\ufeff');

            // 寫入標題列
            writer.write(CSV_HEADER);

            // 寫入資料列
            for (Expense expense : expenses) {
                writer.write(formatExpenseAsCsvRow(expense));
            }

            writer.flush();
        }

        return outputStream.toByteArray();
    }

    /**
     * 將單筆支出格式化為 CSV 行
     *
     * @param expense 支出記錄
     * @return CSV 格式的字串
     */
    private String formatExpenseAsCsvRow(Expense expense) {
        return String.format("%d,\"%s\",%.2f,\"%s\",%s\n",
                expense.getId(),
                escapeCsvField(expense.getTitle()),
                expense.getAmount().doubleValue(),
                escapeCsvField(expense.getCategory()),
                expense.getExpenseDate().format(DATE_FORMATTER)
        );
    }

    /**
     * 處理 CSV 欄位中的特殊字元
     *
     * @param field 原始欄位內容
     * @return 處理後的欄位內容
     */
    private String escapeCsvField(String field) {
        if (field == null) {
            return "";
        }
        // 將雙引號替換為兩個雙引號（CSV 標準跳脫方式）
        return field.replace("\"", "\"\"");
    }
}
