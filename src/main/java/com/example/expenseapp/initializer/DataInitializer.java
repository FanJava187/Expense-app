package com.example.expenseapp.initializer;

import com.example.expenseapp.model.Expense;
import com.example.expenseapp.repository.ExpenseRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDate;

@Component
public class DataInitializer implements CommandLineRunner {

    private final ExpenseRepository expenseRepository;

    public DataInitializer(ExpenseRepository expenseRepository) {
        this.expenseRepository = expenseRepository;
    }

    @Override
    public void run(String... args) throws Exception {
        // 如果資料庫已有資料，就不初始化
        if (expenseRepository.count() > 0) {
            return;
        }

        System.out.println("初始化範例資料...");

        // 食物類
        expenseRepository.save(new Expense(
                "早餐 - 蛋餅豆漿",
                new BigDecimal("50.00"),
                "食物",
                LocalDate.now().minusDays(2)
        ));

        expenseRepository.save(new Expense(
                "午餐 - 便當",
                new BigDecimal("120.00"),
                "食物",
                LocalDate.now().minusDays(1)
        ));

        expenseRepository.save(new Expense(
                "晚餐 - 牛肉麵",
                new BigDecimal("200.00"),
                "食物",
                LocalDate.now()
        ));

        // 交通類
        expenseRepository.save(new Expense(
                "捷運票",
                new BigDecimal("35.00"),
                "交通",
                LocalDate.now().minusDays(3)
        ));

        expenseRepository.save(new Expense(
                "Uber 計程車",
                new BigDecimal("250.00"),
                "交通",
                LocalDate.now().minusDays(1)
        ));

        // 娛樂類
        expenseRepository.save(new Expense(
                "電影票",
                new BigDecimal("320.00"),
                "娛樂",
                LocalDate.now().minusDays(5)
        ));

        // 購物類
        expenseRepository.save(new Expense(
                "衣服",
                new BigDecimal("1200.00"),
                "購物",
                LocalDate.now().minusDays(7)
        ));

        // 生活用品類
        expenseRepository.save(new Expense(
                "衛生紙、洗髮精",
                new BigDecimal("350.00"),
                "生活用品",
                LocalDate.now().minusDays(4)
        ));

        System.out.println("範例資料初始化完成！共 8 筆資料");
    }
}