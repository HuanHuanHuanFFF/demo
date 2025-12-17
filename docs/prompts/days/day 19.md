你是我的 Obsidian 笔记整理助手。请基于“今天我们做的 JWT：access/refresh、Redis 可撤销、logout、refresh rotation、并发 JUnit 测试 ok=1 fail=29、Filter 只认 access、错误码口径统一”等内容，生成一组可直接放进 vault 的 Markdown 文件（无插件）。

请输出为“多文件内容”，每个文件用如下格式分隔：
---FILE: <path>---
<markdown content>

文件要求（模块化 + 互链）：
A) 生成 1 个 Hub 总览页：`MOCs/JWT_MOC.md`
- 顶部：一句话目标 + 今日结论（5 条以内）
- 中部：模块目录（每条是 `[[双链]]`）
- 下部：给一个 Mermaid “链接思维导图”（用 graph TD/flowchart，不用 mindmap 语法；节点文本用模块名）
  - 并在 Mermaid 里把节点变成可点击内链：对节点加 `internal-link` class（按 Obsidian 支持的写法）
- 末尾：给一段“面试 30 秒口述版”（要点式）

B) 生成 7 个小模块页（每页短而完整，互相链接）：
1. `cards/JWT_Why_Single_Token_Not_Enough.md`
   - 解释：只用短 token 会频繁登录；只用长 token 风险大；因此拆成 access/refresh
   - 链接到：[[JWT_Access_vs_Refresh]]、[[JWT_Threat_Model_And_Theft]]
2. `cards/JWT_Access_vs_Refresh.md`
   - 定义、TTL 建议、用途边界（refresh 只能用于 refresh/logout）
3. `cards/JWT_Server_Side_Revocation_Redis.md`
   - 为什么要 Redis：让 refresh 可撤销（否则 JWT 天生“发出就很难收回”）
   - Key 设计：refresh:{uuid}；TTL 与 refresh 对齐
4. `cards/JWT_Logout_Behavior.md`
   - logout 做什么：删除 refreshKey；access 是否立刻失效（注明：可选增强）
5. `cards/JWT_Refresh_Rotation.md`
   - rotation：refresh 成功后换新 refresh，旧的立刻失效
   - 解决：refresh 被盗“可无限续期”的问题（把窗口压小）
6. `cards/JWT_Filter_Only_Access_Allows_Biz.md`
   - Filter 顺序：token 为空→放行；validate 失败→放行；type!=access→放行；成功才 setAuthentication
   - 解释“放行≠进 controller”
7. `cards/JWT_Concurrency_Test_and_Atomicity.md`
   - 记录：JUnit 并发测试思路 + 结果 ok=1 fail=29
   - 指出：hasKey+delete 有理论竞态；生产建议 Lua 原子消费（写“可选增强”小节，不必实现代码）

每个模块页结构统一：
- TL;DR（3 行）
- 核心概念（要点）
- 常见追问（2~4 条）
- 关联链接（至少 3 个 `[[...]]`）

输出只要多文件 Markdown 内容，不要解释过程。
