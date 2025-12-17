# JWT_Concurrency_Test_and_Atomicity

## TL;DR
- 并发 refresh 测试：threads=30，ok=1，fail=29。
- 现有 hasKey+delete 理论上存在竞态。
- 生产建议使用 Lua/原子操作消费 refresh。

## 核心概念
- 并发测试验证 refresh 只允许一次成功。
- hasKey+delete 非原子，可能被多线程击穿。
- 原子消费方案可将成功数稳定在 1。

## 常见追问
- Q: 为什么会出现 ok=1 fail=29？/ A: 设计目标就是单次刷新成功。 → [[JWT_Refresh_Rotation]]
- Q: 竞态的风险是什么？/ A: 极端情况下可能多次成功。 → [[JWT_Server_Side_Revocation_Redis]]
- Q: 如何增强原子性？/ A: 用 Lua 或原子命令一次性校验并删除。 → [[JWT_Server_Side_Revocation_Redis]]

## 关联链接
- [[JWT_Refresh_Rotation]]
