# JWT_Filter_Only_Access_Allows_Biz

## TL;DR
- Filter 只接受 access token。
- token 空/无效/type 不符时放行到后续链路。
- 放行≠进入 controller，最终由安全配置控制。

## 核心概念
- 顺序：token 为空→放行；validate 失败→放行；type!=access→放行；成功才 setAuthentication。
- refresh 只能用于 refresh/logout 接口。
- 认证结果写入 SecurityContext 决定业务是否可达。

## 常见追问
- Q: 为什么 validate 失败也放行？/ A: 让后续鉴权统一处理未认证。 → [[JWT_Access_vs_Refresh]]
- Q: refresh 请求会被拦吗？/ A: refresh 不被设置认证，只能走专门接口。 → [[JWT_Refresh_Rotation]]
- Q: 放行后一定进 controller 吗？/ A: 不是，安全配置会再次拦截。 → [[JWT_Access_vs_Refresh]]

## 关联链接
- [[JWT_Access_vs_Refresh]]
