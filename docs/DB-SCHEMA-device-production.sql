-- 设备生产管理相关表
-- 在 camera_app 数据库下执行

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- 1. 销售商表：vendor
DROP TABLE IF EXISTS `vendor`;
CREATE TABLE `vendor` (
  `id`             BIGINT       NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `vendor_code`    VARCHAR(2)   NOT NULL COMMENT '销售商代码(2位，如00-99/AA)',
  `vendor_name`    VARCHAR(100) NOT NULL COMMENT '销售商名称',
  `contact_person` VARCHAR(50)           DEFAULT NULL COMMENT '联系人',
  `contact_phone`  VARCHAR(20)           DEFAULT NULL COMMENT '联系电话',
  `address`        VARCHAR(255)          DEFAULT NULL COMMENT '地址',
  `status`         TINYINT(1)   NOT NULL DEFAULT 1 COMMENT '状态 0-禁用 1-启用',
  `created_at`     DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at`     DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_vendor_code` (`vendor_code`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='销售商表';

-- 初始化销售商数据
INSERT INTO `vendor` (`vendor_code`, `vendor_name`, `contact_person`, `status`) VALUES
('00', '默认(自营)', NULL, 1),
('01', '销售商01', NULL, 1),
('AA', '测试账号', NULL, 1);

-- 2. 装机商表：assembler
DROP TABLE IF EXISTS `assembler`;
CREATE TABLE `assembler` (
  `id`              BIGINT       NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `assembler_code`  VARCHAR(1)   NOT NULL COMMENT '装机商代码(1位)',
  `assembler_name`  VARCHAR(100) NOT NULL COMMENT '装机商名称',
  `contact_person`  VARCHAR(50)           DEFAULT NULL COMMENT '联系人',
  `contact_phone`   VARCHAR(20)           DEFAULT NULL COMMENT '联系电话',
  `status`          TINYINT(1)   NOT NULL DEFAULT 1 COMMENT '状态 0-禁用 1-启用',
  `created_at`      DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at`      DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_assembler_code` (`assembler_code`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='装机商表';

-- 初始化装机商数据
INSERT INTO `assembler` (`assembler_code`, `assembler_name`, `status`) VALUES
('0', '默认(组装厂=客户)', 1),
('A', '测试账号', 1);

-- 3. 设备生产批次表：device_production_batch
DROP TABLE IF EXISTS `device_production_batch`;
CREATE TABLE `device_production_batch` (
  `id`              BIGINT       NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `batch_no`        VARCHAR(32)  NOT NULL COMMENT '批次号(自动生成，如 PB20241204001)',
  `network_lens`    VARCHAR(2)   NOT NULL COMMENT '网络+镜头配置(第1-2位，如A1/B1)',
  `device_form`     VARCHAR(1)   NOT NULL COMMENT '设备形态(第3位，1-5)',
  `special_req`     VARCHAR(1)   NOT NULL COMMENT '特殊要求(第4位，0-3)',
  `assembler_code`  VARCHAR(1)   NOT NULL COMMENT '装机商代码(第5位)',
  `vendor_code`     VARCHAR(2)   NOT NULL COMMENT '销售商代码(第6-7位)',
  `reserved`        VARCHAR(1)   NOT NULL DEFAULT '0' COMMENT '预留位(第8位，固定0)',
  `quantity`        INT          NOT NULL COMMENT '生产数量',
  `start_serial`    INT          NOT NULL COMMENT '起始序列号',
  `end_serial`      INT          NOT NULL COMMENT '结束序列号',
  `status`          VARCHAR(20)  NOT NULL DEFAULT 'pending' COMMENT '状态: pending-待生产 producing-生产中 completed-已完成',
  `remark`          VARCHAR(500)          DEFAULT NULL COMMENT '备注',
  `created_by`      VARCHAR(50)           DEFAULT NULL COMMENT '创建人',
  `created_at`      DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at`      DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_batch_no` (`batch_no`),
  KEY `idx_vendor_code` (`vendor_code`),
  KEY `idx_status` (`status`),
  KEY `idx_created_at` (`created_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='设备生产批次表';

-- 4. 生产设备表：manufactured_device
DROP TABLE IF EXISTS `manufactured_device`;
CREATE TABLE `manufactured_device` (
  `id`              BIGINT       NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `device_id`       VARCHAR(16)  NOT NULL COMMENT '完整设备ID(16位)',
  `batch_id`        BIGINT                DEFAULT NULL COMMENT '关联批次ID',
  `network_lens`    VARCHAR(2)            DEFAULT NULL COMMENT '网络+镜头(第1-2位)',
  `device_form`     VARCHAR(1)            DEFAULT NULL COMMENT '设备形态(第3位)',
  `special_req`     VARCHAR(1)            DEFAULT NULL COMMENT '特殊要求(第4位)',
  `assembler_code`  VARCHAR(1)            DEFAULT NULL COMMENT '装机商代码(第5位)',
  `vendor_code`     VARCHAR(2)            DEFAULT NULL COMMENT '销售商代码(第6-7位)',
  `serial_no`       VARCHAR(8)   NOT NULL COMMENT '序列号(第9-16位)',
  `mac_address`     VARCHAR(20)           DEFAULT NULL COMMENT 'MAC地址',
  `status`          VARCHAR(20)  NOT NULL DEFAULT 'manufactured' COMMENT '状态: manufactured-已生产 activated-已激活 bound-已绑定',
  `manufactured_at` DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '生产时间',
  `activated_at`    DATETIME              DEFAULT NULL COMMENT '激活时间',
  `created_at`      DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at`      DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_device_id` (`device_id`),
  KEY `idx_batch_id` (`batch_id`),
  KEY `idx_vendor_code` (`vendor_code`),
  KEY `idx_assembler_code` (`assembler_code`),
  KEY `idx_status` (`status`),
  KEY `idx_manufactured_at` (`manufactured_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='生产设备表';

SET FOREIGN_KEY_CHECKS = 1;
