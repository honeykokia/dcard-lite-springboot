# RP-002 看板列表

## SDD-Lite

### Goal
列出所有看板，並回傳board_id/name/description
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

註：本 RP 僅可能出現 `PAGE_INVALID / PAGE_SIZE_INVALID / KEYWORD_INVALID / INTERNAL_ERROR`

### Validation Rules
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
- 400 `PAGE_INVALID`
    - (code detail)
    - page < 1 或非整數
- 400 `PAGE_SIZE_INVALID`
    - pageSize 不在 1..100 或非整數
- 400 `KEYWORD_INVALID`
    - keyword 提供但 `trim()` 後長度不在 `1..50`
- 500 `INTERNAL_ERROR`
    - (code detail)
    - DB/Repository error 或未預期 error
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
    - `page`
    - `pageSize`
    - `total`
    - `items[] { boardId, name, description }`

### Queries Needed (Repositories)
> 最小可行版本先用 contains，效能需求之後再升級
- BoardRepository
    - `Page<Board> findByNameContainingIgnoreCase(String keyword, Pageable pageable)`（有 keyword）
    - `Page<Board> findAll(Pageable pageable)`（無 keyword）

## ## Done Definition

- `GET /boards` 回 `200`，JSON 格式固定 `{ page, pageSize, total, items }`
- 無資料時回 `200` + `total=0` + `items=[]`
- query 不合法回 `400 VALIDATION_FAILED`（PAGE_INVALID / PAGE_SIZE_INVALID / KEYWORD_INVALID）
- DB/系統錯誤回 `500 INTERNAL_ERROR`
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

- UC-07 keyword 超長（trim 後 > 50）
    - Given：keyword = "a".repeat(51)
    - When：listBoards(page=1,pageSize=20,keyword=keyword)
    - Then：丟InvalidKeywordException（code = `KEYWORD_INVALID`）
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
        - verify(BoardService, never()).listBoards(any())
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
        - verify(BoardService, never()).listBoards(any())
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
              **CT-06 `GET /boards?keyword=<51chars>` → 400 + KEYWORD_INVALID**
- Given
    - verify(BoardService, never()).listBoards(any())
- When
    - 呼叫 GET /boards?keyword=` + `"a".repeat(51)
- Then
    - HTTP Status = `400 Bad Request`
    - Response JSON：
        - `message = "VALIDATION_FAILED"`
        - `code = "KEYWORD_INVALID"`
        - `path = "/boards"`