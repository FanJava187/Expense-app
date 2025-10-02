# 💰 Expense App Backend

> 一個功能完整的支出管理系統，基於 Spring Boot 3 + MySQL 開發

[![Java](https://img.shields.io/badge/Java-21-orange.svg)](https://www.oracle.com/java/)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.4.5-brightgreen.svg)](https://spring.io/projects/spring-boot)
[![MySQL](https://img.shields.io/badge/MySQL-8.x-blue.svg)](https://www.mysql.com/)
[![License](https://img.shields.io/badge/License-MIT-yellow.svg)](LICENSE)

---

## ✨ 特色功能

- 🔥 **完整 CRUD API** - 新增、查詢、更新、刪除支出紀錄
- 🔍 **智慧查詢** - 支援分類查詢、日期範圍查詢、組合查詢
- ✅ **資料驗證** - 自動驗證金額、日期、必填欄位
- 📊 **分類管理** - 靈活的支出分類系統
- 📖 **Swagger 文件** - 互動式 API 文件，即測即用
- 🧪 **完整測試** - 17 個單元測試案例，涵蓋所有功能
- 🚀 **開箱即用** - 自動初始化範例資料

---

## 🛠️ 技術棧

| 技術 | 版本 | 說明 |
|------|------|------|
| Java | 21 | 程式語言 |
| Spring Boot | 3.4.5 | 應用框架 |
| Spring Data JPA | - | ORM 框架 |
| MySQL | 8.x | 資料庫 |
| Hibernate Validator | 8.0.1 | 資料驗證 |
| Swagger/OpenAPI | 2.2.0 | API 文件 |
| JUnit 5 | - | 單元測試 |

---

## 🚀 快速開始

### 1. 前置需求
- Java 21+
- Maven 3.6+
- MySQL 8.0+

### 2. 建立資料庫
```sql
CREATE DATABASE expense_db CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
```

### 3. 設定資料庫連線
修改 `src/main/resources/application.properties`：
```properties
spring.datasource.url=jdbc:mysql://localhost:3306/expense_db
spring.datasource.username=root
spring.datasource.password=你的密碼
```

### 4. 啟動應用
```bash
# 方式 1: 使用 Maven
mvn spring-boot:run

# 方式 2: 打包後執行
mvn clean package
java -jar target/expense-app-0.0.1-SNAPSHOT.jar
```

### 5. 驗證運行
開啟瀏覽器訪問：
- **API 文件**：http://localhost:8080/swagger-ui/index.html
- **測試 API**：http://localhost:8080/api/expenses

---

## 📑 API 快速指南

### 基本操作

```bash
# 查詢所有支出
GET http://localhost:8080/api/expenses

# 新增支出
POST http://localhost:8080/api/expenses
{
  "title": "午餐",
  "amount": 120.00,
  "category": "餐飲",
  "expenseDate": "2025-09-30"
}

# 更新支出
PUT http://localhost:8080/api/expenses/{id}

# 刪除支出
DELETE http://localhost:8080/api/expenses/{id}
```

### 查詢功能

```bash
# 根據分類查詢
GET /api/expenses/category/餐飲

# 根據日期範圍查詢
GET /api/expenses/date-range?startDate=2025-09-01&endDate=2025-09-30

# 組合查詢（分類 + 日期）
GET /api/expenses/search?category=餐飲&startDate=2025-09-01&endDate=2025-09-30

# 取得所有分類
GET /api/expenses/categories?startDate=2025-09-01&endDate=2025-09-30
```

**💡 提示：** 推薦使用 [Swagger UI](http://localhost:8080/swagger-ui/index.html) 進行 API 測試！

---

## 🧪 執行測試

```bash
# 執行所有測試
mvn test

# 執行特定測試
mvn test -Dtest=ExpenseControllerTest

# 生成測試報告
mvn test jacoco:report
```

**測試覆蓋：**
- ✅ 基本 CRUD 操作（6 個測試）
- ✅ 資料驗證規則（5 個測試）
- ✅ 查詢功能（6 個測試）

---

## 📂 專案結構

```
expense-app/
├── src/main/java/com/example/expenseapp
│   ├── controller/          # REST API 控制器
│   ├── model/               # 實體模型
│   ├── repository/          # 資料存取層
│   ├── service/             # 業務邏輯層
│   └── initializer/         # 資料初始化
├── src/test/java/           # 單元測試
├── src/main/resources/      # 設定檔
├── DOCUMENTATION.md         # 完整文件
└── README.md               # 本文件
```

---

## 📊 資料模型

| 欄位 | 類型 | 必填 | 說明 |
|------|------|------|------|
| id | Long | 自動 | 主鍵 |
| title | String | ✅ | 支出標題（1-100 字元）|
| amount | BigDecimal | ✅ | 支出金額（必須 > 0）|
| category | String | ✅ | 支出分類（最多 50 字元）|
| expenseDate | LocalDate | ✅ | 支出日期（不能是未來）|

---

## 🎯 預設範例資料

應用啟動時會自動建立 8 筆範例資料：

| 分類 | 筆數 | 範例 |
|------|------|------|
| 食物 | 3 | 早餐、午餐、晚餐 |
| 交通 | 2 | 捷運票、Uber |
| 娛樂 | 1 | 電影票 |
| 購物 | 1 | 衣服 |
| 生活用品 | 1 | 衛生紙、洗髮精 |

---

## 🔧 設定說明

### 開發環境 (`application.properties`)
```properties
# 資料庫設定
spring.datasource.url=jdbc:mysql://localhost:3306/expense_db
spring.datasource.username=root
spring.datasource.password=123456

# JPA 設定
spring.jpa.hibernate.ddl-auto=update
spring.jpa.show-sql=true

# Swagger 啟用
springdoc.swagger-ui.enabled=true
```

### 生產環境 (`application-prod.properties`)
```properties
# 使用環境變數
spring.datasource.url=${DB_URL}
spring.datasource.username=${DB_USERNAME}
spring.datasource.password=${DB_PASSWORD}

# 生產設定
spring.jpa.hibernate.ddl-auto=validate
spring.jpa.show-sql=false

# 關閉 Swagger
springdoc.swagger-ui.enabled=false
```

---

## 📖 詳細文件

想了解更多？請查看 [DOCUMENTATION.md](DOCUMENTATION.md)，內含：
- 完整 API 文件
- 資料庫設計說明
- 部署指南
- 效能優化建議
- 常見問題解答

---

## 🚀 部署選項

### Docker 部署
```bash
# 即將推出 Docker Compose 設定
docker-compose up -d
```

### 雲端平台
- ☁️ Render
- ☁️ Railway
- ☁️ Heroku
- ☁️ AWS EC2

詳細部署指南請參考 [DOCUMENTATION.md](DOCUMENTATION.md)

---

## 🗺️ 開發路線圖

### ✅ 已完成（v1.0）
- [x] 基本 CRUD API
- [x] 資料驗證
- [x] 分類查詢
- [x] 日期範圍查詢
- [x] Swagger 文件
- [x] 單元測試

### 🔜 計劃中（v2.0）
- [ ] 使用者認證系統（JWT）
- [ ] 支出統計 API
- [ ] 資料分頁
- [ ] 匯出功能（CSV/Excel）

### 💡 未來構想（v3.0）
- [ ] 前端介面（React/Vue）
- [ ] 預算管理
- [ ] 多幣別支援
- [ ] 圖表視覺化

---

## 🤝 貢獻指南

歡迎提交 Issue 和 Pull Request！

1. Fork 本專案
2. 建立特性分支 (`git checkout -b feature/AmazingFeature`)
3. 提交變更 (`git commit -m 'Add some AmazingFeature'`)
4. 推送到分支 (`git push origin feature/AmazingFeature`)
5. 開啟 Pull Request

---

## 📝 授權條款

本專案採用 MIT 授權條款 - 詳見 [LICENSE](LICENSE) 檔案

---

## 📧 聯絡方式

- **專案連結**：[GitHub Repository](#)
- **問題回報**：[Issues](#)
- **開發者**：您的名字

---

## 🌟 致謝

- Spring Boot 社群
- 所有貢獻者

---

<div align="center">

**⭐ 如果這個專案對你有幫助，請給我一個星星！**

Made with ❤️ by [Your Name]

</div>