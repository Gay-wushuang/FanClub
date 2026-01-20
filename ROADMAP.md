# FanClubMaker 工程化 Roadmap（M0~M3）

> 目标：以**可验收里程碑**推进，避免“做了很多但验收不了”。

---

## 里程碑总览

- **M0（第 1-2 天）工程基座完成**：仓库结构、CI、契约、Mock、骨架可跑
- **M1（第 1 周）单场基础数据端到端**：场次列表 + summary 可展示
- **M2（第 2 周）明细 + 聚合**：礼物/弹幕/PK/进退房明细 + 时间点/时间段聚合
- **M3（第 3 周）插件/桌面小窗 + 可靠性**：OBS Browser Source + 小窗 + 降级/权限/导出/审计

---

## M0：工程基座（Day 1-2）

### 交付物（必须）
- 后端：Spring Boot 启动成功 + Swagger 可访问 + 统一返回体/异常
- 后端：RBAC 框架落地（最少：主播/经纪人）
- 后端：PostgreSQL migration 跑通（表结构初版）
- 后端：Redis 接入（占位即可）
- 前端：Next.js 工程骨架 + 路由框架 + 登录态占位
- 平台：CI 跑通（后端 mvn test + 前端 build）
- 契约：`docs/api-contract.md` v0.1 冻结
- Mock：前端可基于契约 mock 出完整页面

### 验收方式
- 一条命令可让 reviewer 在本地跑起来（README 写清）
- 提供 1 分钟录屏：Swagger 打开 + 前端页面可访问

---

## M1：单场基础数据（Week 1）

### 功能范围
- 场次（session）列表：按主播/房间/时间范围筛选
- 单场 summary：开播时间、时长、在线峰值、弹幕数、礼物数/流水、付费人数等

### 推荐接口
- `GET /dashboard/sessions?anchorId&roomId&startTime&endTime&page&size`
- `GET /dashboard/sessions/{sessionId}/summary`

### 验收产物
- 主播侧：能选一个 session 看 summary 卡片
- 经纪人侧：能筛选主播/时间范围看到 sessions 列表
- 数据口径说明：字段单位、时区、延迟阈值（初版）

---

## M2：明细 + 聚合（Week 2）

### 功能范围
- 明细：礼物/弹幕/PK/进退房（分页、筛选）
- 聚合：时间点/时间段（minute/hour/day）

### 推荐接口
- `GET /dashboard/sessions/{sessionId}/events?type=gift|danmaku|pk|enterleave&page&size&startTime&endTime`
- `GET /dashboard/aggregate?anchorId&roomId&granularity=minute|hour|day&startTime&endTime`

### 验收产物
- 主播侧：明细 tabs + 图表（聚合）
- 经纪人侧：同上 + 对比/筛选
- 审计日志：导出/查看明细等敏感操作留痕（至少接口留痕）

---

## M3：插件/桌面小窗 + 可靠性（Week 3）

### 功能范围（MVP）
- OBS 插件：**Browser Source 页面**（大字指标 + 断线/延迟提示 + 一键关闭）
- 桌面小窗：Web 独立路由页（后续可 Electron 化）
- 实时：WebSocket/SSE 或轮询（写清延迟口径）
- 可靠性：降级、重试、权限态、导出、审计完善

### 验收产物
- OBS 录屏：添加 Browser Source -> 展示指标 -> 延迟提示/断线提示
- 桌面小窗录屏：打开独立窗口 -> 指标刷新 -> 一键关闭展示
- 部署说明：dev/prod 配置与启动步骤
- 验收 checklist：逐条勾选