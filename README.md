# FanClub

多房间多主播 open-live 事件系统

## 技术栈

- **运行时**: Node.js 20.x LTS
- **包管理**: pnpm 9.x
- **后端**: NestJS 10.x + TypeScript 5.5+
- **前端**: Next.js 14.x (App Router) + React 18
- **数据库**: PostgreSQL 15/16 + Prisma 5.18+
- **代码规范**: ESLint + Prettier + Husky + Commitlint

## 快速开始

### 前置要求

- Node.js 20.x LTS
- pnpm 9.x
- Docker & Docker Compose（用于本地数据库）

### 安装依赖

```bash
pnpm install
```

### 启动数据库

```bash
pnpm db:up
```

### 运行数据库迁移

```bash
pnpm db:migrate
```

### 填充种子数据

```bash
pnpm db:seed
```

### 启动开发服务器

同时启动 API、Web 和 Worker：

```bash
pnpm dev
```

或者分别启动：

```bash
# 仅启动 API (http://localhost:3001)
pnpm dev:api

# 仅启动 Web (http://localhost:3000)
pnpm dev:web

# 仅启动 Worker（Replay 模式）
pnpm dev:worker -- --mode=replay --repeat=10
```

## 项目结构

```
fanclub/
  apps/
    api/                 # NestJS API 服务
    web/                 # Next.js Web 应用
    worker/              # 事件接入/入账 worker（Day2 起）
  packages/
    shared/              # 共享 types、zod schema、常量、api client
  prisma/
    schema.prisma        # Prisma 数据模型
    migrations/          # 数据库迁移文件
    seed.ts              # 种子数据
  infra/
    docker-compose.yml   # Docker Compose 配置
  scripts/               # 工具脚本
  .github/workflows/     # GitHub Actions CI
```

## 常用命令

### 开发

- `pnpm dev` - 同时启动 API、Web 和 Worker
- `pnpm dev:api` - 仅启动 API
- `pnpm dev:web` - 仅启动 Web
- `pnpm dev:worker` - 仅启动 Worker（支持 replay 模式）

### 数据库

- `pnpm db:up` - 启动 Docker 数据库服务
- `pnpm db:down` - 停止 Docker 数据库服务
- `pnpm db:migrate` - 运行数据库迁移
- `pnpm db:seed` - 填充种子数据

### 代码质量

- `pnpm lint` - 运行 ESLint
- `pnpm typecheck` - 运行 TypeScript 类型检查
- `pnpm build` - 构建所有项目

## 环境变量

复制 `.env.example` 为 `.env.local` 并配置：

```bash
cp .env.example .env.local
```

最小必需配置：

- `DATABASE_URL` - PostgreSQL 连接字符串
- `API_PORT` - API 服务端口（默认 3001）
- `NEXT_PUBLIC_API_BASE_URL` - API 基础 URL（前端使用）

## API 端点

### Health Check

- `GET /health` - 健康检查

### Debug

- `GET /debug/db` - 数据库连接状态和统计信息
- `GET /debug/rooms` - 获取所有启用的房间
- `GET /debug/events?roomId=<roomId>` - 获取最近 50 条原始事件和标准化事件
- `GET /debug/ledger?creatorId=<creatorId>` - 获取最近 50 条账本条目

## 开发规范

### 提交信息

使用 [Conventional Commits](https://www.conventionalcommits.org/) 格式：

- `feat:` - 新功能
- `fix:` - 修复 bug
- `docs:` - 文档更新
- `style:` - 代码格式（不影响功能）
- `refactor:` - 重构
- `perf:` - 性能优化
- `test:` - 测试
- `chore:` - 构建/工具链更新

### 分支策略

- `main` - 主分支（生产环境）
- `dev` - 开发分支
- `feat/*` - 功能分支
- `fix/*` - 修复分支

## 许可证

MIT
