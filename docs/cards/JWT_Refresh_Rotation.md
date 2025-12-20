# JWT_Refresh_Rotation

## TL;DR
- rotation：refresh 成功后签发新 refresh，旧的立刻失效。
- 解决“refresh 被盗后无限续期”，把窗口压小到一次。
- 可选增强：reuse detection（复用检测触发全踢）只写概念不实现。

## 核心概念
- 基本流程：校验旧 refresh → 服务端确认仍有效（Redis）→ 生成新 refresh/access → 让旧 refresh 立即失效。
- 核心收益：被盗 refresh 最多只能成功一次。
- 可选增强（概念）：reuse detection
  - 如果发现“已作废的 refresh 再次被使用”，认为凭证泄露，可触发全踢/强制重新登录。

## 常见追问
- Q：rotation 为什么能提升安全性？
  - A：把 refresh 的可用次数限制为一次，泄露后不能无限续期。
  - → [[JWT_Why_Single_Token_Not_Enough]]
- Q：reuse detection 是什么？
  - A：检测到旧 refresh 被再次使用时触发更强风控（例如全踢）。
  - → [[JWT_Server_Side_Revocation_Redis]]
- Q：并发刷新如何保证只有一次成功？
  - A：需要把“校验+消费”做成原子操作，避免竞态。
  - → [[JWT_Filter_And_Concurrency]]

## 关联链接
- [[JWT_Server_Side_Revocation_Redis]]
- [[JWT_Filter_And_Concurrency]]
