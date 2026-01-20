# API 契约与数据口径（v0.1）

> 本文档用于**冻结字段口径**，保障前后端并行开发。

---

## 1. 通用约定

### 1.1 时间与时区
- 所有时间字段：ISO8601 字符串，例如 `2026-01-20T10:12:30+08:00`
- 默认时区：`Asia/Shanghai`（UTC+8）

### 1.2 金额与单位
- 金额使用最小货币单位（建议：分）
- 字段命名：`xxxAmount`（分），`xxxCount`（数量），`xxxUsers`（人数）

### 1.3 分页
- 请求：`page`（从 1 开始）、`size`
- 返回：`total`、`page`、`size`、`items`

### 1.4 统一返回体
```json
{
  "code": 0,
  "message": "ok",
  "data": {}
}
```

---

## 2. 关键实体

### 2.1 Session（直播场次）
- `sessionId`：string
- `anchorId`：string
- `roomId`：string
- `title`：string
- `startedAt`：ISO8601
- `endedAt`：ISO8601（可空）

### 2.2 Summary（单场基础数据）
- `durationSeconds`
- `viewerPeak`
- `danmakuCount`
- `giftCount`
- `giftAmount`（分）
- `paidUsers`

---

## 3. 接口（建议）

### 3.1 查询场次列表
`GET /dashboard/sessions`

Query：
- `anchorId`（可选）
- `roomId`（可选）
- `startTime`/`endTime`（可选）
- `page`/`size`

Response：
- `items[]`：Session

### 3.2 查询单场 summary
`GET /dashboard/sessions/{sessionId}/summary`

Response：Summary

### 3.3 查询单场事件明细
`GET /dashboard/sessions/{sessionId}/events`

Query：
- `type=gift|danmaku|pk|enterleave`
- `startTime`/`endTime`
- `page`/`size`

Response：
- `items[]`：事件对象（按 type 不同而不同）

### 3.4 聚合查询
`GET /dashboard/aggregate`

Query：
- `anchorId`/`roomId`
- `granularity=minute|hour|day`
- `startTime`/`endTime`

Response：
- `items[]`：`{ time, metrics... }`

---

## 4. 延迟口径与降级
- 指标延迟：默认允许 5~30 秒（按接入方式定）
- 超过阈值：前端必须提示“数据延迟/暂不可用”
- 降级：可从实时推送降级为轮询，或从明细降级为聚合
