# 💰 Expense App Backend

> 一個功能完整的支出管理系統，支援使用者認證、支出管理、預算控制、統計分析與資料匯出

[![Java](https://img.shields.io/badge/Java-21-orange.svg)](https://www.oracle.com/java/)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.4.5-brightgreen.svg)](https://spring.io/projects/spring-boot)
[![Spring Security](https://img.shields.io/badge/Spring%20Security-6.4.5-green.svg)](https://spring.io/projects/spring-security)
[![MySQL](https://img.shields.io/badge/MySQL-8.x-blue.svg)](https://www.mysql.com/)
[![License](https://img.shields.io/badge/License-MIT-yellow.svg)](LICENSE)

---

## ✨ 核心功能

### 🔐 使用者認證
- 📝 **傳統註冊登入** - 支援帳號密碼註冊，Email 驗證
- 🔑 **Google OAuth 2.0** - 一鍵使用 Google 帳號登入
- 🎫 **JWT Token** - 統一的身份驗證機制
- 📧 **Email 驗證** - 註冊後需驗證 Email
- 🔄 **忘記密碼** - 透過 Email 重設密碼

### 💸 支出管理
- ➕ **新增支出** - 記錄每筆支出
- 📊 **分類管理** - 靈活的支出分類
- 🔍 **智慧查詢** - 支援分類、日期範圍、組合查詢
- ✏️ **編輯刪除** - 完整的 CRUD 操作
- 📄 **分頁支援** - 高效能資料查詢
- 🔒 **資料隔離** - 使用者只能存取自己的資料

### 💰 預算管理
- 📅 **月度預算** - 設定每月總支出上限
- 🏷️ **分類預算** - 針對特定分類設定預算
- 📈 **使用追蹤** - 即時顯示已用、剩餘金額和百分比
- ⚠️ **預算警示** - 自動計算預算使用狀況

### 📊 統計分析
- 📉 **總覽統計** - 總額、筆數、平均、最大/最小值
- 🥧 **分類統計** - 各分類支出金額與佔比
- 📆 **時間統計** - 月度/年度支出趨勢
- 📈 **圖表資料** - 提供完整的圖表 API（趨勢、佔比、比較、排行）

### 📥 資料匯出
- 📄 **CSV 匯出** - 輕量級純文字格式
- 📊 **Excel 匯出** - 多工作表、豐富格式、自動計算
- 🎨 **專業排版** - 標題樣式、邊框、貨幣格式、百分比格式
- 📑 **多種篩選** - 支援日期範圍、分類篩選

### 🛡️ 安全性
- ✅ **JWT 認證** - 無狀態的 API 認證
- ✅ **BCrypt 加密** - 密碼安全加密
- ✅ **資料驗證** - 自動驗證所有輸入
- ✅ **使用者隔離** - 嚴格的資料存取控制
- ✅ **防 SQL Injection** - JPA 自動防護

### 📖 開發者友善
- 🚀 **Swagger UI** - 互動式 API 文件
- 🧪 **完整測試** - 118 個單元測試
- 📝 **詳細文件** - 完整的技術文件
- 🔄 **RESTful API** - 標準化的 API 設計

---

## 🛠️ 技術棧

| 技術 | 版本 | 用途 |
|------|------|------|
| Java | 21 | 程式語言 |
| Spring Boot | 3.4.5 | 應用框架 |
| Spring Security | 6.4.5 | 安全認證 |
| Spring Data JPA | 3.4.5 | 資料持久化 |
| OAuth2 Client | 6.4.5 | Google 登入 |
| MySQL | 8.x | 資料庫 |
| JWT (jjwt) | 0.11.5 | Token 管理 |
| Apache POI | 5.2.5 | Excel 匯出 |
| dotenv-java | 3.0.0 | 環境變數管理 |
| JavaMail | - | Email 發送 |
| Swagger | 2.2.0 | API 文件 |
| JUnit 5 | - | 單元測試 |

---

## 🚀 快速開始

### 前置需求
- ☕ Java 21+
- 📦 Maven 3.6+
- 🐬 MySQL 8.0+
- 📧 Mailtrap 帳號（測試用）
- 🔐 Google Cloud 專案（OAuth 用）

### 1. Clone 專案
```bash
git clone https://github.com/yourusername/expense-app-backend.git
cd expense-app-backend
```

### 2. 建立資料庫
```bash
mysql -u root -p
```

```sql
CREATE DATABASE expense_db CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
```

執行建表 SQL（位於專案根目錄）

### 3. 設定環境變數
本專案使用 `.env` 文件管理環境變數，更安全且易於管理。

```bash
# 複製環境變數範例檔
cp .env.example .env

# 編輯 .env 檔案，填入真實資訊
notepad .env     # Windows
nano .env        # Linux/Mac
```

**必填項目：**
- `DB_PASSWORD` - MySQL 密碼
- `JWT_SECRET` - JWT 密鑰（至少 256 位元）
- `MAIL_USERNAME` & `MAIL_PASSWORD` - Mailtrap SMTP 帳密
- `GOOGLE_CLIENT_ID` & `GOOGLE_CLIENT_SECRET` - Google OAuth 憑證

**`.env` 檔案範例：**
```properties
# 資料庫設定
DB_URL=jdbc:mysql://localhost:3306/expense_db
DB_USERNAME=root
DB_PASSWORD=你的MySQL密碼

# JWT 設定
JWT_SECRET=你的256位元密鑰
JWT_EXPIRATION=3600000

# Email 設定（Mailtrap）
MAIL_HOST=smtp.mailtrap.io
MAIL_PORT=2525
MAIL_USERNAME=你的Mailtrap帳號
MAIL_PASSWORD=你的Mailtrap密碼

# Google OAuth
GOOGLE_CLIENT_ID=你的Google_Client_ID
GOOGLE_CLIENT_SECRET=你的Google_Client_Secret
```

> **注意**：`.env` 文件已加入 `.gitignore`，不會被提交到版本控制系統

### 4. 啟動應用
```bash
mvn spring-boot:run
```

### 5. 驗證運行
開啟瀏覽器訪問：
- **Swagger UI**：http://localhost:8080/swagger-ui/index.html
- **OAuth 測試**：http://localhost:8080/oauth2-test.html

---

## 📑 API 快速指南

### 🔐 認證 API

#### 註冊
```bash
POST http://localhost:8080/api/auth/register
Content-Type: application/json

{
  "username": "john_doe",
  "email": "john@example.com",
  "password": "password123",
  "name": "John Doe"
}
```

#### 登入
```bash
POST http://localhost:8080/api/auth/login
Content-Type: application/json

{
  "usernameOrEmail": "john_doe",
  "password": "password123"
}

# 返回 JWT Token
```

#### Google 登入
```bash
# 瀏覽器訪問
GET http://localhost:8080/oauth2/authorization/google
```

### 💸 支出 API

**所有支出 API 需要在 Header 加入 JWT Token：**
```
Authorization: Bearer {your_token}
```

#### 新增支出
```bash
POST http://localhost:8080/api/expenses
Authorization: Bearer {token}
Content-Type: application/json

{
  "title": "午餐",
  "amount": 120.00,
  "category": "餐飲",
  "expenseDate": "2025-10-08"
}
```

#### 查詢支出
```bash
# 查詢所有（支援分頁）
GET /api/expenses
GET /api/expenses?page=0&size=20&sortBy=expenseDate&sortDirection=desc

# 根據分類
GET /api/expenses/category/餐飲

# 根據日期
GET /api/expenses/date-range?startDate=2025-10-01&endDate=2025-10-31

# 組合查詢
GET /api/expenses/search?category=餐飲&startDate=2025-10-01&endDate=2025-10-31
```

**分頁參數說明：**
- `page` - 頁碼（從 0 開始，預設 0）
- `size` - 每頁筆數（預設 20）
- `sortBy` - 排序欄位（預設 expenseDate）
- `sortDirection` - 排序方向：asc（升序）或 desc（降序，預設）

**分頁回應格式：**
```json
{
  "content": [...],           // 當前頁的資料
  "totalElements": 100,       // 總筆數
  "totalPages": 5,            // 總頁數
  "size": 20,                 // 每頁筆數
  "number": 0,                // 當前頁碼
  "first": true,              // 是否第一頁
  "last": false               // 是否最後一頁
}
```

### 📊 統計 API

**所有統計 API 需要在 Header 加入 JWT Token**

#### 總覽統計
```bash
GET /api/statistics/summary?startDate=2025-10-01&endDate=2025-10-31
Authorization: Bearer {token}

# 回應
{
  "totalAmount": 5000.00,      // 總金額
  "totalCount": 50,             // 總筆數
  "averageAmount": 100.00,      // 平均金額
  "maxAmount": 500.00,          // 最大金額
  "minAmount": 10.00            // 最小金額
}
```

#### 分類統計
```bash
GET /api/statistics/category?startDate=2025-10-01&endDate=2025-10-31
Authorization: Bearer {token}

# 回應
[
  {
    "category": "餐飲",
    "totalAmount": 2000.00,
    "count": 20,
    "percentage": 40.00      // 占比
  }
]
```

#### 月度統計（每日）
```bash
GET /api/statistics/monthly?year=2025&month=10
Authorization: Bearer {token}

# 回應：該月每一天的統計
[
  {
    "period": "2025-10-01",
    "totalAmount": 150.00,
    "count": 3
  }
]
```

#### 年度統計（每月）
```bash
GET /api/statistics/yearly?year=2025
Authorization: Bearer {token}

# 回應：該年每個月的統計
[
  {
    "period": "2025-10",
    "totalAmount": 5000.00,
    "count": 50
  }
]
```

#### 當月/當年統計（便利方法）
```bash
GET /api/statistics/current-month
GET /api/statistics/current-year
```

### 📥 CSV 匯出 API

**所有匯出 API 需要在 Header 加入 JWT Token**

#### 匯出所有支出
```bash
GET /api/expenses/export/csv
Authorization: Bearer {token}

# 下載檔案：expenses_20251015.csv
```

#### 依日期範圍匯出
```bash
GET /api/expenses/export/csv/date-range?startDate=2025-10-01&endDate=2025-10-31
Authorization: Bearer {token}

# 下載檔案：expenses_20251001_to_20251031.csv
```

#### 依分類匯出
```bash
GET /api/expenses/export/csv/category/餐飲
Authorization: Bearer {token}

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
- ✅ 檔名包含日期或分類
- ✅ 支援所有查詢條件（日期範圍、分類）

### 💰 預算管理 API

**所有預算 API 需要在 Header 加入 JWT Token**

#### 建立預算
```bash
# 建立月度預算
POST /api/budgets
Authorization: Bearer {token}
Content-Type: application/json

{
  "budgetType": "MONTHLY",
  "amount": 10000.00,
  "year": 2025,
  "month": 10
}

# 建立分類預算
POST /api/budgets
Authorization: Bearer {token}
Content-Type: application/json

{
  "budgetType": "CATEGORY",
  "category": "餐飲",
  "amount": 3000.00,
  "year": 2025,
  "month": 10
}
```

#### 查詢預算
```bash
# 取得當月所有預算
GET /api/budgets/current
Authorization: Bearer {token}

# 取得指定月份預算
GET /api/budgets?year=2025&month=10
Authorization: Bearer {token}

# 取得單一預算詳情
GET /api/budgets/{id}
Authorization: Bearer {token}

# 回應範例
{
  "id": 1,
  "budgetType": "MONTHLY",
  "category": null,
  "amount": 10000.00,
  "year": 2025,
  "month": 10,
  "spent": 5500.00,      // 已使用金額
  "remaining": 4500.00,  // 剩餘金額
  "percentage": 55.0     // 使用百分比
}
```

#### 更新/刪除預算
```bash
# 更新預算金額
PUT /api/budgets/{id}
Authorization: Bearer {token}
Content-Type: application/json

{
  "budgetType": "MONTHLY",
  "amount": 12000.00,
  "year": 2025,
  "month": 10
}

# 刪除預算
DELETE /api/budgets/{id}
Authorization: Bearer {token}
```

### 📊 圖表資料 API

**所有圖表 API 需要在 Header 加入 JWT Token**

#### 趨勢圖（折線圖）
```bash
# 每日趨勢
GET /api/charts/trend/daily?year=2025&month=10
Authorization: Bearer {token}

# 月度趨勢
GET /api/charts/trend/monthly?year=2025
Authorization: Bearer {token}

# 回應範例
[
  {
    "period": "2025-10-01",
    "amount": 150.00,
    "count": 3
  }
]
```

#### 圓餅圖（分類佔比）
```bash
GET /api/charts/pie/category?startDate=2025-10-01&endDate=2025-10-31
Authorization: Bearer {token}

# 回應範例
[
  {
    "label": "餐飲",
    "value": 3000.00,
    "percentage": 40.0,
    "count": 20
  }
]
```

#### 比較圖（長條圖）
```bash
# 月度比較（最近 N 個月）
GET /api/charts/comparison/monthly?months=6
Authorization: Bearer {token}

# 分類比較
GET /api/charts/comparison/category?startDate=2025-10-01&endDate=2025-10-31
Authorization: Bearer {token}

# 回應範例
{
  "labels": ["2025-05", "2025-06", "2025-07"],
  "amounts": [5000.00, 4500.00, 6000.00],
  "counts": [50, 45, 60]
}
```

#### Top N 排行榜
```bash
GET /api/charts/top-expenses?startDate=2025-10-01&endDate=2025-10-31&limit=10
Authorization: Bearer {token}

# 回應範例
[
  {
    "id": 123,
    "title": "筆記型電腦",
    "amount": 35000.00,
    "category": "電子產品",
    "expenseDate": "2025-10-15",
    "rank": 1
  }
]
```

### 📊 Excel 匯出 API

**Excel 匯出 API 需要在 Header 加入 JWT Token**

#### 匯出 Excel（多工作表）
```bash
GET /api/expenses/export/excel/date-range?startDate=2025-10-01&endDate=2025-10-31
Authorization: Bearer {token}

# 下載檔案：expenses_20251001_to_20251031.xlsx
```

**Excel 檔案結構：**
- **工作表 1：支出明細** - 完整資料 + 總計公式
- **工作表 2：統計摘要** - 總額、筆數、平均、最大/最小
- **工作表 3：分類統計** - 各分類金額、筆數、佔比

**格式特色：**
- ✅ 專業樣式（標題灰底、粗體、邊框）
- ✅ 貨幣格式（千分位逗號）
- ✅ 百分比格式（如 25.50%）
- ✅ 自動計算公式（SUM）
- ✅ UTF-8 編碼（支援中文檔名）

---

## 🧪 執行測試

```bash
# 執行所有測試
mvn test

# 執行特定測試
mvn test -Dtest=AuthControllerTest
mvn test -Dtest=ExpenseControllerTest

# 產生測試覆蓋率報告
mvn test jacoco:report
```

**測試覆蓋：**
- ✅ 認證功能測試（22 個）
- ✅ 支出管理測試（29 個，含 6 個分頁測試）
- ✅ 統計分析測試（10 個）
- ✅ 預算管理測試（16 個）
- ✅ 圖表資料測試（17 個）
- ✅ CSV 匯出測試（12 個）
- ✅ Excel 匯出測試（12 個）
- ✅ **總計 118 個測試案例**

---

## 📊 資料模型

### User（使用者）
- `username` - 使用者帳號
- `email` - Email
- `password` - 密碼（OAuth 使用者可為空）
- `name` - 姓名
- `provider` - 註冊方式（local / google）
- `google_id` - Google 使用者 ID
- `status` - 狀態（UNVERIFIED / ACTIVE / SUSPENDED）

### Expense（支出）
- `title` - 標題
- `amount` - 金額
- `category` - 分類
- `expense_date` - 日期
- `user_id` - 所屬使用者（外鍵）

### Budget（預算）
- `budget_type` - 預算類型（MONTHLY / CATEGORY）
- `category` - 分類名稱（分類預算使用）
- `amount` - 預算金額
- `year` - 年份
- `month` - 月份
- `user_id` - 所屬使用者（外鍵）

---

## 🌐 Google OAuth 設定

### 1. 建立 Google Cloud 專案
1. 前往 [Google Cloud Console](https://console.cloud.google.com/)
2. 建立新專案
3. 啟用 Google+ API

### 2. 建立 OAuth 憑證
1. 憑證 → 建立憑證 → OAuth 用戶端 ID
2. 應用程式類型：**網頁應用程式**
3. 已授權的重新導向 URI：
   ```
   http://localhost:8080/login/oauth2/code/google
   ```
4. 複製 Client ID 和 Client Secret
5. 貼到 `application.properties`

### 3. 測試 OAuth
訪問：http://localhost:8080/oauth2-test.html

---

## 📂 專案結構

```
expense-app/
├── src/main/java/com/example/expenseapp
│   ├── controller/              # REST API 端點
│   ├── model/                   # 資料模型
│   ├── repository/              # 資料存取層
│   ├── service/                 # 業務邏輯層
│   ├── security/                # 安全認證
│   ├── dto/                     # 資料傳輸物件
│   └── exception/               # 例外處理
├── src/main/resources/
│   ├── static/                  # 靜態資源
│   └── application.properties   # Spring Boot 設定
├── src/test/java/
│   ├── controller/              # Controller 測試
│   └── config/
│       └── DotenvTestConfig.java # 測試環境變數配置
├── .env                         # 環境變數（不提交）
├── .env.example                 # 環境變數範例
├── pom.xml                      # Maven 設定
├── DOCUMENTATION.md             # 詳細文件
└── README.md                    # 本文件
```

---

## 🔧 設定說明

### 環境變數管理
本專案使用 **dotenv-java** 管理環境變數，提供以下優勢：
- ✅ 敏感資訊不會被提交到 Git
- ✅ 開發和生產環境配置分離
- ✅ 團隊協作時配置更簡單
- ✅ 符合 [12-Factor App](https://12factor.net/) 原則

### 開發環境設定
1. 複製 `.env.example` 為 `.env`
2. 填入真實的環境變數值
3. Spring Boot 會自動從 `.env` 載入配置

### application.properties
`application.properties` 使用環境變數占位符：
```properties
# 資料庫設定
spring.datasource.url=${DB_URL}
spring.datasource.username=${DB_USERNAME}
spring.datasource.password=${DB_PASSWORD}

# JWT 設定
jwt.secret=${JWT_SECRET}
jwt.expiration=${JWT_EXPIRATION}

# Email 設定
spring.mail.host=${MAIL_HOST}
spring.mail.username=${MAIL_USERNAME}
spring.mail.password=${MAIL_PASSWORD}
```

### 生產環境部署
生產環境建議使用系統環境變數或容器配置（如 Docker、Kubernetes）：
```bash
# 設置環境變數
export DB_PASSWORD=your_password
export JWT_SECRET=your_secret

# 啟動應用
java -jar expense-app.jar
```

---

## 📖 詳細文件

想了解更多？請查看 [DOCUMENTATION.md](DOCUMENTATION.md)，內含：
- 完整 API 文件
- 資料庫設計說明
- 安全性詳解
- 部署指南
- 常見問題解答

---

## 🗺️ 開發路線圖

### ✅ 已完成（v1.0）
- [x] 使用者註冊/登入
- [x] Email 驗證
- [x] 忘記密碼
- [x] Google OAuth 登入
- [x] JWT 認證
- [x] 支出 CRUD
- [x] 分類查詢
- [x] 日期範圍查詢
- [x] Swagger 文件
- [x] 單元測試（51 個）

### ✅ 已完成（v1.1）
- [x] 支出資料分頁查詢
- [x] 靈活的排序功能
- [x] 分頁測試覆蓋

### ✅ 已完成（v1.2）
- [x] 支出統計 API（總覽、分類、月度、年度）
- [x] 資料匯出（CSV）

### ✅ 已完成（v2.0）
- [x] 預算管理（月度預算、分類預算）
- [x] 支出分析圖表 API（趨勢、圓餅、比較、排行）
- [x] Excel 匯出（多工作表、專業格式）

### 🔜 計劃中（v2.1）
- [ ] 訂閱功能（免費/Pro/Enterprise）

### 💡 未來構想（v3.0）
- [ ] 前端介面（React/Vue）
- [ ] 多幣別支援
- [ ] 共享帳本（多人協作）
- [ ] 行動 App
- [ ] 定期支出提醒
- [ ] AI 支出建議

---

## 🤝 貢獻指南

歡迎提交 Issue 和 Pull Request！

### 開發流程
1. Fork 本專案
2. 建立特性分支 (`git checkout -b feature/AmazingFeature`)
3. 提交變更 (`git commit -m 'Add some AmazingFeature'`)
4. 推送到分支 (`git push origin feature/AmazingFeature`)
5. 開啟 Pull Request

### Commit 規範
- `feat`: 新功能
- `fix`: 修正錯誤
- `docs`: 文件更新
- `test`: 測試相關
- `refactor`: 重構程式碼

---

## 🐛 問題回報

遇到問題？請到 [Issues](https://github.com/yourusername/expense-app-backend/issues) 回報，並提供：
- 問題描述
- 錯誤訊息
- 重現步驟
- 環境資訊（OS、Java 版本等）

---

## 📝 授權條款

本專案採用 MIT 授權條款 - 詳見 [LICENSE](LICENSE) 檔案

---

## 🌟 致謝

感謝以下技術和社群：
- Spring Boot 團隊
- Google OAuth 文件
- 所有貢獻者

---

## 📧 聯絡方式

- **專案連結**：[GitHub Repository](https://github.com/yourusername/expense-app-backend)
- **問題回報**：[Issues](https://github.com/yourusername/expense-app-backend/issues)
- **開發者**：您的名字

---

<div align="center">

### ⭐ 如果這個專案對你有幫助，請給我一個星星！

**Made with ❤️ by [Your Name]**

[⬆ 回到頂部](#-expense-app-backend)

</div>