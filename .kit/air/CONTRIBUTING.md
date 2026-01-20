# FanClubMaker 贡献指南（工程化协作规范）

> 适用团队规模：5 人（2 后端 Java + 3 前端）。
> 目标：**接口契约先行、可验收里程碑推进、CI 质量门禁、主干稳定可发布**。

---

## 0. 可复制执行的工程约定（目录 + 版本）

本文档默认技术栈：**Java 17 + Spring Boot 3（Maven）+ PostgreSQL + Redis + Next.js（pnpm）**。

### 0.1 仓库目录约定（推荐）

> 建议统一到如下目录结构（下面所有命令均基于此结构，可直接复制）。

```
repo/
  backend/                  # Spring Boot 3（Maven）
    mvnw  mvnw.cmd          # ✅ 必须提交（Maven Wrapper）
    pom.xml
    src/
    .env.example
  frontend/                 # Next.js
    package.json
    pnpm-lock.yaml
    next.config.*
    .env.example
  deploy/
    docker-compose.dev.yml  # 本地开发依赖（Postgres/Redis）
    docker-compose.stg.yml  # 预发（可选）
  docs/
    api-contract.md
  .github/
  CONTRIBUTING.md
  RELEASE_CHECKLIST.md
```

### 0.2 必备版本（写死，避免“我电脑能跑你电脑不能跑”）

- Java：**17**（建议 Temurin/Corretto 任一）
- Maven：使用仓库内的 `mvnw`（避免本机 Maven 版本差异）
- Node.js：**20 LTS**（建议用 nvm/fnm 管理）
- pnpm：**9**（建议用 corepack 管理）
- Docker Desktop：用于启动 Postgres/Redis

---

## 0.3 一键启动（新同事/新机器直接复制）

> 以下命令默认你在仓库根目录（`repo/`）。

### 0.3.1 启动依赖（Postgres + Redis）

```bash
docker compose -f deploy/docker-compose.dev.yml up -d
```

### 0.3.2 启动后端（Spring Boot）

```bash
cd backend
cp .env.example .env  # Windows: copy .env.example .env
./mvnw -q -DskipTests=true spring-boot:run
```

### 0.3.3 启动前端（Next.js）

```bash
cd ../frontend
corepack enable
corepack prepare pnpm@9.0.0 --activate
pnpm install
cp .env.example .env  # Windows: copy .env.example .env
pnpm dev
```

---

## 1. 分支策略（必须遵守）

### 分支说明
- `main`：发布分支（稳定、可回滚）。**受保护**。
- `dev`：集成分支（每天集成、持续可演示）。
- 功能分支：从 `dev` 拉出，完成后 PR 回 `dev`。

### 分支命名规范
- 功能：`feat/<scope>-<short-desc>`
  - 例：`feat/auth-rbac`、`feat/live-session-api`、`feat/dashboard-streamer`、`feat/obs-overlay`
- 修复：`fix/<scope>-<short-desc>`
  - 例：`fix/pagination-bug`
- 文档：`docs/<short-desc>`
- 重构：`refactor/<short-desc>`

### 合并规则
- **禁止**直接 push 到 `main`。
- `dev -> main` 仅在里程碑（M1/M2/M3）验收通过后合并。
- 所有合并必须走 PR，并满足：
  1) 关联 Issue
  2) CI 全绿
  3) 至少 1 名 Reviewer 通过

### 常用 Git 命令（可直接复制）

> 默认你已拉取仓库并在仓库根目录。

```bash
# 保持本地 dev 最新
git checkout dev
git pull --rebase origin dev

# 从 dev 创建功能分支
git checkout -b feat/dashboard-streamer

# 开发提交（示例）
git add .
git commit -m "feat(dashboard): add streamer session summary"

# 推送分支
git push -u origin feat/dashboard-streamer
```

---

## 2. 任务管理与 Owner 责任制

### Owner 划分（建议）
- **BE Owner A（平台基础）**：工程骨架、RBAC、审计、导出、统一规范
- **BE Owner B（数据链路）**：事件接入、聚合口径、明细查询、实时推送
- **FE Owner C（主播侧 + 小窗）**：主播面板、桌面小窗 UI、组件与图表
- **FE Owner D（经纪人侧）**：经纪人面板、权限态、筛选对比、导出入口
- **Platform/Delivery Owner E（交付与联调）**：OpenAPI 契约、Mock、CI、验收清单、OBS Browser Source

### Owner 的交付责任
Owner 对其模块的以下事项负责：
- 接口/数据口径对齐
- 单测与边界条件
- 文档更新（README/接口说明/验收口径）
- 联调与验收 demo 可用

---

## 3. 接口契约（OpenAPI）先行

### 规则
- **先定契约再写业务**：字段名、单位、时间口径、分页规范、错误码。
- 前端允许使用 Mock Server 开发；后端先返回 mock 数据也可，但**结构必须与契约一致**。
- 契约变更：必须发 PR，更新 `docs/api-contract.md` 与 OpenAPI 描述，并 @相关 Owner。

### 统一协议（建议）
- 时间：ISO8601、明确时区（默认 UTC+8）
- 金额：明确单位（分/厘/元），建议使用 `long` 表示最小货币单位
- 分页：`page`（从 1 开始）、`size`、`sort=field,asc|desc`
- 过滤：`startTime` / `endTime`（闭区间/开区间在文档写清）

---

## 4. 代码规范与质量门禁（CI）

### 后端（Java）
- 统一格式化：Spotless / Checkstyle（二选一也行，但必须统一）
- 单测：核心 service 必须有（聚合口径/权限判断/幂等等）
- 构建：必须使用 Maven Wrapper：`cd backend && ./mvnw -q test`

#### 后端命令（可复制）

```bash
# 运行单测 + 编译
cd backend
./mvnw -q test

# 仅编译（不跑单测）
./mvnw -q -DskipTests package

# （如已接入 Spotless）自动格式化
./mvnw -q spotless:apply

# 运行本地服务（不跑单测）
./mvnw -q -DskipTests=true spring-boot:run
```

### 前端（Next.js）
- 统一格式化：Prettier
- Lint：ESLint
- 构建：`cd frontend && pnpm -s lint && pnpm -s build`

#### 前端命令（可复制）

```bash
cd frontend
corepack enable
corepack prepare pnpm@9.0.0 --activate
pnpm install

# Lint + Build
pnpm -s lint
pnpm -s build

# 本地开发
pnpm dev
```

### PR 必须满足
- CI 全绿
- 无大段未使用代码
- 不引入敏感信息（token/密钥/个人信息）

---

## 5. 提交信息规范（Conventional Commits）

格式：`type(scope): subject`
- `feat:` 新功能
- `fix:` 修复
- `docs:` 文档
- `refactor:` 重构
- `chore:` 工程杂项

例：
- `feat(dashboard): add session summary cards`
- `fix(auth): prevent token refresh loop`

---

## 6. 环境与配置

### 6.1 本地开发（可复制）

#### 6.1.1 启动/停止依赖

```bash
# 启动 Postgres + Redis
docker compose -f deploy/docker-compose.dev.yml up -d

# 查看状态
docker compose -f deploy/docker-compose.dev.yml ps

# 停止（保留数据卷）
docker compose -f deploy/docker-compose.dev.yml down

# 停止并清空数据（⚠️ 会删库）
docker compose -f deploy/docker-compose.dev.yml down -v
```

#### 6.1.2 环境变量（必须复制示例文件）

```bash
# 后端
cd backend
cp .env.example .env

# 前端
cd ../frontend
cp .env.example .env
```

> Windows PowerShell 等价命令：

```powershell
copy backend\.env.example backend\.env
copy frontend\.env.example frontend\.env
```

#### 6.1.3 推荐端口（如需改动请在 .env 里改）

- Postgres：`5432`
- Redis：`6379`
- 后端 API：`8080`
- 前端 Web：`3000`

### 6.2 多环境配置（dev / stg / prod）

- 后端：使用 Spring Profile（推荐 `application-dev.yml / application-stg.yml / application-prod.yml`），敏感项走环境变量。
- 前端：使用 `.env`（本地）+ 部署平台环境变量（stg/prod）。

### 6.3 禁止事项（CI 会检查）

- 禁止提交：`.env`、任何密钥/token、生产域名私钥等。
- 必须提供：`.env.example`（包含所有必填项的占位符）。

---

## 7. 联调与演示标准

### 每天必须有一个“可演示”的 dev 版本
- 面板至少能打开
- 接口至少能返回（允许 mock）
- 关键路径有降级提示（断线/延迟/无权限）

### 里程碑合入 main 的标准
- 录屏 + 验收 checklist 全部通过
- 部署说明可复现
- 数据口径说明定版
