-- ============================================================
-- 配件流转管理系统 数据库初始化脚本 v2.0
-- Database: cm_db
-- ============================================================

CREATE DATABASE IF NOT EXISTS `cm_db` DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE `cm_db`;

-- -----------------------------------------------------------
-- 1. 系统用户表
-- -----------------------------------------------------------
DROP TABLE IF EXISTS `sys_user`;
CREATE TABLE `sys_user` (
  `id`            BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
  `username`      VARCHAR(50)     NOT NULL COMMENT '用户名',
  `password_hash` VARCHAR(100)    NOT NULL COMMENT '密码哈希',
  `real_name`     VARCHAR(50)     DEFAULT NULL COMMENT '真实姓名',
  `role`          TINYINT UNSIGNED NOT NULL DEFAULT 2 COMMENT '1=管理员 2=操作员',
  `status`        TINYINT UNSIGNED NOT NULL DEFAULT 1 COMMENT '1=正常 0=禁用',
  `login_fail_count` INT UNSIGNED NOT NULL DEFAULT 0 COMMENT '连续登录失败次数',
  `lock_time`     DATETIME        DEFAULT NULL COMMENT '账号锁定截止时间',
  `create_time`   DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `update_time`   DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_username` (`username`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='系统用户表';

-- -----------------------------------------------------------
-- 2. 配件分类表
-- -----------------------------------------------------------
DROP TABLE IF EXISTS `category`;
CREATE TABLE `category` (
  `id`          BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
  `name`        VARCHAR(50)     NOT NULL COMMENT '分类名称',
  `sort`        INT UNSIGNED    NOT NULL DEFAULT 0 COMMENT '排序号',
  `remark`      VARCHAR(255)    DEFAULT NULL,
  `status`      TINYINT UNSIGNED NOT NULL DEFAULT 1 COMMENT '1=正常 0=已删除',
  `create_time` DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `update_time` DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='配件分类表';

-- -----------------------------------------------------------
-- 3. 师傅信息表
-- -----------------------------------------------------------
DROP TABLE IF EXISTS `worker`;
CREATE TABLE `worker` (
  `id`          BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
  `name`        VARCHAR(50)     NOT NULL COMMENT '师傅姓名',
  `job_no`      VARCHAR(50)     DEFAULT NULL COMMENT '工号',
  `phone`       VARCHAR(20)     DEFAULT NULL COMMENT '联系电话',
  `remark`      VARCHAR(255)    DEFAULT NULL,
  `status`      TINYINT UNSIGNED NOT NULL DEFAULT 1 COMMENT '1=正常 0=已删除',
  `create_time` DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `update_time` DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='师傅信息表';

-- -----------------------------------------------------------
-- 4. 工件表（每个物理工件一条记录）
-- -----------------------------------------------------------
DROP TABLE IF EXISTS `accessory`;
CREATE TABLE `accessory` (
  `id`               BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
  `barcode`          VARCHAR(100)    NOT NULL COMMENT '配件条码（可重复，同型号共享）',
  `item_code`        VARCHAR(50)     NOT NULL COMMENT '工件业务编号（唯一，系统生成）',
  `category_id`      BIGINT UNSIGNED DEFAULT NULL COMMENT '分类ID',
  `remark`           VARCHAR(255)    DEFAULT NULL COMMENT '备注',
  `status`           TINYINT UNSIGNED NOT NULL DEFAULT 1 COMMENT '1=在库 2=已出库 3=已完成 4=寄回厂家 5=旧件待返厂 6=已售卖',
  `worker_id`        BIGINT UNSIGNED DEFAULT NULL COMMENT '当前持有师傅（已出库时）',
  `related_item_code` VARCHAR(50)    DEFAULT NULL COMMENT '关联新工件编号（旧件专用）',
  `shelf_id`      BIGINT UNSIGNED  DEFAULT NULL COMMENT '货架ID',
  `is_high_value`    TINYINT         DEFAULT NULL COMMENT '1=高价值 0=低价值（退件时）',
  `operator`         VARCHAR(50)     DEFAULT NULL COMMENT '入库操作人',
  `version`          INT UNSIGNED    NOT NULL DEFAULT 0 COMMENT '乐观锁',
  `create_time`      DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `update_time`      DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_item_code` (`item_code`),
  KEY `idx_barcode` (`barcode`),
  KEY `idx_status` (`status`),
  KEY `idx_worker_id` (`worker_id`),
  KEY `idx_category_id` (`category_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='工件表（每个物理工件一条记录）';

-- -----------------------------------------------------------
-- 5. 流转流水表（全量记录，不可删除）
-- -----------------------------------------------------------
DROP TABLE IF EXISTS `flow_record`;
CREATE TABLE `flow_record` (
  `id`              BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
  `accessory_id`    BIGINT UNSIGNED NOT NULL COMMENT '工件ID',
  `item_code`       VARCHAR(50)     NOT NULL COMMENT '工件编号',
  `barcode`         VARCHAR(100)    NOT NULL COMMENT '配件条码',
  `flow_type`       TINYINT UNSIGNED NOT NULL COMMENT '1=入库 2=出库 3=归还(完成) 4=归还(取消-退回) 5=归还(取消-寄回厂家) 6=售卖 7=转移',
  `from_worker_id`  BIGINT UNSIGNED DEFAULT NULL COMMENT '来源师傅（转移时）',
  `from_worker_name` VARCHAR(50)    DEFAULT NULL,
  `to_worker_id`    BIGINT UNSIGNED DEFAULT NULL COMMENT '目标师傅（出库/转移时）',
  `to_worker_name`  VARCHAR(50)     DEFAULT NULL,
  `price`           DECIMAL(10,2)   DEFAULT NULL COMMENT '售卖单价',
  `customer_name`   VARCHAR(50)     DEFAULT NULL COMMENT '客户姓名（售卖时）',
  `customer_phone`  VARCHAR(20)     DEFAULT NULL,
  `remark`          VARCHAR(255)    DEFAULT NULL,
  `operator`        VARCHAR(50)     DEFAULT NULL COMMENT '操作人',
  `create_time`     DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_item_code` (`item_code`),
  KEY `idx_barcode` (`barcode`),
  KEY `idx_flow_type` (`flow_type`),
  KEY `idx_to_worker` (`to_worker_id`),
  KEY `idx_create_time` (`create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='流转流水表';

-- -----------------------------------------------------------
-- 6. 操作日志表
-- -----------------------------------------------------------
DROP TABLE IF EXISTS `operation_log`;
CREATE TABLE `operation_log` (
  `id`            BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
  `action_type`   VARCHAR(50)     NOT NULL COMMENT '操作类型',
  `content`       VARCHAR(500)    DEFAULT NULL COMMENT '操作内容',
  `related_barcode` VARCHAR(100)  DEFAULT NULL COMMENT '关联条码',
  `related_worker_id` BIGINT UNSIGNED DEFAULT NULL COMMENT '关联师傅ID',
  `operator`      VARCHAR(50)     DEFAULT NULL COMMENT '操作人',
  `ip`            VARCHAR(50)     DEFAULT NULL COMMENT '操作IP',
  `create_time`   DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_action_type` (`action_type`),
  KEY `idx_create_time` (`create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='操作日志表';

-- -----------------------------------------------------------


DROP TABLE IF EXISTS `shelf`;
CREATE TABLE `shelf` (
  `id`          BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
  `name`        VARCHAR(100)    NOT NULL COMMENT '货架名称',
  `location`    VARCHAR(200)    DEFAULT NULL COMMENT '位置描述',
  `remark`      VARCHAR(255)    DEFAULT NULL,
  `create_time` DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='货架表';
-- 初始数据
-- -----------------------------------------------------------
INSERT INTO `category` (`name`, `sort`) VALUES
('遥控器', 1),
('传感器', 2),
('电路板', 3),
('电机', 4),
('密封件', 5),
('工具', 6),
('其他', 99);
