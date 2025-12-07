# Todo Demo（Spring Boot + MyBatis-Plus）

## 项目简介
- 基于 Spring Boot 3 + MyBatis-Plus 的 Todo 示例项目，提供 Todo 的增删改查 REST 接口。
- 数据模型：`todo` 表包含 `id`、`title`、`created_time`，代码中 `Todo` 实体字段对应 `id`、`title`、`createdTime`。
- 分页、排序和模糊查询通过 MyBatis-Plus 的 `Page` 与 `QueryWrapper` 实现。

## 本地运行
1) 数据库配置：`src/main/resources/application.properties` 默认指向 `jdbc:mysql://localhost:3306/demo`，用户名/密码均为 `root`，可按需修改。确保库中存在 `todo` 表（`id` 自增主键，`title`，`created_time` 时间戳）。
2) 启动：在项目根目录执行 `mvnw.cmd spring-boot:run`（Windows）或 `./mvnw spring-boot:run`（类 Unix）。
3) 默认端口：未显式配置，使用 Spring Boot 默认的 `8080`，基础路径 `http://localhost:8080`。

## 统一返回与错误码
- 统一返回包装 `Result<T>`：`{ "code": number, "msg": string, "data": any }`。成功场景使用 `Result.ok(...)`，异常和校验错误通过 `Result.fail(...)` 返回。
- 错误码枚举 `CodeStatus`：
  - `SUCCESS`：200，`ok`
  - `PARAM_ERROR`：1001，请求参数错误（校验失败、类型/缺失等）
  - `NOT_FOUND`：1004，数据不存在（更新/删除行数为 0 时抛出）
  - `SERVER_ERROR`：500，服务器内部错误（兜底异常）

## 接口文档
基础路径均以 `/todos` 开头。

### GET /todos/hello
- 功能：健康检查/示例返回。
- 请求示例：
```
GET /todos/hello
```
- 响应示例：
```json
{
  "code": 200,
  "msg": "ok",
  "data": {
    "msg": "Hello,HuanF!",
    "author": "HuanF"
  }
}
```

### GET /todos/all
- 功能：返回全部 Todo 列表。
- 请求示例：
```
GET /todos/all
```
- 响应示例：
```json
{
  "code": 200,
  "msg": "ok",
  "data": [
    {
      "id": 1,
      "title": "写文档",
      "createdTime": "2024-12-03T12:00:00"
    },
    {
      "id": 2,
      "title": "跑步",
      "createdTime": "2024-12-03T13:00:00"
    }
  ]
}
```

### GET /todos
- 功能：分页 + 排序 + `title` 模糊查询。
- 查询参数：

| 参数名      | 类型            | 必填 | 默认值         | 含义                                       |
| ----------- | --------------- | ---- | -------------- | ------------------------------------------ |
| `pageIndex` | Long            | 是   | 无             | 页码（从 1 开始），受 `@Min(1)` 约束       |
| `pageSize`  | Long            | 是   | 无             | 每页大小，`@Min(1)` 且 `@Max(50)`          |
| `title`     | String          | 否   | 空             | 按标题 `LIKE` 模糊查询                     |
| `sortBy`    | TodoSortBy enum | 否   | `CREATED_TIME` | 排序字段，当前仅支持 `CREATED_TIME`        |
| `sortDir`   | SortDir enum    | 否   | `DESC`         | 排序方向，`ASC` 或 `DESC`                  |

- 请求示例：
```
GET /todos?pageIndex=1&pageSize=10&title=文档&sortBy=CREATED_TIME&sortDir=DESC
```
- 响应示例：
```json
{
  "code": 200,
  "msg": "ok",
  "data": {
    "pageIndex": 1,
    "pageSize": 10,
    "total": 2,
    "pages": 1,
    "rows": [
      {
        "id": 2,
        "title": "跑步",
        "createdTime": "2024-12-03T13:00:00"
      },
      {
        "id": 1,
        "title": "写文档",
        "createdTime": "2024-12-03T12:00:00"
      }
    ]
  }
}
```

### POST /todos
- 功能：新增 Todo，`title` 必填且长度 ≤ 50。
- 请求示例：
```http
POST /todos
Content-Type: application/json

{
  "title": "买牛奶"
}
```
- 响应示例：
```json
{
  "code": 200,
  "msg": "ok",
  "data": "success"
}
```

### PUT /todos
- 功能：按 `id` 更新 `title`（`id` ≥ 1），更新行数为 0 时抛出 `NOT_FOUND` 业务异常。
- 请求示例：
```http
PUT /todos
Content-Type: application/json

{
  "id": 1,
  "title": "更新标题"
}
```
- 响应示例（业务成功时返回字符串消息，代码中为拼接后的提示）：
```json
{
  "code": 200,
  "msg": "ok",
  "data": "id=1 的数据更新成功"
}
```

### DELETE /todos/{id}
- 功能：按路径参数删除 Todo，`id` ≥ 1，删除行数为 0 时抛出 `NOT_FOUND`。
- 请求示例：
```
DELETE /todos/1
```
- 响应示例：
```json
{
  "code": 200,
  "msg": "ok",
  "data": "id=1 的数据删除成功"
}
```

### DELETE /todos
- 功能：批量删除，查询参数 `ids`（必填，列表不能为空）。
- 请求示例：
```
DELETE /todos?ids=1&ids=2&ids=3
```
- 响应示例：
```json
{
  "code": 200,
  "msg": "ok",
  "data": "删除了 3 条数据"
}
```

## 说明
- 入参校验基于 `jakarta.validation` 注解，校验异常会被 `GlobalExceptionHandler` 转换为 `PARAM_ERROR`，业务未找到场景返回 `NOT_FOUND`。
- 分页返回模型 `PageDTO` 字段包括：`pageIndex`、`pageSize`、`total`、`pages`、`rows`，其中 `rows` 为当前页 Todo 列表。
