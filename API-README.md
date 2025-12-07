# 摄像头后端接口文档

## 概述

本项目实现了摄像头设备与后端服务器的 HTTP 通信接口，包括时间同步、设备配置获取、设备重置和消息通知等功能。

## 接口列表

### 1. 获取时间戳 - `/get_time`

**功能说明**: 返回服务器当前 UTC 时间戳（秒）

**请求方式**: `POST`

**Content-Type**: `application/x-www-form-urlencoded`

**请求参数**: 无

**响应示例**:
```
1732517698
```

**使用场景**: 摄像头启动时同步服务器时间

---

### 2. 获取设备信息 - `/get_info`

**功能说明**: 设备每隔一个小时请求一次，获取设备的配置信息（MQTT连接、云存储、AI服务等）

**请求方式**: `POST`

**Content-Type**: `application/x-www-form-urlencoded`

**JWT 加密**: 使用自定义 JWT token（payload.signature 格式），JWT 密钥: `secret.ipcam.pura365.app`

**请求参数**（JWT payload 中的 JSON）:
```json
{
  "id": "设备序列号",
  "exp": 1732517698,
  "mac": "60:C2:2A:0A:BF:36",
  "region": "cn"
}
```

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| id | String | 是 | 摄像头序列号 |
| exp | Int64 | 是 | 有效时间戳（秒），用于验证请求是否在5分钟内有效 |
| mac | String | 是 | WiFi MAC地址，用于验证设备绑定 |
| region | String | 否 | 配网时提供的区域信息，用于判断服务器范围 |

**响应参数**:
```json
{
  "DeviceID": "abc789",
  "DeviceEnable": true,
  "CloudStorage": 1,
  "NormalAI": false,
  "S3Hostname": "s3.pura365.com",
  "S3Region": "us-east-1",
  "S3SecretKey": "your-secret-key",
  "S3AccessKey": "your-access-key",
  "MqttHostname": "mqtts://cam.pura365.cn:8883",
  "MqttPass": "123456",
  "MqttUser": "camera_test",
  "GPTHostname": "ai.pura365.com",
  "GPTKey": "gpt-access-key"
}
```

| 字段 | 类型 | 说明 |
|------|------|------|
| DeviceID | String | 摄像头序列号 |
| DeviceEnable | Boolean | 0: 禁用, 1: 启用 |
| CloudStorage | Int | 0: 未启用云存, 1: 连续存储, 2: 事件存储 |
| NormalAI | Boolean | 0: 禁用, 1: 启用 AI 功能 |
| S3Hostname | String | 云存储服务器地址 |
| S3Region | String | 云存储服务器区域 |
| S3SecretKey | String | 云存储服务器 SecretKey |
| S3AccessKey | String | 云存储服务器 AccessKey |
| MqttHostname | String | 完整 MQTT 地址，如 mqtts://xxxxx:xxx |
| MqttPass | String | MQTT 登录密码 |
| MqttUser | String | MQTT 登录用户名 |
| GPTHostname | String | AI 服务器地址 |
| GPTKey | String | AI 服务器访问 Key |

---

### 3. 重置设备 - `/reset_device`

**功能说明**: 摄像头第一次配网连接后请求该 API，用于清除历史数据，包括 APP 中该设备的分享信息、之前 APP 的已连接信息，以及云存储中的历史数据

**请求方式**: `POST`

**Content-Type**: `application/x-www-form-urlencoded`

**JWT 加密**: 使用自定义 JWT token（payload.signature 格式）

**请求参数**（JWT payload 中的 JSON）:
```json
{
  "id": "设备序列号",
  "exp": 1732517698,
  "mac": "60:C2:2A:0A:BF:36"
}
```

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| id | String | 是 | 摄像头序列号 |
| exp | Int64 | 是 | 有效时间戳（秒） |
| mac | String | 是 | WiFi MAC地址，验证该序列号已绑定的 MAC 地址 |

**响应参数**:
```json
{
  "code": 0
}
```

| 字段 | 类型 | 说明 |
|------|------|------|
| code | Int | 0: 设备复位成功, 其他: 失败 |

---

### 4. 发送消息通知 - `/send_msg`

**功能说明**: 通知信息发送，用于发送事件信息或 AI 结果，服务器收到后不需要回复内容

**请求方式**: `POST`

**Content-Type**: `application/x-www-form-urlencoded`

**JWT 加密**: 使用自定义 JWT token（payload.signature 格式）

**请求参数**（JWT payload 中的 JSON）:
```json
{
  "topic": "event/motion",
  "title": "检测到移动",
  "msg": "摄像头在 2024-11-25 14:30:00 检测到移动物体",
  "exp": 1732517698
}
```

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| topic | String | 是 | 通知的主题，用于分类 |
| title | String | 是 | 通知的标题 |
| msg | String | 是 | 通知的内容 |
| exp | Int64 | 是 | 请求时间戳，如时间戳不在认可范围（5分钟内），则忽略请求 |

**响应**: 
- HTTP 200 OK（无响应体）
- 即使处理失败也返回 200，避免摄像头重试

---

## JWT Token 格式说明

本项目使用自定义 JWT token 格式，与标准 JWT 略有不同：

### Token 结构
```
payload.signature
```
或
```
header.payload.signature
```

### Payload 编码
- Payload 部分为 Base64 编码的 JSON 数据
- 支持无填充的 Base64 字符串（服务器会自动补齐）

### 示例

**原始 JSON**:
```json
{
  "id": "abc789",
  "exp": 1732517698,
  "mac": "60:C2:2A:0A:BF:36"
}
```

**编码后的 Token**:
```
eyJpZCI6ImFiYzc4OSIsImV4cCI6MTczMjUxNzY5OCwibWFjIjoiNjA6QzI6MkE6MEE6QkY6MzYifQ.signature
```

---

## 测试工具接口

### 生成测试 Token - `/generate-test-token`

**功能说明**: 用于生成测试用的 JWT token

**请求方式**: `GET`

**请求参数**:
- `id`: 设备序列号
- `exp`: 时间戳
- `mac`: MAC地址
- `region`: 区域（可选）

**示例**:
```
GET /generate-test-token?id=abc789&exp=1732517698&mac=60:C2:2A:0A:BF:36&region=cn
```

**响应**: 生成的 JWT token 字符串

---

## 时间戳验证规则

- 所有接口（除 `/get_time` 外）都会验证请求时间戳
- 有效期: **5 分钟**（300秒）
- 验证逻辑: `|当前时间 - 请求时间| <= 300秒`
- 超时响应: 
  - `/get_info` 和 `/reset_device`: 返回 "Request expired" 错误
  - `/send_msg`: 静默忽略（返回 200 OK）

---

## 部署说明

### 本地开发
```bash
mvn clean package
java -jar target/camera-server-1.0.0.jar
```

应用将在 `http://localhost:8080` 启动

### Docker 部署
```bash
mvn clean package
docker-compose up -d
```

应用将在 `http://localhost:80` 启动

---

## 注意事项

1. **JWT 密钥**: 当前使用固定密钥 `secret.ipcam.pura365.app`，生产环境需要修改
2. **时间同步**: 确保摄像头和服务器时间同步，避免时间戳验证失败
3. **MAC 地址验证**: 目前未实现 MAC 地址与设备序列号的绑定验证，需要后续完善
4. **数据持久化**: 当前所有数据为内存模拟数据，需要接入数据库
5. **消息推送**: `/send_msg` 接口目前仅记录日志，未实现真实的消息推送功能

---

## TODO 清单

- [ ] 接入数据库（MySQL/PostgreSQL）
- [ ] 实现设备序列号与 MAC 地址绑定验证
- [ ] 实现真实的设备重置逻辑（清除分享、连接信息、云存储数据）
- [ ] 实现消息推送功能（APP推送、短信、邮件）
- [ ] 实现 MQTT 消息处理（CODE 10-28）
- [ ] 添加用户认证和权限管理
- [ ] 实现设备管理后台 API
- [ ] 配置多环境支持（dev/test/prod）
- [ ] 添加接口监控和日志审计
