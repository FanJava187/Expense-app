package com.example.expenseapp;

import io.github.cdimascio.dotenv.Dotenv;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class ExpenseAppApplication {
    public static void main(String[] args) {
        // 載入 .env 文件並設置為系統屬性
        Dotenv dotenv = Dotenv.configure()
                .ignoreIfMissing() // 如果沒有 .env 文件則忽略
                .load();

        // 將 .env 中的環境變數設置為系統屬性
        dotenv.entries().forEach(entry -> {
            System.setProperty(entry.getKey(), entry.getValue());
        });

        SpringApplication.run(ExpenseAppApplication.class, args);
    }
}
