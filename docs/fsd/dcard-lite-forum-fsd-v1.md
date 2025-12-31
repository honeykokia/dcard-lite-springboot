# Functional Specification Document (FSD)

## Dcard Lite Forum System

**Version:** 1.0  
**Author:** Leo # CONTEXT  
**Date:** 2025-12-23

本 FSD 提供系統總覽與範圍定義；**API/DTO、錯誤格式、DB schema、各 RP 的 SDD/TDD** 以 `/docs/**` 為準。

---

## 1. 專案內容

### 1.1 專案簡介

本系統提供使用者在「看板（Board）」內發文、留言、按讚/取消讚、追蹤看板與查看熱門文章。  
系統支援基本的內容審查（敏感字/空白文）、權限控制（作者可編輯刪除）、以及列表查詢（分頁、排序）。  
適用於面試展示「REST API 設計、資料建模、權限與一致性、查詢效能與可觀測性」。

### 1.2 專案目標

- 提供 **看板 / 文章 / 留言** 的核心 CRUD。

- 提供 **按讚** 與 **熱門排序**（可驗證的演算法與資料一致性策略）。

- 提供 **登入與權限控制**（作者/管理者）。

- 支援 **分頁查詢**、搜尋（最小可行版本：標題關鍵字）。

- 在 95% 請求下，列表查詢回應時間 ≤ 300ms（本機/小規模環境基準）。


### **1.3 專案的技術堆疊**

- **後端框架**：Spring Boot 3.x（REST API、Validation、Security）

- **Java 版本**：Java 17 LTS

- **前端框架**：Vue 3（列表/表單/互動）

- **資料庫**：MySQL（關聯建模、索引）

- **快取/排名（可選）**：Redis（用於熱門榜與按讚計數快取）

- **測試策略**：JUnit 5（單元測試）、Mockito（mock）、Testcontainers（可選，用 DB integration）

- **部署（可選）**：Docker + Nginx（反向代理）


---

## 2. 功能列表與驗收條件

| ID     | 功能                                 | 驗收條件                                 |
| ------ | ---------------------------------- | ------------------------------------ |
| RP-001 | 使用者註冊/登入（Auth）                     | 可取得 JWT，帶 token 可呼叫受保護 API           |
| RP-002 | 看板列表（Boards）                       | 可列出所有看板，回傳 board_id/name             |
| RP-003 | 發文（Create Post）                    | 指定 board 建立文章成功，回傳 post_id           |
| RP-004 | 文章列表（List Posts）                   | 支援分頁、排序（latest/hot），回傳文章摘要           |
| RP-005 | 文章詳情（Get Post）                     | 可取得文章內容、作者、like_count、留言列表（分頁）       |
| RP-006 | 編輯/刪除文章（Post Owner）                | 只有作者或管理者可編輯/刪除                       |
| RP-007 | 留言（Create Comment）                 | 可新增留言，回傳 comment_id                  |
| RP-008 | 編輯/刪除留言（Comment Owner）             | 只有作者或管理者可編輯/刪除                       |
| RP-009 | 按讚/取消讚（Like Toggle）                | 同一 user 對同一 post 只能有 0/1 個 like；計數正確 |
| RP-010 | 追蹤看板（Follow Board）                 | 可 follow/unfollow；可查詢我的追蹤列表          |
| RP-011 | 搜尋（最小可行）                           | 以 title keyword 搜尋文章，支援分頁            |
| RP-012 | 基本內容審查（Validation/Moderation Lite） | 禁止空白內容、超長內容、敏感字（可配置）                 |

### 2.1 參考文件
- #### System FSD（本文件）`/docs/fsd/dcard-lite-forum-fsd-v1.md`
- #### API/DTO Document：`/docs/api/api-spec.yaml`
    - 覆蓋所有 endpoint、驗證規則、錯誤碼。
- #### Entity Diagram：`/docs/db/er-diagram.puml`
    - Boards, Users, Posts, Comments, Likes, Follows 關聯圖。
- #### Database Schema：`/docs/db/database-schema.sql`
    - 表結構、索引策略、唯一約束。
- #### 模組功能 RP 文件（示例）
    - RP-001 設計：`/docs/rp/RP-001-register-login.md`

---

## 3. 系統架構與流程

### 3.1 架構概觀

```
Frontend (Vue 3)
   │
   └─ REST API ─→ Spring Boot Backend
                     │
                     ├─ MySQL (Users/Boards/Posts/Comments/Likes/Follows)
                     └─ Redis (Optional: hot ranking / like_count cache)
```

### 3.2 結構概要

- **專案目錄**：

    ```
    dcard-lite/
    ├── backend/
    │   ├── src/main/java/com.leo.dcard
    │   │   ├── controller/
    │   │   ├── service/
    │   │   ├── repository/
    │   │   ├── domain/          # Entity + Domain Model
    │   │   ├── dto/             # Request/Response DTO
    │   │   ├── security/        # JWT, auth filters
    │   │   └── common/          # error, utils, constants
    │   └── src/main/resources/
    │       ├── application.yml
    │       └── migration/       # Liquibase
    ├── frontend/
    │   └── src/                 # Vue pages/components
    └── docker/
    ```


### 3.3 關鍵流程

**發文**

1. 使用者帶 JWT 呼叫 `POST /boards/{boardId}/posts`。

2. 後端驗證 title/body（不可空、長度限制、敏感字）。

3. 寫入 `posts`。

4. 回傳 `post_id` 與 `created_at`。


**按讚/取消讚（Like Toggle）**

1. 使用者呼叫 `POST /posts/{postId}/like`。

2. 後端以 `unique(user_id, post_id)` 保證不重複。

3. 若已存在則刪除（unlike），否則新增（like）。

4. 更新 `posts.like_count`（或以查詢計算，依策略）。

5. 回傳 `liked=true/false` 與最新 `like_count`。


**熱門排序（Hot）**

- 最小版：`score = like_count * 2 + comment_count - hours_since_created * decay`

- 查詢時依 score 排序（或每日/每小時批次更新一個 `hot_score` 欄位）


---

## 4. API 設計

### 4.1 註冊

**POST /users/register**

**Request**

```json
{
  "email": "leo@example.com",
  "password": "P@ssw0rd!",
  "displayName": "Leo"
}
```

**Response**

```json
{
  "user_id": 1001,
  "message": "registered"
}
```

---

### 4.2 登入

**POST /users/login**

**Request**

```json
{
  "email": "leo@example.com",
  "password": "P@ssw0rd!"
}
```

**Response**

```json
{
  "accessToken": "jwt...",
  "tokenType": "Bearer",
  "expiresIn": 3600
}
```

---

### 4.3 建立文章

**POST /boards/{board_id}/posts**

**Request**

```json
{
  "title": "我如何用 Java 練面試？",
  "body": "整理我的 7 天補強計畫…",
  "tags": ["java", "interview"]
}
```

**Response**

```json
{
  "postId": 20001,
  "status": "created"
}
```

---

### 4.4 文章列表（分頁/排序）

**GET /boards/{board_id}/posts?sort=latest&page=1&pageSize=20**

**Response**

```json
{
  "page": 1,
  "pageSize": 20,
  "total": 135,
  "items": [
    {
      "postId": 20001,
      "title": "我如何用 Java 練面試？",
      "author": {"user_id": 1001, "display_name": "Leo"},
      "likeCount": 12,
      "commentCount": 3,
      "createdAt": "2025-12-23T10:00:00Z"
    }
  ]
}
```

---

### 4.5 文章詳情

**GET /posts/{post_id}**

**Response**

```json
{
  "postId": 20001,
  "board": {"board_id": 10, "name": "程式"},
  "title": "我如何用 Java 練面試？",
  "body": "整理我的 7 天補強計畫…",
  "author": {"user_id": 1001, "display_name": "Leo"},
  "likeCount": 12,
  "likedByMe": true,
  "createdAt": "2025-12-23T10:00:00Z"
}
```

---

### 4.6 新增留言

**POST /posts/{post_id}/comments**

**Request**

```json
{
  "body": "這套安排很實用，我也想試試！"
}
```

**Response**

```json
{
  "commentId": 90001,
  "status": "created"
}
```

---

### 4.7 Like Toggle

**POST /posts/{post_id}/like**

**Response**

```json
{
  "liked": true,
  "likeCount": 13
}
```

---

### 4.8 錯誤格式

所有錯誤回傳：

```json
{
  "status": 400,
  "error": "Bad Request",
  "message": "VALIDATION_FAILED",
  "code": "TITLE_REQUIRED",
  "path": "/boards/10/posts",
  "timestamp": "..."
}
```

| 狀況                      | 狀態碼 | code                 | message               |
| ----------------------- | --- | -------------------- | --------------------- |
| 看板不存在                   | 404 | BOARD_NOT_FOUND      | Board not found       |
| 文章不存在                   | 404 | POST_NOT_FOUND       | Post not found        |
| 留言不存在                   | 404 | COMMENT_NOT_FOUND    | Comment not found     |
| 未登入/Token 無效            | 401 | UNAUTHORIZED         | Missing/invalid token |
| 無權限編輯/刪除                | 403 | FORBIDDEN            | Not allowed           |
| 內容空白/過長                 | 400 | VALIDATION_FAILED    | Invalid fields        |
| 觸發敏感字                   | 400 | CONTENT_BLOCKED      | Content blocked       |
| 信箱已存在                   | 409 | EMAIL_ALREADY_EXISTS | Email is exists       |
| 重複 Like（若採 insert-only） | 409 | ALREADY_LIKED        | Already liked         |
| 伺服器錯誤                   | 500 | INTERNAL_ERROR       | Internal server error |

---

## 5. 資料模型

| Table        | 欄位              | 型別        | 說明             |
| ------------ | --------------- | --------- | -------------- |
| **users**    | user_id (PK)    | bigint    | 使用者 ID         |
|              | email (UK)      | varchar   | 登入帳號           |
|              | password_hash   | varchar   | 密碼雜湊           |
|              | display_name    | varchar   | 顯示名稱           |
|              | role            | varchar   | USER/ADMIN     |
|              | created_at      | timestamp | 建立時間           |
| **boards**   | board_id (PK)   | bigint    | 看板 ID          |
|              | name (UK)       | varchar   | 看板名稱           |
|              | description     | varchar   | 看板描述           |
| **posts**    | post_id (PK)    | bigint    | 文章 ID          |
|              | board_id (FK)   | bigint    | 所屬看板           |
|              | author_id (FK)  | bigint    | 作者             |
|              | title           | varchar   | 標題             |
|              | body            | text      | 內文             |
|              | like_count      | int       | 讚數（快取欄位，可選）    |
|              | comment_count   | int       | 留言數（快取欄位，可選）   |
|              | hot_score       | double    | 熱門分數（可選）       |
|              | status          | varchar   | ACTIVE/DELETED |
|              | created_at      | timestamp | 建立             |
|              | updated_at      | timestamp | 更新             |
| **comments** | comment_id (PK) | bigint    | 留言 ID          |
|              | post_id (FK)    | bigint    | 所屬文章           |
|              | author_id (FK)  | bigint    | 作者             |
|              | body            | text      | 內容             |
|              | status          | varchar   | ACTIVE/DELETED |
|              | created_at      | timestamp | 建立             |
| **likes**    | like_id (PK)    | bigint    | Like ID        |
|              | post_id (FK)    | bigint    | 文章             |
|              | user_id (FK)    | bigint    | 按讚者            |
|              | created_at      | timestamp | 按讚時間           |
| **follows**  | follow_id (PK)  | bigint    | Follow ID      |
|              | board_id (FK)   | bigint    | 看板             |
|              | user_id (FK)    | bigint    | 追蹤者            |
|              | created_at      | timestamp | 追蹤時間           |

**關聯關係**

- Board 1 ⟶ N Post

- Post 1 ⟶ N Comment

- User 1 ⟶ N Post / N Comment

- User N ⟷ N Post（透過 Likes）

- User N ⟷ N Board（透過 Follows）


**關鍵約束（面試會問）**

- `likes`：unique(user_id, post_id)

- `follows`：unique(user_id, board_id)


---

## 6. 非功能性需求 (NFR)

|類別|需求|
|---|---|
|效能|列表查詢 P95 ≤ 300ms（本機/小規模基準），熱門榜單 P95 ≤ 500ms|
|併發|支援同時 200 個活躍使用者瀏覽列表與互動|
|一致性|like_count/comment_count 可採「最終一致性」但 DB 為真實來源|
|可用性|99.9%（面試展示：基本 health check + graceful error）|
|安全性|JWT 驗證；敏感操作需登入；作者/管理者權限檢查|
|可觀測性|API latency、錯誤率、慢查詢 log、like toggle 成功率|

---

## 7. 驗收測試案例

|編號|測試項目|預期結果|已完成|
|---|---|---|---|
|TC-001|註冊成功|回傳 user_id||
|TC-002|登入成功|回傳 access_token||
|TC-003|發文成功|回傳 post_id||
|TC-004|列表分頁|page/pageSize/total 正確||
|TC-005|文章詳情|回傳內容與作者資訊||
|TC-006|非作者不可編輯|回 403 FORBIDDEN||
|TC-007|留言成功|回傳 comment_id||
|TC-008|Like toggle|like_count 正確變化||
|TC-009|Like 唯一約束|不可重複產生兩筆||
|TC-010|搜尋|keyword 可找到對應文章||

---

## 8. 延伸功能（未來規劃）

- 檢舉系統（Report + Admin Review）。

- 圖片上傳（S3/Cloud Storage + CDN）。

- 通知系統（有人留言/被按讚）。

- 全文檢索（Elastic/OpenSearch）。

- 內容審查升級（ML/第三方 service）。

- 多層留言（threaded comments）。


---

## 9. 版本歷史

|版本|日期|修改內容|
|---|---|---|
|1.0|2025-12-23|初版建立：Auth、Boards、Posts、Comments、Likes、Follows、Search、Moderation Lite|

    
