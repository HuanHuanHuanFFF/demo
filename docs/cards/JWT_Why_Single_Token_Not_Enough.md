# JWT_Why_Single_Token_Not_Enough

## TL;DR
- 只有短 token 会频繁登录，体验差。
- 只有长 token 风险大，泄露后长期有效。
- access/refresh 拆分兼顾体验与安全。

## 核心概念
- access 短 TTL、只用于访问业务接口。
- refresh 长 TTL、只用于刷新/登出，配合服务端撤销。
- 角色分离降低泄露成本和攻击窗口。

## 常见追问
- Q: 为什么不能只用一个短 token？/ A: 频繁登录成本高，移动端更明显。 → [[JWT_Access_vs_Refresh]]
- Q: 为什么不能只用一个长 token？/ A: 泄露后长期有效，风险不可控。 → [[JWT_Server_Side_Revocation_Redis]]
- Q: 拆分之后还需要 rotation 吗？/ A: rotation 能缩短 refresh 被盗窗口。 → [[JWT_Refresh_Rotation]]

## 关联链接
- [[JWT_Access_vs_Refresh]]
