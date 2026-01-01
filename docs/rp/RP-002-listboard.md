# RP-002 看板列表

## SDD-Lite

### Goal
1. 列出所有看板，並回傳board_id/name/description
### API Contract
#### `GET /boards`
##### Request Query
- `page`（optional，default=1）
- `pageSize`（optional，default=20，max=100）
- `keyword`（optional，用於 name 模糊查詢；空白視為未提供）

##### Success
- `200 OK`
```json
{
  "page": 1,
  "pageSize": 20,
  "total": 135,
  "items": [
    {
      "boardId": 1,
      "name": "原神",
      "description": "空月之歌-月之三"
    },
    {
      "boardId": 2,
      "name": "程式",
      "description": "Java / Spring / 前端"
    }
  ]
}

```
- **沒有看板時不是 error**，仍回 `200`：
```json
{
  "page": 1,
  "pageSize": 20,
  "total": 0,
  "items": []
}
```

##### Errors
- `400 VALIDATION_FAILED`（query 不合法）
    - `PAGE_INVALID`
    - `PAGE_SIZE_INVALID`
```json
{
  "status": 400,
  "error": "Bad Request",
  "message": "VALIDATION_FAILED",
  "code": "PAGE_INVALID",
  "path": "/boards",
  "timestamp": "..."
}

```

- `500 INTERNAL_ERROR` （DB 連線、SQL 例外、未預期錯誤）
```json
{
  "status": 500,
  "error": "Internal Server Error",
  "message": "INTERNAL_ERROR",
  "code": "INTERNAL_ERROR",
  "path": "/boards",
  "timestamp": "..."
}
```

註：`code` 可能為 `INVALID_NAME` / `INVALID_EMAIL` / `INVALID_PASSWORD` / `INVALID_CONFIRM_PASSWORD` 等，詳見 Error Mapping。
### Validation Rules
- name
    - 必填（不可為空字串 / 純空白；需先 `trim()`）
    - 最大長度：50
    - 限制內容：不接受純數字或只含符號
- description
    - 必填（不可為空字串 / 純空白）
    - 最大長度：200
- page
    - default = 1
    - 必須為整數且 `page >= 1`

- pageSize
    - default = 20
    - 必須為整數且 `1 <= pageSize <= 100`

- keyword
    - optional
    - 若提供：`trim()` 後長度 `1..50`
    - 空白字串視為未提供（等同沒有 keyword）

### Error Mapping（Domain → HTTP）
> 你的 error JSON 格式固定為：status）」與「code（詳細訊息）」要用什麼。
- 400 `VALIDATION_FAILED`
    - (code detail)
    - page invalid
    - page size invalid
    - validation fail
- 500 `INTERNAL_ERROR`
    - (code detail)
    - database connection error
### DB Changes (MySQL + Liquibase)

- Table: `boards`
    - columns（填你要的最小集合）：
        - board_id (BIGINT, PK, auto increment, NOT NULL)
        - name (VARCHAR(50), NOT NULL)
        - description (VARCHAR(200), NOT NULL)
        - created_at (DATETIME(6), NOT NULL)
    - constraints:
        - PK: `pk_boards` (board_id)
        - UK: `uq_boards_name` (name)
    - index
        - idx_boards_name (name)
### Use Case Boundary
#### Use Case: ListBoards
- Use Case Name：
    - `ListBoards`
- Signature（入口）：
    - `ListBoardsResult listBoards(ListBoardsQuery query)`
- Transaction：
    - `@Transactional(readOnly = true)`
- Input Model：
    - `page`
    - `pageSize`
    - `keyword`
- Return Model：
    - `page
    - `pageSize`
    - `total`
    - `items[] { boardId, name, description }`

### Queries Needed (Repositories)
- BoardRepository
    - `Page<Board> findByNameContainingIgnoreCase(String keyword, Pageable pageable)`（有 keyword）
    - `Page<Board> findAll(Pageable pageable)`（無 keyword）

## ## Done Definition

- `GET /boards` 回 `200`，JSON 格式固定 `{ items: [...] }`
- 無資料時回 `{ items: [] }`
- DB/系統錯誤會回 `500 INTERNAL_ERROR`（符合全域錯誤格式）
- TDD 綠
- Swagger UI smoke test：`GET /boards` 可成功呼叫
---
## TDD Lite

### Use Case Tests (behavior-focused)
- UC-01 成功取得看板列表（無 keyword）
    - Given：boards 表有 2 筆 (A,B)
    - When：listBoards(page=1,pageSize=20,keyword=null)
    - Then：
        - total=2
        - items 長度=2
        - items 含 boardId/name/description

- UC-02 keyword 搜尋有結果
    - Given：boards 有「程式」、「原神」
    - When：keyword="程式"
    - Then：total=1，items[0].name="程式"

- UC-03 keyword 搜尋無結果 → 空列表（不是錯）
    - Given：boards 有資料
    - When：keyword="不存在"
    - Then：total=0，items=[]

- UC-04 page 不合法 → 400 PAGE_INVALID
    - Given：page=0
    - When：listBoards(...)
    - Then：丟 InvalidPageException（映射 400）

- UC-05 pageSize 不合法 → 400 PAGE_SIZE_INVALID
    - Given：pageSize=0 或 101
    - Then：丟 InvalidPageSizeException（映射 400）

- UC-06 Repository 查詢失敗 → 500 INTERNAL_ERROR
    - Given：BoardRepository 丟 DataAccessException
    - Then：丟 InternalErrorException（映射 500）


### Controller Contract Tests (thin)

- CT-01 `GET /boards` 成功 → 200 + 分頁欄位齊全（GWT）
    - Given
        - `BoardService.listBoards(query)` 回傳：
            - `page=1, pageSize=20, total=2`
            - `items` 含 2 筆 `{boardId,name,description}`
    - When
        - 呼叫 `GET /boards`（不帶 query）
    - Then
        - HTTP Status = `200 OK`
        - Response JSON：
            - `page = 1`
            - `pageSize = 20`
            - `total = 2`
            - `items` 為陣列且長度 = 2
            - `items[0].boardId/name/description` 存在

- CT-02 `GET /boards?keyword=xxx` 無結果 → 200 + total=0 + items=[]
    - Given
        - `BoardService.listBoards(keyword="xxx", page=1, pageSize=20)` 回傳：
            - `page=1, pageSize=20, total=0, items=[]`
    - When
        - 呼叫 `GET /boards?keyword=xxx`

    - Then
        - HTTP Status = `200 OK`
        - Response JSON：
            - `total = 0`
            - `items = []`（空陣列）
            - `page/pageSize` 仍存在且為預設值（1/20）

- CT-03 `GET /boards?page=0` → 400 + VALIDATION_FAILED + PAGE_INVALID
    - Given
        - 無（或宣告 service 不應被呼叫）
    - When
        - 呼叫 `GET /boards?page=0`
    - Then
        - HTTP Status = `400 Bad Request`
        - Response JSON：
            - `message = "VALIDATION_FAILED"`
            - `code = "PAGE_INVALID"`
            - `path = "/boards"`

- CT-04 `GET /boards?pageSize=101` → 400 + VALIDATION_FAILED + PAGE_SIZE_INVALID
    - Given
        - 無（service 不應被呼叫）
    - When
        - 呼叫 `GET /boards?pageSize=101`
    - Then
        - HTTP Status = `400 Bad Request`
        - Response JSON：
            - `message = "VALIDATION_FAILED"`
            - `code = "PAGE_SIZE_INVALID"`
            - `path = "/boards"`

- CT-05 service 丟 InternalErrorException → 500 + INTERNAL_ERROR
    - Given
        - mock `BoardService.listBoards(...)` 丟 `InternalErrorException`
    - When
        - 呼叫 `GET /boards`
    - Then
        - HTTP Status = `500 Internal Server Error`
        - Response JSON：
            - `message = "INTERNAL_ERROR"`
            - `code = "INTERNAL_ERROR"`
            - `path = "/boards"`
