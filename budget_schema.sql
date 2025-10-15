-- 預算管理功能資料表

CREATE TABLE IF NOT EXISTS budgets (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    budget_type VARCHAR(20) NOT NULL,
    category VARCHAR(50),
    amount DECIMAL(10, 2) NOT NULL,
    year INT NOT NULL,
    month INT NOT NULL,
    created_at DATE NOT NULL,
    updated_at DATE,

    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,

    -- 唯一約束：同一使用者在同一年月的同類型同分類預算只能有一筆
    UNIQUE KEY unique_budget (user_id, budget_type, category, year, month),

    -- 索引
    INDEX idx_user_year_month (user_id, year, month),
    INDEX idx_budget_type (budget_type)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 預算類型說明：
-- MONTHLY: 月度總預算（category 為 NULL）
-- CATEGORY: 分類預算（category 有值）
