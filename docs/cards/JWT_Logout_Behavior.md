# JWT_Logout_Behavior

## TL;DR
- logout 主要撤销 refresh，使后续刷新失败。
- access 是否立刻失效是可选增强。
- 统一错误码口径可降低前端处理复杂度。

## 核心概念
- 删除 refresh key：refresh:{uuid}。
- access 立即失效可用黑名单或缩短 TTL。
- 登出行为应与 refresh rotation 兼容。

## 常见追问
- Q: access 需要立刻失效吗？/ A: 可选增强，权衡复杂度与风险。 → [[JWT_Filter_Only_Access_Allows_Biz]]
- Q: logout 失败会怎样？/ A: refresh 未撤销会允许继续刷新。 → [[JWT_Server_Side_Revocation_Redis]]
- Q: 前端怎么处理错误？/ A: 统一错误码便于复用逻辑。 → [[JWT_Access_vs_Refresh]]

## 关联链接
- [[JWT_Server_Side_Revocation_Redis]]
- [[JWT_Refresh_Rotation]]
