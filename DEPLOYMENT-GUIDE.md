# 部署和使用指南

## 一、环境准备

### 1.1 软件要求

- JDK 8 或以上
- Maven 3.6+
- MySQL 8.0+
- MQTT Broker（如 EMQ X 或 Mosquitto）

### 1.2 数据库初始化

```bash
# 1. 登录 MySQL
mysql -u root -p

# 2. 创建数据库并初始化表结构
source D:/workspace/camera-server/src/main/resources/db/init.sql
```

### 1.3 配置文件

修改 `src/main/resources/application.properties`，配置数据库和MQTT连接信息：

```properties
# MySQL配置
spring.datasource.url=jdbc:mysql://localhost:3306/camera_db?useUnicode=true&characterEncoding=utf8&useSSL=false&serverTimezone=Asia/Shanghai
spring.datasource.username=root
spring.datasource.password=your_password  # 修改为实际密码

# MQTT配置
mqtt.broker.url=tcp://your-mqtt-broker:1883  # 修改为实际MQTT地址
mqtt.client.id=camera-backend-server
mqtt.username=your_username  # 修改为实际用户名
mqtt.password=your_password  # 修改为实际密码
```

---

## 二、编译和运行

### 2.1 编译项目

```bash
mvn clean package
```

### 2.2 运行项目

```bash
# 方式1：使用Maven
mvn spring-boot:run

# 方式2：运行JAR包
java -jar target/camera-server-0.0.1-SNAPSHOT.jar
```

项目启动后，默认监听端口：**8080**

---

## 三、接口使用示例

### 3.1 配网接口

#### 提交配网信息
APP在完成设备配网后调用，保存配网信息到数据库：

```bash
curl -X POST "http://localhost:8080/api/network/config" \
  -H "Content-Type: application/json" \
  -d '{
    "deviceId": "abc789",
    "ssid": "MyWiFi",
    "password": "password123",
    "timezone": "+8",
    "region": "cn",
    "configMethod": "qrcode",
    "configSource": "APP-v1.0.0"
  }'
```

响应：
```json
{
  "success": true,
  "message": "配网信息已保存",
  "deviceId": "abc789"
}
```

#### 更新配网状态
设备配网成功后，更新状态：

```bash
# status: 0-配网中 1-成功 2-失败
curl -X POST "http://localhost:8080/api/network/config/abc789/status?status=1"
```

#### 查询配网历史
```bash
curl "http://localhost:8080/api/network/config/abc789/history"
```

---

### 3.2 HTTP设备接口

#### 获取服务器时间
```bash
curl -X POST "http://localhost:8080/api/camera/get_time" \
  -H "Content-Type: application/json" \
  -d '{"token": "eyJhbGciOi..."}'
```

#### 获取设备信息
```bash
curl -X POST "http://localhost:8080/api/camera/get_info" \
  -H "Content-Type: application/json" \
  -d '{
    "token": "eyJhbGciOi...",
    "ssid": "MyWiFi"
  }'
```

#### 重置设备
```bash
curl -X POST "http://localhost:8080/api/camera/reset_device" \
  -H "Content-Type: application/json" \
  -d '{
    "token": "eyJhbGciOi...",
    "deviceId": "abc789",
    "mac": "AA:BB:CC:DD:EE:FF",
    "ssid": "MyWiFi"
  }'
```

#### 发送消息到设备
```bash
curl -X POST "http://localhost:8080/api/camera/send_msg" \
  -H "Content-Type: application/json" \
  -d '{
    "token": "eyJhbGciOi...",
    "msg": "hello"
  }'
```

---

### 3.3 MQTT控制接口

#### 获取设备信息
```bash
curl -X POST "http://localhost:8080/api/mqtt/device/abc789/info?ssid=MyWiFi"
```

#### 格式化SD卡
```bash
curl -X POST "http://localhost:8080/api/mqtt/device/abc789/format?ssid=MyWiFi"
```

#### 重启设备
```bash
curl -X POST "http://localhost:8080/api/mqtt/device/abc789/reboot?ssid=MyWiFi"
```

#### 旋转画面
```bash
# angle: 0, 90, 180, 270
curl -X POST "http://localhost:8080/api/mqtt/device/abc789/rotate?angle=180&ssid=MyWiFi"
```

#### 控制白光灯
```bash
# on=true 开灯，on=false 关灯
curl -X POST "http://localhost:8080/api/mqtt/device/abc789/white-led?on=true&ssid=MyWiFi"
```

#### 发送WebRTC Offer
```bash
curl -X POST "http://localhost:8080/api/mqtt/device/abc789/webrtc/offer" \
  -H "Content-Type: application/json" \
  -d '{
    "ssid": "MyWiFi",
    "sdp": "v=0\r\no=- ...",
    "channel": 0
  }'
```

#### 发送WebRTC Answer
```bash
curl -X POST "http://localhost:8080/api/mqtt/device/abc789/webrtc/answer" \
  -H "Content-Type: application/json" \
  -d '{
    "ssid": "MyWiFi",
    "sdp": "v=0\r\na=..."
  }'
```

#### 发送ICE Candidate
```bash
curl -X POST "http://localhost:8080/api/mqtt/device/abc789/webrtc/candidate" \
  -H "Content-Type: application/json" \
  -d '{
    "ssid": "MyWiFi",
    "candidate": "candidate:...",
    "sdpMid": "0",
    "sdpMLineIndex": 0
  }'
```

---

### 3.4 WebRTC DataChannel 控制接口

#### 云台控制
```bash
# 左转
curl -X POST "http://localhost:8080/api/datachannel/device/abc789/ptz/left?ssid=MyWiFi"

# 右转
curl -X POST "http://localhost:8080/api/datachannel/device/abc789/ptz/right?ssid=MyWiFi"

# 向上
curl -X POST "http://localhost:8080/api/datachannel/device/abc789/ptz/up?ssid=MyWiFi"

# 向下
curl -X POST "http://localhost:8080/api/datachannel/device/abc789/ptz/down?ssid=MyWiFi"
```

#### 静音控制
```bash
# 静音
curl -X POST "http://localhost:8080/api/datachannel/device/abc789/mute?mute=true&ssid=MyWiFi"

# 取消静音
curl -X POST "http://localhost:8080/api/datachannel/device/abc789/mute?mute=false&ssid=MyWiFi"
```

#### 切换直播/回放
```bash
# 切换到直播
curl -X POST "http://localhost:8080/api/datachannel/device/abc789/live?ssid=MyWiFi"

# 切换到回放
curl -X POST "http://localhost:8080/api/datachannel/device/abc789/replay?timestamp=1732517698&channel=0&ssid=MyWiFi"
```

#### 获取文件列表
```bash
curl -X POST "http://localhost:8080/api/datachannel/device/abc789/fileindex?date=2025-11-25&ssid=MyWiFi"
```

#### 通用指令接口
```bash
curl -X POST "http://localhost:8080/api/datachannel/device/abc789/command?command=left&ssid=MyWiFi"
```

---

## 四、完整接口清单

### HTTP接口（端口8080）

| 接口 | 方法 | 描述 |
|------|------|------|
| `/api/camera/get_time` | POST | 获取服务器时间 |
| `/api/camera/get_info` | POST | 获取设备配置信息 |
| `/api/camera/reset_device` | POST | 重置设备（首次连接） |
| `/api/camera/send_msg` | POST | 发送消息到设备 |

### 配网接口

| 接口 | 方法 | 描述 |
|------|------|------|
| `/api/network/config` | POST | 提交配网信息 |
| `/api/network/config/{deviceId}/status` | POST | 更新配网状态 |
| `/api/network/config/{deviceId}/history` | GET | 查询配网历史 |

### MQTT控制接口

| 接口 | 方法 | 描述 |
|------|------|------|
| `/api/mqtt/device/{deviceId}/info` | POST | 获取设备信息（CODE 10） |
| `/api/mqtt/device/{deviceId}/format` | POST | 格式化SD卡（CODE 11） |
| `/api/mqtt/device/{deviceId}/reboot` | POST | 重启设备（CODE 12） |
| `/api/mqtt/device/{deviceId}/rotate` | POST | 旋转画面（CODE 13） |
| `/api/mqtt/device/{deviceId}/white-led` | POST | 控制白光灯（CODE 14） |
| `/api/mqtt/device/{deviceId}/register-ssid` | POST | 手动注册SSID |
| `/api/mqtt/device/{deviceId}/webrtc/offer` | POST | 发送WebRTC Offer |
| `/api/mqtt/device/{deviceId}/webrtc/answer` | POST | 发送WebRTC Answer |
| `/api/mqtt/device/{deviceId}/webrtc/candidate` | POST | 发送ICE Candidate |

### DataChannel控制接口

| 接口 | 方法 | 描述 |
|------|------|------|
| `/api/datachannel/device/{deviceId}/command` | POST | 发送通用控制指令 |
| `/api/datachannel/device/{deviceId}/ptz/{direction}` | POST | 云台控制（left/right/up/down） |
| `/api/datachannel/device/{deviceId}/mute` | POST | 静音控制 |
| `/api/datachannel/device/{deviceId}/live` | POST | 切换到直播 |
| `/api/datachannel/device/{deviceId}/replay` | POST | 切换到回放 |
| `/api/datachannel/device/{deviceId}/fileindex` | POST | 获取文件列表 |

---

## 五、MQTT主题订阅

系统自动订阅所有设备的消息主题：

- **订阅主题**：`camera/pura365/+/device`（所有设备的上行消息）
- **发布主题**：`camera/pura365/{deviceId}/master`（下行到特定设备）

---

## 六、数据库表说明

### 6.1 device（设备表）
- 存储设备基本信息、云存储配置、MQTT配置、AI配置
- 主键：设备序列号（id）

### 6.2 network_config（配网记录表）
- 存储设备配网信息（SSID、密码、时区、区域等）
- 用于追溯设备配网历史

### 6.3 device_message（设备消息表）
- 存储设备上报的事件、告警、AI消息
- 支持未读/已读状态管理

### 6.4 其他表
- user（用户表）
- user_device（用户-设备关联表）
- device_status_history（设备状态历史）
- webrtc_session（WebRTC会话记录）

---

## 七、重要说明

### 7.1 SSID获取策略

系统支持多种方式获取设备SSID（用于MQTT加密）：

1. **配网接口提交**：APP提交配网信息时自动保存
2. **HTTP接口提取**：从`get_info`和`reset_device`请求中提取
3. **数据库查询**：从device表自动查询
4. **手动注册**：通过`/api/mqtt/device/{deviceId}/register-ssid`手动注册

### 7.2 MQTT消息加密

所有MQTT消息使用 **AES-128-ECB** 加密：
- 密钥：WiFi SSID的MD5值（前16字节）
- 算法：AES/ECB/PKCS5Padding

### 7.3 Token格式

HTTP接口的token格式为：`payload.signature`
- payload：Base64编码的JSON
- signature：HMAC-SHA256签名
- 时间戳有效期：5分钟

---

## 八、故障排查

### 8.1 无法连接MQTT
- 检查MQTT Broker是否运行
- 确认配置文件中的MQTT地址、用户名、密码正确
- 查看日志：`MqttMessageService`相关日志

### 8.2 MQTT消息解密失败
- 确认设备SSID已正确保存
- 检查SSID是否包含特殊字符
- 确认设备和服务器使用相同的加密算法

### 8.3 数据库连接失败
- 确认MySQL服务已启动
- 检查数据库连接配置（URL、用户名、密码）
- 确认数据库和表已创建

---

## 九、开发和测试

### 9.1 查看日志
```bash
tail -f logs/spring.log
```

### 9.2 测试配网流程
```bash
# 1. 提交配网信息
curl -X POST "http://localhost:8080/api/network/config" \
  -H "Content-Type: application/json" \
  -d '{"deviceId":"test001","ssid":"TestWiFi","password":"12345678","region":"cn"}'

# 2. 更新配网状态为成功
curl -X POST "http://localhost:8080/api/network/config/test001/status?status=1"

# 3. 发送MQTT控制指令
curl -X POST "http://localhost:8080/api/mqtt/device/test001/info?ssid=TestWiFi"
```

### 9.3 直接操作数据库
```sql
-- 查看所有设备
SELECT * FROM device;

-- 查看配网记录
SELECT * FROM network_config ORDER BY created_at DESC;

-- 查看设备消息
SELECT * FROM device_message WHERE device_id = 'abc789' ORDER BY created_at DESC;
```

---

## 十、生产环境注意事项

1. **修改数据库密码**：不要使用默认的root/root
2. **配置HTTPS**：生产环境建议使用HTTPS
3. **配置MQTT SSL/TLS**：保障MQTT通信安全
4. **密码加密存储**：配网密码应加密后存储到数据库
5. **日志管理**：配置日志轮转，避免日志文件过大
6. **监控告警**：配置设备离线告警、异常消息告警

---

## 完成清单

✅ HTTP设备接口（4个）  
✅ MQTT控制接口（9个）  
✅ 配网接口（3个）  
✅ DataChannel控制接口（6个）  
✅ 数据库集成（7张表）  
✅ SSID自动管理  
✅ MQTT消息加解密  
✅ WebRTC信令转发  

**系统已完整实现！**
