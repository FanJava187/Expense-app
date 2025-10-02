# Expense App Backend 專案文件

## 📌 專案簡介
本專案是一個功能完整的 **支出管理系統 (Expense Management App)**，  
使用 **Spring Boot 3 + Spring Data JPA + MySQL + Swagger(OpenAPI)** 開發。  
提供完整的 **CRUD REST API**、**資料驗證**、**分類查詢**、**日期範圍查詢**等功能。

---

## ⚙️ 技術棧
- **Java 21**
- **Spring Boot 3.4.5**
- **Spring Web (REST API)**
- **Spring Data JPA (Hibernate)**
- **MySQL 8.x**
- **Bean Validation (Hibernate Validator)**
- **Swagger / OpenAPI (springdoc-openapi)**
- **JUnit 5 + MockMvc (單元測試)**

---

## 🗄️ 資料庫設定

### 1. 建立資料庫
```sql
CREATE DATABASE expense_db CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
```

### 2. 建立資料表
```sql
USE expense_db;

CREATE TABLE expenses (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主鍵 ID',
    title VARCHAR(100) NOT NULL COMMENT '支出標題',
    amount DECIMAL(12, 2) NOT NULL COMMENT '支出金額',
    category VARCHAR(50) NOT NULL COMMENT '支出分類',
    expense_date DATE NOT NULL COMMENT '支出日期',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '建立時間',
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新時間',
    CONSTRAINT chk_amount_positive CHECK (amount > 0)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='支出紀錄表';

-- 建立索引以提升查詢效能
CREATE INDEX idx_category ON expenses(category);
CREATE INDEX idx_expense_date ON expenses(expense_date);
CREATE INDEX idx_category_date ON expenses(category, expense_date);
```

### 3. 設定應用程式連線
修改 `src/main/resources/application.properties`：
```properties
# 資料庫連線設定
spring.datasource.url=jdbc:mysql://localhost:3306/expense_db
spring.datasource.username=root
spring.datasource.password=123456
spring.datasource.driver-class-name=com.mysql.cj.jdbc.Driver

# JPA 設定
spring.jpa.hibernate.ddl-auto=update
spring.jpa.show-sql=true
spring.jpa.open-in-view=false

# Swagger OpenAPI
springdoc.api-docs.enabled=true
springdoc.swagger-ui.enabled=true
```

---

## 📂 專案結構
```
expense-app/
├── src/main/java/com/example/expenseapp
│   ├── controller
│   │   └── ExpenseController.java       # REST API 控制器
│   ├── model
│   │   └── Expense.java                 # 實體模型（含驗證規則）
│   ├── repository
│   │   └── ExpenseRepository.java       # 資料存取層（含查詢方法）
│   ├── service
│   │   └── ExpenseService.java          # 業務邏輯層
│   ├── initializer
│   │   └── DataInitializer.java         # 初始化範例資料
│   └── ExpenseAppApplication.java       # 主程式
│
├── src/test/java/com/example/expenseapp
│   └── ExpenseControllerTest.java       # 控制器單元測試（17 個測試案例）
│
├── src/main/resources
│   └── application.properties           # 應用程式設定
│
├── pom.xml                               # Maven 依賴管理
├── DOCUMENTATION.md                      # 本文件
└── README.md                             # 專案說明
```

---

## 🔍 資料模型

### Expense 實體
| 欄位 | 類型 | 說明 | 驗證規則 |
|------|------|------|----------|
| id | Long | 主鍵（自動產生） | - |
| title | String | 支出標題 | 必填，1-100 字元 |
| amount | BigDecimal | 支出金額 | 必填，必須 > 0 |
| category | String | 支出分類 | 必填，最多 50 字元 |
| expenseDate | LocalDate | 支出日期 | 必填，不能是未來 |
| createdAt | Timestamp | 建立時間 | 自動產生 |
| updatedAt | Timestamp | 更新時間 | 自動更新 |

---

## 📑 REST API 文件

### 基本 CRUD 操作

#### 1. 新增支出
```http
POST /api/expenses
Content-Type: application/json

{
  "title": "午餐 - 便當",
  "amount": 120.50,
  "category": "餐飲",
  "expenseDate": "2025-09-30"
}

回應: 201 Created
{
  "id": 1,
  "title": "午餐 - 便當",
  "amount": 120.50,
  "category": "餐飲",
  "expenseDate": "2025-09-30"
}
```

#### 2. 查詢所有支出
```http
GET /api/expenses

回應: 200 OK
[
  {
    "id": 1,
    "title": "早餐 - 蛋餅豆漿",
    "amount": 50.00,
    "category": "餐飲",
    "expenseDate": "2025-09-28"
  },
  ...
]
```

#### 3. 根據 ID 查詢支出
```http
GET /api/expenses/{id}

回應: 200 OK（找到）或 404 Not Found（找不到）
```

#### 4. 更新支出
```http
PUT /api/expenses/{id}
Content-Type: application/json

{
  "title": "午餐 - 自助餐",
  "amount": 150.00,
  "category": "餐飲",
  "expenseDate": "2025-09-30"
}

回應: 200 OK
```

#### 5. 刪除支出
```http
DELETE /api/expenses/{id}

回應: 204 No Content
```

---

### 查詢功能

#### 6. 根據分類查詢支出
```http
GET /api/expenses/category/{category}

範例: GET /api/expenses/category/餐飲

回應: 200 OK
[
  {
    "id": 1,
    "title": "早餐",
    "amount": 50.00,
    "category": "餐飲",
    "expenseDate": "2025-09-28"
  },
  ...
]
```

#### 7. 根據日期範圍查詢支出
```http
GET /api/expenses/date-range?startDate={start}&endDate={end}

範例: GET /api/expenses/date-range?startDate=2025-09-01&endDate=2025-09-30

回應: 200 OK
```

#### 8. 根據分類和日期範圍組合查詢
```http
GET /api/expenses/search?category={cat}&startDate={start}&endDate={end}

範例: GET /api/expenses/search?category=餐飲&startDate=2025-09-01&endDate=2025-09-30

回應: 200 OK
```

#### 9. 取得日期範圍內的所有分類
```http
GET /api/expenses/categories?startDate={start}&endDate={end}

範例: GET /api/expenses/categories?startDate=2025-09-01&endDate=2025-09-30

回應: 200 OK
["餐飲", "交通", "娛樂", "購物", "生活用品"]
```

---

## ✅ 資料驗證規則

### 自動驗證
- **title（標題）**
    - 不能為空白
    - 長度必須在 1-100 字元之間

- **amount（金額）**
    - 不能為空
    - 必須大於 0（不接受負數或零）
    - 最多 10 位整數，2 位小數

- **category（分類）**
    - 不能為空白
    - 長度不能超過 50 字元

- **expenseDate（日期）**
    - 不能為空
    - 不能是未來日期

### 驗證錯誤回應
```json
{
  "title": "標題不能為空",
  "amount": "金額必須大於 0",
  "expenseDate": "日期不能是未來"
}
```

---

## 📖 Swagger API 文件
專案啟動後可至以下網址檢視互動式 API 文件：
```
http://localhost:8080/swagger-ui/index.html
```

可以直接在瀏覽器中測試所有 API！

---

## 🧪 單元測試

### 測試覆蓋範圍
- ✅ **基本 CRUD 測試**（6 個測試）
- ✅ **資料驗證測試**（5 個測試）
- ✅ **查詢功能測試**（6 個測試）
- 總計：**17 個測試案例**

### 執行測試
```bash
# 執行所有測試
mvn test

# 執行特定測試類別
mvn test -Dtest=ExpenseControllerTest

# 查看測試覆蓋率報告
mvn test jacoco:report
```

測試檔案位置：`src/test/java/com/example/expenseapp/ExpenseControllerTest.java`

---

## 📥 初始化資料
專案啟動時若資料庫無資料，會自動插入 8 筆範例資料：

| 標題 | 金額 | 分類 | 日期 |
|------|------|------|------|
| 早餐 - 蛋餅豆漿 | 50.00 | 食物 | 2 天前 |
| 午餐 - 便當 | 120.00 | 食物 | 昨天 |
| 晚餐 - 牛肉麵 | 200.00 | 食物 | 今天 |
| 捷運票 | 35.00 | 交通 | 3 天前 |
| Uber 計程車 | 250.00 | 交通 | 昨天 |
| 電影票 | 320.00 | 娛樂 | 5 天前 |
| 衣服 | 1200.00 | 購物 | 7 天前 |
| 衛生紙、洗髮精 | 350.00 | 生活用品 | 4 天前 |

---

## ▶️ 執行專案

### 1. 確保 MySQL 已啟動
```bash
# 檢查 MySQL 服務狀態
sudo service mysql status

# 或使用 Docker
docker run -d -p 3306:3306 --name mysql \
  -e MYSQL_ROOT_PASSWORD=123456 \
  -e MYSQL_DATABASE=expense_db \
  mysql:8.0
```

### 2. 啟動 Spring Boot 應用
```bash
# 使用 Maven
mvn spring-boot:run

# 或先打包成 JAR
mvn clean package
java -jar target/expense-app-0.0.1-SNAPSHOT.jar
```

### 3. 驗證服務運行
```bash
# 檢查健康狀態
curl http://localhost:8080/api/expenses

# 或開啟 Swagger UI
open http://localhost:8080/swagger-ui/index.html
```

---

## 🔧 常用操作範例

### 使用 curl 測試 API

```bash
# 1. 新增支出
curl -X POST http://localhost:8080/api/expenses \
  -H "Content-Type: application/json" \
  -d '{
    "title": "星巴克咖啡",
    "amount": 150.00,
    "category": "餐飲",
    "expenseDate": "2025-09-30"
  }'

# 2. 查詢所有支出
curl http://localhost:8080/api/expenses

# 3. 查詢特定分類
curl http://localhost:8080/api/expenses/category/餐飲

# 4. 查詢日期範圍
curl "http://localhost:8080/api/expenses/date-range?startDate=2025-09-01&endDate=2025-09-30"

# 5. 更新支出
curl -X PUT http://localhost:8080/api/expenses/1 \
  -H "Content-Type: application/json" \
  -d '{
    "title": "修改後的標題",
    "amount": 200.00,
    "category": "餐飲",
    "expenseDate": "2025-09-30"
  }'

# 6. 刪除支出
curl -X DELETE http://localhost:8080/api/expenses/1
```

---

## 🚀 效能優化

### 資料庫索引
專案已建立以下索引以提升查詢效能：
- `idx_category` - 分類查詢
- `idx_expense_date` - 日期查詢
- `idx_category_date` - 組合查詢（最常用）

### JPA 查詢優化
- 使用 `@Query` 自訂查詢語句
- 避免 N+1 查詢問題
- 關閉 `open-in-view` 模式

---

## 📌 注意事項

### 開發環境
- 確保 MySQL 已啟動，且有 `expense_db` 資料庫
- 預設帳號/密碼：`root / 123456`
- 可於 `application.properties` 調整設定

### 生產環境
- **必須修改** `spring.jpa.hibernate.ddl-auto` 為 `validate` 或 `none`
- **必須關閉** Swagger UI (`springdoc.swagger-ui.enabled=false`)
- **建議使用** 環境變數管理敏感資訊（資料庫密碼）
- **建議使用** Flyway 或 Liquibase 管理資料庫遷移

### 安全性
- 不要將資料庫密碼提交到版本控制
- 建議使用環境變數或 Spring Profiles
- 生產環境應使用 HTTPS
- 考慮加入 API 認證機制（JWT）

---

## 🔄 未來改進計畫

### 第二階段功能
- [ ] 使用者註冊/登入系統
- [ ] JWT 身份驗證
- [ ] 支出統計 API（月度報表、分類統計）
- [ ] 分頁功能

### 第三階段優化
- [ ] 整合 Flyway 資料庫版本控制
- [ ] Docker 容器化部署
- [ ] CI/CD 自動化部署
- [ ] 監控與日誌系統

---

## 📞 技術支援
如有問題或建議，請透過以下方式聯繫：
- 建立 GitHub Issue
- 發送 Pull Request

---

**最後更新日期：** 2025-10-01