# Expense App Backend 專案文件

## 📌 專案簡介
本專案是一個功能完整的 **支出管理系統 (Expense Management App)**，
使用 **Spring Boot 3 + Spring Data JPA + MySQL + Spring Security + OAuth2** 開發。
提供完整的 **使用者認證**、**支出管理**、**預算控制**、**統計分析**、**圖表資料**、**資料匯出**等功能。

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
- **Apache POI 5.2.5 (Excel 匯出)**
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
- **budgets** - 預算管理（月度預算、分類預算）

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
│   │   ├── ExpenseController.java        # 支出 API
│   │   ├── BudgetController.java         # 預算管理 API
│   │   ├── StatisticsController.java     # 統計分析 API
│   │   └── ChartController.java          # 圖表資料 API
│   ├── model
│   │   ├── User.java                     # 使用者實體
│   │   ├── VerificationToken.java        # 驗證 Token
│   │   ├── Expense.java                  # 支出實體
│   │   └── Budget.java                   # 預算實體
│   ├── repository
│   │   ├── UserRepository.java
│   │   ├── VerificationTokenRepository.java
│   │   ├── ExpenseRepository.java
│   │   └── BudgetRepository.java
│   ├── service
│   │   ├── AuthService.java              # 認證邏輯
│   │   ├── EmailService.java             # Email 發送
│   │   ├── ExpenseService.java           # 支出邏輯
│   │   ├── BudgetService.java            # 預算邏輯
│   │   ├── StatisticsService.java        # 統計分析邏輯
│   │   ├── ChartService.java             # 圖表資料邏輯
│   │   ├── CsvExportService.java         # CSV 匯出
│   │   └── ExcelExportService.java       # Excel 匯出
│   ├── security
│   │   ├── SecurityConfig.java           # Security 設定
│   │   ├── JwtService.java               # JWT 處理
│   │   ├── JwtAuthenticationFilter.java  # JWT 過濾器
│   │   ├── UserDetailsServiceImpl.java   # 使用者載入
│   │   ├── CustomOAuth2UserService.java  # OAuth2 處理
│   │   ├── OAuth2AuthenticationSuccessHandler.java
│   │   └── OAuth2AuthenticationFailureHandler.java
│   ├── dto                                # 資料傳輸物件
│   │   ├── BudgetRequest.java            # 預算請求 DTO
│   │   ├── BudgetResponse.java           # 預算回應 DTO
│   │   ├── SummaryStatistics.java        # 總覽統計 DTO
│   │   ├── CategoryStatistics.java       # 分類統計 DTO
│   │   ├── PeriodStatistics.java         # 時間統計 DTO
│   │   ├── TrendData.java                # 趨勢圖資料 DTO
│   │   ├── PieChartData.java             # 圓餅圖資料 DTO
│   │   ├── ComparisonData.java           # 比較圖資料 DTO
│   │   └── TopExpenseItem.java           # 排行榜資料 DTO
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
├── schema.sql                             # 資料庫建表 SQL
├── pom.xml                                # Maven 設定
├── DOCUMENTATION.md                       # 本文件
└── README.md                              # 快速開始指南
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

### 預算管理 API

**所有預算 API 都需要 JWT Token 認證**

#### 建立預算
```http
# 建立月度預算
POST /api/budgets
Authorization: Bearer {your_token}
Content-Type: application/json

{
  "budgetType": "MONTHLY",
  "amount": 10000.00,
  "year": 2025,
  "month": 10
}

# 建立分類預算
POST /api/budgets
Authorization: Bearer {your_token}
Content-Type: application/json

{
  "budgetType": "CATEGORY",
  "category": "餐飲",
  "amount": 3000.00,
  "year": 2025,
  "month": 10
}

回應 201 Created:
{
  "id": 1,
  "budgetType": "MONTHLY",
  "category": null,
  "amount": 10000.00,
  "year": 2025,
  "month": 10,
  "spent": 0.00,
  "remaining": 10000.00,
  "percentage": 0.0,
  "createdAt": "2025-10-15",
  "updatedAt": "2025-10-15"
}
```

#### 查詢預算
```http
# 取得當月所有預算
GET /api/budgets/current
Authorization: Bearer {your_token}

# 取得指定月份預算
GET /api/budgets?year=2025&month=10
Authorization: Bearer {your_token}

# 取得單一預算詳情
GET /api/budgets/{id}
Authorization: Bearer {your_token}

回應範例（包含計算欄位）:
{
  "id": 1,
  "budgetType": "MONTHLY",
  "category": null,
  "amount": 10000.00,
  "year": 2025,
  "month": 10,
  "spent": 5500.00,      // 已使用金額（自動計算）
  "remaining": 4500.00,  // 剩餘金額（自動計算）
  "percentage": 55.0,    // 使用百分比（自動計算）
  "createdAt": "2025-10-01",
  "updatedAt": "2025-10-01"
}
```

#### 更新預算
```http
PUT /api/budgets/{id}
Authorization: Bearer {your_token}
Content-Type: application/json

{
  "budgetType": "MONTHLY",
  "amount": 12000.00,
  "year": 2025,
  "month": 10
}

回應 200 OK（包含最新的計算結果）
```

#### 刪除預算
```http
DELETE /api/budgets/{id}
Authorization: Bearer {your_token}

回應 204 No Content
```

**預算類型說明：**
- **MONTHLY**: 月度總預算，統計該月所有支出
- **CATEGORY**: 分類預算，統計特定分類的支出

**自動計算欄位：**
- `spent`: 根據預算類型查詢對應的支出總額
- `remaining`: amount - spent
- `percentage`: (spent / amount) × 100

---

### 統計分析 API

**所有統計 API 都需要 JWT Token 認證**

#### 總覽統計
```http
GET /api/statistics/summary?startDate=2025-10-01&endDate=2025-10-31
Authorization: Bearer {your_token}

回應 200 OK:
{
  "totalAmount": 5000.00,      // 總金額
  "totalCount": 50,             // 總筆數
  "averageAmount": 100.00,      // 平均金額
  "maxAmount": 500.00,          // 最大金額
  "minAmount": 10.00            // 最小金額
}
```

#### 分類統計
```http
GET /api/statistics/category?startDate=2025-10-01&endDate=2025-10-31
Authorization: Bearer {your_token}

回應 200 OK:
[
  {
    "category": "餐飲",
    "totalAmount": 2000.00,
    "count": 20,
    "percentage": 40.00      // 占總額的百分比
  },
  {
    "category": "交通",
    "totalAmount": 1500.00,
    "count": 15,
    "percentage": 30.00
  }
]
```

#### 月度統計（每日）
```http
GET /api/statistics/monthly?year=2025&month=10
Authorization: Bearer {your_token}

回應 200 OK（該月每一天的統計）:
[
  {
    "period": "2025-10-01",
    "totalAmount": 150.00,
    "count": 3
  },
  {
    "period": "2025-10-02",
    "totalAmount": 200.00,
    "count": 5
  }
]
```

#### 年度統計（每月）
```http
GET /api/statistics/yearly?year=2025
Authorization: Bearer {your_token}

回應 200 OK（該年每個月的統計）:
[
  {
    "period": "2025-01",
    "totalAmount": 4500.00,
    "count": 45
  },
  {
    "period": "2025-02",
    "totalAmount": 5000.00,
    "count": 50
  }
]
```

#### 當月/當年統計（便利方法）
```http
# 取得當月統計
GET /api/statistics/current-month
Authorization: Bearer {your_token}

# 取得當年統計
GET /api/statistics/current-year
Authorization: Bearer {your_token}
```

---

### 圖表資料 API

**所有圖表 API 都需要 JWT Token 認證**

#### 趨勢圖資料（折線圖）
```http
# 每日趨勢
GET /api/charts/trend/daily?year=2025&month=10
Authorization: Bearer {your_token}

# 月度趨勢
GET /api/charts/trend/monthly?year=2025
Authorization: Bearer {your_token}

回應範例（包含所有日期/月份，無資料的顯示 0）:
[
  {
    "period": "2025-10-01",
    "amount": 150.00,
    "count": 3
  },
  {
    "period": "2025-10-02",
    "amount": 0.00,
    "count": 0
  }
]
```

#### 圓餅圖資料（分類佔比）
```http
GET /api/charts/pie/category?startDate=2025-10-01&endDate=2025-10-31
Authorization: Bearer {your_token}

回應範例:
[
  {
    "label": "餐飲",
    "value": 3000.00,
    "percentage": 40.0,
    "count": 20
  },
  {
    "label": "交通",
    "value": 2250.00,
    "percentage": 30.0,
    "count": 15
  }
]
```

#### 比較圖資料（長條圖）
```http
# 月度比較（最近 N 個月）
GET /api/charts/comparison/monthly?months=6
Authorization: Bearer {your_token}

# 分類比較
GET /api/charts/comparison/category?startDate=2025-10-01&endDate=2025-10-31
Authorization: Bearer {your_token}

回應範例:
{
  "labels": ["2025-05", "2025-06", "2025-07", "2025-08", "2025-09", "2025-10"],
  "amounts": [5000.00, 4500.00, 6000.00, 5500.00, 4800.00, 5200.00],
  "counts": [50, 45, 60, 55, 48, 52]
}
```

#### Top N 排行榜
```http
GET /api/charts/top-expenses?startDate=2025-10-01&endDate=2025-10-31&limit=10
Authorization: Bearer {your_token}

回應範例（按金額排序）:
[
  {
    "id": 123,
    "title": "筆記型電腦",
    "amount": 35000.00,
    "category": "電子產品",
    "expenseDate": "2025-10-15",
    "rank": 1
  },
  {
    "id": 124,
    "title": "手機",
    "amount": 18000.00,
    "category": "電子產品",
    "expenseDate": "2025-10-18",
    "rank": 2
  }
]
```

---

### 資料匯出 API

**所有匯出 API 都需要 JWT Token 認證**

#### CSV 匯出

##### 匯出所有支出
```http
GET /api/expenses/export/csv
Authorization: Bearer {your_token}

# 下載檔案：expenses_20251015.csv
```

##### 依日期範圍匯出
```http
GET /api/expenses/export/csv/date-range?startDate=2025-10-01&endDate=2025-10-31
Authorization: Bearer {your_token}

# 下載檔案：expenses_20251001_to_20251031.csv
```

##### 依分類匯出
```http
GET /api/expenses/export/csv/category/餐飲
Authorization: Bearer {your_token}

# 下載檔案：expenses_餐飲_20251015.csv
```

**CSV 格式：**
```csv
ID,標題,金額,分類,日期
1,"午餐",120.00,"餐飲",2025-10-15
2,"捷運",30.00,"交通",2025-10-15
```

**功能特點：**
- ✅ UTF-8 編碼（含 BOM，Excel 可正確開啟）
- ✅ 自動處理特殊字元（逗號、引號）
- ✅ 檔名支援中文（使用 RFC 5987 編碼）
- ✅ 支援所有查詢條件

#### Excel 匯出

##### 匯出 Excel（多工作表）
```http
GET /api/expenses/export/excel/date-range?startDate=2025-10-01&endDate=2025-10-31
Authorization: Bearer {your_token}

# 下載檔案：expenses_20251001_to_20251031.xlsx
```

**Excel 檔案結構（3 個工作表）：**

**工作表 1：支出明細**
- 包含所有支出記錄
- 欄位：ID、標題、金額、分類、日期
- 最後一行：總計（使用 SUM 公式自動計算）

**工作表 2：統計摘要**
- 總支出金額
- 支出筆數
- 平均金額
- 最大金額
- 最小金額

**工作表 3：分類統計**
- 各分類的支出金額
- 各分類的支出筆數
- 各分類佔總額的百分比

**格式特色：**
- ✅ 專業樣式（標題灰底、粗體、邊框）
- ✅ 貨幣格式（千分位逗號，如 1,234.56）
- ✅ 百分比格式（如 25.50%）
- ✅ 日期格式（居中對齊）
- ✅ 自動計算公式（SUM）
- ✅ UTF-8 編碼（支援中文檔名）

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
- **StatisticsControllerTest**: 10 個測試
- **BudgetControllerTest**: 16 個測試
- **ChartControllerTest**: 17 個測試
- **CsvExportServiceTest**: 12 個測試
- **ExcelExportServiceTest**: 12 個測試
- **總計**: 118 個測試案例

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

### budgets 資料表
| 欄位 | 類型 | 說明 |
|------|------|------|
| id | BIGINT | 主鍵 |
| user_id | BIGINT | 使用者 ID（外鍵） |
| budget_type | VARCHAR(20) | 預算類型（MONTHLY / CATEGORY） |
| category | VARCHAR(50) | 分類名稱（分類預算使用） |
| amount | DECIMAL(10,2) | 預算金額 |
| year | INT | 年份 |
| month | INT | 月份 |
| created_at | DATE | 建立時間 |
| updated_at | DATE | 更新時間 |

**索引與約束：**
- 唯一約束：(user_id, budget_type, category, year, month)
- 索引：idx_user_year_month, idx_budget_type
- 外鍵：關聯到 users 資料表（CASCADE DELETE）

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

### v2.0.0 (2025-10-16)
- ✅ **預算管理功能**
  - 支援月度預算和分類預算
  - 自動計算已用、剩餘金額和使用百分比
  - 完整的 CRUD API（6 個端點）
- ✅ **圖表資料 API**
  - 趨勢圖：每日趨勢、月度趨勢
  - 圓餅圖：分類佔比分析
  - 比較圖：月度比較、分類比較
  - 排行榜：Top N 高額支出
- ✅ **Excel 匯出功能**
  - 多工作表設計（明細、統計、分類）
  - 專業格式（樣式、貨幣、百分比、公式）
  - 支援中文檔名（UTF-8 編碼）
- ✅ 新增 Apache POI 5.2.5 依賴
- ✅ 更新完整的 API 文件

### v1.2.0 (2025-10-15)
- ✅ 新增支出統計 API
  - 總覽統計（總額、筆數、平均、最大/最小）
  - 分類統計（各分類金額、筆數、佔比）
  - 月度統計（該月每日資料）
  - 年度統計（該年每月資料）
- ✅ 新增 CSV 匯出功能
  - 支援全部匯出、日期範圍匯出、分類匯出
  - UTF-8 編碼含 BOM，Excel 可正確開啟
  - 檔名支援中文（RFC 5987 編碼）

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

**最後更新日期：** 2025-10-16
**版本：** 2.0.0