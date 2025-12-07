# MQTT 和 WebRTC 实现文档

## 概述

本文档描述摄像头后端的 MQTT 通信和 WebRTC 信令实现。

---

## 架构设计

```
APP/Web客户端
    ↓ ↑ (HTTP)
后端服务器
    ↓ ↑ (MQTT)
摄像头设备
```

### 核心组件

1. **MqttEncryptService** - MQTT 消息加解密服务
   - 使用 AES-128-ECB 加密
   - 密钥：WiFi SSID 的 MD5 值（原始 16 字节）
   - 基于你测试通过的 `MqttEncryptUtil`

2. **MqttMessageService** - MQTT 消息收发服务
   - 连接 MQTT Broker
   - 订阅设备主题：`camera/pura365/+/device`
   - 发布到设备主题：`camera/pura365/{deviceId}/master`
   - 处理收到的消息（CODE 10-28）

3. **MqttControlController** - HTTP API 控制接口
   - 让 APP 通过 HTTP 控制摄像头
   - 后端将 HTTP 请求转换为 MQTT 消息

---

## MQTT 主题

| 主题 | 方向 | 说明 |
|------|------|------|
| `camera/pura365/{deviceId}/master` | 服务器→设备 | 服务器发送指令到摄像头 |
| `camera/pura365/{deviceId}/device` | 设备→服务器 | 摄像头上报状态和响应 |

---

## 消息加解密

### 加密流程

1. 将消息对象序列化为 JSON
2. 计算 WiFi SSID 的 MD5（原始 16 字节）作为 AES 密钥
3. 用空格将 JSON 字节补齐到 16 字节整数倍
4. 使用 AES-128-ECB/NoPadding 加密
5. 发送加密后的字节数组

### 解密流程

1. 使用相同的 SSID 计算 AES 密钥
2. AES-128-ECB/NoPadding 解密
3. 去除右侧空格
4. 解析 JSON

### 示例代码

```java
// 加密
byte[] encrypted = mqttEncryptService.encrypt(messageObject, "AOCCX");

// 解密
String json = mqttEncryptService.decrypt(encryptedBytes, "AOCCX");
MqttBaseMessage msg = objectMapper.readValue(json, MqttBaseMessage.class);
```

---

## 支持的MQTT指令（CODE）

### 设备上报（CODE + 128）

| CODE | 名称 | 说明 |
|------|------|------|
| 138 (10+128) | MQTT已连接 | 设备连接到MQTT后发送 |
| 139 (11+128) | 设备信息响应 | 响应服务器的设备信息请求 |
| 140 (12+128) | 格式化响应 | TF卡格式化结果 |
| 141 (13+128) | 重启响应 | 重启指令确认 |
| 147 (19+128) | 固件更新响应 | 固件更新状态 |
| 148 (20+128) | 遗言 | 设备离线前发送 |
| 151 (23+128) | WebRTC Offer | 响应Offer请求 |
| 152 (24+128) | WebRTC Answer确认 | Answer设置成功 |
| 153 (25+128) | WebRTC Candidate确认 | Candidate设置成功 |
| 154 (26+128) | 旋转设置响应 | 画面旋转设置结果 |
| 156 (28+128) | 白光灯响应 | 白光灯设置结果 |

### 服务器下发指令（CODE）

| CODE | 名称 | 说明 |
|------|------|------|
| 11 | 请求设备信息 | 获取设备状态、WiFi、TF卡等信息 |
| 12 | 格式化TF卡 | 格式化存储卡 |
| 13 | 重启摄像头 | 重启设备 |
| 19 | 固件更新 | 推送固件更新 |
| 23 | 请求WebRTC Offer | 请求设备生成Offer |
| 24 | 发送WebRTC Answer | 发送Answer给设备 |
| 25 | 发送WebRTC Candidate | 发送ICE Candidate |
| 26 | 设置画面旋转 | 0:不旋转 1:旋转180度 |
| 28 | 设置白光灯 | 0:禁用 1:启用 |

---

## HTTP API 接口

### 1. 注册设备SSID（用于加解密）

```http
POST /api/mqtt/device/{deviceId}/register-ssid
```

**参数**:
- `deviceId`: 设备序列号
- `ssid`: WiFi SSID

**示例**:
```bash
curl -X POST "http://localhost:8080/api/mqtt/device/abc789/register-ssid?ssid=AOCCX"
```

**注意**: 实际生产环境应该从数据库获取设备的SSID，这个接口仅用于测试。

---

### 2. 请求设备信息（CODE 11）

```http
POST /api/mqtt/device/{deviceId}/info
```

**参数**:
- `deviceId`: 设备序列号
- `ssid` (可选): WiFi SSID

**响应**:
```json
{
  "success": true,
  "message": "已发送请求"
}
```

**设备响应** (CODE 139):
```json
{
  "code": 139,
  "uid": "abc789",
  "time": 1732517698,
  "wifiname": "AOCCX",
  "wifirssi": -45,
  "ver": "v1.0.0",
  "sdstate": 1,
  "sdcap": 31457280,
  "sdblock": 512,
  "sdfree": 20000000,
  "rotate": 0,
  "lightled": 0,
  "whiteled": 1
}
```

---

### 3. 格式化TF卡（CODE 12）

```http
POST /api/mqtt/device/{deviceId}/format
```

---

### 4. 重启摄像头（CODE 13）

```http
POST /api/mqtt/device/{deviceId}/reboot
```

---

### 5. 设置画面旋转（CODE 26）

```http
POST /api/mqtt/device/{deviceId}/rotate
```

**参数**:
- `enable`: 0=不旋转, 1=旋转180度

**示例**:
```bash
curl -X POST "http://localhost:8080/api/mqtt/device/abc789/rotate?enable=1"
```

---

### 6. 设置白光灯（CODE 28）

```http
POST /api/mqtt/device/{deviceId}/whiteled
```

**参数**:
- `enable`: 0=禁用, 1=启用

---

## WebRTC 信令流程

### 1. APP 请求 WebRTC Offer

```http
POST /api/mqtt/device/{deviceId}/webrtc/offer
```

**参数**:
- `sid`: 会话ID（Peer ID）
- `rtcServer`: WebRTC服务器信息，格式：`server,user,pass`

**示例**:
```bash
curl -X POST "http://localhost:8080/api/mqtt/device/abc789/webrtc/offer" \
  -d "sid=1732517698" \
  -d "rtcServer=stun.example.com,username,password"
```

**后端发送MQTT消息** (CODE 23):
```json
{
  "code": 23,
  "time": 1732517698,
  "sid": "1732517698",
  "rtc": "stun.example.com,username,password"
}
```

**设备响应** (CODE 151):
```json
{
  "code": 151,
  "uid": "abc789",
  "time": 1732517700,
  "sid": "1732517698",
  "sdp": "v=0\r\no=...",
  "status": 1
}
```

---

### 2. APP 发送 Answer

```http
POST /api/mqtt/device/{deviceId}/webrtc/answer
```

**参数**:
- `sid`: 会话ID
- `sdp`: SDP Answer内容

**后端发送MQTT消息** (CODE 24):
```json
{
  "code": 24,
  "time": 1732517701,
  "sid": "1732517698",
  "sdp": "v=0\r\na=..."
}
```

---

### 3. APP 发送 ICE Candidate

```http
POST /api/mqtt/device/{deviceId}/webrtc/candidate
```

**参数**:
- `sid`: 会话ID
- `candidate`: ICE Candidate内容（仅IPv4）

**后端发送MQTT消息** (CODE 25):
```json
{
  "code": 25,
  "time": 1732517702,
  "sid": "1732517698",
  "candidate": "candidate:..."
}
```

---

## WebRTC DataChannel 控制指令

### 文档中的控制指令

通过 WebRTC DataChannel 发送字符串指令来控制摄像头：

| 指令 | 说明 |
|------|------|
| `left` | 控制云台左转 |
| `right` | 控制云台右转 |
| `up` | 控制云台上转 |
| `down` | 控制云台下转 |
| `mute` | 静音 |
| `unmute` | 解除静音 |
| `live` | 结束回放，切换回直播状态 |
| `replay 时间戳 通道` | 切换到TF卡回放 |
| `fileindex YYYY-mm-dd` | 获取某天的文件列表 |

### 实现方式

DataChannel 控制通常是在 APP 和摄像头之间直接通过 WebRTC 连接传输，**不经过后端服务器**。

如果需要通过后端转发，可以：
1. APP 将 DataChannel 指令发送到后端 HTTP 接口
2. 后端通过 MQTT 转发给摄像头
3. 摄像头执行指令

**示例API**（可选实现）:
```http
POST /api/mqtt/device/{deviceId}/datachannel
```

**参数**:
- `command`: 指令字符串（如 `left`, `right`, `mute` 等）

---

## 配置说明

### application.properties

```properties
# MQTT Broker配置
mqtt.broker.url=tcp://cam.pura365.cn:1883
mqtt.client.id=camera-server
mqtt.username=
mqtt.password=
```

### MQTTS（加密连接）

如果使用 MQTTS（SSL/TLS），修改配置：

```properties
mqtt.broker.url=ssl://cam.pura365.cn:8883
```

并且需要配置证书（如果需要）。

---

## 测试流程

### 1. 启动服务

```bash
mvn clean package
java -jar target/camera-server-1.0.0.jar
```

### 2. 注册设备SSID

```bash
curl -X POST "http://localhost:8080/api/mqtt/device/test001/register-ssid?ssid=AOCCX"
```

### 3. 模拟摄像头发送消息

使用 MQTT 客户端工具（如 MQTTX）：

**连接配置**:
- Broker: `tcp://cam.pura365.cn:1883`
- Topic: `camera/pura365/test001/device`

**发送测试消息** (CODE 138):
1. 准备 JSON: `{"code":138,"uid":"test001","time":1732517698,"status":0}`
2. 使用 `MqttEncryptUtil` 加密（SSID=AOCCX）
3. 以十六进制格式发送加密后的字节

### 4. 通过API控制摄像头

```bash
# 请求设备信息
curl -X POST "http://localhost:8080/api/mqtt/device/test001/info"

# 重启设备
curl -X POST "http://localhost:8080/api/mqtt/device/test001/reboot"

# 设置旋转
curl -X POST "http://localhost:8080/api/mqtt/device/test001/rotate?enable=1"
```

### 5. 查看日志

```bash
tail -f logs/camera-server.log
```

关键日志：
- `已连接到MQTT Broker`
- `收到MQTT消息`
- `解密后的消息`
- `处理设备 xxx 的消息，CODE: xxx`

---

## 注意事项

1. **SSID管理**: 当前 SSID 存储在内存中，生产环境应该从数据库获取
2. **消息加密**: 每个设备可能有不同的 WiFi SSID，需要单独管理
3. **消息时效性**: MQTT消息有5分钟有效期，超时会被忽略
4. **WebRTC信令**: Offer/Answer/Candidate 需要按顺序交换
5. **错误处理**: 加解密失败时会记录日志，不会中断服务
6. **连接管理**: MQTT 支持自动重连，连接丢失后会自动恢复

---

## TODO 清单

- [ ] 从数据库动态加载设备SSID
- [ ] 实现 WebSocket 服务用于实时推送设备消息给 APP
- [ ] 实现完整的 WebRTC 信令服务器（转发 Offer/Answer/Candidate）
- [ ] 添加设备在线状态管理
- [ ] 实现设备消息持久化（存储到数据库）
- [ ] 添加 MQTT 消息队列（处理消息积压）
- [ ] 实现设备分组和批量控制
- [ ] 添加 MQTT 消息加密密钥的动态配置
- [ ] 实现 DataChannel 指令转发接口
- [ ] 添加 WebRTC TURN 服务器配置

---

## 扩展阅读

- [Eclipse Paho MQTT 文档](https://www.eclipse.org/paho/)
- [WebRTC 规范](https://webrtc.org/)
- [AES 加密算法](https://en.wikipedia.org/wiki/Advanced_Encryption_Standard)
