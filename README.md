# Todo Demo（Spring Boot + MyBatis-Plus + Redis）

## 项目简介
- 基于 Spring Boot 3、MyBatis-Plus 的 Todo 示例项目，提供 Todo 的增删改查 REST 接口。
- 数据模型：`todo` 表字段 `id`（自增主键）、`title`、`created_time`、`version`（乐观锁）。实体 `Todo` 映射 `id`、`title`、`createdTime`、`version`。
- 校验：`title` 必填且长度 ≤ 50；分页参数 `pageIndex`、`pageSize` 需 ≥ 1，`pageSize` ≤ 50。
- 分页/排序/模糊查询通过 MyBatis-Plus 的 `Page`、`QueryWrapper` 实现。
- 统一返回包装 `Result<T>`，搭配全局异常处理。

## 本地运行
1) 依赖：需要可用的 MySQL（默认 `demo` 库）和 Redis。
2) 配置：`src/main/resources/application.properties`
   - MySQL：`jdbc:mysql://localhost:3306/demo`，用户名/密码 `root`/`root`，可按需修改。
   - Redis：`spring.data.redis.host=localhost`，`spring.data.redis.port=6379`（密码如有请放开 `spring.data.redis.password`）。
3) 数据表：确保存在 `todo` 表，包含 `id`(AUTO)、`title`(varchar≤50)、`created_time`(timestamp/datetime)、`version`(int/默认0)。
4) 启动：在项目根目录运行 `mvnw.cmd spring-boot:run`（Windows）或 `./mvnw spring-boot:run`（类 Unix）。
5) 访问：默认端口 8080，基础路径 `http://localhost:8080`。

## 统一返回与错误码
- 返回结构：`{ "code": number, "msg": string, "data": any }`，成功场景使用 `Result.ok(...)`。
- 错误码（`CodeStatus`）：
  - `SUCCESS`：200，ok
  - `PARAM_ERROR`：1001，请求参数错误（校验、类型、缺失）
  - `NOT_FOUND`：1004，数据不存在（删除/更新影响行数为 0）
  - `UPDATE_CONFLICT`：409，数据已被其他请求修改或不存在（乐观锁/更新失败）
  - `SERVER_ERROR`：500，服务器内部错误
- 异常转换：`GlobalExceptionHandler` 将校验异常映射为 `PARAM_ERROR`，业务异常映射为对应 CodeStatus，未知异常兜底 `SERVER_ERROR`。

## Redis 缓存策略（新增）
项目为 Todo 模块接入 Redis，提升读性能并避免击穿/雪崩。

### 1) 单条查询缓存（byId 缓存）
- Key：`todo:byId:{id}`
- 命中直接返回 JSON；未命中查库并回填。
- TTL：基础 30 分钟 + 抖动（0~5 分钟）`RandomUtils.randomJitterSeconds`，避免同一时刻集中过期。

### 2) NULL 缓存（防穿透）
- 当数据库返回空时写入特殊值 `"NULL"`，TTL 60 秒。
- 防止对不存在 id 的高频访问直接打到数据库。

### 3) 列表缓存（分页缓存）
- Key：`todo:list:v{ver}:{page}-{size}-{sort}-{dir}-{title}`
- TTL：基础 3 分钟 + 抖动（0~2 分钟）。
- 覆盖分页、排序、模糊查询场景。

### 4) 列表缓存命名空间（版本号机制）
- 全局版本号：`todo:list:ver`。
- 读列表时自动拼接当前版本（见上方 key 规则）。
- 任意写操作成功后：`INCR todo:list:ver`，使旧版本缓存自然失效（无需 keys 扫描，旧缓存等待 TTL 过期）。

### 5) 写后缓存一致性
- 新增/更新/删除成功后：
  1. 删除对应 `todo:byId:{id}` 缓存
  2. `INCR todo:list:ver` 使列表缓存换代
- 更新使用乐观锁（`@Version`），行数为 0 时抛出 `UPDATE_CONFLICT`。

### 6) Redis 可用性探针
- `GET /redis/ping`：每次访问自增 `test:ping` 并返回访问计数，用于本地验证 Redis 连接。

## 接口文档（Todo）
基础路径 `/todos`。

### GET /todos/hello
- 功能：示例/健康检查。
- 请求示例：`GET /todos/hello`
- 响应示例：
```json
{
  "code": 200,
  "msg": "ok",
  "data": { "msg": "Hello,HuanF!", "author": "HuanF" }
}
```

### GET /todos/all
- 功能：返回全部 Todo 列表（不分页，直接查库）。
- 请求示例：`GET /todos/all`
- 响应示例：
```json
{
  "code": 200,
  "msg": "ok",
  "data": [
    { "id": 1, "title": "写文档", "createdTime": "2024-12-03T12:00:00", "version": 0 },
    { "id": 2, "title": "跑步",   "createdTime": "2024-12-03T13:00:00", "version": 0 }
  ]
}
```

### GET /todos
- 功能：分页 + 排序 + 标题模糊查询（结果带列表缓存 + 版本号命名空间）。

查询参数：

| 参数名      | 类型            | 是否必填 | 默认值         | 含义                                       |
| ----------- | --------------- | -------- | -------------- | ------------------------------------------ |
| `pageIndex` | Long            | 是       | 无             | 页码（从 1 开始），`@Min(1)`               |
| `pageSize`  | Long            | 是       | 无             | 每页大小，`@Min(1)`、`@Max(50)`            |
| `title`     | String          | 否       | 空串           | 标题模糊查询                               |
| `sortBy`    | TodoSortBy enum | 否       | `CREATED_TIME` | 排序字段（当前仅 `CREATED_TIME`）          |
| `sortDir`   | SortDir enum    | 否       | `DESC`         | 排序方向（`ASC` / `DESC`）                 |

- 请求示例：`GET /todos?pageIndex=1&pageSize=10&title=文档&sortBy=CREATED_TIME&sortDir=DESC`
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
      { "id": 2, "title": "跑步",   "createdTime": "2024-12-03T13:00:00", "version": 0 },
      { "id": 1, "title": "写文档", "createdTime": "2024-12-03T12:00:00", "version": 0 }
    ]
  }
}
```

### GET /todos/{id}
- 功能：按 id 查询 Todo，命中 byId 缓存或查库；未找到返回 `data=null`（会写 NULL 缓存 60 秒）。
- 请求示例：`GET /todos/1`
- 响应示例（存在时）：
```json
{
  "code": 200,
  "msg": "ok",
  "data": { "id": 1, "title": "写文档", "createdTime": "2024-12-03T12:00:00", "version": 0 }
}
```

### POST /todos
- 功能：新增 Todo，`title` 必填、长度 ≤ 50，`createdTime` 服务端填充。
- 请求示例：
```http
POST /todos
Content-Type: application/json

{ "title": "买牛奶" }
```
- 响应示例：
```json
{ "code": 200, "msg": "ok", "data": "success" }
```

### PUT /todos/{id}
- 功能：按 id 更新 Todo（依赖乐观锁字段 `version`）；行数为 0 会抛出 `UPDATE_CONFLICT`。
- 请求示例：
```http
PUT /todos/1
Content-Type: application/json

{ "title": "更新标题", "version": 0 }
```
- 响应示例（成功场景，data 为提示字符串）：
```json
{ "code": 200, "msg": "ok", "data": "id=1 的数据更新成功" }
```

### DELETE /todos/{id}
- 功能：按 id 删除 Todo，`id` ≥ 1；影响行数为 0 抛出 `NOT_FOUND`。
- 请求示例：`DELETE /todos/1`
- 响应示例：
```json
{ "code": 200, "msg": "ok", "data": "id=1 的数据删除成功" }
```

### DELETE /todos
- 功能：批量删除，查询参数 `ids`（必填，列表不能为空）。
- 请求示例：`DELETE /todos?ids=1&ids=2&ids=3`
- 响应示例：
```json
{ "code": 200, "msg": "ok", "data": "删除了 3 条数据" }
```

## 其他
- 乐观锁：实体字段 `version` 使用 `@Version`，更新时自动带上版本号，防止并发覆盖。
- 缓存删除策略：不做 keys 扫描，写操作仅删除对应 byId、然后通过版本号让列表缓存自然过期，避免 Redis 阻塞。
- Redis 探针：`GET /redis/ping` 可快速验证 Redis 连接是否正常。
