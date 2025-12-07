# WiFi SSID 获取与管理方案

## 为什么需要 SSID？

MQTT 消息使用 **AES-128-ECB 加密**，密钥是 **WiFi SSID 的 MD5 值（原始 16 字节）**。

因此，后端需要知道每个设备连接的 WiFi SSID，才能正确加解密 MQTT 消息。

---

## 方案概览

### 核心流程

```
设备配网 → 设备上报SSID → 后端存储SSID → MQTT通信时使用SSID加解密
```

---

## 📋 实现方案

### 方案 1：从设备请求中自动提取（✅ 已实现）

**原理**：摄像头在调用 HTTP 接口时，在请求参数中携带 SSID

#### 1.1 从 `/get_info` 请求中提取

摄像头每小时调用一次 `/get_info`，请求参数中增加 `ssid` 字段：

**请求 JSON（在 JWT token 的 payload 中）**：
```json
{
  "id": "abc789",
  "exp": 1732517698,
  "mac": "60:C2:2A:0A:BF:36",
  "region": "cn",
  "ssid": "MyWiFi"
}
```

**后端处理**：
- 解析请求后，自动提取 `ssid` 字段
- 调用 `DeviceSsidService.saveSsid(deviceId, ssid)` 保存
- 日志输出：`已保存设备 abc789 的SSID`

**实现代码**（已完成）：
```java
// CameraController.java - getInfo 方法
if (info.getSsid() != null && !info.getSsid().isEmpty() && mqttMessageService != null) {
    mqttMessageService.registerDeviceSsid(info.getId(), info.getSsid());
    log.info("已保存设备 {} 的SSID", info.getId());
}
```

#### 1.2 从 `/reset_device` 请求中提取

摄像头第一次配网连接后调用 `/reset_device`，同样可以携带 `ssid`：

**请求 JSON**：
```json
{
  "id": "abc789",
  "exp": 1732517698,
  "mac": "60:C2:2A:0A:BF:36",
  "ssid": "MyWiFi"
}
```

**优点**：
- ✅ 无需修改摄像头固件太多，只需在 HTTP 请求的 JSON 中加一个字段
- ✅ 自动化，无需人工干预
- ✅ 实时更新，设备更换 WiFi 后会自动更新

---

### 方案 2：手动通过 API 注册（✅ 已实现，用于测试）

**API 接口**：
```http
POST /api/mqtt/device/{deviceId}/register-ssid?ssid={ssid}
```

**示例**：
```bash
curl -X POST "http://localhost:8080/api/mqtt/device/abc789/register-ssid?ssid=MyWiFi"
```

**响应**：
```json
{
  "success": true,
  "message": "SSID已注册"
}
```

**使用场景**：
- 测试环境：手动注册设备 SSID
- 管理后台：运维人员手动配置

**优点**：
- ✅ 灵活，可以随时修改
- ✅ 适合测试和调试

**缺点**：
- ❌ 需要人工操作
- ❌ 无法自动更新

---

### 方案 3：从数据库配置（🔜 待实现）

**设计思路**：

在数据库中创建设备表，包含 `ssid` 字段：

```sql
CREATE TABLE device (
    id VARCHAR(50) PRIMARY KEY,
    mac VARCHAR(20),
    ssid VARCHAR(32),
    region VARCHAR(10),
    created_at TIMESTAMP,
    updated_at TIMESTAMP
);
```

**数据来源**：
1. **配网时由 APP 写入**
   - APP 完成配网后，调用后端 API 创建设备记录
   - 包含设备 ID、MAC、SSID 等信息

2. **设备自己上报**
   - 设备首次调用 `/reset_device` 或 `/get_info` 时上报
   - 后端自动更新数据库

**实现步骤**：

1. 创建数据库表
2. 创建 `DeviceRepository`
3. 修改 `DeviceSsidService`，从数据库读取：

```java
public String getSsid(String deviceId) {
    // 先从缓存读取
    String ssid = ssidCache.get(deviceId);
    if (ssid != null) {
        return ssid;
    }
    
    // 缓存中没有，从数据库查询
    Device device = deviceRepository.findById(deviceId).orElse(null);
    if (device != null && device.getSsid() != null) {
        ssid = device.getSsid();
        ssidCache.put(deviceId, ssid); // 加入缓存
        return ssid;
    }
    
    return null; // 找不到，返回 null（使用默认 SSID）
}
```

**优点**：
- ✅ 持久化存储，服务重启不丢失
- ✅ 可以在管理后台统一管理
- ✅ 支持批量导入

**缺点**：
- ❌ 需要接入数据库
- ❌ 需要设计表结构

---

### 方案 4：从配网服务获取（🔮 可选方案）

**适用场景**：如果你有独立的配网服务

**流程**：
1. APP 调用配网服务完成设备配网
2. 配网服务记录 `deviceId → SSID` 的映射
3. 后端 MQTT 服务启动时，从配网服务批量拉取 SSID
4. 或者配网服务主动推送给后端

**优点**：
- ✅ 集中管理配网信息
- ✅ 后端和配网解耦

**缺点**：
- ❌ 增加系统复杂度
- ❌ 依赖配网服务

---

## 🔧 当前实现状态

### ✅ 已完成

1. **DeviceSsidService** - SSID 管理服务
   - 内存存储（支持并发）
   - 提供保存、获取、删除接口
   - 预留数据库接口

2. **自动提取 SSID**
   - 从 `/get_info` 请求自动提取
   - 从 `/reset_device` 请求自动提取

3. **手动注册接口**
   - `POST /api/mqtt/device/{deviceId}/register-ssid`

4. **日志记录**
   - 保存 SSID 时记录日志
   - 获取 SSID 时记录（如果需要）

### 🔜 待实现（TODO）

1. **数据库持久化**
   - 创建设备表
   - 实现 `DeviceRepository`
   - 修改 `DeviceSsidService` 读写数据库

2. **SSID 验证**
   - 检查 SSID 格式是否合法
   - SSID 长度限制（1-32 字节）

3. **管理后台接口**
   - 批量导入设备 SSID
   - 查询设备 SSID
   - 修改设备 SSID

4. **缓存优化**
   - 使用 Redis 做分布式缓存
   - 设置缓存过期时间

---

## 📝 使用指南

### 开发/测试环境

#### 1. 手动注册 SSID（推荐用于测试）

```bash
# 注册设备 test001 的 SSID
curl -X POST "http://localhost:8080/api/mqtt/device/test001/register-ssid?ssid=AOCCX"
```

**响应**：
```json
{
  "success": true,
  "message": "SSID已注册"
}
```

#### 2. 摄像头自动上报（实际设备）

摄像头在调用 `/get_info` 或 `/reset_device` 时，在 JSON 中增加 `ssid` 字段：

**摄像头端代码示例**（C/C++）：
```c
// 构造 JSON
cJSON *root = cJSON_CreateObject();
cJSON_AddStringToObject(root, "id", device_id);
cJSON_AddNumberToObject(root, "exp", current_timestamp);
cJSON_AddStringToObject(root, "mac", wifi_mac);
cJSON_AddStringToObject(root, "region", "cn");
cJSON_AddStringToObject(root, "ssid", wifi_ssid);  // 添加这一行

char *json_str = cJSON_PrintUnformatted(root);

// 使用 JWT 加密
char *token = jwt_encode(json_str, secret_key);

// 发送 HTTP POST 请求
http_post("/get_info", token);
```

#### 3. 验证 SSID 是否已保存

**查看日志**：
```bash
tail -f logs/camera-server.log | grep "已保存设备"
```

**日志输出**：
```
2025-11-25 14:30:00 INFO  已保存设备 test001 的SSID（长度: 5）
```

---

### 生产环境

#### 1. 接入数据库（推荐）

**步骤**：

1. 创建数据库表（见上方 SQL）
2. 添加数据库配置到 `application.properties`：

```properties
spring.datasource.url=jdbc:mysql://localhost:3306/camera_db
spring.datasource.username=root
spring.datasource.password=your_password
spring.jpa.hibernate.ddl-auto=update
```

3. 创建 `Device` 实体类
4. 创建 `DeviceRepository`
5. 修改 `DeviceSsidService` 的 TODO 部分

#### 2. 数据迁移

如果之前使用内存存储，需要迁移到数据库：

```java
@PostConstruct
public void migrateToDatabase() {
    // 将内存中的 SSID 批量写入数据库
    ssidCache.forEach((deviceId, ssid) -> {
        Device device = new Device();
        device.setId(deviceId);
        device.setSsid(ssid);
        deviceRepository.save(device);
    });
}
```

---

## ⚠️ 注意事项

### 1. SSID 的安全性

**问题**：SSID 是敏感信息吗？

- ❌ 不是高度敏感（SSID 本身是公开广播的）
- ✅ 但是建议加密存储（如果存数据库）
- ✅ HTTP 请求建议使用 HTTPS

### 2. SSID 更新

**场景**：设备更换 WiFi 怎么办？

- ✅ 设备会自动上报新的 SSID
- ✅ 后端会自动更新
- ✅ 旧的 MQTT 消息无法解密（这是正常的）

### 3. 默认 SSID

**场景**：如果后端查不到设备的 SSID？

**当前实现**：使用默认 SSID `AOCCX`

**位置**：`MqttEncryptService.java`
```java
private static final String DEFAULT_SSID = "AOCCX";
```

**建议**：
- 测试环境：使用默认 SSID
- 生产环境：必须要求设备上报真实 SSID

### 4. SSID 冲突

**问题**：两个设备连接同一个 WiFi（相同 SSID）？

- ✅ 没问题！SSID 是按设备存储的（`deviceId → SSID`）
- ✅ 同一个 WiFi 下的多个设备，每个设备都存储自己的 SSID

---

## 🧪 测试用例

### 测试1：手动注册SSID

```bash
# 1. 注册 SSID
curl -X POST "http://localhost:8080/api/mqtt/device/test001/register-ssid?ssid=TestWiFi"

# 2. 发送 MQTT 消息到设备
curl -X POST "http://localhost:8080/api/mqtt/device/test001/info"

# 3. 查看日志，确认使用了正确的 SSID
tail -f logs/camera-server.log
```

### 测试2：设备自动上报SSID

**摄像头发送请求**：
```json
{
  "id": "test002",
  "exp": 1732517698,
  "mac": "AA:BB:CC:DD:EE:FF",
  "ssid": "AutoReportedWiFi"
}
```

**后端日志**：
```
2025-11-25 14:30:00 INFO  解析出的请求参数: id=test002, ssid=AutoReportedWiFi
2025-11-25 14:30:00 INFO  已保存设备 test002 的SSID（长度: 16）
```

---

## 📊 总结

| 方案 | 实现难度 | 推荐度 | 适用场景 |
|------|---------|-------|---------|
| 从请求自动提取 | ⭐ 简单 | ⭐⭐⭐⭐⭐ | 生产环境（推荐） |
| 手动API注册 | ⭐ 简单 | ⭐⭐⭐ | 测试/调试 |
| 数据库配置 | ⭐⭐⭐ 中等 | ⭐⭐⭐⭐⭐ | 生产环境（持久化） |
| 从配网服务获取 | ⭐⭐⭐⭐ 复杂 | ⭐⭐ | 大型系统 |

**最佳实践**：
1. **测试环境**：使用手动 API 注册
2. **生产环境**：设备自动上报 + 数据库持久化
3. **兜底方案**：配置默认 SSID

---

## 🔗 相关文档

- [MQTT-WEBRTC-README.md](MQTT-WEBRTC-README.md) - MQTT 和 WebRTC 实现文档
- [API-README.md](API-README.md) - HTTP 接口文档
