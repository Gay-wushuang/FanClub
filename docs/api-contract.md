# API 契约与数据口径（v0.1）

> 本文档用于**冻结字段口径**，保障前后端并行开发，与 `openapi.yaml` 保持一致。

---

## 1. 通用约定

### 1.1 时间与时区
- 所有时间字段：ISO8601 字符串，例如 `2026-01-20T10:12:30+08:00`
- 默认时区：`Asia/Shanghai`（UTC+8）
- 字段命名：统一使用 `start_time`、`end_time` 格式（下划线命名法）

### 1.2 金额与单位
- 金额使用最小货币单位（分）
- 字段命名：`xxx_fen`（分），`xxx_count`（数量），`xxx_users`（人数）

### 1.3 分页
- **请求参数**：`page`（从 1 开始，默认 1）、`size`（默认 20）、`sort`（可选，默认 `-time` 倒序）
- **返回结构**：
  ```json
  {
    "total": 100,
    "page": 1,
    "size": 20,
    "items": []
  }
  ```

### 1.4 统一返回体
```json
{
  "code": 0,
  "message": "ok",
  "traceId": "xxx",
  "data": {}
}
```

---

## 2. 关键实体

### 2.1 SessionBrief（直播场次基础信息）
```json
{
  "session_id": "string",
  "anchor_id": "string",
  "room_id": "string",
  "title": "string",
  "category": "string",
  "start_time": "ISO8601",
  "end_time": "ISO8601|null",
  "duration_sec": "integer|null",
  "status": "LIVE|ENDED",
  "cover_url": "string|null",
  "tags": ["string"]
}
```

### 2.2 SessionSummary（单场基础数据）
```json
{
  "title": "string",
  "category": "string",
  "start_time": "ISO8601",
  "end_time": "ISO8601|null",
  "duration_sec": "integer",
  "status": "LIVE|ENDED",
  "revenue_fen": "integer",
  "income_fen": "integer",
  "paid_users": "integer",
  "paid_count": "integer",
  "paid_rate": "float",
  "danmaku_users": "integer",
  "danmaku_count": "integer",
  "sc_count": "integer",
  "gift_users": "integer",
  "gift_count": "integer",
  "peak_online": "integer",
  "avg_online": "integer",
  "avg_stay_sec": "integer",
  "data_delay_sec": "integer"
}
```

### 2.3 枚举类型

#### 2.3.1 SessionStatus
- `LIVE`：直播中
- `ENDED`：已结束

#### 2.3.2 Granularity
- `MINUTE`：分钟级
- `HOUR`：小时级
- `DAY`：天级
- `WEEK`：周级
- `MONTH`：月级

#### 2.3.3 DanmakuType
- `NORMAL`：普通弹幕
- `SC`：超级弹幕

#### 2.3.4 DanmakuTypeFilter（查询用）
- `NORMAL`：普通弹幕
- `SC`：超级弹幕
- `ALL`：所有弹幕

---

## 3. 接口规范

### 3.1 API 基础路径
- 所有 API 接口路径前缀：`/api/v1`

### 3.2 认证方式
- 统一使用 JWT Bearer 认证
- 请求头：`Authorization: Bearer <token>`

### 3.3 错误码规范
| 错误码 | 描述 |
|--------|------|
| 401 | 未认证，需要登录 |
| 403 | 权限不足，禁止访问 |
| 404 | 资源不存在 |
| 409 | 资源冲突 |
| 500 | 服务器内部错误 |

---

## 4. 关键接口示例

### 4.1 查询场次列表
`GET /api/v1/sessions`

**Query 参数**：
- `anchor_id`（可选）
- `room_id`（可选）
- `start_time`/`end_time`（可选，ISO8601 格式）
- `page`（可选，默认 1）
- `size`（可选，默认 20）
- `sort`（可选，默认 `-start_time`）

**Response**：
```json
{
  "code": 0,
  "message": "ok",
  "traceId": "xxx",
  "data": {
    "total": 138,
    "page": 1,
    "size": 20,
    "items": [
      {
        "session_id": "sess_123",
        "anchor_id": "anc_456",
        "room_id": "room_789",
        "title": "直播标题",
        "category": "游戏",
        "start_time": "2026-01-20T10:00:00+08:00",
        "status": "LIVE",
        "cover_url": "https://example.com/cover.jpg",
        "tags": ["游戏", "热门"]
      }
    ]
  }
}
```

### 4.2 查询单场数据
`GET /api/v1/sessions/{sessionId}`

**Response**：
```json
{
  "code": 0,
  "message": "ok",
  "traceId": "xxx",
  "data": {
    "session_id": "sess_123",
    "anchor_id": "anc_456",
    "room_id": "room_789",
    "title": "直播标题",
    "category": "游戏",
    "start_time": "2026-01-20T10:00:00+08:00",
    "end_time": "2026-01-20T12:00:00+08:00",
    "duration_sec": 7200,
    "status": "ENDED",
    "cover_url": "https://example.com/cover.jpg",
    "tags": ["游戏", "热门"]
  }
}
```

### 4.3 查询单场数据汇总
`GET /api/v1/sessions/{sessionId}/summary`

**Response**：
```json
{
  "code": 0,
  "message": "ok",
  "traceId": "xxx",
  "data": {
    "title": "直播标题",
    "category": "游戏",
    "start_time": "2026-01-20T10:00:00+08:00",
    "end_time": "2026-01-20T12:00:00+08:00",
    "duration_sec": 7200,
    "status": "ENDED",
    "revenue_fen": 10000,
    "income_fen": 8000,
    "paid_users": 50,
    "paid_count": 120,
    "paid_rate": 0.25,
    "danmaku_users": 200,
    "danmaku_count": 5000,
    "sc_count": 30,
    "gift_users": 80,
    "gift_count": 200,
    "peak_online": 300,
    "avg_online": 150,
    "avg_stay_sec": 1200,
    "data_delay_sec": 10
  }
}
```

### 4.4 查询弹幕事件
`GET /api/v1/sessions/{sessionId}/events/danmaku`

**Query 参数**：
- `type`（可选，`NORMAL|SC|ALL`，默认 `ALL`）
- `start_time`/`end_time`（可选）
- `page`（可选，默认 1）
- `size`（可选，默认 20）
- `sort`（可选，默认 `-time`）

### 4.5 聚合数据查询
`GET /api/v1/metrics/aggregate`

**Query 参数**：
- `anchor_id`/`room_id`（可选）
- `granularity`（必填，`MINUTE|HOUR|DAY|WEEK|MONTH`）
- `start_time`/`end_time`（必填，ISO8601 格式）

---

## 5. 延迟口径与降级
- **指标延迟**：默认允许 5~30 秒（按接入方式定）
- **延迟阈值**：超过 30 秒时，前端必须提示“数据延迟/暂不可用”
- **降级策略**：
  - 从实时推送降级为轮询
  - 从明细数据降级为聚合数据
  - 优先保障核心指标可用性

---

## 6. 健康检查
- 根路径健康检查：`GET /`
- API 健康检查：`GET /api/v1/`
- 返回 200 OK 表示服务正常

