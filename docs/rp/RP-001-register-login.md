# RP-001 使用者註冊/登入

## SDD-Lite

### Goal
使用者註冊帳號密碼，進行登入後取得JWT Token
### API Contract
#### `POST /users/register`
**Request JSON**
```json
{
  "name" : "<string>",
  "email": "<string>",
  "password": "<string>",
  "confirmPassword": "<string>"
}
```

**Success**
- `201 Created`
```json
{
  "userId": 123,
  "displayName": "Leo",
  "email": "leo@example.com",
  "role": "USER",
  "createdAt": "2025-12-25T10:00:00Z"
}
```

**Errors**
- `400 Bad Request` (validation failed)
```json
{
  "status": 400,
  "error": "Bad Request",
  "message": "VALIDATION_FAILED",
  "code": "PASSWORD_REQUIRED",
  "path": "/users/register",
  "timestamp": "..."
}
```

#### `POST /users/login`
**Request JSON**
```json
{
  "email": "<string>",
  "password": "<string>"
}
```

**Success**
- `200 Success`
```json
{ "userId": 123,
  "displayName": "Leo",
  "role" : "USER",
  "accessToken": "f98dyuthgj893w5ejyng90pwernjy9oghnewrs9hg"
}
```

**Errors**
- `400 Bad Request` (validation failed)
```json
{
  "status": 400,
  "error": "Bad Request",
  "message": "VALIDATION_FAILED",
  "code": "PASSWORD_REQUIRED",
  "path": "/users/login",
  "timestamp": "..."
}
```
- `401 Unauthorized` (validation failed)
```json
{
  "status": 401,
  "error": "Unauthorized",
  "message": "AUTHENTICATION_FAILED",
  "code": "AUTHENTICATION_FAILED",
  "path": "/users/login",
  "timestamp": "..."
}
```
註：`code` 可能為 `INVALID_NAME` / `INVALID_EMAIL` / `INVALID_PASSWORD` / `INVALID_CONFIRM_PASSWORD` 等，詳見 Error Mapping。
### Validation Rules
- name
    - 必填（不可為空字串 / 純空白；需先 `trim()`）
    - 最大長度：20
    - 限制內容：不接受純數字或只含符號
- email
    - 必填（不可為空字串／純空白）
    - 必須符合 Email 格式（後端驗證 e.g. regex / library）
    - 最大長度：100
    - 儲存前轉小寫（`toLowerCase()`），方便做唯一索引比較
- password
    - 必填（不可為空字串/純空白）
    - 至少要有一個英文字母和1個數字
    - 密碼長度：8~12
- confirmPassword
    - 必填（不可為空字串/純空白）
    - 必須與 `password` 完全相同

### Error Mapping（Domain → HTTP）

> 你的 error JSON 格式固定為：status）」與「code（詳細訊息）」要用什麼。

- 400 `INVALID_NAME`
    - (code detail)
    - Name must not be blank
    - Name maximum length 20 characters
    - Values consisting only of digits or only symbols are not allowed
- 400 `INVALID_EMAIL`
    - (code detail)
    - Email must not be blank
    - Email must be in a valid email format.
- 400 `INVALID_PASSWORD`
    - (code detail)
    - Password must not be blank
    - Password length must be between 8 and 12 characters.
    - Password must contain at least one letter.
- 400 `INVALID_CONFIRM_PASSWORD`
    - (code detail)
    - ConfirmPassword must not be blank
    - Confirm password must match password.
- 401`AUTHENTICATION_FAILED`
    - (code detail)
    - Email or password is incorrect
- 409 `EMAIL_ALREADY_EXISTS`
    - (code detail)
    - Email is exists
### DB Changes (MySQL + Liquibase)

- Table: `users`
    - columns（填你要的最小集合）：
        - user_id (BIGINT, PK, auto increment, NOT NULL)
        - email (VARCHAR(100), UK, NOT NULL)
        - password_hash (VARCHAR(200), NOT NULL)
        - display_name (VARCHAR(20), NOT NULL)
        - role (VARCHAR(10), NOT NULL, default='USER')
        - created_at (DATETIME(6), NOT NULL)
### Use Case Boundary
#### Use Case: Register
- Use Case Name：
    - `RegisterUser`
- Signature（入口）：
    - `RegisterUserResult registerUser(RegisterUserRequest request)`
- Transaction：
    - `@Transactional`
        - 建議：`users` 新增一筆，未來如果有 `user_profiles` 之類也一起包在同一個交易。
- Return Model（你要回哪些欄位）：
    - `userId`
    - `displayName`
    - `email`
    - `role`（預設 USER）
    - `createdAt`
#### Use Case: Login
- Use Case Name：
    - `LoginUser`
- Signature（入口）：
    - `LoginResult login(LoginRequest request)`
- Transaction：
  - `@Transactional(readOnly = true)`
- Return Model（你要回哪些欄位）：
    - `userId`
    - `displayName`
    - `role`
    - `accessToken`（JWT）
    - （可選）`refreshToken`
### Queries Needed (Repositories)
- UserRepository
    - save(user)
    - findByEmail(String email)


### Done Definition
- `POST /users/register` 成功回傳 RegisterUserResult JSON
- `POST /users/login` 成功回傳 LoginResult JSON（至少包含 accessToken）
- 錯誤情境會回正確 status + message(code) + code(detail)
- TDD 測試全部綠
- Swagger UI smoke test：register/login 成功一次（非主驗收）
---
## TDD Lite
### Use Case Tests (behavior-focused)
> 每條都用 Given-When-Then 寫清楚，這些就是你要 AI 產測試的清單。

## TDD Lite

### Use Case Tests (behavior-focused)
> 每條都用 Given-When-Then 寫清楚，這些就是你要 AI 產測試的清單。

- UC-01 成功建立使用者
    - Given：name="Leo", email="leo@example.com", password="abc12345", confirmPassword="abc12345"
    - When：registerUser(...)
    - Then：
        - users 資料表新增 1 筆
        - 回傳的 RegisterUserResult 內容包含：userId, displayName, email, role="USER", createdAt（非空）

- UC-02 密碼不合法（缺少字母或數字）→ 400 INVALID_PASSWORD
    - Case A：
        - Given：password="12345678"（只有數字）、confirmPassword="12345678"
    - Case B：
        - Given：password="abcdefgh"（只有字母）、confirmPassword="abcdefgh"
    - When：registerUser(...)
    - Then：
        - 丟 InvalidPasswordException（映射 400 INVALID_PASSWORD）

- UC-03 密碼長度不合法 → 400 INVALID_PASSWORD
    - Case A：太短
        - Given：password="a1b2c"（長度 < 8）、confirmPassword="a1b2c"
    - Case B：太長
        - Given：password="abc123456789"（長度 > 12）、confirmPassword="abc123456789"
    - When：registerUser(...)
    - Then：
        - 丟 InvalidPasswordException（映射 400 INVALID_PASSWORD）

- UC-04 confirmPassword 不一致 → 400 INVALID_CONFIRM_PASSWORD
    - Given：password="abc12345", confirmPassword="abc12346"
    - When：registerUser(...)
    - Then：
        - 丟 InvalidConfirmPasswordException（映射 400 INVALID_CONFIRM_PASSWORD）

- UC-05 email 格式不合法 → 400 INVALID_EMAIL
    - Given：email="not-an-email"
    - When：registerUser(...)
    - Then：
        - 丟 InvalidEmailException（映射 400 INVALID_EMAIL）

- UC-06 email 已存在 → 409 EMAIL_ALREADY_EXISTS
    - Given：
        - users 資料表已存在 email="leo@example.com"
        - registerUserRequest.email="leo@example.com"
    - When：registerUser(...)
    - Then：
        - 丟 EmailAlreadyExistsException（映射 409 EMAIL_ALREADY_EXISTS）
### Controller Contract Tests (thin)

- CT-01 `POST /users/register` 成功 → 201 + JSON body 正確
    - Given：
        - HTTP Request Body 為合法 JSON（name/email/password/confirmPassword）
        - Service.registerUser(...) 回傳 RegisterUserResult（模擬 / mock）
    - When：
        - 呼叫 `POST /users/register`
    - Then：
        - HTTP status = 201 Created
        - Response JSON 的欄位對應 RegisterUserResult（userId, displayName, email, role, createdAt）

- CT-02 `POST /users/register` 缺少 password → 400 + PASSWORD_REQUIRED
    - Given：
        - HTTP Request Body 裡 password 為空字串 / 缺欄位
    - When：
        - 呼叫 `POST /users/register`
    - Then：
        - HTTP status = 400 Bad Request
        - Response JSON：
            - message = "VALIDATION_FAILED"
            - code = "PASSWORD_REQUIRED"

- CT-03 `POST /users/register` Service 丟 InvalidPasswordException → 400 INVALID_PASSWORD
    - Given：
        - Mock RegisterUserUseCase.registerUser(...) 丟 InvalidPasswordException
    - When：
        - 呼叫 `POST /users/register`
    - Then：
        - HTTP status = 400 Bad Request
        - Response JSON：
            - message = "VALIDATION_FAILED"
            - code = "INVALID_PASSWORD"

- CT-04 `POST /users/register` Service 丟 EmailAlreadyExistsException → 409 EMAIL_ALREADY_EXISTS
    - Given：
        - Mock RegisterUserUseCase.registerUser(...) 丟 EmailAlreadyExistsException
    - When：
        - 呼叫 `POST /users/register`
    - Then：
        - HTTP status = 409 Conflict
        - Response JSON：
            - message = "EMAIL_ALREADY_EXISTS"
            - code = "EMAIL_ALREADY_EXISTS"

- CT-05 `POST /users/login` 成功 → 200 + accessToken
    - Given：
        - 合法的 email/password
        - Service.login(...) 回傳 LoginResult（含 accessToken）
    - When：
        - 呼叫 `POST /users/login`
    - Then：
        - HTTP status = 200 OK
        - Response JSON 至少包含：userId, displayName, role, accessToken

- CT-06 `POST /users/login` 密碼缺失 → 400 PASSWORD_REQUIRED
    - Given：
        - Request 中 password 為空字串 / 缺欄位
    - Then：
        - HTTP status = 400
        - message = "VALIDATION_FAILED"
        - code = "PASSWORD_REQUIRED"

- CT-07 `POST /users/login` 認證失敗 → 401 AUTHENTICATION_FAILED
    - Given：
        - Mock LoginUseCase.login(...) 丟 AuthenticationFailedException
    - Then：
        - HTTP status = 401 Unauthorized
        - message = "AUTHENTICATION_FAILED"
        - code = "AUTHENTICATION_FAILED"
