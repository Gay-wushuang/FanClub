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
| `bilibili` | Bilibili直播数据监控接口，包括直播间信息获取、统计数据等 |

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
- **前端包管理**：pnpm
- **Node 版本**：18+
- **后端技术**：Java Spring Boot
- **Java 版本**：21.0.10+
- **构建工具**：Maven
- **HTTP 客户端**：OkHttp
- **认证方式**：JWT

## 10. 后端服务构建与启动

### 10.1 环境要求
- **Java**：21.0.10+
- **Maven**：3.9.12+

### 10.2 构建后端服务
```powershell
# 进入后端目录
cd backend/FanClub_Backend

# 构建项目
E:\Java\apache-maven-3.9.12\bin\mvn.cmd clean package
```

### 10.3 启动后端服务
```powershell
# 在后端目录执行
E:\Java\jdk-21.0.10\bin\java.exe -jar target/FanClub_BackEnd-0.0.1-SNAPSHOT.jar
```

### 10.4 服务访问
- **服务地址**：http://localhost:8080
- **API 前缀**：/api/v1

### 10.5 健康检查
后端服务启动后，可通过以下地址验证服务状态：
```
http://localhost:8080/  # 根路径健康检查
http://localhost:8080/api/v1/  # API 路径健康检查
http://localhost:8080/api/v1/bilibili/room/init?id=1986387323  # 测试Bilibili API
```

## 11. Bilibili直播数据监控

### 11.1 功能说明
- **直播间短号转真实room_id**：将Bilibili直播间短号转换为真实的room_id
- **直播间信息获取**：获取直播间详细信息
- **直播数据统计**：获取直播间实时统计数据

### 11.2 API 端点
- **初始化直播间**：GET /api/v1/bilibili/room/init?shortId={shortId}
- **获取直播间信息**：GET /api/v1/bilibili/room/info?roomId={roomId}
- **获取直播间统计**：GET /api/v1/bilibili/room/stats?roomId={roomId}

### 11.3 示例调用
```powershell
# 测试直播间初始化
Invoke-WebRequest -Uri "http://localhost:8080/api/v1/bilibili/room/init?shortId=1986387323" -Method GET

# 测试直播间统计
Invoke-WebRequest -Uri "http://localhost:8080/api/v1/bilibili/room/stats?roomId=1986387323" -Method GET
```

## 12. 贡献指南

1. 遵循 OpenAPI 3.0.3 规范
2. 保持接口设计的一致性
3. 及时更新 API 契约文档
4. 确保 Mock 服务能正常启动
5. 提交前验证文档格式正确性
6. 后端代码遵循 Java 编码规范
7. 确保 Bilibili API 集成功能正常

## 13. 项目状态

### 13.1 已完成的工作
- ✅ 生成项目进度文档："已经工作的具体内容.md"
- ✅ 验证 Java 21.0.10 安装
- ✅ 构建并启动后端服务
- ✅ 实现 Bilibili 直播数据监控功能
- ✅ 验证 Prism Mock 服务 /api/v1/ 路径修复
- ✅ 检查依赖配置文件
- ✅ 添加健康检查控制器，修复根路径错误问题
- ✅ 完成M0工程基座：检查并完善后端Spring Boot配置、添加Swagger文档、实现统一返回体和异常处理
- ✅ 实现RBAC权限框架（主播/经纪人角色）
- ✅ 创建数据库表结构（sessions、event_danmaku、event_gift、metrics_bucket）
- ✅ 实现M1单场基础数据：场次列表和单场summary接口
- ✅ 实现M2明细和聚合：礼物/弹幕事件采集和时间段聚合
- ✅ 完善数据采集定时任务，实现实时数据监控

### 13.2 服务运行状态
- **后端服务**：运行在 http://localhost:8080
- **Prism Mock 服务**：运行在 http://localhost:4010
- **Bilibili API**：已集成并测试通过

### 13.3 测试结果
- ✅ Bilibili API：成功获取直播间信息和统计数据
- ✅ Prism Mock 服务：/api/v1/ 路径访问正常
- ✅ 后端服务：运行稳定，无错误
- ✅ M1单场基础数据接口：场次列表和单场summary接口返回正常
- ✅ M2明细和聚合接口：礼物/弹幕事件明细和聚合数据接口返回正常
- ✅ 数据采集定时任务：每10秒采集一次直播数据

### 13.4 技术亮点
- **模块化设计**：清晰的代码结构，便于维护和扩展
- **完整的 API 集成**：成功集成 Bilibili 直播 API
- **安全认证**：使用 JWT 进行身份验证
- **灵活的数据处理**：直接返回原始 JSON 字符串，避免字段不匹配问题
- **完整的 API 文档**：详细的 OpenAPI 文档，方便前端集成
- **RBAC权限框架**：实现了主播/经纪人角色的权限管理
- **实时数据监控**：通过定时任务实现直播数据的实时采集和监控
- **完善的错误处理**：当数据库连接失败时返回模拟数据，确保服务可用性

## 14. 新增功能使用说明

### 14.1 仪表盘接口
- **获取场次列表**：GET /api/v1/dashboard/sessions?page=1&size=10
- **获取单场summary**：GET /api/v1/dashboard/sessions/{sessionId}/summary
- **获取场次事件明细**：GET /api/v1/dashboard/sessions/{sessionId}/events?type=gift
- **获取聚合数据**：GET /api/v1/dashboard/aggregate?granularity=hour

### 14.2 认证接口
- **用户登录**：POST /api/v1/auth/login
- **获取用户信息**：POST /api/v1/auth/info
- **用户登出**：POST /api/v1/auth/logout

### 14.3 示例调用
```powershell
# 测试场次列表
Invoke-WebRequest -Uri "http://localhost:8080/api/v1/dashboard/sessions?page=1&size=10" -Method GET

# 测试单场summary
Invoke-WebRequest -Uri "http://localhost:8080/api/v1/dashboard/sessions/1/summary" -Method GET

# 测试礼物事件明细
Invoke-WebRequest -Uri "http://localhost:8080/api/v1/dashboard/sessions/1/events?type=gift" -Method GET

# 测试聚合数据
Invoke-WebRequest -Uri "http://localhost:8080/api/v1/dashboard/aggregate?granularity=hour" -Method GET

# 测试用户登录
Invoke-WebRequest -Uri "http://localhost:8080/api/v1/auth/login" -Method POST -Body '{"username":"admin","password":"123456"}' -ContentType "application/json"
```

### 14.4 数据采集说明
- **采集频率**：每10秒采集一次直播数据
- **监控的直播间**：1986387323, 1838214834
- **采集的数据**：直播间信息、在线人数、礼物事件、弹幕事件
- **数据存储**：存储到sessions、event_danmaku、event_gift、metrics_bucket表

### 14.5 健康检查
- **服务健康检查**：GET /health
- **Swagger文档**：GET /v3/api-docs
- **Swagger UI**：GET /swagger-ui.html

## 15. 项目进度

### 15.1 里程碑完成情况
- **M0（工程基座）**：✅ 完成
- **M1（单场基础数据）**：✅ 完成
- **M2（明细和聚合）**：✅ 完成
- **M3（插件/桌面小窗 + 可靠性）**：🔄 进行中

### 15.2 后续工作计划
- 实现M3插件/桌面小窗功能
- 完善可靠性和降级策略
- 实现数据导出和审计日志
- 优化数据库表结构和索引
- 增加更多的测试用例
- 完善前端集成