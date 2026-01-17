# FanClub Day 0 ~ Day 1（开工落地版）

> 目标：**24~36 小时内把工程底座 + 本地可运行环境 + Prisma 数据结构骨架**搭好；所有人可以并行开发且不互相阻塞。  
> 技术栈：**Node.js + TypeScript + NestJS + Prisma + Postgres + Next.js**（多房间多主播 open-live 事件后续接入）

---

## 0. 版本与工具锁定（必须在 Day0 完成）

### 0.1 运行时与包管理

- Node.js：`20.x LTS`
- 包管理：`pnpm 9.x`
- TypeScript：`5.5+`
- Prisma：`5.18+`
- NestJS：`10.x`
- Next.js：`14.x`（App Router）
- 数据库：Postgres `15/16`
- Redis：`7.x`（Day0-1 可以不启用）
- 推荐：`volta`（在 `package.json` 写 engines / volta 字段）

### 0.2 代码规范

- ESLint + Prettier
- lint-staged + husky
- commitlint（conventional commits）
- EditorConfig
- 统一 import 路径（TS path alias）：`@/` 指向当前 app src，`@shared/` 指向 packages/shared

### 0.3 分支与发布策略（极简）

- 主分支：`main`
- 开发分支：`dev`
- 功能分支：`feat/*`、`fix/*`
- PR 必过：`lint` + `typecheck` + `unit test（如有）` + `build`

---

## 1. 仓库结构（推荐最省事的 monorepo）

> 目标：一个 repo，前后端共享 types/schema/client。

```
fanclub/
  apps/
    api/                 # NestJS
    web/                 # Next.js
    worker/              # 事件接入/入账 worker（Day2 起）
  packages/
    shared/              # 共享 types、zod schema、常量、api client
  prisma/
    schema.prisma
    migrations/
    seed.ts
  infra/
    docker-compose.yml
  scripts/
  .github/workflows/
  package.json
  pnpm-workspace.yaml
  tsconfig.base.json
  .env.example
```

### 1.1 pnpm workspace

- `pnpm-workspace.yaml`：
  - `apps/*`
  - `packages/*`

### 1.2 API & Web 的启动命令约定

- `pnpm dev`：同时启动 `api` & `web`（用 `concurrently`/`turbo`）
- `pnpm dev:api`、`pnpm dev:web`
- `pnpm lint`、`pnpm typecheck`、`pnpm build`

---

## 2. 本地运行环境（Day0 必须跑通）

### 2.1 docker-compose（Postgres 必选）

`infra/docker-compose.yml`（示例）

- postgres:
  - 端口：`5432`
  - 数据库：`fanclub`
  - 用户：`fanclub`
  - 密码：`fanclub`

（可选）redis:

- 端口：`6379`

### 2.2 环境变量（统一在 repo 根）

- `.env.example`（必须提交，模板文件）
- 本地复制到各项目目录（不提交）
  - `apps/api/.env`（API 服务使用）
  - `apps/web/.env`（Web 应用使用，可选）

最小必需项：

- `DATABASE_URL=postgresql://fanclub:fanclub@localhost:5432/fanclub?schema=public`
- `NEXT_PUBLIC_API_BASE_URL=http://localhost:3001`（示例）
- `API_PORT=3001`
- `JWT_SECRET=...`（若 Day1 做登录）

---

## 3. Day0 ~ Day1 分工（3 人并行，不互卡）

> 原则：**A 负责 DB/Prisma & 后端骨架**；**B 负责工程/CI/规范**；**C 负责前端骨架 & API client**。  
> Day1 结束时：前后端都能跑、能连库、能跑 migration、能看到一个“登录后展示余额/占位数据”的页面。

### Dev A（后端/DB）——“让数据结构先落地”

- [ ] 初始化 Prisma：`prisma/schema.prisma` + 第一版迁移
- [ ] 建核心 enum & 模型（见第 4 节）
- [ ] 写 seed：创建 1 个 creator、2 个 room（不同 platformRoomId）、1 个 fanclub
- [ ] API 项目骨架：NestJS app 起服 + health check
- [ ] 最小查询接口（可用 mock / hardcode）：`GET /health`, `GET /debug/db`

### Dev B（工程/CI/规范）——“让团队快起来”

- [ ] 建 monorepo + pnpm workspace
- [ ] ESLint/Prettier/husky/lint-staged/commitlint
- [ ] tsconfig.base + path alias
- [ ] GitHub Actions（或你们 CI）：lint/typecheck/build
- [ ] docker-compose + README（如何启动）
- [ ] 一键脚本：`pnpm db:up`, `pnpm db:migrate`, `pnpm db:seed`

### Dev C（前端/体验）——“让可见结果尽早出现”

- [ ] Next.js app 起服（App Router）
- [ ] 最小页面：
  - `/`：Landing + 登录按钮（可先 fake）
  - `/dashboard`：展示 fanclub/room 列表（先读 mock 或调用 api）
- [ ] API client（fetch 封装）+ types 直接引用 `packages/shared`
- [ ] 最小 UI 组件（不纠结样式）

---

## 4. Day0~Day1 数据结构类型（Prisma + TS）

> Day0~1 只建**骨架**，不要过度设计；但 **幂等与追溯字段必须先预留**。  
> 多房间多主播的关键：`Room(platform, platformRoomId)` 唯一；事件链路后续用 `RawEvent -> NormalizedEvent -> LedgerBEntry`。

### 4.1 Prisma：核心 Enum（最小集）

```prisma
enum Platform {
  BILIBILI
}

enum EventType {
  GIFT
  SUPERCHAT
  GUARD
  ENTER
  DANMU
}
```

### 4.2 Prisma：Day0~1 先落的最小模型（骨架版）

> 说明：Day0~1 不必全部字段齐全，但以下这些建议一次性写好，避免 Day2 返工。

```prisma
model Creator {
  id        String   @id @default(cuid())
  name      String?
  createdAt DateTime @default(now())
  updatedAt DateTime @updatedAt
  rooms     Room[]
  fanclubs  Fanclub[]
}

model Room {
  id             String   @id @default(cuid())
  platform       Platform
  platformRoomId String
  creatorId      String
  isEnabled      Boolean  @default(true)
  createdAt      DateTime @default(now())
  updatedAt      DateTime @updatedAt

  creator        Creator  @relation(fields: [creatorId], references: [id])

  @@unique([platform, platformRoomId])
  @@index([creatorId])
}

model Fanclub {
  id        String   @id @default(cuid())
  creatorId String
  name      String
  createdAt DateTime @default(now())
  updatedAt DateTime @updatedAt

  creator   Creator  @relation(fields: [creatorId], references: [id])
  @@index([creatorId])
}

/**
 * Day0~1：先把 RawEvent/NormalizedEvent/账本表“定义出来”，
 * 但 Day0~1 可以先不写业务逻辑（逻辑从 Day2 开始）
 */
model RawEvent {
  id         String   @id @default(cuid())
  platform   Platform
  roomId     String
  receivedAt DateTime @default(now())
  payload    Json
  traceId    String   @default(cuid())

  room       Room     @relation(fields: [roomId], references: [id])

  @@index([platform, roomId, receivedAt])
}

model NormalizedEvent {
  id             String   @id @default(cuid())
  platform       Platform
  roomId         String
  eventType      EventType
  occurredAt     DateTime
  receivedAt     DateTime @default(now())

  idempotencyKey String
  platformEventId String?

  actorUid       String?
  giftId         String?
  giftCount      Int?
  amount         Decimal? @db.Numeric(18,6)

  rawEventId     String?
  rawEvent       RawEvent? @relation(fields: [rawEventId], references: [id])
  room           Room      @relation(fields: [roomId], references: [id])

  @@unique([platform, idempotencyKey]) // 幂等必须 Day0~1 就加
  @@index([roomId, occurredAt])
}

model LedgerBEntry {
  id                String   @id @default(cuid())
  roomId            String
  creatorId         String
  fanUid            String?
  delta             Int
  reason            String
  normalizedEventId String   @unique
  occurredAt        DateTime
  createdAt         DateTime @default(now())

  room              Room      @relation(fields: [roomId], references: [id])
  creator           Creator   @relation(fields: [creatorId], references: [id])
  normalizedEvent   NormalizedEvent @relation(fields: [normalizedEventId], references: [id])

  @@index([creatorId, occurredAt])
  @@index([roomId, occurredAt])
  @@index([fanUid, occurredAt])
}
```

### 4.3 TS：共享事件类型（packages/shared）

> 目的：前后端、worker、API 全部共享结构。Day0~1 先定义，Day2 再实现处理。

```ts
// packages/shared/src/events.ts
export type Platform = 'BILIBILI';
export type EventType = 'GIFT' | 'SUPERCHAT' | 'GUARD' | 'ENTER' | 'DANMU';

export type RawEventEnvelope = {
  platform: Platform;
  roomId: string; // internal Room.id
  receivedAt: string; // ISO
  payload: unknown; // 原始 JSON
  traceId: string;
};

export type NormalizedEvent = {
  platform: Platform;
  roomId: string;
  eventType: EventType;
  occurredAt: string; // ISO
  idempotencyKey: string; // 唯一幂等键（Day2 计算）
  platformEventId?: string;

  actorUid?: string;
  giftId?: string;
  giftCount?: number;
  amount?: string; // Decimal as string
};
```

---

## 5. Day0~Day1 交付验收（Checklist）

### 必须完成

- [ ] `pnpm i` 一次成功
- [ ] `docker compose up -d` 启 Postgres 成功
- [ ] `pnpm db:migrate` 成功（Prisma migrate）
- [ ] `pnpm db:seed` 成功（插入 creator/room/fanclub）
- [ ] `pnpm dev` 同时启动 api/web
- [ ] `GET /health` 返回 ok
- [ ] 前端 `/dashboard` 能看到“房间列表/club 占位数据”（mock 或调用 api）

### 可选加分（不影响 Day2 开工）

- [ ] GitHub Actions 通过（lint/typecheck/build）
- [ ] 生成 Prisma Client 类型共享无报错
- [ ] 统一 logger（pino/winston 随意）

---

## 6. Day2 前的“别做”清单（为了极致效率）

- 不上消息队列、不拆微服务
- 不做复杂 RBAC（先能登录即可）
- 不做全量后台（先有读页面）
- 不做全量事件类型（只定义骨架）

---

## 7. 快速命令参考（建议写入根 package.json scripts）

- `pnpm dev`
- `pnpm dev:api`
- `pnpm dev:web`
- `pnpm db:up`
- `pnpm db:down`
- `pnpm db:migrate`
- `pnpm db:seed`
- `pnpm lint`
- `pnpm typecheck`
- `pnpm build`
