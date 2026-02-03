-- 创建sessions表
CREATE TABLE IF NOT EXISTS `sessions` (
  `id` BIGINT(20) NOT NULL AUTO_INCREMENT COMMENT '场次ID',
  `room_id` BIGINT(20) NOT NULL COMMENT '直播间ID',
  `anchor_id` BIGINT(20) NOT NULL COMMENT '主播ID',
  `platform_uid` BIGINT(20) DEFAULT NULL COMMENT '平台用户ID',
  `game_id` VARCHAR(64) NULL COMMENT 'open-live game_id',
  `openlive_status` VARCHAR(16) NULL COMMENT 'NONE/STARTED/ENDED',
  `room_title` VARCHAR(255) NOT NULL COMMENT '直播间标题',
  `start_time` DATETIME NOT NULL COMMENT '开播时间',
  `end_time` DATETIME DEFAULT NULL COMMENT '结束时间',
  `duration` INT(11) DEFAULT 0 COMMENT '时长（秒）',
  `status` TINYINT(4) NOT NULL DEFAULT 0 COMMENT '状态：0-未开始，1-直播中，2-已结束',
  `online_peak` INT(11) DEFAULT 0 COMMENT '在线峰值',
  `danmaku_count` INT(11) DEFAULT 0 COMMENT '弹幕数',
  `gift_count` INT(11) DEFAULT 0 COMMENT '礼物数',
  `gift_amount` DECIMAL(12,2) DEFAULT 0.00 COMMENT '礼物金额',
  `viewer_count` INT(11) DEFAULT 0 COMMENT '观看人数',
  `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  KEY `idx_room_id` (`room_id`),
  KEY `idx_anchor_id` (`anchor_id`),
  KEY `idx_status` (`status`),
  KEY `idx_start_time` (`start_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='直播场次表';

-- 创建event_danmaku表
CREATE TABLE IF NOT EXISTS `event_danmaku` (
  `id` BIGINT(20) NOT NULL AUTO_INCREMENT COMMENT '弹幕ID',
  `session_id` BIGINT(20) NOT NULL COMMENT '场次ID',
  `room_id` BIGINT(20) NOT NULL COMMENT '直播间ID',
  `user_id` BIGINT(20) NOT NULL COMMENT '用户ID',
  `username` VARCHAR(100) NOT NULL COMMENT '用户名',
  `content` VARCHAR(500) NOT NULL COMMENT '弹幕内容',
  `timestamp` DATETIME NOT NULL COMMENT '发送时间',
  `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`),
  KEY `idx_session_id` (`session_id`),
  KEY `idx_room_id` (`room_id`),
  KEY `idx_user_id` (`user_id`),
  KEY `idx_timestamp` (`timestamp`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='弹幕事件表';

-- 创建event_gift表
CREATE TABLE IF NOT EXISTS `event_gift` (
  `id` BIGINT(20) NOT NULL AUTO_INCREMENT COMMENT '礼物ID',
  `session_id` BIGINT(20) NOT NULL COMMENT '场次ID',
  `room_id` BIGINT(20) NOT NULL COMMENT '直播间ID',
  `user_id` BIGINT(20) NOT NULL COMMENT '用户ID',
  `username` VARCHAR(100) NOT NULL COMMENT '用户名',
  `gift_name` VARCHAR(100) NOT NULL COMMENT '礼物名称',
  `gift_count` INT(11) NOT NULL DEFAULT 1 COMMENT '礼物数量',
  `gift_price` DECIMAL(10,2) NOT NULL COMMENT '礼物单价',
  `total_amount` DECIMAL(12,2) NOT NULL COMMENT '总金额',
  `timestamp` DATETIME NOT NULL COMMENT '发送时间',
  `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`),
  KEY `idx_session_id` (`session_id`),
  KEY `idx_room_id` (`room_id`),
  KEY `idx_user_id` (`user_id`),
  KEY `idx_timestamp` (`timestamp`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='礼物事件表';

-- 创建metrics_bucket表
CREATE TABLE IF NOT EXISTS `metrics_bucket` (
  `id` BIGINT(20) NOT NULL AUTO_INCREMENT COMMENT '指标ID',
  `session_id` BIGINT(20) NOT NULL COMMENT '场次ID',
  `room_id` BIGINT(20) NOT NULL COMMENT '直播间ID',
  `granularity` TINYINT(4) NOT NULL COMMENT '粒度：1-分钟，2-小时，3-天',
  `bucket_time` DATETIME NOT NULL COMMENT '时间段',
  `online_count` INT(11) DEFAULT 0 COMMENT '在线人数',
  `danmaku_count` INT(11) DEFAULT 0 COMMENT '弹幕数',
  `gift_count` INT(11) DEFAULT 0 COMMENT '礼物数',
  `gift_amount` DECIMAL(12,2) DEFAULT 0.00 COMMENT '礼物金额',
  `viewer_count` INT(11) DEFAULT 0 COMMENT '观看人数',
  `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`),
  KEY `idx_session_id` (`session_id`),
  KEY `idx_room_id` (`room_id`),
  KEY `idx_bucket_time` (`bucket_time`),
  KEY `idx_granularity` (`granularity`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='指标桶表';

-- 创建用户表（如果不存在）
CREATE TABLE IF NOT EXISTS `user` (
  `id` BIGINT(20) NOT NULL AUTO_INCREMENT COMMENT '用户ID',
  `username` VARCHAR(50) NOT NULL COMMENT '用户名',
  `password` VARCHAR(100) NOT NULL COMMENT '密码',
  `nickname` VARCHAR(100) NOT NULL COMMENT '昵称',
  `role_id` INT(11) NOT NULL DEFAULT 1 COMMENT '角色ID',
  `status` TINYINT(4) NOT NULL DEFAULT 1 COMMENT '状态：0-禁用，1-启用',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `idx_username` (`username`),
  KEY `idx_role_id` (`role_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户表';

-- 创建角色表（如果不存在）
CREATE TABLE IF NOT EXISTS `role` (
  `id` INT(11) NOT NULL AUTO_INCREMENT COMMENT '角色ID',
  `role_name` VARCHAR(50) NOT NULL COMMENT '角色名称',
  `role_desc` VARCHAR(255) DEFAULT NULL COMMENT '角色描述',
  `status` TINYINT(4) NOT NULL DEFAULT 1 COMMENT '状态：0-禁用，1-启用',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `idx_role_name` (`role_name`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='角色表';

-- 创建权限表（如果不存在）
CREATE TABLE IF NOT EXISTS `permission` (
  `id` INT(11) NOT NULL AUTO_INCREMENT COMMENT '权限ID',
  `perm_name` VARCHAR(100) NOT NULL COMMENT '权限名称',
  `perm_key` VARCHAR(100) NOT NULL COMMENT '权限标识',
  `perm_desc` VARCHAR(255) DEFAULT NULL COMMENT '权限描述',
  `status` TINYINT(4) NOT NULL DEFAULT 1 COMMENT '状态：0-禁用，1-启用',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `idx_perm_key` (`perm_key`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='权限表';

-- 创建角色权限关联表（如果不存在）
CREATE TABLE IF NOT EXISTS `role_permission` (
  `id` INT(11) NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `role_id` INT(11) NOT NULL COMMENT '角色ID',
  `perm_id` INT(11) NOT NULL COMMENT '权限ID',
  PRIMARY KEY (`id`),
  UNIQUE KEY `idx_role_perm` (`role_id`,`perm_id`),
  KEY `idx_role_id` (`role_id`),
  KEY `idx_perm_id` (`perm_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='角色权限关联表';

-- 插入默认角色数据
INSERT INTO `role` (`role_name`, `role_desc`) VALUES
('主播', '直播间主播'),
('经纪人', '直播间经纪人'),
('管理员', '系统管理员')
ON DUPLICATE KEY UPDATE `role_desc` = VALUES(`role_desc`);

-- 插入默认用户数据（密码：123456）
INSERT INTO `user` (`username`, `password`, `nickname`, `role_id`) VALUES
('anchor', '123456', '测试主播', 1),
('broker', '123456', '测试经纪人', 2),
('admin', '123456', '系统管理员', 3)
ON DUPLICATE KEY UPDATE `nickname` = VALUES(`nickname`), `role_id` = VALUES(`role_id`);
