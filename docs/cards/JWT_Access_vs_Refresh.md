# JWT_Access_vs_Refresh

## TL;DR
- access 用于业务访问，短 TTL。
- refresh 用于续期/登出，长 TTL。
- refresh 不应被业务接口接受。

## 核心概念
- access TTL 建议 5-15 分钟；refresh TTL 7-30 天（按风险调）。
- refresh 只能用于 refresh/logout，避免权限扩大。
- 服务端可通过 Redis 控制 refresh 撤销。

## 常见追问
- Q: refresh 能不能直接访问业务？/ A: 不能，必须只用于刷新或登出。 → [[JWT_Filter_Only_Access_Allows_Biz]]
- Q: access 太短会怎样？/ A: 需要更频繁刷新，但风险更低。 → [[JWT_Refresh_Rotation]]
- Q: refresh 为什么要配 Redis？/ A: 便于撤销，降低泄露影响。 → [[JWT_Server_Side_Revocation_Redis]]

## 关联链接
- [[JWT_Why_Single_Token_Not_Enough]]
- [[JWT_Server_Side_Revocation_Redis]]
