~~~md
# FanClubMaker MySQL 建库 + 查询SQL模板（给 Trae CN 直接照着实现）
> 技术栈：Java Spring Boot + MyBatis + MySQL 8.x  
> 目标：先跑通「回放/导入 → MySQL 落库 → API 查询」闭环；后续接入 open-live WS 事件时 **不改表结构**。

---

## 0）约定与原则（必须遵守）
### 字符集与引擎
- 全库/全表：`utf8mb4` + `InnoDB`

### 时间与金额
- 时间：`DATETIME(3)`（毫秒）
- 金额：统一用 `*_fen`（分）`BIGINT`，避免浮点误差

### 幂等（非常关键）
- 事件表必须做幂等去重：唯一键 `UNIQUE(session_id, msg_id)`
- 导入/实时写入一旦重复到达，不能重复入库导致统计翻倍

### 排序安全（避免 SQL 注入）
- `sort` 参数必须做白名单映射（只能按预定义字段排序）
- MyBatis 不允许将 sort 直接 `${sort}` 拼接（除非经过严格白名单转换）

---

## 1）数据库创建（如需）
```sql
CREATE DATABASE IF NOT EXISTS fanclub
  DEFAULT CHARACTER SET utf8mb4
  DEFAULT COLLATE utf8mb4_0900_ai_ci;

USE fanclub;
~~~

------

## 2）建表 DDL（MVP：4 张表）

### 2.1 sessions（直播场次）

用途：支撑 `GET /sessions` 列表、场次基本信息、快速汇总字段
主键：`session_id`（你们生成：雪花/序列/自增均可）

```sql
CREATE TABLE `sessions` (
  `session_id`      BIGINT       NOT NULL COMMENT '平台内场次ID（你们生成）',
  `room_id`         BIGINT       NOT NULL COMMENT 'B站直播间room_id',
  `anchor_id`       BIGINT       NOT NULL COMMENT '主播/虚拟主播ID（你们系统）',
  `platform_uid`    BIGINT       NULL     COMMENT 'B站主播UID（可选）',

  `title`           VARCHAR(255) NOT NULL COMMENT '场次标题',
  `category`        VARCHAR(64)  NOT NULL COMMENT '分区/分类',
  `status`          VARCHAR(16)  NOT NULL COMMENT 'LIVE/ENDED',
  `start_time`      DATETIME(3)  NOT NULL COMMENT '开播时间',
  `end_time`        DATETIME(3)  NULL     COMMENT '下播时间（可空）',

  `cover_url`       VARCHAR(512) NULL COMMENT '封面（可选）',
  `tags_json`       JSON         NULL COMMENT '标签（可选）',

  `danmaku_count`   BIGINT       NOT NULL DEFAULT 0 COMMENT '弹幕总数（可更新）',
  `gift_count`      BIGINT       NOT NULL DEFAULT 0 COMMENT '礼物总数（可更新）',
  `revenue_fen`     BIGINT       NOT NULL DEFAULT 0 COMMENT '收入(分)（可更新）',
  `peak_online`     BIGINT       NULL COMMENT '峰值在线（上游可能不给，允许为空）',
  `data_delay_sec`  INT          NULL COMMENT '数据延迟秒（可空）',

  `created_at`      DATETIME(3)  NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
  `updated_at`      DATETIME(3)  NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3),

  PRIMARY KEY (`session_id`),
  KEY `idx_room_time` (`room_id`, `start_time`),
  KEY `idx_anchor_time` (`anchor_id`, `start_time`),
  KEY `idx_status_time` (`status`, `start_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
```

------

### 2.2 event_danmaku（弹幕事件）

用途：支撑 `GET /sessions/{sessionId}/events/danmaku` 分页查询
幂等：`UNIQUE(session_id, msg_id)`

```sql
CREATE TABLE `event_danmaku` (
  `id`            BIGINT       NOT NULL AUTO_INCREMENT,
  `session_id`    BIGINT       NOT NULL,
  `room_id`       BIGINT       NOT NULL,

  `msg_id`        VARCHAR(64)  NOT NULL COMMENT '上游消息ID/去重ID（必须）',
  `event_time`    DATETIME(3)  NOT NULL COMMENT '事件发生时间',

  `user_id`       BIGINT       NULL COMMENT '用户ID（可选/可脱敏）',
  `user_name`     VARCHAR(64)  NULL COMMENT '用户昵称（可选/可脱敏）',

  `content`       VARCHAR(500) NOT NULL COMMENT '弹幕内容',
  `type`          VARCHAR(16)  NOT NULL COMMENT 'NORMAL/SC',
  `sc_value_fen`  BIGINT       NULL COMMENT '若是SC则可能有金额(分)',

  `raw_json`      JSON         NULL COMMENT '原始包（建议保留）',
  `created_at`    DATETIME(3)  NOT NULL DEFAULT CURRENT_TIMESTAMP(3),

  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_session_msg` (`session_id`, `msg_id`),
  KEY `idx_session_time` (`session_id`, `event_time`),
  KEY `idx_room_time` (`room_id`, `event_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
```

------

### 2.3 event_gift（礼物事件）

用途：支撑 `GET /sessions/{sessionId}/events/gifts` 分页查询
幂等：`UNIQUE(session_id, msg_id)`

```sql
CREATE TABLE `event_gift` (
  `id`             BIGINT       NOT NULL AUTO_INCREMENT,
  `session_id`     BIGINT       NOT NULL,
  `room_id`        BIGINT       NOT NULL,

  `msg_id`         VARCHAR(64)  NOT NULL COMMENT '上游消息ID/去重ID（必须）',
  `event_time`     DATETIME(3)  NOT NULL COMMENT '事件发生时间',

  `user_id`        BIGINT       NULL COMMENT '用户ID（可选/可脱敏）',
  `user_name`      VARCHAR(64)  NULL COMMENT '用户昵称（可选/可脱敏）',

  `gift_id`        BIGINT       NOT NULL COMMENT '礼物ID',
  `gift_name`      VARCHAR(64)  NOT NULL COMMENT '礼物名称',
  `gift_count`     INT          NOT NULL COMMENT '数量',
  `gift_value_fen` BIGINT       NOT NULL COMMENT '该次事件总价值(分)',

  `raw_json`       JSON         NULL COMMENT '原始包（建议保留）',
  `created_at`     DATETIME(3)  NOT NULL DEFAULT CURRENT_TIMESTAMP(3),

  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_session_msg` (`session_id`, `msg_id`),
  KEY `idx_session_time` (`session_id`, `event_time`),
  KEY `idx_room_time` (`room_id`, `event_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
```

------

### 2.4 metrics_bucket（趋势聚合桶）

用途：支撑 `GET /sessions/{sessionId}/metrics/range`
设计：`(session_id, granularity, bucket_time)` 唯一键（Upsert 叠加聚合）

```sql
CREATE TABLE `metrics_bucket` (
  `id`            BIGINT      NOT NULL AUTO_INCREMENT,
  `session_id`    BIGINT      NOT NULL,
  `bucket_time`   DATETIME(3) NOT NULL COMMENT '桶起始时间',
  `granularity`   VARCHAR(8)  NOT NULL COMMENT '10s/1m/5m',

  `danmaku_count` BIGINT      NOT NULL DEFAULT 0,
  `gift_count`    BIGINT      NOT NULL DEFAULT 0,
  `revenue_fen`   BIGINT      NOT NULL DEFAULT 0,
  `online`        BIGINT      NULL COMMENT '在线数（上游可能不给，允许为空）',

  `created_at`    DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),

  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_session_bucket` (`session_id`, `granularity`, `bucket_time`),
  KEY `idx_session_time` (`session_id`, `bucket_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
```

------

## 3）导入/实时写入 SQL（幂等 + 汇总更新）

### 3.1 插入 sessions（存在则更新更新时间/状态等）

```sql
INSERT INTO sessions (
  session_id, room_id, anchor_id, platform_uid,
  title, category, status, start_time, end_time,
  cover_url, tags_json
) VALUES (
  ?, ?, ?, ?,
  ?, ?, ?, ?, ?,
  ?, ?
)
ON DUPLICATE KEY UPDATE
  title = VALUES(title),
  category = VALUES(category),
  status = VALUES(status),
  start_time = VALUES(start_time),
  end_time = VALUES(end_time),
  cover_url = VALUES(cover_url),
  tags_json = VALUES(tags_json),
  updated_at = CURRENT_TIMESTAMP(3);
```

------

### 3.2 插入弹幕事件（幂等去重）

**推荐写法：INSERT IGNORE**（重复 msg_id 直接忽略；后续汇总更新必须只对“新增成功”执行）

```sql
INSERT IGNORE INTO event_danmaku (
  session_id, room_id, msg_id, event_time,
  user_id, user_name, content, type, sc_value_fen, raw_json
) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?);
```

> 说明：如果你希望重复时更新 raw_json，可改用 `ON DUPLICATE KEY UPDATE raw_json=VALUES(raw_json)`，但务必保证汇总不会重复加。

------

### 3.3 插入礼物事件（幂等去重）

```sql
INSERT IGNORE INTO event_gift (
  session_id, room_id, msg_id, event_time,
  user_id, user_name,
  gift_id, gift_name, gift_count, gift_value_fen,
  raw_json
) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?);
```

------

### 3.4 sessions 汇总字段更新（仅在“事件新增成功”后执行）

弹幕新增后：

```sql
UPDATE sessions
SET danmaku_count = danmaku_count + 1,
    updated_at = CURRENT_TIMESTAMP(3)
WHERE session_id = ?;
```

礼物新增后（注意礼物事件里 gift_count 可能 > 1）：

```sql
UPDATE sessions
SET gift_count = gift_count + ?,
    revenue_fen = revenue_fen + ?,
    updated_at = CURRENT_TIMESTAMP(3)
WHERE session_id = ?;
```

------

### 3.5 metrics_bucket Upsert（按 granularity 归桶）

> bucket_time 规则建议：
>
> - granularity=1m：bucket_time = `YYYY-MM-DD HH:MM:00.000`
> - granularity=10s：bucket_time = 秒向下取整到 10s（如 12:00:20）
>   具体 floor 逻辑在 Java 侧做，保证同一事件算入同一 bucket。

弹幕聚合（新增后执行）：

```sql
INSERT INTO metrics_bucket (
  session_id, bucket_time, granularity, danmaku_count
) VALUES (?, ?, ?, 1)
ON DUPLICATE KEY UPDATE
  danmaku_count = danmaku_count + 1;
```

礼物聚合（新增后执行）：

```sql
INSERT INTO metrics_bucket (
  session_id, bucket_time, granularity, gift_count, revenue_fen
) VALUES (?, ?, ?, ?, ?)
ON DUPLICATE KEY UPDATE
  gift_count = gift_count + VALUES(gift_count),
  revenue_fen = revenue_fen + VALUES(revenue_fen);
```

------

## 4）对外 API 查询 SQL 模板（MyBatis 用）

> 目标：尽快实现可演示接口：
>
> - `GET /sessions`
> - `GET /sessions/{id}/events/danmaku`
> - `GET /sessions/{id}/events/gifts`
> - `GET /sessions/{id}/metrics/range`

------

## 4.1 `GET /sessions` 场次列表（分页 + 过滤 + 排序）

### 4.1.1 参数建议

- filters：
  - `anchorId`（可选）
  - `roomId`（可选）
  - `status`（可选，LIVE/ENDED）
  - `startFrom` / `startTo`（可选）
- 分页：
  - `page`（1-based）
  - `size`
- 排序：
  - `sort`（默认 `-start_time`）
  - 允许：`start_time`, `-start_time`, `end_time`, `-end_time`, `created_at`, `-created_at`

### 4.1.2 sort 白名单映射（必须由 Java 层转换）

- sortKey => orderBySql
  - `start_time` => `s.start_time ASC`
  - `-start_time` => `s.start_time DESC`
  - `end_time` => `s.end_time ASC`
  - `-end_time` => `s.end_time DESC`
  - `created_at` => `s.created_at ASC`
  - `-created_at` => `s.created_at DESC`
- 若不在白名单：强制用 `s.start_time DESC`

### 4.1.3 列表查询 SQL（示例）

```sql
SELECT
  s.session_id, s.room_id, s.anchor_id, s.platform_uid,
  s.title, s.category, s.status,
  s.start_time, s.end_time,
  s.cover_url, s.tags_json,
  s.danmaku_count, s.gift_count, s.revenue_fen,
  s.peak_online, s.data_delay_sec
FROM sessions s
WHERE 1=1
  /* 可选过滤 */
  /* AND s.anchor_id = ? */
  /* AND s.room_id = ? */
  /* AND s.status = ? */
  /* AND s.start_time >= ? */
  /* AND s.start_time <= ? */
ORDER BY {orderBySql}
LIMIT ? OFFSET ?;
```

### 4.1.4 count SQL

```sql
SELECT COUNT(1)
FROM sessions s
WHERE 1=1
  /* 同上过滤条件 */;
```

------

## 4.2 `GET /sessions/{id}/events/danmaku` 弹幕分页（time range + type + sort）

### 4.2.1 参数建议

- path：`sessionId`
- query：
  - `startTime`（可选）
  - `endTime`（可选）
  - `type`（可选：NORMAL/SC）
  - `page/size`
  - `sort` 默认 `-event_time`
- 允许 sort：
  - `event_time`, `-event_time`, `id`, `-id`

### 4.2.2 sort 白名单映射（Java 层转换）

- `event_time` => `d.event_time ASC`
- `-event_time` => `d.event_time DESC`
- `id` => `d.id ASC`
- `-id` => `d.id DESC`
- fallback：`d.event_time DESC`

### 4.2.3 列表查询 SQL

```sql
SELECT
  d.id, d.session_id, d.room_id,
  d.msg_id, d.event_time,
  d.user_id, d.user_name,
  d.content, d.type, d.sc_value_fen
FROM event_danmaku d
WHERE d.session_id = ?
  /* AND d.event_time >= ? */
  /* AND d.event_time <= ? */
  /* AND d.type = ? */
ORDER BY {orderBySql}
LIMIT ? OFFSET ?;
```

### 4.2.4 count SQL

```sql
SELECT COUNT(1)
FROM event_danmaku d
WHERE d.session_id = ?
  /* 同上过滤 */;
```

------

## 4.3 `GET /sessions/{id}/events/gifts` 礼物分页（time range + sort）

### 4.3.1 参数建议

- query：
  - `startTime/endTime`
  - `page/size`
  - `sort` 默认 `-event_time`
- 允许 sort：
  - `event_time`, `-event_time`, `gift_value_fen`, `-gift_value_fen`, `id`, `-id`

### 4.3.2 sort 白名单映射（Java 层转换）

- `event_time` => `g.event_time ASC`
- `-event_time` => `g.event_time DESC`
- `gift_value_fen` => `g.gift_value_fen ASC`
- `-gift_value_fen` => `g.gift_value_fen DESC`
- `id` => `g.id ASC`
- `-id` => `g.id DESC`
- fallback：`g.event_time DESC`

### 4.3.3 列表查询 SQL

```sql
SELECT
  g.id, g.session_id, g.room_id,
  g.msg_id, g.event_time,
  g.user_id, g.user_name,
  g.gift_id, g.gift_name, g.gift_count, g.gift_value_fen
FROM event_gift g
WHERE g.session_id = ?
  /* AND g.event_time >= ? */
  /* AND g.event_time <= ? */
ORDER BY {orderBySql}
LIMIT ? OFFSET ?;
```

### 4.3.4 count SQL

```sql
SELECT COUNT(1)
FROM event_gift g
WHERE g.session_id = ?
  /* 同上过滤 */;
```

------

## 4.4 `GET /sessions/{id}/metrics/range` 趋势点位（按桶返回）

### 4.4.1 参数建议

- query：
  - `startTime`（必填）
  - `endTime`（必填）
  - `granularity`（必填：10s/1m/5m）

### 4.4.2 查询 SQL

```sql
SELECT
  m.bucket_time,
  m.granularity,
  m.danmaku_count,
  m.gift_count,
  m.revenue_fen,
  m.online
FROM metrics_bucket m
WHERE m.session_id = ?
  AND m.granularity = ?
  AND m.bucket_time >= ?
  AND m.bucket_time <= ?
ORDER BY m.bucket_time ASC;
```

------

## 5）MyBatis 实现提示（Trae CN 必读）

### 5.1 分页计算

- page 为 1-based
- `offset = (page - 1) * size`

### 5.2 sort 安全实现方式（推荐）

**不要**在 XML 里直接 `${sort}`
必须由 Java 层将 sort 参数映射为 `orderBySql`（白名单），然后传到 Mapper：

- Java：
  - `String orderBySql = SortWhitelist.toOrderBy(sort, defaultSort);`
- XML：
  - `ORDER BY ${orderBySql}`
    （此处允许 `${}` 的前提是 `orderBySql` 只可能来自白名单常量）

### 5.3 幂等写入与“是否新增”的判断

使用 `INSERT IGNORE` 后：

- MyBatis 返回的影响行数 `rows`：
  - `rows = 1` 表示新增成功 → 才允许更新 sessions 汇总、metrics_bucket
  - `rows = 0` 表示重复被忽略 → 不做任何汇总更新

------

## 6）验收标准（建库正确 + SQL 可跑）

### 6.1 表存在

```sql
SHOW TABLES LIKE 'sessions';
SHOW TABLES LIKE 'event_danmaku';
SHOW TABLES LIKE 'event_gift';
SHOW TABLES LIKE 'metrics_bucket';
```

### 6.2 关键索引存在

- sessions：`idx_room_time`, `idx_anchor_time`, `idx_status_time`
- event_danmaku：`uk_session_msg`, `idx_session_time`
- event_gift：`uk_session_msg`, `idx_session_time`
- metrics_bucket：`uk_session_bucket`, `idx_session_time`

------

## 7）下一步实现顺序（建议照做）

1. 先用 fixtures 导入：sessions + event_danmaku + event_gift（幂等）
2. 同步更新 sessions 汇总 + metrics_bucket（分钟桶）
3. 实现 API 查询：/sessions、/events/danmaku、/events/gifts、/metrics/range
4. 最后接 open-live：把数据源换成 WS 事件流，落库逻辑不变