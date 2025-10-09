package com.example.expenseapp.config;

import io.github.cdimascio.dotenv.Dotenv;
import org.springframework.boot.test.util.TestPropertyValues;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.stereotype.Component;

/**
 * 測試環境的 .env 加載器
 * 在測試啟動時自動加載 .env 文件中的環境變數
 */
@Component
public class DotenvTestConfig implements ApplicationContextInitializer<ConfigurableApplicationContext> {

    @Override
    public void initialize(ConfigurableApplicationContext applicationContext) {
        // 載入 .env 文件
        Dotenv dotenv = Dotenv.configure()
                .ignoreIfMissing() // 如果沒有 .env 文件則忽略
                .load();

        // 將 .env 中的環境變數添加到 Spring 環境中
        TestPropertyValues.of(
                dotenv.entries().stream()
                        .map(entry -> entry.getKey() + "=" + entry.getValue())
                        .toArray(String[]::new)
        ).applyTo(applicationContext.getEnvironment());
    }
}
