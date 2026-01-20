# Release Checklist（可复制执行）

> 适用：Java 17 + Spring Boot 3（Maven）+ PostgreSQL + Redis + Next.js（pnpm）
> 
> 建议流程：**dev 集成稳定 →（可选）release 分支冻结 → staging 验收 → 打 tag → 合并 main → 部署 prod**。

---

## 0) 版本命名规则

- Tag：`vX.Y.Z`（例：`v0.1.0`）
- Release 分支（可选）：`release/vX.Y.Z`

---

## 1) 发布前（本地/CI 必须全绿）

> 在仓库根目录执行。

### 1.1 确认工作区干净

```bash
git status
```

### 1.2 拉取最新 dev

```bash
git checkout dev
git pull --rebase origin dev
```

### 1.3 后端测试/构建

```bash
cd backend
./mvnw -q test
./mvnw -q -DskipTests package
cd ..
```

### 1.4 前端 Lint/构建

```bash
cd frontend
corepack enable
corepack prepare pnpm@9.0.0 --activate
pnpm install
pnpm -s lint
pnpm -s build
cd ..
```

---

## 2)（可选）冻结 release 分支

> 当你们进入验收周或需要“只修 bug 不加新功能”时使用。

```bash
VERSION=v0.1.0

git checkout dev
git pull --rebase origin dev

git checkout -b release/${VERSION}
git push -u origin release/${VERSION}
```

之后：所有修复走 `fix/*` 分支 → PR 合入 `release/${VERSION}`。

---

## 3) 部署到 staging（验收环境）

### 3.1 确认 staging 配置文件存在

- `backend/application-stg.yml`（或环境变量）
- `frontend/.env.stg`（或环境变量）
- `deploy/docker-compose.stg.yml`（若使用 Docker Compose 部署）

### 3.2 一键启动 staging（Docker Compose 方式）

```bash
# 进入部署目录（或直接在仓库根目录执行）
# 注意：staging 通常使用独立的 DB/Redis，不要复用本地 dev 的数据卷。

docker compose -f deploy/docker-compose.stg.yml up -d

docker compose -f deploy/docker-compose.stg.yml ps
```

### 3.3 健康检查（可复制）

```bash
# 后端健康检查（建议启用 Spring Actuator）
curl -fsS http://localhost:8080/actuator/health | cat

# 前端页面（返回 200 即可）
curl -I http://localhost:3000
```

> 如果你们不是本机部署，把 `localhost` 换成 staging 域名即可。

---

## 4) 验收通过后：打 tag + 合并 main

### 4.1 生成 tag

```bash
VERSION=v0.1.0

git checkout dev

git tag -a ${VERSION} -m "release: ${VERSION}"

git push origin ${VERSION}
```

### 4.2 合并到 main（必须 PR）

- 从 `dev`（或 `release/${VERSION}`）开 PR → `main`
- PR 必须附：
  - staging 验收证据（录屏/截图）
  - 回归清单（见下文 5）
  - 回滚方案（见下文 6）

---

## 5) 回归清单（建议 10 分钟跑完）

> 只要这些过了，基本不会“上线就炸”。

- 登录 / 刷新 token 正常
- 主播端：
  - 场次列表可打开
  - 单场 summary 可展示
  - 事件明细分页可翻页
- 经纪人端：
  - 日期筛选可用
  - 无权限态/脱敏展示正确
- 插件/Overlay（如有）：
  - token 失效提示正确
  - 断流显示“最后更新时间”
- 监控：
  - 后端错误率/日志能检索
  - DB/Redis 连接正常

---

## 6) 回滚方案（必须写在 PR/发布单里）

### 6.1 代码回滚（Git 层）

```bash
# 回到上一个稳定 tag（示例）
git checkout v0.0.9
```

### 6.2 Docker Compose 回滚（推荐）

> 前提：你们的镜像使用“版本 tag”（例如 `fanclub-backend:v0.1.0`），而不是 `latest`。

```bash
# 将 deploy/docker-compose.prod.yml 里的镜像 tag 改回上一个稳定版本
# 然后重启

docker compose -f deploy/docker-compose.prod.yml up -d
```

### 6.3 数据库回滚（原则）

- **发布前必须备份**（至少 staging/prod）
- schema 变更尽量“向前兼容”（先加字段/表，再灰度切流，再删旧字段）
- 如必须回滚 schema：要有对应的回滚脚本或可恢复备份

