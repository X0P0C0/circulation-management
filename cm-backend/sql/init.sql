-- ============================================================
-- 配件流转管理系统 数据库初始化脚本
-- Database: cm_db
-- Character Set: utf8mb4
-- Engine: InnoDB
-- ============================================================

-- 创建数据库
CREATE DATABASE IF NOT EXISTS `cm_db` DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE `cm_db`;

-- -----------------------------------------------------------
-- 1. 管理员用户表
-- -----------------------------------------------------------
DROP TABLE IF EXISTS `sys_user`;
CREATE TABLE `sys_user` (
  `id`            BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `username`      VARCHAR(50)     NOT NULL COMMENT '用户名',
  `password_hash` VARCHAR(100)    NOT NULL COMMENT '密码哈希（BCrypt）',
  `real_name`     VARCHAR(50)     DEFAULT NULL COMMENT '真实姓名',
  `status`        TINYINT UNSIGNED NOT NULL DEFAULT 1 COMMENT '状态：1=正常 0=禁用',
  `create_time`   DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time`   DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_username` (`username`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='管理员用户表';

-- -----------------------------------------------------------
-- 2. 配件分类表
-- -----------------------------------------------------------
DROP TABLE IF EXISTS `category`;
CREATE TABLE `category` (
  `id`          BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `name`        VARCHAR(50)     NOT NULL COMMENT '分类名称',
  `sort`        INT UNSIGNED    NOT NULL DEFAULT 0 COMMENT '排序号（越小越靠前）',
  `remark`      VARCHAR(255)    DEFAULT NULL COMMENT '备注',
  `status`      TINYINT UNSIGNED NOT NULL DEFAULT 1 COMMENT '状态：1=正常 0=已删除',
  `create_time` DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='配件分类表';

-- -----------------------------------------------------------
-- 3. 师傅信息表
-- -----------------------------------------------------------
DROP TABLE IF EXISTS `worker`;
CREATE TABLE `worker` (
  `id`          BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `name`        VARCHAR(50)     NOT NULL COMMENT '师傅姓名',
  `job_no`      VARCHAR(50)     DEFAULT NULL COMMENT '工号',
  `phone`       VARCHAR(20)     DEFAULT NULL COMMENT '联系电话',
  `remark`      VARCHAR(255)    DEFAULT NULL COMMENT '备注',
  `status`      TINYINT UNSIGNED NOT NULL DEFAULT 1 COMMENT '状态：1=正常 0=已删除',
  `create_time` DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='师傅信息表';

-- -----------------------------------------------------------
-- 4. 配件信息表（主数据）
-- -----------------------------------------------------------
DROP TABLE IF EXISTS `accessory`;
CREATE TABLE `accessory` (
  `id`          BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `barcode`     VARCHAR(100)    NOT NULL COMMENT '配件条码',
  `name`        VARCHAR(100)    NOT NULL COMMENT '配件名称',
  `spec`        VARCHAR(100)    DEFAULT NULL COMMENT '规格型号',
  `category_id` BIGINT UNSIGNED DEFAULT NULL COMMENT '分类ID',
  `unit`        VARCHAR(20)     DEFAULT NULL COMMENT '单位（个、件、套等）',
  `remark`      VARCHAR(255)    DEFAULT NULL COMMENT '备注',
  `create_time` DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_barcode` (`barcode`),
  KEY `idx_category_id` (`category_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='配件信息表';

-- -----------------------------------------------------------
-- 5. 库存主表（总库存，每个配件一条记录）
-- -----------------------------------------------------------
DROP TABLE IF EXISTS `inventory`;
CREATE TABLE `inventory` (
  `id`            BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `accessory_id`  BIGINT UNSIGNED NOT NULL COMMENT '配件ID',
  `total_qty`     INT UNSIGNED    NOT NULL DEFAULT 0 COMMENT '总入库数量',
  `available_qty` INT UNSIGNED    NOT NULL DEFAULT 0 COMMENT '当前可用库存（未被领用/售卖）',
  `create_time`   DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time`   DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_accessory_id` (`accessory_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='库存主表';

-- -----------------------------------------------------------
-- 6. 师傅库存表（配件在师傅名下的数量）
-- -----------------------------------------------------------
DROP TABLE IF EXISTS `inventory_owner`;
CREATE TABLE `inventory_owner` (
  `id`           BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `accessory_id` BIGINT UNSIGNED NOT NULL COMMENT '配件ID',
  `worker_id`    BIGINT UNSIGNED NOT NULL COMMENT '师傅ID',
  `qty`          INT UNSIGNED    NOT NULL DEFAULT 0 COMMENT '持有数量',
  `create_time`  DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time`  DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_accessory_worker` (`accessory_id`, `worker_id`),
  KEY `idx_worker_id` (`worker_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='师傅库存表';

-- -----------------------------------------------------------
-- 7. 流转流水表（全量记录，不可删除）
-- -----------------------------------------------------------
DROP TABLE IF EXISTS `flow_record`;
CREATE TABLE `flow_record` (
  `id`              BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `accessory_id`    BIGINT UNSIGNED NOT NULL COMMENT '配件ID',
  `barcode`         VARCHAR(100)    NOT NULL COMMENT '配件条码（冗余，便于查询）',
  `accessory_name`  VARCHAR(100)    DEFAULT NULL COMMENT '配件名称（冗余）',
  `flow_type`       TINYINT UNSIGNED NOT NULL COMMENT '流转类型：1=入库 2=领用 3=归还 4=售卖',
  `worker_id`       BIGINT UNSIGNED DEFAULT NULL COMMENT '关联师傅ID（领用/归还时）',
  `worker_name`     VARCHAR(50)     DEFAULT NULL COMMENT '师傅姓名（冗余）',
  `customer_name`   VARCHAR(50)     DEFAULT NULL COMMENT '客户姓名（售卖时选填）',
  `customer_phone`  VARCHAR(20)     DEFAULT NULL COMMENT '客户电话（售卖时选填）',
  `qty`             INT UNSIGNED    NOT NULL DEFAULT 1 COMMENT '数量',
  `remark`          VARCHAR(255)    DEFAULT NULL COMMENT '备注',
  `operator`        VARCHAR(50)     DEFAULT NULL COMMENT '操作人',
  `create_time`     DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '操作时间',
  PRIMARY KEY (`id`),
  KEY `idx_barcode` (`barcode`),
  KEY `idx_worker_id` (`worker_id`),
  KEY `idx_flow_type` (`flow_type`),
  KEY `idx_create_time` (`create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='流转流水表';

-- -----------------------------------------------------------
-- 8. 操作日志表
-- -----------------------------------------------------------
DROP TABLE IF EXISTS `operation_log`;
CREATE TABLE `operation_log` (
  `id`              BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `action_type`     VARCHAR(50)     NOT NULL COMMENT '操作类型（LOGIN/INBOUND/TRANSFER_OUT/TRANSFER_IN/SELL/CREATE_WORKER 等）',
  `content`         VARCHAR(500)    DEFAULT NULL COMMENT '操作内容描述',
  `related_barcode` VARCHAR(100)    DEFAULT NULL COMMENT '关联条码',
  `related_worker_id` BIGINT UNSIGNED DEFAULT NULL COMMENT '关联师傅ID',
  `operator`        VARCHAR(50)     DEFAULT NULL COMMENT '操作人',
  `ip`              VARCHAR(50)     DEFAULT NULL COMMENT '操作IP',
  `create_time`     DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '操作时间',
  PRIMARY KEY (`id`),
  KEY `idx_action_type` (`action_type`),
  KEY `idx_create_time` (`create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='操作日志表';

-- -----------------------------------------------------------
-- 初始数据
-- -----------------------------------------------------------

-- 默认管理员账号：admin / admin123（BCrypt 加密）
-- BCrypt hash of "admin123": $2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iAt6Z5EH
INSERT INTO `sys_user` (`username`, `password_hash`, `real_name`, `status`)
VALUES ('admin', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iAt6Z5EH', '系统管理员', 1);

-- 默认配件分类
INSERT INTO `category` (`name`, `sort`) VALUES
('螺丝螺母', 1),
('轴承', 2),
('密封件', 3),
('传动件', 4),
('电气元件', 5),
('工具', 6),
('其他', 99);
