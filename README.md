# FanClub API

## 1. 简介
FanClub API 提供了完整的直播平台接口，包括主播端、经纪人端、粉丝端和管理端功能，支持前后端并行开发。

## 2. 文档体系

### 2.1 OpenAPI 文档
- **位置**：`docs/openapi.yaml`
- **规范**：OpenAPI 3.0.3
- **功能**：完整的 API 接口定义，包含请求/响应结构、错误码、认证方式等
- **用途**：自动生成文档、Mock 服务、前端类型定义、后端代码生成

### 2.2 API 契约文档
- **位置**：`docs/api-contract.md`
- **功能**：冻结字段口径，保障前后端并行开发
- **内容**：通用约定、关键实体定义、接口建议、延迟口径与降级策略

## 3. Mock 服务
使用 Prism 启动 Mock 服务，支持前端并行开发。

### 3.1 启动 Mock 服务
```powershell
# 项目已配置 @stoplight/prism-cli 依赖
pnpm prism mock docs/openapi.yaml -p 4010
```

### 3.2 前端接入 Mock
在前端项目的 `.env.local` 文件中添加：
```
NEXT_PUBLIC_API_BASE=http://localhost:4010/api/v1
```

### 3.3 健康检查
Mock 服务启动后，可通过以下地址验证服务状态：
```
http://localhost:4010/  # 根路径健康检查
http://localhost:4010/api/v1/  # API 健康检查
```

## 4. API 分组

| 分组 | 描述 |
|------|------|
| `auth` | 认证相关接口，包括登录、刷新令牌等 |
| `sessions` | 直播场次管理，包括场次创建、状态查询等 |
| `events` | 直播事件处理，包括礼物、弹幕、PK、进退房、笔记等 |
| `metrics` | 数据指标接口，包括时间点指标和时间段聚合数据 |
| `agent` | 经纪人增强接口，提供经纪人专属功能 |
| `overlay` | OBS/桌面小窗插件接口，用于实时数据展示 |
| `fanclub` | 粉丝端接口，包括粉丝团、任务、事件查询等 |
| `admin` | 管理端接口，包括主播管理、经纪人管理、黑名单、审计日志等 |
| `export` | 数据导出接口，支持批量数据导出 |
| `audit` | 审计日志接口，记录系统操作日志 |

## 5. 核心数据模型

### 5.1 直播场次相关
- `SessionBrief`：场次基本信息（sessionId, roomId, anchorId, title, category, startTime, endTime, durationSec, status, coverUrl, tags）
- `SessionSummary`：单场基础数据（durationSeconds, viewerPeak, danmakuCount, giftCount, giftAmount, paidUsers）

### 5.2 事件相关
- `GiftEvent`：礼物事件
- `DanmakuEvent`：弹幕事件
- `PkEvent`：PK事件
- `EnterLeaveEvent`：进退房事件

### 5.3 数据指标相关
- `MetricPoint`：时间点指标
- `MetricBucket`：时间段聚合指标

### 5.4 Overlay 相关
- `OverlayConfig`：Overlay 配置
- `OverlayData`：Overlay 实时数据

## 6. 通用约定

### 6.1 认证方式
使用 JWT Bearer 认证，在请求头中添加：
```
Authorization: Bearer <token>
```

### 6.2 统一返回格式

#### 6.2.1 成功响应
```json
{
  "code": 0,
  "message": "ok",
  "traceId": "xxx",
  "data": { ... }
}
```

#### 6.2.2 分页响应
```json
{
  "code": 0,
  "message": "ok",
  "traceId": "xxx",
  "data": {
    "total": 100,
    "page": 1,
    "size": 20,
    "items": [ ... ]
  }
}
```

#### 6.2.3 失败响应
```json
{
  "code": 40001,
  "message": "invalid param",
  "traceId": "xxx",
  "details": { ... }
}
```

### 6.3 错误码规范
- `401`：未认证，需要登录
- `403`：权限不足，禁止访问
- `404`：资源不存在
- `409`：资源冲突
- `500`：服务器内部错误

### 6.4 分页规范
- **请求参数**：`page`（从 1 开始）、`size`（每页大小）、`sort`（排序字段，可选）
- **响应结构**：`total`（总条数）、`page`（当前页码）、`size`（每页大小）、`items`（数据列表）

### 6.5 时间与金额格式
- **时间**：ISO8601 格式，包含时区，如 `2026-01-20T10:30:00+08:00`
- **金额**：分（int64），字段命名建议：`xxxAmount`（分）、`xxxCount`（数量）、`xxxUsers`（人数）

## 7. 开发流程

1. **接口设计**：根据需求设计 API 接口，更新 `docs/openapi.yaml`
2. **契约同步**：将关键字段口径同步到 `docs/api-contract.md`
3. **Mock 测试**：启动 Prism Mock 服务，验证接口设计
4. **前端开发**：基于 Mock 服务进行前端开发
5. **后端开发**：根据 OpenAPI 文档进行后端开发
6. **联调测试**：前后端联调，验证接口正确性
7. **验收测试**：根据验收标准进行测试

## 8. 验收标准

- ✅ `docs/openapi.yaml` 可被 Prism 启动 Mock 服务
- ✅ 前端可在 Mock 模式下跑通核心功能
- ✅ 后端导出的 `/v3/api-docs` 与 `openapi.yaml` 字段口径一致
- ✅ 所有接口遵循统一的返回格式和错误码规范
- ✅ 认证接口包含完整的 401/403 错误处理
- ✅ 分页接口返回正确的分页结构

## 9. 技术栈

- **API 规范**：OpenAPI 3.0.3
- **Mock 工具**：@stoplight/prism-cli
- **包管理器**：pnpm
- **Node 版本**：18+

## 10. 贡献指南

1. 遵循 OpenAPI 3.0.3 规范
2. 保持接口设计的一致性
3. 及时更新 API 契约文档
4. 确保 Mock 服务能正常启动
5. 提交前验证文档格式正确性