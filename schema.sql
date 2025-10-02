-- ============================================
-- Expense App 資料庫建立腳本
-- ============================================

-- 1. 建立資料庫（如果還沒建立）
CREATE DATABASE IF NOT EXISTS expense_db
CHARACTER SET utf8mb4
COLLATE utf8mb4_unicode_ci;

-- 2. 使用資料庫
USE expense_db;

-- 3. 刪除舊表（小心使用！會清空資料）
-- DROP TABLE IF EXISTS expenses;

-- 4. 建立 expenses 表格
CREATE TABLE expenses (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主鍵 ID',
    title VARCHAR(100) NOT NULL COMMENT '支出標題',
    amount DECIMAL(12, 2) NOT NULL COMMENT '支出金額',
    category VARCHAR(50) NOT NULL COMMENT '支出分類',
    expense_date DATE NOT NULL COMMENT '支出日期',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '建立時間',
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新時間',

    -- 約束條件（金額必須大於 0）
    CONSTRAINT chk_amount_positive CHECK (amount > 0)
    -- 注意：日期驗證改由應用層（Java @PastOrPresent）處理
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='支出紀錄表';

-- 5. 建立索引（提升查詢效能）
CREATE INDEX idx_category ON expenses(category) COMMENT '分類索引';
CREATE INDEX idx_expense_date ON expenses(expense_date) COMMENT '日期索引';
CREATE INDEX idx_category_date ON expenses(category, expense_date) COMMENT '分類+日期組合索引';

-- 6. 查看表格結構
DESC expenses;

-- 7. 查看索引
SHOW INDEX FROM expenses;

-- ============================================
-- 測試資料（可選）
-- ============================================

-- 插入範例資料
INSERT INTO expenses (title, amount, category, expense_date) VALUES
('早餐 - 蛋餅豆漿', 50.00, '食物', DATE_SUB(CURDATE(), INTERVAL 2 DAY)),
('午餐 - 便當', 120.00, '食物', DATE_SUB(CURDATE(), INTERVAL 1 DAY)),
('晚餐 - 牛肉麵', 200.00, '食物', CURDATE()),
('捷運票', 35.00, '交通', DATE_SUB(CURDATE(), INTERVAL 3 DAY)),
('Uber 計程車', 250.00, '交通', DATE_SUB(CURDATE(), INTERVAL 1 DAY)),
('電影票', 320.00, '娛樂', DATE_SUB(CURDATE(), INTERVAL 5 DAY)),
('衣服', 1200.00, '購物', DATE_SUB(CURDATE(), INTERVAL 7 DAY)),
('衛生紙、洗髮精', 350.00, '生活用品', DATE_SUB(CURDATE(), INTERVAL 4 DAY));

-- 查看插入的資料
SELECT * FROM expenses ORDER BY expense_date DESC;

-- ============================================
-- 常用查詢範例
-- ============================================

-- 查詢所有支出
SELECT * FROM expenses;

-- 根據分類查詢
SELECT * FROM expenses WHERE category = '食物';

-- 根據日期範圍查詢（本月支出）
SELECT * FROM expenses
WHERE expense_date BETWEEN DATE_FORMAT(CURDATE(), '%Y-%m-01') AND LAST_DAY(CURDATE())
ORDER BY expense_date DESC;

-- 查詢各分類的總金額
SELECT
    category AS 分類,
    COUNT(*) AS 筆數,
    SUM(amount) AS 總金額,
    AVG(amount) AS 平均金額
FROM expenses
GROUP BY category
ORDER BY 總金額 DESC;

-- 查詢本月每日支出
SELECT
    expense_date AS 日期,
    COUNT(*) AS 筆數,
    SUM(amount) AS 當日總支出
FROM expenses
WHERE expense_date BETWEEN DATE_FORMAT(CURDATE(), '%Y-%m-01') AND LAST_DAY(CURDATE())
GROUP BY expense_date
ORDER BY expense_date DESC;