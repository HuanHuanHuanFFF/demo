# JWT_Access_vs_Refresh

## TL;DR
- access：业务访问用，短 TTL；refresh：续期/登出用，长 TTL。
- refresh 不应被业务接口接受，避免权限边界被打破。
- 设计目标：体验（免频繁登录）与安全（可撤销、缩窗口）同时满足。

## 核心概念
| 维度   | access       | refresh           |
| ---- | ------------ | ----------------- |
| 职责   | 访问业务接口       | 换 access / logout |
| TTL  | 10–30min（建议） | 7–30d（建议）         |
| 风险   | 泄露窗口短        | 泄露窗口长（必须可撤销）      |
| 存储   | 内存/短期存储      | 更谨慎（尽量安全存储）       |
| 接口使用 | 绝大多数业务接口     | 仅 refresh/logout  |

- 为什么这样设计：把“访问权限”与“续期能力”拆开，业务面只接受 access，refresh 只走少数专用接口。

## 常见追问
- Q：为什么 refresh 不能访问业务接口？
  - A：refresh 泄露代价更高，必须限制在少数专用接口。
  - → [[JWT_Filter_And_Concurrency]]
- Q：access TTL 该怎么选？
  - A：在安全与刷新频率之间折中，一般 10–30min 足够。
  - → [[JWT_AccessToken]]
- Q：refresh TTL 该怎么选？
  - A：按敏感度设 7–30d，并配合撤销与 rotation。
  - → [[JWT_RefreshToken]]

## 关联链接
- [[JWT_Why_Single_Token_Not_Enough]]
- [[JWT_RefreshToken]]
- [[JWT_AccessToken]]
