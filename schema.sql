-- ============================================
-- Expense App 完整資料庫建立腳本
-- ============================================

-- 1. 建立資料庫（如果還沒建立）
CREATE DATABASE IF NOT EXISTS expense_db
CHARACTER SET utf8mb4
COLLATE utf8mb4_unicode_ci;

-- 2. 使用資料庫
USE expense_db;

-- ============================================
-- 3. 刪除舊表（如果需要重新建立）
-- ⚠️ 警告：這會刪除所有資料！
-- ============================================
-- DROP TABLE IF EXISTS verification_tokens;
-- DROP TABLE IF EXISTS expenses;
-- DROP TABLE IF EXISTS users;

-- ============================================
-- 4. 建立 users 資料表
-- ============================================
CREATE TABLE IF NOT EXISTS users (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主鍵 ID',
    username VARCHAR(50) UNIQUE NOT NULL COMMENT '使用者帳號',
    email VARCHAR(255) UNIQUE NOT NULL COMMENT 'Email',
    password VARCHAR(255) NOT NULL COMMENT '加密後的密碼',
    name VARCHAR(100) NOT NULL COMMENT '使用者姓名',

    -- 帳號狀態
    status VARCHAR(20) DEFAULT 'UNVERIFIED' COMMENT '狀態: UNVERIFIED, ACTIVE, SUSPENDED',

    -- OAuth 相關（預留給 Google 登入）
    google_id VARCHAR(255) UNIQUE COMMENT 'Google 使用者 ID',
    provider VARCHAR(20) DEFAULT 'local' COMMENT '註冊方式: local, google',
    avatar_url VARCHAR(500) COMMENT '頭像網址',

    -- 時間戳記
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '建立時間',
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新時間',
    last_login_at TIMESTAMP NULL COMMENT '最後登入時間',

    -- 索引
    INDEX idx_username (username),
    INDEX idx_email (email),
    INDEX idx_status (status),
    INDEX idx_google_id (google_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='使用者資料表';

-- ============================================
-- 5. 建立 verification_tokens 資料表
-- ============================================
CREATE TABLE IF NOT EXISTS verification_tokens (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主鍵 ID',
    user_id BIGINT NOT NULL COMMENT '使用者 ID',
    token VARCHAR(255) UNIQUE NOT NULL COMMENT '驗證 Token',
    token_type VARCHAR(30) NOT NULL COMMENT '類型: EMAIL_VERIFICATION, PASSWORD_RESET',
    expires_at TIMESTAMP NOT NULL COMMENT '過期時間',
    used_at TIMESTAMP NULL COMMENT '使用時間',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '建立時間',

    -- 外鍵約束
    CONSTRAINT fk_verification_tokens_user
        FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,

    -- 索引
    INDEX idx_token (token),
    INDEX idx_user_id (user_id),
    INDEX idx_expires_at (expires_at),
    INDEX idx_token_type (token_type)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='驗證 Token 資料表';

-- ============================================
-- 6. 建立或修改 expenses 資料表
-- ============================================
CREATE TABLE IF NOT EXISTS expenses (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主鍵 ID',
    user_id BIGINT NOT NULL COMMENT '使用者 ID',
    title VARCHAR(100) NOT NULL COMMENT '支出標題',
    amount DECIMAL(12, 2) NOT NULL COMMENT '支出金額',
    category VARCHAR(50) NOT NULL COMMENT '支出分類',
    expense_date DATE NOT NULL COMMENT '支出日期',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '建立時間',
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新時間',

    -- 約束條件
    CONSTRAINT chk_amount_positive CHECK (amount > 0),

    -- 外鍵約束
    CONSTRAINT fk_expenses_user
        FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,

    -- 索引
    INDEX idx_user_id (user_id),
    INDEX idx_category (category),
    INDEX idx_expense_date (expense_date),
    INDEX idx_user_date (user_id, expense_date),
    INDEX idx_user_category (user_id, category),
    INDEX idx_category_date (category, expense_date)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='支出紀錄表';

-- ============================================
-- 7. 如果 expenses 表已存在但缺少 user_id 欄位
-- ============================================
-- 檢查並新增 user_id 欄位（如果不存在）
SET @column_exists = (
    SELECT COUNT(*)
    FROM INFORMATION_SCHEMA.COLUMNS
    WHERE TABLE_SCHEMA = 'expense_db'
    AND TABLE_NAME = 'expenses'
    AND COLUMN_NAME = 'user_id'
);

SET @sql = IF(@column_exists = 0,
    'ALTER TABLE expenses ADD COLUMN user_id BIGINT NULL COMMENT ''使用者 ID'' AFTER id',
    'SELECT ''Column user_id already exists'' AS message'
);

PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- 新增外鍵約束（如果尚未存在）
SET @fk_exists = (
    SELECT COUNT(*)
    FROM INFORMATION_SCHEMA.KEY_COLUMN_USAGE
    WHERE TABLE_SCHEMA = 'expense_db'
    AND TABLE_NAME = 'expenses'
    AND CONSTRAINT_NAME = 'fk_expenses_user'
);

SET @sql = IF(@fk_exists = 0,
    'ALTER TABLE expenses ADD CONSTRAINT fk_expenses_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE',
    'SELECT ''Foreign key fk_expenses_user already exists'' AS message'
);

PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- ============================================
-- 8. 查看資料表結構
-- ============================================
SHOW TABLES;

DESC users;
DESC verification_tokens;
DESC expenses;

-- ============================================
-- 9. 查看索引
-- ============================================
SHOW INDEX FROM users;
SHOW INDEX FROM verification_tokens;
SHOW INDEX FROM expenses;

-- ============================================
-- 10. 查看外鍵關係
-- ============================================
SELECT
    TABLE_NAME,
    COLUMN_NAME,
    CONSTRAINT_NAME,
    REFERENCED_TABLE_NAME,
    REFERENCED_COLUMN_NAME
FROM
    INFORMATION_SCHEMA.KEY_COLUMN_USAGE
WHERE
    REFERENCED_TABLE_SCHEMA = 'expense_db'
    AND REFERENCED_TABLE_NAME IS NOT NULL;

-- ============================================
-- 11. 測試資料（選擇性）
-- ⚠️ 注意：這些是測試資料，實際使用時應該透過 API 註冊
-- ============================================
-- 插入測試使用者（密碼是 BCrypt 加密的 "password123"）
-- INSERT INTO users (username, email, password, name, status) VALUES
-- ('test_user', 'test@example.com', '$2a$10$XYZ...', '測試使用者', 'ACTIVE');

-- 查詢測試
-- SELECT * FROM users;
-- SELECT * FROM verification_tokens;
-- SELECT * FROM expenses;

-- ============================================
-- 12. 清理過期的 Token（定期執行）
-- ============================================
-- 刪除過期且未使用的 Token
-- DELETE FROM verification_tokens
-- WHERE expires_at < NOW() AND used_at IS NULL;

-- ============================================
-- 完成！
-- ============================================
SELECT '資料庫建立完成！' AS message;