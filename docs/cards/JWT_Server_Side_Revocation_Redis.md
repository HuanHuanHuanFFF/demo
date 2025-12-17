# JWT_Server_Side_Revocation_Redis

## TL;DR
- JWT 天生不可撤销，需服务端补一层。
- Redis 保存 refresh key 让 logout/rotation 生效。
- key 设计与 TTL 对齐，避免脏数据。

## 核心概念
- Key 设计：refresh:{uuid}，值可为空或用户标识。
- TTL 与 refresh 有效期一致，过期自动清理。
- 撤销即删除 key，保证 refresh 失效。

## 常见追问
- Q: 为什么只撤销 refresh？/ A: access 本就短命，主要控制 refresh。 → [[JWT_Access_vs_Refresh]]
- Q: logout 怎么做？/ A: 删除 refresh key 即可。 → [[JWT_Logout_Behavior]]
- Q: 并发刷新会有竞态吗？/ A: hasKey+delete 可能竞态，需原子化。 → [[JWT_Concurrency_Test_and_Atomicity]]

## 关联链接
- [[JWT_Access_vs_Refresh]]
- [[JWT_Refresh_Rotation]]
