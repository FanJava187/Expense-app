# Expense App Backend 專案文件

## 📌 專案簡介
本專案是一個功能完整的 **支出管理系統 (Expense Management App)**，  
使用 **Spring Boot 3 + Spring Data JPA + MySQL + Spring Security + OAuth2** 開發。  
提供完整的 **使用者認證**、**CRUD REST API**、**資料驗證**、**分類查詢**、**日期範圍查詢**等功能。

---

## ⚙️ 技術棧
- **Java 21**
- **Spring Boot 3.4.5**
- **Spring Web (REST API)**
- **Spring Data JPA (Hibernate)**
- **Spring Security + JWT**
- **Spring Security OAuth2 Client**
- **MySQL 8.x**
- **dotenv-java 3.0.0 (環境變數管理)**
- **Bean Validation (Hibernate Validator)**
- **Swagger / OpenAPI (springdoc-openapi)**
- **JavaMail (Email 發送)**
- **JUnit 5 + MockMvc (單元測試)**

---

## 🗄️ 資料庫設定

### 1. 建立資料庫
```sql
CREATE DATABASE expense_db CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
```

### 2. 建立資料表
請執行 SQL 腳本檔案（完整內容請見專案根目錄）

主要資料表：
- **users** - 使用者資料（支援傳統註冊和 Google OAuth）
- **verification_tokens** - Email 驗證和密碼重設 Token
- **expenses** - 支出紀錄

### 3. 設定環境變數
本專案使用 `.env` 文件管理環境變數，確保敏感資訊不會被提交到版本控制。

#### 建立 .env 文件
```bash
# 複製環境變數範例檔
cp .env.example .env

# 編輯 .env 檔案
nano .env    # Linux/Mac
notepad .env # Windows
```

#### 環境變數說明
`.env` 文件包含以下設定：

**資料庫設定**
- `DB_URL` - 資料庫連線 URL
- `DB_USERNAME` - 資料庫使用者名稱
- `DB_PASSWORD` - 資料庫密碼

**JWT 設定**
- `JWT_SECRET` - JWT 加密密鑰（至少 256 位元）
- `JWT_EXPIRATION` - Token 有效期（毫秒）

**Email 設定**
- `MAIL_HOST` - SMTP 主機
- `MAIL_PORT` - SMTP 端口
- `MAIL_USERNAME` - SMTP 帳號
- `MAIL_PASSWORD` - SMTP 密碼
- `EMAIL_FROM` - 寄件人 Email
- `EMAIL_FROM_NAME` - 寄件人名稱

**應用程式設定**
- `FRONTEND_URL` - 前端應用網址
- `TOKEN_EMAIL_VERIFICATION_EXPIRATION` - Email 驗證 Token 有效期
- `TOKEN_PASSWORD_RESET_EXPIRATION` - 密碼重設 Token 有效期

**Google OAuth**
- `GOOGLE_CLIENT_ID` - Google OAuth Client ID
- `GOOGLE_CLIENT_SECRET` - Google OAuth Client Secret

#### 自動載入機制
專案使用 **dotenv-java** 自動載入環境變數：
- 應用啟動時，`ExpenseAppApplication.java` 會自動讀取 `.env` 文件
- 測試環境使用 `DotenvTestConfig.java` 確保測試也能正確載入環境變數
- `application.properties` 使用 `${變數名}` 語法引用環境變數

---

## 📂 專案結構
```
expense-app/
├── src/main/java/com/example/expenseapp
│   ├── controller
│   │   ├── AuthController.java           # 認證 API
│   │   └── ExpenseController.java        # 支出 API
│   ├── model
│   │   ├── User.java                     # 使用者實體
│   │   ├── VerificationToken.java        # 驗證 Token
│   │   └── Expense.java                  # 支出實體
│   ├── repository
│   │   ├── UserRepository.java
│   │   ├── VerificationTokenRepository.java
│   │   └── ExpenseRepository.java
│   ├── service
│   │   ├── AuthService.java              # 認證邏輯
│   │   ├── EmailService.java             # Email 發送
│   │   └── ExpenseService.java           # 支出邏輯
│   ├── security
│   │   ├── SecurityConfig.java           # Security 設定
│   │   ├── JwtService.java               # JWT 處理
│   │   ├── JwtAuthenticationFilter.java  # JWT 過濾器
│   │   ├── UserDetailsServiceImpl.java   # 使用者載入
│   │   ├── CustomOAuth2UserService.java  # OAuth2 處理
│   │   ├── OAuth2AuthenticationSuccessHandler.java
│   │   └── OAuth2AuthenticationFailureHandler.java
│   ├── dto                                # 資料傳輸物件
│   ├── exception                          # 例外處理
│   └── ExpenseAppApplication.java
│
├── src/main/resources
│   ├── static
│   │   └── oauth2-test.html              # OAuth 測試頁面
│   └── application.properties            # Spring Boot 設定
│
├── src/test/java
│   ├── controller                         # Controller 測試
│   └── config
│       └── DotenvTestConfig.java          # 測試環境變數配置
│
├── .env                                   # 環境變數（不提交）
├── .env.example                           # 環境變數範例
├── pom.xml
├── DOCUMENTATION.md                       # 本文件
└── README.md
```

---

## 🔐 認證系統

### 支援的登入方式

#### 1. 傳統帳號密碼登入
- 使用者註冊（需 Email 驗證）
- Email 驗證
- 登入（返回 JWT Token）
- 忘記密碼
- 密碼重設

#### 2. Google OAuth 2.0 登入
- 使用 Google 帳號一鍵登入
- 自動建立使用者帳號
- 返回統一的 JWT Token

---

## 📑 REST API 文件

### 認證相關 API

#### 註冊
```http
POST /api/auth/register
Content-Type: application/json

{
  "username": "john_doe",
  "email": "john@example.com",
  "password": "SecurePass123",
  "name": "John Doe"
}

回應 201 Created:
{
  "message": "註冊成功！請檢查您的 Email 完成驗證"
}
```

#### 登入
```http
POST /api/auth/login
Content-Type: application/json

{
  "usernameOrEmail": "john_doe",
  "password": "SecurePass123"
}

成功回應 200 OK:
{
  "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "type": "Bearer",
  "expiresIn": 3600000,
  "user": {
    "id": 1,
    "username": "john_doe",
    "email": "john@example.com",
    "name": "John Doe",
    "status": "ACTIVE"
  }
}
```

#### Email 驗證
```http
GET /api/auth/verify?token={驗證Token}

回應 200 OK:
{
  "message": "Email 驗證成功！您現在可以登入了"
}
```

#### 忘記密碼
```http
POST /api/auth/forgot-password
Content-Type: application/json

{
  "email": "john@example.com"
}

回應 200 OK:
{
  "message": "密碼重設信已發送，請檢查您的 Email"
}
```

#### 重設密碼
```http
POST /api/auth/reset-password
Content-Type: application/json

{
  "token": "重設Token",
  "newPassword": "NewSecurePass123"
}

回應 200 OK:
{
  "message": "密碼已成功重設，請使用新密碼登入"
}
```

#### Google OAuth 登入
```http
GET /oauth2/authorization/google

# 會重定向到 Google 登入頁面
# 授權後返回：
http://localhost:8080/oauth2-test.html?token=xxx&username=xxx&email=xxx&name=xxx
```

---

### 支出管理 API

**所有支出 API 都需要 JWT Token 認證**

#### 新增支出
```http
POST /api/expenses
Authorization: Bearer {your_token}
Content-Type: application/json

{
  "title": "午餐",
  "amount": 120.50,
  "category": "餐飲",
  "expenseDate": "2025-10-08"
}

回應 201 Created
```

#### 查詢所有支出（支援分頁）
```http
GET /api/expenses
Authorization: Bearer {your_token}

# 使用分頁參數
GET /api/expenses?page=0&size=20&sortBy=expenseDate&sortDirection=desc
Authorization: Bearer {your_token}

回應 200 OK（分頁格式）:
{
  "content": [
    {
      "id": 1,
      "title": "午餐",
      "amount": 120.50,
      "category": "餐飲",
      "expenseDate": "2025-10-08"
    }
  ],
  "totalElements": 100,        // 總筆數
  "totalPages": 5,              // 總頁數
  "size": 20,                   // 每頁筆數
  "number": 0,                  // 當前頁碼
  "first": true,                // 是否第一頁
  "last": false,                // 是否最後一頁
  "numberOfElements": 20,       // 當前頁的資料筆數
  "empty": false                // 是否為空
}

分頁參數說明：
- page: 頁碼（從 0 開始，預設 0）
- size: 每頁筆數（預設 20，建議最大 100）
- sortBy: 排序欄位（預設 expenseDate，可用: expenseDate, amount, title, category）
- sortDirection: 排序方向（asc 升序 / desc 降序，預設 desc）
```

#### 根據分類查詢
```http
GET /api/expenses/category/{category}
Authorization: Bearer {your_token}

範例: GET /api/expenses/category/餐飲
```

#### 根據日期範圍查詢
```http
GET /api/expenses/date-range?startDate=2025-10-01&endDate=2025-10-31
Authorization: Bearer {your_token}
```

#### 組合查詢（分類 + 日期）
```http
GET /api/expenses/search?category=餐飲&startDate=2025-10-01&endDate=2025-10-31
Authorization: Bearer {your_token}
```

#### 取得所有分類
```http
GET /api/expenses/categories?startDate=2025-10-01&endDate=2025-10-31
Authorization: Bearer {your_token}

回應: ["餐飲", "交通", "娛樂", "購物"]
```

#### 更新支出
```http
PUT /api/expenses/{id}
Authorization: Bearer {your_token}
Content-Type: application/json

{
  "title": "晚餐",
  "amount": 200.00,
  "category": "餐飲",
  "expenseDate": "2025-10-08"
}
```

#### 刪除支出
```http
DELETE /api/expenses/{id}
Authorization: Bearer {your_token}

回應 204 No Content
```

---

## ✅ 資料驗證規則

### User（使用者）
- **username**: 3-50 字元，必填
- **email**: 有效的 Email 格式，必填
- **password**: 至少 8 字元，必填（OAuth 使用者可為空）
- **name**: 最多 100 字元，必填

### Expense（支出）
- **title**: 1-100 字元，必填
- **amount**: 大於 0，必填
- **category**: 最多 50 字元，必填
- **expenseDate**: 不能是未來日期，必填

---

## 🔒 安全性設計

### JWT Token
- **有效期**: 1 小時
- **包含資訊**: username
- **加密演算法**: HS256
- **密鑰長度**: 至少 256 位元

### 密碼安全
- 使用 **BCrypt** 加密
- 強度: 10 rounds
- 絕不明文儲存

### OAuth2 安全
- 使用 Google 官方 OAuth 2.0
- Token 由 Google 驗證
- 自動綁定或建立帳號

### API 安全
- 所有支出 API 需要 JWT 認證
- 使用者只能存取自己的資料
- 自動防止 SQL Injection（JPA）
- CSRF 保護（Stateless API 已禁用）

---

## 📧 Email 系統

### 測試環境（Mailtrap）
- 註冊：https://mailtrap.io
- 不會真的寄信
- 可以檢視信件內容

### Email 類型
1. **驗證信**
    - 註冊後自動發送
    - 有效期 24 小時
    - 包含驗證連結

2. **密碼重設信**
    - 使用者請求後發送
    - 有效期 1 小時
    - 包含重設連結

---

## 🧪 測試

### 單元測試
```bash
# 執行所有測試
mvn test

# 執行特定測試
mvn test -Dtest=AuthControllerTest
mvn test -Dtest=ExpenseControllerTest
```

### 測試覆蓋率
- **AuthControllerTest**: 22 個測試
- **ExpenseControllerTest**: 29 個測試（含 6 個分頁測試）
- **總計**: 51 個測試案例

### 分頁測試案例
- `testPagination_Basic`: 基本分頁（預設參數）
- `testPagination_SecondPage`: 第二頁查詢
- `testPagination_CustomPageSize`: 自訂每頁筆數
- `testPagination_SortAscending`: 升序排序
- `testPagination_SortDescending`: 降序排序（預設）
- `testPagination_EmptyResult`: 空結果處理

### 使用 Swagger 測試
```
http://localhost:8080/swagger-ui/index.html
```

### 使用 Postman 測試
1. 註冊/登入取得 Token
2. 在 Authorization 選擇 Bearer Token
3. 貼上 Token
4. 測試各種 API

---

## 📖 Swagger API 文件
專案啟動後訪問：
```
http://localhost:8080/swagger-ui/index.html
```

---

## ▶️ 執行專案

### 開發環境
```bash
# 1. 確保 MySQL 已啟動並建立資料庫

# 2. 設定 application.properties

# 3. 執行專案
mvn spring-boot:run

# 4. 訪問
http://localhost:8080/swagger-ui/index.html
http://localhost:8080/oauth2-test.html
```

### 打包部署
```bash
# 打包成 JAR
mvn clean package

# 執行 JAR
java -jar target/expense-app-0.0.1-SNAPSHOT.jar
```

---

## 🌐 Google OAuth 設定

### 1. 建立 Google Cloud 專案
1. 前往 https://console.cloud.google.com/
2. 建立新專案：`Expense App`
3. 啟用 Google+ API

### 2. 建立 OAuth 憑證
1. API 和服務 → 憑證
2. 建立憑證 → OAuth 用戶端 ID
3. 應用程式類型：網頁應用程式
4. 已授權的重新導向 URI：
   ```
   http://localhost:8080/login/oauth2/code/google
   ```
5. 複製 Client ID 和 Client Secret
6. 填入 `application.properties`

### 3. 測試 OAuth
訪問：
```
http://localhost:8080/oauth2-test.html
```

點擊「使用 Google 登入」按鈕

---

## 🔧 常用操作

### 查看所有使用者
```sql
SELECT id, username, email, provider, status FROM users;
```

### 查看 Google 使用者
```sql
SELECT * FROM users WHERE provider = 'google';
```

### 查看使用者的支出統計
```sql
SELECT 
    u.username,
    COUNT(e.id) AS expense_count,
    SUM(e.amount) AS total_amount
FROM users u
LEFT JOIN expenses e ON u.id = e.user_id
GROUP BY u.id;
```

### 清理過期 Token
```sql
DELETE FROM verification_tokens 
WHERE expires_at < NOW() AND used_at IS NULL;
```

---

## 📊 資料庫架構

### users 資料表
| 欄位 | 類型 | 說明 |
|------|------|------|
| id | BIGINT | 主鍵 |
| username | VARCHAR(50) | 使用者帳號 |
| email | VARCHAR(255) | Email |
| password | VARCHAR(255) | 密碼（可為 NULL） |
| name | VARCHAR(100) | 姓名 |
| status | VARCHAR(20) | 狀態 |
| google_id | VARCHAR(255) | Google ID |
| provider | VARCHAR(20) | 註冊方式 |
| avatar_url | VARCHAR(500) | 頭像 |
| created_at | TIMESTAMP | 建立時間 |
| updated_at | TIMESTAMP | 更新時間 |
| last_login_at | TIMESTAMP | 最後登入 |

### expenses 資料表
| 欄位 | 類型 | 說明 |
|------|------|------|
| id | BIGINT | 主鍵 |
| user_id | BIGINT | 使用者 ID（外鍵） |
| title | VARCHAR(100) | 標題 |
| amount | DECIMAL(12,2) | 金額 |
| category | VARCHAR(50) | 分類 |
| expense_date | DATE | 日期 |
| created_at | TIMESTAMP | 建立時間 |
| updated_at | TIMESTAMP | 更新時間 |

---

## 🆘 常見問題

### Q1: 無法啟動應用 - 找不到環境變數
**錯誤訊息**：`Could not resolve placeholder 'DB_PASSWORD' in value "${DB_PASSWORD}"`

**解決方法**：
1. 確認專案根目錄有 `.env` 文件
2. 檢查 `.env` 文件內容是否正確
3. 確認環境變數名稱拼寫正確（區分大小寫）
4. 重新啟動應用

### Q2: 測試失敗 - 環境變數未載入
**問題**：執行 `mvn test` 或 `mvn package` 時測試失敗

**解決方法**：
1. 確認測試類已加入 `@ContextConfiguration(initializers = DotenvTestConfig.class)`
2. 檢查 `DotenvTestConfig.java` 是否存在於 `src/test/java/config/` 目錄
3. 確認 `.env` 文件存在且格式正確

### Q3: 無法啟動應用 - MySQL 連線失敗
- 檢查 MySQL 是否啟動
- 檢查 `.env` 中的資料庫設定
- 確認資料庫 `expense_db` 已建立
- 檢查 8080 port 是否被佔用

### Q4: 登入失敗
- 檢查帳號是否已驗證 Email
- 檢查密碼是否正確
- 查看 logs 錯誤訊息

### Q3: Google 登入失敗
- 檢查 Client ID 和 Secret 是否正確
- 檢查 redirect URI 是否匹配
- 檢查 Google+ API 是否啟用

### Q4: Email 收不到
- 檢查 Mailtrap 設定
- 登入 Mailtrap 查看 Inbox
- 檢查 application.properties 的 SMTP 設定

### Q5: JWT Token 無效
- Token 可能已過期（1 小時）
- 檢查 jwt.secret 是否正確
- 重新登入取得新 Token

---

## 🚀 效能優化

### 資料庫索引
已建立以下索引：
- users: username, email, google_id, provider
- expenses: user_id, category, expense_date
- 組合索引: (user_id, expense_date), (user_id, category)

### JPA 設定
- `open-in-view=false` - 避免 Lazy Loading 問題
- 使用 `@Transactional` - 明確的交易邊界

---

## 📝 注意事項

### 開發環境
- **使用 .env 管理環境變數**：所有敏感資訊（MySQL 密碼、JWT Secret、SMTP 密碼等）都存放在 `.env` 文件中
- **不要提交 .env 到 Git**：`.env` 已加入 `.gitignore`，確保敏感資訊不會被上傳
- **提供 .env.example 作為範本**：團隊成員可以複製此範例並填入自己的設定
- **使用 dotenv-java 自動載入**：應用啟動和測試時會自動讀取環境變數

### 生產環境
- 設定 `spring.jpa.hibernate.ddl-auto=validate`
- 關閉 Swagger UI
- 使用環境變數管理敏感資訊
- 啟用 HTTPS
- 設定適當的 CORS 規則
- 考慮使用 Flyway 管理資料庫遷移

---

## 📞 技術支援
如有問題或建議，請透過以下方式聯繫：
- 建立 GitHub Issue
- 發送 Pull Request

---

---

## 🔄 更新日誌

### v1.1.0 (2025-10-14)
- ✅ 新增支出查詢分頁功能
- ✅ 支援靈活的排序參數（欄位、方向可自訂）
- ✅ 新增 6 個分頁相關測試案例
- ✅ 更新 API 文件，包含分頁使用範例
- ✅ 測試案例總數增加至 51 個

### v1.0.1 (2025-10-09)
- ✅ 新增 dotenv-java 支援，使用 .env 文件管理環境變數
- ✅ 新增 DotenvTestConfig 確保測試環境正確載入環境變數
- ✅ 更新文件說明環境變數設定方式
- ✅ 提供 .env.example 範例文件

### v1.0.0 (2025-10-08)
- ✅ 完成使用者認證系統（註冊、登入、Email 驗證、忘記密碼）
- ✅ 完成 Google OAuth 2.0 登入
- ✅ 完成支出管理 CRUD 功能
- ✅ 完成分類查詢、日期範圍查詢
- ✅ 完成 45 個單元測試

---

**最後更新日期：** 2025-10-14
**版本：** 1.1.0