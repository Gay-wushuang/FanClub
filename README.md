# FanClub API

## 1. 简介
FanClub API 提供了完整的直播平台接口，包括主播端、经纪人端、粉丝端和管理端功能。

## 2. OpenAPI 文档
API 文档位于 `docs/openapi.yaml`，遵循 OpenAPI 3.0.3 规范。

## 3. Mock 服务
使用 Prism 启动 Mock 服务，支持前端并行开发。

### 3.1 安装 Prism CLI
```powershell
# 需要 Node 18+
corepack enable
pnpm add -D @stoplight/prism-cli
```

### 3.2 启动 Mock 服务
```powershell
pnpm prism mock docs/openapi.yaml -p 4010
```

### 3.3 前端接入 Mock
在前端项目的 `.env.local` 文件中添加：
```
NEXT_PUBLIC_API_BASE=http://localhost:4010/api/v1
```

## 4. API 分组
- `auth`：认证相关接口
- `sessions`：直播场次管理
- `events`：礼物/弹幕/PK/进退房/笔记
- `metrics`：时间点/时间段聚合数据
- `agent`：经纪人增强接口
- `overlay`：OBS/桌面小窗插件
- `fanclub`：粉丝端接口
- `admin`：管理端接口
- `export`：数据导出
- `audit`：审计日志

## 5. 核心数据模型
- `SessionBrief`：场次基本信息
- `SessionSummary`：单场基础数据
- `GiftEvent`：礼物事件
- `DanmakuEvent`：弹幕事件
- `PkEvent`：PK事件
- `EnterLeaveEvent`：进退房事件
- `MetricPoint`：时间点指标
- `MetricBucket`：时间段聚合指标
- `OverlayConfig`：Overlay 配置
- `OverlayData`：Overlay 实时数据

## 6. 认证方式
使用 JWT Bearer 认证，在请求头中添加：
```
Authorization: Bearer <token>
```

## 7. 统一返回格式
所有 API 响应都包含 `traceId`，用于请求追踪。

### 7.1 成功响应
```json
{ "code": 0, "message": "ok", "traceId": "xxx", "data": { ... } }
```

### 7.2 失败响应
```json
{ "code": 40001, "message": "invalid param", "traceId": "xxx", "details": { ... } }
```

## 8. 分页规范
列表接口统一使用分页参数：
- `page`：页码，从 1 开始
- `size`：每页大小
- `sort`：排序字段，可选，默认 `-time`（倒序）

## 9. 时间与金额格式
- 时间：ISO8601 格式，包含时区，如 `2026-01-20T10:30:00+08:00`
- 金额：分（int64），字段以 `_fen` 结尾

## 10. 验收标准
- `docs/openapi.yaml` 可被 Prism 启动 Mock 服务
- 前端可在 Mock 模式下跑通核心功能
- 后端导出的 `/v3/api-docs` 与 `openapi.yaml` 字段口径一致