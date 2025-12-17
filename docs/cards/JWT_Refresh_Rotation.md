# JWT_Refresh_Rotation

## TL;DR
- refresh 成功后签发新 refresh，旧的立刻失效。
- 让 refresh 被盗也只能使用一次。
- 需要服务端状态或原子操作配合。

## 核心概念
- rotation 流程：验证旧 refresh → 生成新 refresh → 替换存储。
- 旧 refresh 立即作废，缩短攻击窗口。
- 配合 Redis key/uuid 设计。

## 常见追问
- Q: rotation 必要吗？/ A: 可显著降低 refresh 被盗风险。 → [[JWT_Why_Single_Token_Not_Enough]]
- Q: 并发刷新会冲突吗？/ A: 需要原子消费避免多次成功。 → [[JWT_Concurrency_Test_and_Atomicity]]
- Q: rotation 失败如何处理？/ A: 保证旧 refresh 作废并返回重新登录。 → [[JWT_Logout_Behavior]]

## 关联链接
- [[JWT_Server_Side_Revocation_Redis]]
- [[JWT_Concurrency_Test_and_Atomicity]]
