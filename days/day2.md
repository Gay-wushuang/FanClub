# Day 2— 多房间 open-live 事件链路最小闭环

> 面向 Cursor/自动化执行：**按最短路径**把“多房间 → RawEvent → NormalizedEvent（幂等）→ LedgerBEntry（幂等）→ Debug 可视化”跑通。  
> 约束：只实现 1~2 类事件（建议 `GIFT` + `SUPERCHAT`），其他事件只写 RawEvent。

---

## 0. 你今天要交付的东西（DoD）

**到 EOD 你必须能证明：**

- [ ] `pnpm db:up` 之后 Postgres/Redis running
- [ ] `pnpm db:migrate` 成功（无 prompt 或一次输入 migration name 后完成）
- [ ] `apps/worker` 能并发跑至少 2 个房间（来自 DB `Room.isEnabled=true`）
- [ ] replay 事件 10 次：`RawEvent` 记录增长 10；`NormalizedEvent` 只增长 1；`LedgerBEntry` 只增长 1
- [ ] API Debug：能查到最近事件与流水；Web dashboard 能看到“数据在动”
- [ ] CI：`lint` / `typecheck` / `build` 绿（重点：CI 里有 `prisma generate`）

---

## 1. 最短执行顺序（强烈建议照着做）

### Step 1：确保本地可跑（10 分钟）

1. 启动 DB：

```bash
pnpm db:up
```

**成功输出长这样：**

- `fanclub-postgres Running`
- `fanclub-redis Running`

2. 确保 `apps/api/.env` 存在（不要提交）：
   `apps/api/.env`

```env
DATABASE_URL="postgresql://fanclub:fanclub@localhost:5432/fanclub?schema=public"
```

3. 迁移 & seed（首次需要）：

```bash
pnpm db:migrate
cd apps/api
npx prisma generate
npx prisma db seed
cd ../..
```

**成功输出长这样：**

- migrate：`Applied migrations` / `Database is in sync`
- seed：`🎉 种子数据完成！`（或你们 seed.ts 的成功日志）

4. 起 API/Web（为了后面可视化）：

```bash
pnpm dev
```

**成功输出长这样：**

- Next：`Ready in ...` + `http://localhost:3000`
- API：`API 服务运行在 http://localhost:3001`

---

### Step 2：先做 replay worker，闭环入库（60~120 分钟）

> 先别碰真实 open-live，先用 replay 事件把链路打通，最快看到 DB 在涨。

**要做的文件/新增的文件（按 repo 路径）：**

- ✅ 新增：`apps/worker/package.json`
- ✅ 新增：`apps/worker/tsconfig.json`
- ✅ 新增：`apps/worker/src/index.ts`
- ✅ 新增：`apps/worker/src/supervisor/RoomSupervisor.ts`
- ✅ 新增：`apps/worker/src/supervisor/RoomRunner.ts`
- ✅ 新增：`apps/worker/src/adapters/replayAdapter.ts`
- ✅ 新增：`apps/worker/src/pipeline/persistRawEvent.ts`
- ✅ 新增：`apps/worker/src/pipeline/normalize.ts`
- ✅ 新增：`apps/worker/src/pipeline/persistNormalizedEvent.ts`
- ✅ 新增：`apps/worker/src/pipeline/applyLedgerB.ts`
- ✅ 新增：`apps/worker/src/pipeline/types.ts`
- ✅ 新增：`apps/worker/src/utils/backoff.ts`
- ✅ 新增：`apps/worker/src/utils/logger.ts`
- ✅ 新增：`apps/worker/fixtures/gift.sample.json`
- ✅ 新增：`apps/worker/fixtures/superchat.sample.json`
- ✅ 修改（若缺）：`pnpm-workspace.yaml`（确保包含 `apps/worker`）
- ✅ 可选：根目录 `package.json` 增加脚本 `dev:worker`

**实现要点（Cursor 直接照做）：**

1. RoomSupervisor：

- 启动时 `SELECT Room WHERE isEnabled=true`
- 对每个 room 启动 RoomRunner（并发）
- 每 10 秒 refresh 一次列表（新增 room 自动接入）

2. RoomRunner（replay 模式）：

- 从 `fixtures/*.json` 读事件 JSON
- 每 500ms~1s “吐出”一条事件
- 每条事件调用 pipeline：`persistRawEvent → normalize → persistNormalized → applyLedgerB`

3. 幂等（Day2 必做）：

- `NormalizedEvent`：`@@unique([platform, idempotencyKey])`
- `LedgerBEntry`：`normalizedEventId @unique`
- pipeline 对 unique 冲突（P2002）视为 dedup，不报错退出

**本地启动 worker（replay）：**

```bash
pnpm --filter worker dev -- --mode=replay
```

**成功输出长这样：**

- `RoomSupervisor started`
- `room=<id> started runner`
- `raw saved`
- `normalized saved` / `dedup hit`
- `ledger applied` / `ledger dedup`
- API debug 中能看到记录增长

---

### Step 3：做 Debug 可视化（30~60 分钟）

> 目的：不看 DB 也能确认“在跑”。

**API（Nest）改动文件：**

- ✅ 新增或修改：`apps/api/src/debug/debug.controller.ts`
- ✅ 新增或修改：`apps/api/src/debug/debug.service.ts`
- ✅ 确认模块：`apps/api/src/debug/debug.module.ts` 已注册

**建议实现 3 个端点：**

- `GET /debug/rooms`
- `GET /debug/events?roomId=<roomId>`（最近 50 条 RawEvent + 最近 50 条 NormalizedEvent）
- `GET /debug/ledger?creatorId=<creatorId>`（最近 50 条 LedgerBEntry）

**成功输出长这样：**

- 浏览器访问 `http://localhost:3001/debug/rooms` 返回 JSON 数组
- 访问 `.../debug/events?roomId=...` 返回含 raw/normalized 列表
- 访问 `.../debug/ledger?creatorId=...` 返回流水列表

**Web（Next）改动文件：**

- ✅ 修改：`apps/web/src/app/dashboard/page.tsx`
- ✅ 修改：`apps/web/src/lib/api.ts`（新增对 debug API 的 fetch）

**成功输出长这样：**

- `http://localhost:3000/dashboard` 页面能看到“房间状态/最近事件/最近流水”列表滚动变化

---

### Step 4：把 CI 修绿（10 分钟）

> 关键：CI typecheck/build 前必须 `prisma generate`（CI 环境是干净的）。

**要修改的文件：**

- ✅ 修改：`.github/workflows/ci.yml`

在 `typecheck` job：`pnpm typecheck` 前加：

```yaml
- name: Prisma generate (api)
  run: pnpm --filter api exec prisma generate
```

在 `build` job：`pnpm build` 前加同样一步。

**成功输出长这样：**

- GitHub Actions：Lint ✅ Type Check ✅ Build ✅

---

## 2. 幂等键（idempotencyKey）— 直接用这套规则

优先级：

1. 有平台事件唯一 ID：
   `{platform}:{platformRoomId}:{eventType}:{eventId}`
2. 没有唯一 ID：
   `sha256(platform + room + type + uid + giftId + amount + count + ts)`
3. 最差：
   `sha256(platform + room + type + canonical_json(raw))`（json key 排序+稳定序列化）

**Day2 最低要求：** 只要 replay 时同一事件不会重复入账即可。

---

## 3. Pipeline 事务策略（按这个做，不纠结）

- RawEvent：每条都插入（不做去重）
- NormalizedEvent：用 unique 去重（P2002 => dedup）
- LedgerBEntry：用 normalizedEventId unique 去重（P2002 => dedup）
- Normalized + Ledger 用一个 `prisma.$transaction`（RawEvent 可不放事务里）

---

## 4. 快速自检命令（Cursor 自检用）

### DB & 服务

```bash
pnpm db:up
pnpm dev
```

### Prisma（CI/本地一致性）

```bash
pnpm --filter api exec prisma generate
pnpm typecheck
pnpm build
```

### replay 闭环验证（重复跑 10 次）

```bash
pnpm --filter worker dev -- --mode=replay --repeat=10
```

**预期：**

- RawEvent +10
- NormalizedEvent +1
- LedgerBEntry +1

> 如果你们没有做统计接口，至少用 `/debug/events` `/debug/ledger` 看条数变化。

---

## 5. 提交建议（最少 2 个 commit）

1. `feat: add worker replay pipeline`
2. `chore: update ci prisma generate`

> 你们有 commitlint，type 只能用 feat/fix/docs/style/refactor/perf/test/chore/revert。

---

## 6. 常见故障 & 立刻解决

### A) `Environment variable not found: DATABASE_URL`

- 原因：没创建 `apps/api/.env`
- 解：创建 `.env` 并填 DATABASE_URL

### B) CI 报 `PrismaClient` 不存在 / `this.prisma.creator` 不存在

- 原因：CI 没有 `prisma generate`
- 解：在 `.github/workflows/ci.yml` typecheck/build 前加 generate

### C) replay 重复入账

- 原因：没用 DB unique 兜底
- 解：补 `NormalizedEvent unique(platform,idempotencyKey)` + `LedgerBEntry unique(normalizedEventId)`，并 catch P2002 当 dedup

---

## 7. Day2 收工前 Checklist（最终验收）

- [ ] worker 并发跑 2 个 room
- [ ] replay 同一事件 10 次只入账 1 次
- [ ] `/debug/*` 能查到最新数据
- [ ] dashboard 能看到事件/流水在动
- [ ] CI 绿（至少 typecheck/build 绿）
