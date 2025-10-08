# 💰 Expense App Backend

> 一個功能完整的支出管理系統，支援傳統註冊與 Google OAuth 登入

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
- 🔒 **資料隔離** - 使用者只能存取自己的資料

### 🛡️ 安全性
- ✅ **JWT 認證** - 無狀態的 API 認證
- ✅ **BCrypt 加密** - 密碼安全加密
- ✅ **資料驗證** - 自動驗證所有輸入
- ✅ **使用者隔離** - 嚴格的資料存取控制
- ✅ **防 SQL Injection** - JPA 自動防護

### 📖 開發者友善
- 🚀 **Swagger UI** - 互動式 API 文件
- 🧪 **完整測試** - 45 個單元測試
- 📝 **詳細文件** - 完整的技術文件

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

### 3. 設定應用程式
```bash
# 複製範例設定檔
cp src/main/resources/application.properties.example src/main/resources/application.properties

# 編輯設定檔，填入真實資訊
notepad src/main/resources/application.properties  # Windows
nano src/main/resources/application.properties     # Linux/Mac
```

必填項目：
- MySQL 密碼
- JWT Secret（至少 256 位元）
- Mailtrap SMTP 帳密
- Google OAuth Client ID & Secret

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
# 查詢所有
GET /api/expenses

# 根據分類
GET /api/expenses/category/餐飲

# 根據日期
GET /api/expenses/date-range?startDate=2025-10-01&endDate=2025-10-31

# 組合查詢
GET /api/expenses/search?category=餐飲&startDate=2025-10-01&endDate=2025-10-31
```

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
- ✅ 支出管理測試（23 個）
- ✅ 總計 45 個測試案例

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
│   └── application.properties   # 設定檔（不提交）
├── src/test/java/               # 測試程式碼
├── pom.xml                      # Maven 設定
├── DOCUMENTATION.md             # 詳細文件
└── README.md                    # 本文件
```

---

## 🔧 設定說明

### 開發環境 (application.properties)
```properties
# 開發時的設定
spring.jpa.hibernate.ddl-auto=update
spring.jpa.show-sql=true
springdoc.swagger-ui.enabled=true
```

### 生產環境 (application-prod.properties)
```properties
# 生產環境建議
spring.jpa.hibernate.ddl-auto=validate
spring.jpa.show-sql=false
springdoc.swagger-ui.enabled=false

# 使用環境變數
spring.datasource.password=${DB_PASSWORD}
jwt.secret=${JWT_SECRET}
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
- [x] 單元測試（45 個）

### 🔜 計劃中（v2.0）
- [ ] 支出統計 API
- [ ] 月度/年度報表
- [ ] 資料匯出（CSV/Excel）
- [ ] 資料分頁
- [ ] 預算管理
- [ ] 支出分析圖表

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