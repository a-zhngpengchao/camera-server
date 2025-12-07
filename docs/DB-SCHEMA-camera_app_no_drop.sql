-- camera_app 数据库建表脚本（无 DROP 版本）
-- 使用方式：
--   1. CREATE DATABASE IF NOT EXISTS camera_app DEFAULT CHARSET utf8mb4 COLLATE utf8mb4_general_ci;
--   2. USE camera_app;
--   3. 执行本脚本。

CREATE DATABASE IF NOT EXISTS camera_app DEFAULT CHARSET utf8mb4 COLLATE utf8mb4_general_ci;
USE camera_app;

-- 1. 设备表：device
CREATE TABLE IF NOT EXISTS `device` (
  `id` varchar(50) NOT NULL COMMENT '设备序列号，主键',
  `mac` varchar(20) NOT NULL COMMENT '设备 MAC 地址',
  `ssid` varchar(32) DEFAULT NULL COMMENT 'WiFi SSID',
  `region` varchar(10) DEFAULT NULL COMMENT '区域，如 cn/us',
  `name` varchar(100) DEFAULT NULL COMMENT '设备名称',
  `firmware_version` varchar(20) DEFAULT NULL COMMENT '固件版本',
  `status` tinyint(1) NOT NULL DEFAULT 0 COMMENT '设备状态 0-离线 1-在线',
  `enabled` tinyint(1) NOT NULL DEFAULT 1 COMMENT '是否启用 0-禁用 1-启用',
  `cloud_storage` tinyint(1) NOT NULL DEFAULT 0 COMMENT '云存储开关 0-关 1-开',
  `s3_hostname` varchar(255) DEFAULT NULL COMMENT '对象存储地址',
  `s3_region` varchar(50) DEFAULT NULL COMMENT '对象存储区域',
  `s3_access_key` varchar(100) DEFAULT NULL COMMENT '对象存储AK',
  `s3_secret_key` varchar(100) DEFAULT NULL COMMENT '对象存储SK',
  `mqtt_hostname` varchar(255) DEFAULT NULL COMMENT 'MQTT 服务器地址',
  `mqtt_username` varchar(50) DEFAULT NULL COMMENT 'MQTT 用户名',
  `mqtt_password` varchar(50) DEFAULT NULL COMMENT 'MQTT 密码',
  `ai_enabled` tinyint(1) NOT NULL DEFAULT 0 COMMENT 'AI 功能开关 0-关 1-开',
  `gpt_hostname` varchar(255) DEFAULT NULL COMMENT 'AI 服务地址',
  `gpt_key` varchar(100) DEFAULT NULL COMMENT 'AI 服务 Key',
  `last_online_time` datetime DEFAULT NULL COMMENT '最近在线时间',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  KEY `idx_device_mac` (`mac`),
  KEY `idx_device_region` (`region`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 ROW_FORMAT=DYNAMIC COMMENT='设备表';

-- 2. 配网记录表：network_config
CREATE TABLE IF NOT EXISTS `network_config` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `device_id` varchar(50) NOT NULL COMMENT '设备ID，对应 device.id',
  `ssid` varchar(32) NOT NULL COMMENT 'WiFi SSID',
  `password` varchar(64) DEFAULT NULL COMMENT 'WiFi 密码（建议加密存储）',
  `timezone` varchar(10) DEFAULT NULL COMMENT '时区，如 +8/-8',
  `region` varchar(10) DEFAULT NULL COMMENT '区域，如 cn/us',
  `ip_address` varchar(50) DEFAULT NULL COMMENT '设备获取到的 IP 地址',
  `config_status` tinyint(1) NOT NULL DEFAULT 0 COMMENT '配网状态 0-配网中 1-成功 2-失败',
  `config_method` varchar(20) DEFAULT NULL COMMENT '配网方式 qrcode/ble/audio',
  `config_source` varchar(50) DEFAULT NULL COMMENT '配网来源，如 APP 版本',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  KEY `idx_network_device` (`device_id`),
  KEY `idx_network_status` (`config_status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 ROW_FORMAT=DYNAMIC COMMENT='设备配网记录表';

-- 3. 设备消息表：device_message
CREATE TABLE IF NOT EXISTS `device_message` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `device_id` varchar(50) NOT NULL COMMENT '设备ID，对应 device.id',
  `topic` varchar(100) NOT NULL COMMENT '来源主题，例如 MQTT Topic',
  `title` varchar(200) DEFAULT NULL COMMENT '标题',
  `content` text COMMENT '内容，JSON 或文本',
  `message_type` varchar(20) DEFAULT NULL COMMENT '消息类型 event/alert/ai 等',
  `severity` tinyint(1) NOT NULL DEFAULT 0 COMMENT '严重级别 0-普通 1-警告 2-严重',
  `is_read` tinyint(1) NOT NULL DEFAULT 0 COMMENT '是否已读 0-未读 1-已读',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`),
  KEY `idx_msg_device` (`device_id`),
  KEY `idx_msg_type` (`message_type`),
  KEY `idx_msg_read` (`is_read`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 ROW_FORMAT=DYNAMIC COMMENT='设备消息表';

-- 4. 用户表：user（App 账号体系）
CREATE TABLE IF NOT EXISTS `user` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `uid` varchar(64) NOT NULL COMMENT '业务用户ID（如 user_001）',
  `username` varchar(64) DEFAULT NULL COMMENT '登录账号（可为手机号/邮箱）',
  `phone` varchar(20) DEFAULT NULL COMMENT '手机号',
  `email` varchar(100) DEFAULT NULL COMMENT '邮箱',
  `password_hash` varchar(255) DEFAULT NULL COMMENT '密码哈希（BCrypt）',
  `nickname` varchar(64) DEFAULT NULL COMMENT '昵称',
  `avatar` varchar(255) DEFAULT NULL COMMENT '头像地址',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_user_uid` (`uid`),
  UNIQUE KEY `uk_user_username` (`username`),
  UNIQUE KEY `uk_user_phone` (`phone`),
  UNIQUE KEY `uk_user_email` (`email`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 ROW_FORMAT=DYNAMIC COMMENT='用户表（App 账号）';

-- 5. 用户设备关联表：user_device
CREATE TABLE IF NOT EXISTS `user_device` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `user_id` bigint NOT NULL COMMENT 'user.id',
  `device_id` varchar(50) NOT NULL COMMENT 'device.id',
  `role` varchar(20) DEFAULT 'owner' COMMENT '角色 owner/share 等',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '绑定时间',
  PRIMARY KEY (`id`),
  KEY `idx_ud_user` (`user_id`),
  KEY `idx_ud_device` (`device_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 ROW_FORMAT=DYNAMIC COMMENT='用户-设备关联表';

-- 6. 用户第三方登录表：user_auth
CREATE TABLE IF NOT EXISTS `user_auth` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `user_id` bigint NOT NULL COMMENT 'user.id',
  `auth_type` varchar(20) NOT NULL COMMENT '登录类型 wechat/apple/google/sms',
  `open_id` varchar(128) NOT NULL COMMENT '第三方唯一用户标识，如 openid/sub',
  `union_id` varchar(128) DEFAULT NULL COMMENT '微信 unionid 等',
  `extra_info` text COMMENT '第三方返回的原始信息 JSON',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`),
  KEY `idx_ua_user` (`user_id`),
  KEY `idx_ua_type` (`auth_type`),
  UNIQUE KEY `uk_ua_type_open` (`auth_type`,`open_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 ROW_FORMAT=DYNAMIC COMMENT='用户第三方登录表';

-- 7. 用户 Token 表：user_token
CREATE TABLE IF NOT EXISTS `user_token` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `user_id` bigint NOT NULL COMMENT 'user.id',
  `access_token` varchar(255) NOT NULL COMMENT '访问 token',
  `refresh_token` varchar(255) NOT NULL COMMENT '刷新 token',
  `expires_at` datetime NOT NULL COMMENT 'access_token 过期时间',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  KEY `idx_ut_user` (`user_id`),
  UNIQUE KEY `uk_ut_refresh` (`refresh_token`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 ROW_FORMAT=DYNAMIC COMMENT='用户 Token 表';

-- 8. 设备状态历史表：device_status_history
CREATE TABLE IF NOT EXISTS `device_status_history` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `device_id` varchar(50) NOT NULL COMMENT '设备ID',
  `status` tinyint(1) NOT NULL COMMENT '设备状态 0-离线 1-在线',
  `reason` varchar(100) DEFAULT NULL COMMENT '变化原因',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '记录时间',
  PRIMARY KEY (`id`),
  KEY `idx_dsh_device` (`device_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 ROW_FORMAT=DYNAMIC COMMENT='设备状态历史表';

-- 9. WebRTC 会话表：webrtc_session
CREATE TABLE IF NOT EXISTS `webrtc_session` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `device_id` varchar(50) NOT NULL COMMENT '设备ID',
  `sid` varchar(100) NOT NULL COMMENT '会话ID/Peer ID',
  `client_id` varchar(100) DEFAULT NULL COMMENT '客户端标识',
  `status` varchar(20) DEFAULT NULL COMMENT '会话状态',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  KEY `idx_ws_device` (`device_id`),
  KEY `idx_ws_sid` (`sid`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 ROW_FORMAT=DYNAMIC COMMENT='WebRTC 会话表';

-- 10. 设备设置表：device_settings
CREATE TABLE IF NOT EXISTS `device_settings` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `device_id` varchar(50) NOT NULL COMMENT '设备ID',
  `motion_detection` tinyint(1) NOT NULL DEFAULT 0 COMMENT '移动侦测开关',
  `night_vision` tinyint(1) NOT NULL DEFAULT 0 COMMENT '夜视开关',
  `audio_enabled` tinyint(1) NOT NULL DEFAULT 1 COMMENT '拾音开关',
  `flip_image` tinyint(1) NOT NULL DEFAULT 0 COMMENT '画面翻转',
  `sensitivity` varchar(16) DEFAULT 'medium' COMMENT '灵敏度 low/medium/high',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_ds_device` (`device_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 ROW_FORMAT=DYNAMIC COMMENT='设备设置表';

-- 11. 本地录像表：local_video
CREATE TABLE IF NOT EXISTS `local_video` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `video_id` varchar(64) NOT NULL COMMENT '业务录像ID',
  `device_id` varchar(50) NOT NULL COMMENT '设备ID',
  `type` varchar(20) NOT NULL COMMENT 'alarm/manual/scheduled/continuous',
  `title` varchar(128) DEFAULT NULL COMMENT '标题',
  `thumbnail` varchar(255) DEFAULT NULL COMMENT '缩略图URL',
  `video_url` varchar(255) DEFAULT NULL COMMENT '视频URL',
  `duration` int DEFAULT NULL COMMENT '时长（秒）',
  `size` bigint DEFAULT NULL COMMENT '大小（字节）',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_lv_video` (`video_id`),
  KEY `idx_lv_device` (`device_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 ROW_FORMAT=DYNAMIC COMMENT='本地录像表';

-- 12. 设备录制会话表：device_record
CREATE TABLE IF NOT EXISTS `device_record` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `record_id` varchar(64) NOT NULL COMMENT '业务录制ID',
  `device_id` varchar(50) NOT NULL COMMENT '设备ID',
  `status` varchar(20) NOT NULL DEFAULT 'recording' COMMENT 'recording/stopped',
  `video_url` varchar(255) DEFAULT NULL COMMENT '录制完成后的视频URL',
  `duration` int DEFAULT NULL COMMENT '时长（秒）',
  `size` bigint DEFAULT NULL COMMENT '大小（字节）',
  `started_at` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '开始时间',
  `ended_at` datetime DEFAULT NULL COMMENT '结束时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_dr_record` (`record_id`),
  KEY `idx_dr_device` (`device_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 ROW_FORMAT=DYNAMIC COMMENT='设备录制会话表';

-- 13. App 消息表：app_message
CREATE TABLE IF NOT EXISTS `app_message` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `user_id` bigint DEFAULT NULL COMMENT '用户ID',
  `device_id` varchar(50) DEFAULT NULL COMMENT '设备ID',
  `type` varchar(20) DEFAULT NULL COMMENT 'alarm/system/promotion',
  `title` varchar(200) DEFAULT NULL COMMENT '标题',
  `content` text COMMENT '内容',
  `thumbnail_url` varchar(255) DEFAULT NULL COMMENT '缩略图URL',
  `video_url` varchar(255) DEFAULT NULL COMMENT '关联视频URL',
  `is_read` tinyint(1) NOT NULL DEFAULT 0 COMMENT '是否已读',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`),
  KEY `idx_am_user` (`user_id`),
  KEY `idx_am_device` (`device_id`),
  KEY `idx_am_type` (`type`),
  KEY `idx_am_read` (`is_read`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 ROW_FORMAT=DYNAMIC COMMENT='App 消息表';

-- 14. 云存储套餐表：cloud_plan
CREATE TABLE IF NOT EXISTS `cloud_plan` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `plan_id` varchar(64) NOT NULL COMMENT '业务套餐ID',
  `name` varchar(64) NOT NULL COMMENT '名称',
  `description` varchar(255) DEFAULT NULL COMMENT '描述',
  `storage_days` int DEFAULT NULL COMMENT '存储天数',
  `price` decimal(10,2) DEFAULT NULL COMMENT '当前价格',
  `original_price` decimal(10,2) DEFAULT NULL COMMENT '原价',
  `period` varchar(20) DEFAULT NULL COMMENT '周期 month/year',
  `features` text COMMENT '特性JSON',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_cp_plan` (`plan_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 ROW_FORMAT=DYNAMIC COMMENT='云存储套餐表';

-- 15. 云存储订阅表：cloud_subscription
CREATE TABLE IF NOT EXISTS `cloud_subscription` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `user_id` bigint NOT NULL COMMENT '用户ID',
  `device_id` varchar(50) NOT NULL COMMENT '设备ID',
  `plan_id` varchar(64) NOT NULL COMMENT '套餐ID',
  `plan_name` varchar(64) DEFAULT NULL COMMENT '套餐名称',
  `expire_at` datetime DEFAULT NULL COMMENT '到期时间',
  `auto_renew` tinyint(1) NOT NULL DEFAULT 0 COMMENT '自动续费',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  KEY `idx_cs_user` (`user_id`),
  KEY `idx_cs_device` (`device_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 ROW_FORMAT=DYNAMIC COMMENT='云存储订阅表';

-- 16. 云录像表：cloud_video
CREATE TABLE IF NOT EXISTS `cloud_video` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `video_id` varchar(64) NOT NULL COMMENT '业务视频ID',
  `device_id` varchar(50) NOT NULL COMMENT '设备ID',
  `type` varchar(20) DEFAULT NULL COMMENT 'alarm/manual/scheduled',
  `title` varchar(128) DEFAULT NULL COMMENT '标题',
  `thumbnail` varchar(255) DEFAULT NULL COMMENT '缩略图URL',
  `video_url` varchar(255) DEFAULT NULL COMMENT '视频URL',
  `duration` int DEFAULT NULL COMMENT '时长（秒）',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_cv_video` (`video_id`),
  KEY `idx_cv_device` (`device_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 ROW_FORMAT=DYNAMIC COMMENT='云录像表';

-- 17. 支付订单表：payment_order
CREATE TABLE IF NOT EXISTS `payment_order` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `order_id` varchar(64) NOT NULL COMMENT '业务订单ID',
  `user_id` bigint NOT NULL COMMENT '用户ID',
  `device_id` varchar(50) DEFAULT NULL COMMENT '设备ID',
  `product_type` varchar(32) DEFAULT NULL COMMENT 'cloud_storage 等',
  `product_id` varchar(64) DEFAULT NULL COMMENT '对应套餐ID等',
  `amount` decimal(10,2) DEFAULT NULL COMMENT '金额',
  `currency` varchar(8) DEFAULT 'CNY' COMMENT '币种',
  `status` varchar(20) DEFAULT 'pending' COMMENT '状态',
  `payment_method` varchar(32) DEFAULT NULL COMMENT '支付方式 wechat/paypal/apple',
  `third_order_id` varchar(128) DEFAULT NULL COMMENT '三方订单号',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `paid_at` datetime DEFAULT NULL COMMENT '支付时间',
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_po_order` (`order_id`),
  KEY `idx_po_user` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 ROW_FORMAT=DYNAMIC COMMENT='支付订单表';

-- 18. 微信支付扩展表：payment_wechat
CREATE TABLE IF NOT EXISTS `payment_wechat` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `order_id` varchar(64) NOT NULL COMMENT '业务订单ID',
  `prepay_id` varchar(128) DEFAULT NULL COMMENT '预支付ID',
  `raw_response` text COMMENT '微信返回的原始内容',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`),
  KEY `idx_pw_order` (`order_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 ROW_FORMAT=DYNAMIC COMMENT='微信支付扩展表';

-- 19. 直播流表：live_stream
CREATE TABLE IF NOT EXISTS `live_stream` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `stream_id` varchar(64) NOT NULL COMMENT '业务流ID',
  `device_id` varchar(50) NOT NULL COMMENT '设备ID',
  `user_id` bigint NOT NULL COMMENT '用户ID',
  `protocol` varchar(16) DEFAULT NULL COMMENT 'webrtc/hls/rtmp',
  `quality` varchar(16) DEFAULT NULL COMMENT 'sd/hd/fhd',
  `signaling_url` varchar(255) DEFAULT NULL COMMENT '信令地址',
  `ice_servers` text COMMENT 'ICE 服务器 JSON',
  `expires_at` datetime DEFAULT NULL COMMENT '过期时间',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_ls_stream` (`stream_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 ROW_FORMAT=DYNAMIC COMMENT='直播流表';

-- 20. WiFi 历史表：wifi_history
CREATE TABLE IF NOT EXISTS `wifi_history` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `user_id` bigint NOT NULL COMMENT '用户ID',
  `ssid` varchar(64) NOT NULL COMMENT 'WiFi SSID',
  `signal` int DEFAULT NULL COMMENT '信号强度',
  `security` varchar(32) DEFAULT NULL COMMENT '加密方式',
  `is_connected` tinyint(1) NOT NULL DEFAULT 0 COMMENT '是否当前连接',
  `last_used_at` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '最后使用时间',
  PRIMARY KEY (`id`),
  KEY `idx_wh_user` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 ROW_FORMAT=DYNAMIC COMMENT='WiFi 历史表';

-- 21. 设备绑定表：device_binding
CREATE TABLE IF NOT EXISTS `device_binding` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `device_id` varchar(50) NOT NULL COMMENT '设备ID',
  `device_sn` varchar(64) DEFAULT NULL COMMENT '设备序列号',
  `user_id` bigint NOT NULL COMMENT '用户ID',
  `wifi_ssid` varchar(64) DEFAULT NULL COMMENT 'WiFi 名称',
  `wifi_password` varchar(128) DEFAULT NULL COMMENT 'WiFi 密码',
  `status` varchar(20) DEFAULT 'binding' COMMENT 'binding/success/failed',
  `progress` int DEFAULT 0 COMMENT '绑定进度 0-100',
  `message` varchar(255) DEFAULT NULL COMMENT '提示信息',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  KEY `idx_db_device` (`device_id`),
  KEY `idx_db_user` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 ROW_FORMAT=DYNAMIC COMMENT='设备绑定表';

-- 22. App 版本表：app_version
CREATE TABLE IF NOT EXISTS `app_version` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `platform` varchar(16) NOT NULL COMMENT 'ios/android',
  `latest_version` varchar(16) NOT NULL COMMENT '最新版本',
  `min_version` varchar(16) DEFAULT NULL COMMENT '最小支持版本',
  `download_url` varchar(255) DEFAULT NULL COMMENT '下载地址',
  `release_notes` text COMMENT '更新说明',
  `force_update` tinyint(1) NOT NULL DEFAULT 0 COMMENT '是否强制更新',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 ROW_FORMAT=DYNAMIC COMMENT='App 版本表';

-- 23. 用户反馈表：feedback
CREATE TABLE IF NOT EXISTS `feedback` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `feedback_id` varchar(64) NOT NULL COMMENT '业务反馈ID',
  `user_id` bigint NOT NULL COMMENT '用户ID',
  `content` text COMMENT '反馈内容',
  `contact` varchar(128) DEFAULT NULL COMMENT '联系方式',
  `images` text COMMENT '图片URL数组JSON',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_fb_feedback` (`feedback_id`),
  KEY `idx_fb_user` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 ROW_FORMAT=DYNAMIC COMMENT='用户反馈表';