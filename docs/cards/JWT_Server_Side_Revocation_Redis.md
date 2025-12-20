# JWT_Server_Side_Revocation_Redis

## TL;DR
- JWT 天生“发出后难撤销”，长 TTL 的 refresh 必须补服务端状态。
- 用 Redis 保存 refresh key：`refresh:{uuid}`，TTL 与 refresh 对齐。
- 错误码口径统一：减少前端分支与灰色状态。

## 核心概念
- 为什么要 Redis：refresh 若不可撤销，logout/风控就失效。
- Key 设计：`refresh:{uuid}`（uuid 来自 refresh 的 `jti/refreshId`）。
- TTL 对齐：Redis key 的过期时间与 refresh 的 exp 一致，自动清理。
- 错误口径统一（思路）：
  - refresh 校验失败 / key 不存在 / 已撤销：统一返回同一类“需要重新登录或重新 refresh”的错误码。

## 常见追问
- Q：logout 到底做什么才算“登出”？
  - A：删除 refresh 对应的 Redis key，让后续 refresh 立即失败。
- Q：rotation 和 Redis 的关系是什么？
  - A：rotation 本质是“替换旧 refresh 的服务端状态”，需要 Redis 支撑。
- Q：并发刷新为什么会提到原子性？
  - A：如果校验与消费不是原子操作，极端并发下可能出现多次成功风险。
  - → [[JWT_Filter_And_Concurrency]]

## 关联链接
- [[JWT_RefreshToken]]
- [[JWT_Refresh_Rotation]]
- [[JWT_Filter_And_Concurrency]]
