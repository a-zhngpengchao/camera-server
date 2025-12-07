-- 摄像头测试数据
-- 在 camera_app 数据库下执行

SET NAMES utf8mb4;

-- 插入测试设备数据
INSERT INTO `device` (`id`, `mac`, `ssid`, `region`, `name`, `firmware_version`, `status`, `enabled`, `cloud_storage`, `mqtt_hostname`, `ai_enabled`, `last_online_time`, `created_at`) VALUES
-- 在线设备
('B120000000000001', 'AA:BB:CC:DD:EE:01', 'Home_WiFi', 'cn', '客厅摄像头', 'v1.2.0', 1, 1, 1, 'tcp://cam.pura365.cn:1883', 1, NOW(), NOW()),
('B120000000000002', 'AA:BB:CC:DD:EE:02', 'Home_WiFi', 'cn', '卧室摄像头', 'v1.2.0', 1, 1, 0, 'tcp://cam.pura365.cn:1883', 0, NOW(), NOW()),
('B120000000000003', 'AA:BB:CC:DD:EE:03', 'Office_5G', 'cn', '门口监控', 'v1.1.5', 1, 1, 1, 'tcp://cam.pura365.cn:1883', 1, NOW(), NOW()),
('A110000100000001', 'AA:BB:CC:DD:EE:04', 'Company_WiFi', 'cn', '会议室摄像头', 'v1.2.0', 1, 1, 0, 'tcp://cam.pura365.cn:1883', 0, NOW(), NOW()),
('B230000000000001', 'AA:BB:CC:DD:EE:05', 'Home_WiFi', 'cn', '车库双目摄像头', 'v1.3.0', 1, 1, 1, 'tcp://cam.pura365.cn:1883', 1, NOW(), NOW()),

-- 离线设备
('B120000000000004', 'AA:BB:CC:DD:EE:06', 'Guest_WiFi', 'cn', '阳台摄像头', 'v1.0.8', 0, 1, 0, 'tcp://cam.pura365.cn:1883', 0, DATE_SUB(NOW(), INTERVAL 2 DAY), NOW()),
('A110000100000002', 'AA:BB:CC:DD:EE:07', 'Home_WiFi', 'cn', '儿童房摄像头', 'v1.1.0', 0, 1, 0, 'tcp://cam.pura365.cn:1883', 0, DATE_SUB(NOW(), INTERVAL 5 DAY), NOW()),
('C110000000000001', 'AA:BB:CC:DD:EE:08', NULL, 'cn', '户外4G摄像头', 'v1.2.0', 0, 1, 1, 'tcp://cam.pura365.cn:1883', 1, DATE_SUB(NOW(), INTERVAL 1 DAY), NOW()),

-- 禁用设备
('B120000000000005', 'AA:BB:CC:DD:EE:09', 'Old_WiFi', 'cn', '旧仓库摄像头', 'v0.9.5', 0, 0, 0, NULL, 0, DATE_SUB(NOW(), INTERVAL 30 DAY), NOW()),

-- 海外区域设备
('B120000100000001', 'AA:BB:CC:DD:EE:10', 'US_Home', 'us', 'Living Room Cam', 'v1.2.0', 1, 1, 1, 'tcp://cam-us.pura365.cn:1883', 1, NOW(), NOW()),
('B120000100000002', 'AA:BB:CC:DD:EE:11', 'US_Office', 'us', 'Office Camera', 'v1.2.0', 0, 1, 0, 'tcp://cam-us.pura365.cn:1883', 0, DATE_SUB(NOW(), INTERVAL 3 HOUR), NOW()),
('R110000000000001', 'AA:BB:CC:DD:EE:12', NULL, 'cn', '机房有线摄像头', 'v1.2.0', 1, 1, 1, 'tcp://cam.pura365.cn:1883', 0, NOW(), NOW())

ON DUPLICATE KEY UPDATE
  `name` = VALUES(`name`),
  `status` = VALUES(`status`),
  `last_online_time` = VALUES(`last_online_time`);

-- 插入对应的设备设置
INSERT INTO `device_settings` (`device_id`, `motion_detection`, `night_vision`, `audio_enabled`, `flip_image`, `sensitivity`) VALUES
('B120000000000001', 1, 1, 1, 0, 'high'),
('B120000000000002', 1, 0, 1, 0, 'medium'),
('B120000000000003', 1, 1, 1, 0, 'high'),
('A110000100000001', 0, 0, 1, 0, 'low'),
('B230000000000001', 1, 1, 1, 0, 'high'),
('B120000000000004', 1, 0, 1, 1, 'medium'),
('A110000100000002', 1, 1, 1, 0, 'medium'),
('C110000000000001', 1, 1, 0, 0, 'high'),
('B120000000000005', 0, 0, 0, 0, 'low'),
('B120000100000001', 1, 1, 1, 0, 'medium'),
('B120000100000002', 1, 0, 1, 0, 'medium'),
('R110000000000001', 1, 0, 1, 0, 'high')
ON DUPLICATE KEY UPDATE
  `motion_detection` = VALUES(`motion_detection`),
  `night_vision` = VALUES(`night_vision`);

-- 查看插入结果
SELECT id AS device_id, name, status, enabled, firmware_version, region, last_online_time FROM device ORDER BY created_at DESC;
