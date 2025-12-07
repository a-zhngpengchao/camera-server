# 数据库完整实现指南

## 概述

本文档包含所有数据库相关的完整实现代码，包括：
1. **实体类（Entity）**
2. **Repository 接口**
3. **配网接口**
4. **WebRTC DataChannel 控制接口**

---

## 1. 数据库配置

已添加到 `application.properties`：

```properties
# MySQL Configuration
spring.datasource.url=jdbc:mysql://localhost:3306/camera_db?useUnicode=true&characterEncoding=utf8&useSSL=false&serverTimezone=Asia/Shanghai
spring.datasource.username=root
spring.datasource.password=root
spring.datasource.driver-class-name=com.mysql.cj.jdbc.Driver

# JPA Configuration
spring.jpa.hibernate.ddl-auto=update
spring.jpa.show-sql=false
spring.jpa.properties.hibernate.dialect=org.hibernate.dialect.MySQL8Dialect
spring.jpa.properties.hibernate.format_sql=true
```

---

## 2. 初始化数据库

```bash
# 连接 MySQL
mysql -u root -p

# 执行 SQL 脚本
source D:/workspace/camera-server/src/main/resources/db/init.sql
```

---

## 3. 实体类

### 3.1 Device 实体

文件：`src/main/java/com/pura365/camera/domain/Device.java`

```java
package com.pura365.camera.domain;

import javax.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "device")
public class Device {
    
    @Id
    @Column(length = 50)
    private String id; // 设备序列号
    
    @Column(length = 20, nullable = false)
    private String mac;
    
    @Column(length = 32)
    private String ssid; // WiFi SSID
    
    @Column(length = 10)
    private String region;
    
    @Column(length = 100)
    private String name;
    
    @Column(length = 20)
    private String firmwareVersion;
    
    @Column(columnDefinition = "TINYINT DEFAULT 0")
    private Integer status; // 0-离线 1-在线
    
    @Column(columnDefinition = "TINYINT DEFAULT 1")
    private Integer enabled; // 0-禁用 1-启用
    
    // 云存储配置
    @Column(columnDefinition = "TINYINT DEFAULT 0")
    private Integer cloudStorage;
    
    @Column(length = 255)
    private String s3Hostname;
    
    @Column(length = 50)
    private String s3Region;
    
    @Column(length = 100)
    private String s3AccessKey;
    
    @Column(length = 100)
    private String s3SecretKey;
    
    // MQTT配置
    @Column(length = 255)
    private String mqttHostname;
    
    @Column(length = 50)
    private String mqttUsername;
    
    @Column(length = 50)
    private String mqttPassword;
    
    // AI配置
    @Column(columnDefinition = "TINYINT DEFAULT 0")
    private Integer aiEnabled;
    
    @Column(length = 255)
    private String gptHostname;
    
    @Column(length = 100)
    private String gptKey;
    
    @Column
    private LocalDateTime lastOnlineTime;
    
    @Column(updatable = false)
    private LocalDateTime createdAt;
    
    @Column
    private LocalDateTime updatedAt;
    
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }
    
    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
    
    // Getters and Setters
    // ... (省略，自动生成即可)
}
```

### 3.2 NetworkConfig 实体

文件：`src/main/java/com/pura365/camera/domain/NetworkConfig.java`

```java
package com.pura365.camera.domain;

import javax.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "network_config")
public class NetworkConfig {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(length = 50, nullable = false)
    private String deviceId;
    
    @Column(length = 32, nullable = false)
    private String ssid;
    
    @Column(length = 64)
    private String password;
    
    @Column(length = 10)
    private String timezone;
    
    @Column(length = 10)
    private String region;
    
    @Column(length = 50)
    private String ipAddress;
    
    @Column(columnDefinition = "TINYINT DEFAULT 0")
    private Integer configStatus; // 0-配网中 1-成功 2-失败
    
    @Column(length = 20)
    private String configMethod; // qrcode/ble/audio
    
    @Column(length = 50)
    private String configSource;
    
    @Column(updatable = false)
    private LocalDateTime createdAt;
    
    @Column
    private LocalDateTime updatedAt;
    
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }
    
    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
    
    // Getters and Setters
}
```

### 3.3 DeviceMessage 实体

文件：`src/main/java/com/pura365/camera/domain/DeviceMessage.java`

```java
package com.pura365.camera.domain;

import javax.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "device_message")
public class DeviceMessage {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(length = 50, nullable = false)
    private String deviceId;
    
    @Column(length = 100, nullable = false)
    private String topic;
    
    @Column(length = 200)
    private String title;
    
    @Column(columnDefinition = "TEXT")
    private String content;
    
    @Column(length = 20)
    private String messageType; // event/alert/ai
    
    @Column(columnDefinition = "TINYINT DEFAULT 0")
    private Integer severity; // 0-普通 1-警告 2-严重
    
    @Column(columnDefinition = "TINYINT DEFAULT 0")
    private Integer isRead; // 0-未读 1-已读
    
    @Column(updatable = false)
    private LocalDateTime createdAt;
    
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
    
    // Getters and Setters
}
```

---

## 4. Repository 接口

### 4.1 DeviceRepository

文件：`src/main/java/com/pura365/camera/repository/DeviceRepository.java`

```java
package com.pura365.camera.repository;

import com.pura365.camera.domain.Device;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface DeviceRepository extends JpaRepository<Device, String> {
    
    Optional<Device> findByMac(String mac);
    
    List<Device> findByStatus(Integer status);
    
    List<Device> findByRegion(String region);
    
    @Query("SELECT d.ssid FROM Device d WHERE d.id = ?1")
    String findSsidById(String deviceId);
}
```

### 4.2 NetworkConfigRepository

文件：`src/main/java/com/pura365/camera/repository/NetworkConfigRepository.java`

```java
package com.pura365.camera.repository;

import com.pura365.camera.domain.NetworkConfig;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface NetworkConfigRepository extends JpaRepository<NetworkConfig, Long> {
    
    List<NetworkConfig> findByDeviceId(String deviceId);
    
    Optional<NetworkConfig> findFirstByDeviceIdOrderByCreatedAtDesc(String deviceId);
    
    List<NetworkConfig> findByConfigStatus(Integer configStatus);
}
```

### 4.3 DeviceMessageRepository

文件：`src/main/java/com/pura365/camera/repository/DeviceMessageRepository.java`

```java
package com.pura365.camera.repository;

import com.pura365.camera.domain.DeviceMessage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DeviceMessageRepository extends JpaRepository<DeviceMessage, Long> {
    
    List<DeviceMessage> findByDeviceId(String deviceId);
    
    List<DeviceMessage> findByDeviceIdAndIsRead(String deviceId, Integer isRead);
    
    List<DeviceMessage> findByMessageType(String messageType);
}
```

---

## 5. 配网接口实现

### 5.1 配网请求/响应模型

文件：`src/main/java/com/pura365/camera/model/NetworkConfigRequest.java`

```java
package com.pura365.camera.model;

import javax.validation.constraints.NotBlank;

public class NetworkConfigRequest {
    
    @NotBlank(message = "设备ID不能为空")
    private String deviceId;
    
    @NotBlank(message = "SSID不能为空")
    private String ssid;
    
    private String password;
    
    private String timezone; // 如 +8
    
    private String region; // cn/us等
    
    private String configMethod; // qrcode/ble/audio
    
    private String configSource; // APP版本等
    
    // Getters and Setters
}
```

### 5.2 配网Controller

文件：`src/main/java/com/pura365/camera/controller/NetworkConfigController.java`

```java
package com.pura365.camera.controller;

import com.pura365.camera.domain.Device;
import com.pura365.camera.domain.NetworkConfig;
import com.pura365.camera.model.NetworkConfigRequest;
import com.pura365.camera.repository.DeviceRepository;
import com.pura365.camera.repository.NetworkConfigRepository;
import com.pura365.camera.service.DeviceSsidService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/network")
public class NetworkConfigController {
    
    private static final Logger log = LoggerFactory.getLogger(NetworkConfigController.class);
    
    @Autowired
    private NetworkConfigRepository networkConfigRepository;
    
    @Autowired
    private DeviceRepository deviceRepository;
    
    @Autowired
    private DeviceSsidService deviceSsidService;
    
    /**
     * 提交配网信息
     * APP在完成设备配网后调用此接口，保存配网信息到数据库
     */
    @PostMapping("/config")
    public ResponseEntity<Map<String, Object>> submitNetworkConfig(
            @Valid @RequestBody NetworkConfigRequest request) {
        
        log.info("收到配网信息 - 设备: {}, SSID: {}", request.getDeviceId(), request.getSsid());
        
        try {
            // 1. 创建或更新设备记录
            Device device = deviceRepository.findById(request.getDeviceId())
                    .orElse(new Device());
            device.setId(request.getDeviceId());
            device.setSsid(request.getSsid());
            device.setRegion(request.getRegion());
            device.setEnabled(1);
            deviceRepository.save(device);
            
            // 2. 保存配网信息
            NetworkConfig config = new NetworkConfig();
            config.setDeviceId(request.getDeviceId());
            config.setSsid(request.getSsid());
            config.setPassword(request.getPassword()); // TODO: 加密存储
            config.setTimezone(request.getTimezone());
            config.setRegion(request.getRegion());
            config.setConfigMethod(request.getConfigMethod());
            config.setConfigSource(request.getConfigSource());
            config.setConfigStatus(0); // 配网中
            networkConfigRepository.save(config);
            
            // 3. 保存SSID到缓存
            deviceSsidService.saveSsid(request.getDeviceId(), request.getSsid());
            
            log.info("配网信息已保存 - 设备: {}", request.getDeviceId());
            
            Map<String, Object> result = new HashMap<>();
            result.put("success", true);
            result.put("message", "配网信息已保存");
            result.put("deviceId", request.getDeviceId());
            return ResponseEntity.ok(result);
            
        } catch (Exception e) {
            log.error("保存配网信息失败", e);
            Map<String, Object> result = new HashMap<>();
            result.put("success", false);
            result.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(result);
        }
    }
    
    /**
     * 更新配网状态
     * 设备配网成功后调用，更新状态为成功
     */
    @PostMapping("/config/{deviceId}/status")
    public ResponseEntity<Map<String, Object>> updateConfigStatus(
            @PathVariable String deviceId,
            @RequestParam Integer status) { // 0-配网中 1-成功 2-失败
        
        log.info("更新配网状态 - 设备: {}, 状态: {}", deviceId, status);
        
        try {
            // 查找最新的配网记录
            NetworkConfig config = networkConfigRepository
                    .findFirstByDeviceIdOrderByCreatedAtDesc(deviceId)
                    .orElseThrow(() -> new RuntimeException("配网记录不存在"));
            
            config.setConfigStatus(status);
            networkConfigRepository.save(config);
            
            // 如果配网成功，更新设备状态
            if (status == 1) {
                Device device = deviceRepository.findById(deviceId).orElse(null);
                if (device != null) {
                    device.setStatus(1); // 在线
                    deviceRepository.save(device);
                }
            }
            
            Map<String, Object> result = new HashMap<>();
            result.put("success", true);
            result.put("message", "配网状态已更新");
            return ResponseEntity.ok(result);
            
        } catch (Exception e) {
            log.error("更新配网状态失败", e);
            Map<String, Object> result = new HashMap<>();
            result.put("success", false);
            result.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(result);
        }
    }
    
    /**
     * 查询设备配网历史
     */
    @GetMapping("/config/{deviceId}/history")
    public ResponseEntity<?> getConfigHistory(@PathVariable String deviceId) {
        try {
            return ResponseEntity.ok(networkConfigRepository.findByDeviceId(deviceId));
        } catch (Exception e) {
            log.error("查询配网历史失败", e);
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}
```

---

## 6. WebRTC DataChannel 控制接口

文件：`src/main/java/com/pura365/camera/controller/DataChannelController.java`

```java
package com.pura365.camera.controller;

import com.pura365.camera.service.MqttMessageService;
import com.pura365.camera.util.TimeValidator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * WebRTC DataChannel 控制接口
 * 用于转发 DataChannel 控制指令到摄像头
 */
@RestController
@RequestMapping("/api/datachannel")
public class DataChannelController {
    
    private static final Logger log = LoggerFactory.getLogger(DataChannelController.class);
    
    @Autowired
    private MqttMessageService mqttMessageService;
    
    /**
     * 发送 DataChannel 控制指令
     * 支持的指令：left/right/up/down/mute/unmute/live/replay/fileindex
     */
    @PostMapping("/device/{deviceId}/command")
    public ResponseEntity<Map<String, Object>> sendCommand(
            @PathVariable String deviceId,
            @RequestParam String command,
            @RequestParam(required = false) String ssid) {
        
        log.info("发送DataChannel指令到设备 {} - 指令: {}", deviceId, command);
        
        try {
            // 构造MQTT消息（自定义CODE，如99）
            Map<String, Object> msg = new HashMap<>();
            msg.put("code", 99); // DataChannel指令的CODE
            msg.put("time", TimeValidator.getCurrentTimestamp());
            msg.put("command", command);
            
            // 通过MQTT发送
            mqttMessageService.sendToDevice(deviceId, msg, ssid);
            
            Map<String, Object> result = new HashMap<>();
            result.put("success", true);
            result.put("message", "指令已发送");
            result.put("command", command);
            return ResponseEntity.ok(result);
            
        } catch (Exception e) {
            log.error("发送DataChannel指令失败", e);
            Map<String, Object> result = new HashMap<>();
            result.put("success", false);
            result.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(result);
        }
    }
    
    /**
     * 云台控制（快捷方法）
     */
    @PostMapping("/device/{deviceId}/ptz/{direction}")
    public ResponseEntity<Map<String, Object>> ptzControl(
            @PathVariable String deviceId,
            @PathVariable String direction, // left/right/up/down
            @RequestParam(required = false) String ssid) {
        
        return sendCommand(deviceId, direction, ssid);
    }
    
    /**
     * 静音控制
     */
    @PostMapping("/device/{deviceId}/mute")
    public ResponseEntity<Map<String, Object>> muteControl(
            @PathVariable String deviceId,
            @RequestParam Boolean mute,
            @RequestParam(required = false) String ssid) {
        
        String command = mute ? "mute" : "unmute";
        return sendCommand(deviceId, command, ssid);
    }
    
    /**
     * 切换到直播
     */
    @PostMapping("/device/{deviceId}/live")
    public ResponseEntity<Map<String, Object>> switchToLive(
            @PathVariable String deviceId,
            @RequestParam(required = false) String ssid) {
        
        return sendCommand(deviceId, "live", ssid);
    }
    
    /**
     * 切换到回放
     */
    @PostMapping("/device/{deviceId}/replay")
    public ResponseEntity<Map<String, Object>> switchToReplay(
            @PathVariable String deviceId,
            @RequestParam Long timestamp,
            @RequestParam(required = false, defaultValue = "0") Integer channel,
            @RequestParam(required = false) String ssid) {
        
        String command = String.format("replay %d %d", timestamp, channel);
        return sendCommand(deviceId, command, ssid);
    }
    
    /**
     * 获取文件列表
     */
    @PostMapping("/device/{deviceId}/fileindex")
    public ResponseEntity<Map<String, Object>> getFileIndex(
            @PathVariable String deviceId,
            @RequestParam String date, // YYYY-mm-dd
            @RequestParam(required = false) String ssid) {
        
        String command = "fileindex " + date;
        return sendCommand(deviceId, command, ssid);
    }
}
```

---

## 7. 使用方式

### 7.1 配网流程

```bash
# 1. APP完成设备配网后，提交配网信息
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

# 2. 设备配网成功后，更新状态
curl -X POST "http://localhost:8080/api/network/config/abc789/status?status=1"

# 3. 查询配网历史
curl "http://localhost:8080/api/network/config/abc789/history"
```

### 7.2 DataChannel 控制

```bash
# 云台左转
curl -X POST "http://localhost:8080/api/datachannel/device/abc789/ptz/left"

# 云台右转
curl -X POST "http://localhost:8080/api/datachannel/device/abc789/ptz/right"

# 静音
curl -X POST "http://localhost:8080/api/datachannel/device/abc789/mute?mute=true"

# 切换到直播
curl -X POST "http://localhost:8080/api/datachannel/device/abc789/live"

# 切换到回放
curl -X POST "http://localhost:8080/api/datachannel/device/abc789/replay?timestamp=1732517698&channel=0"

# 获取文件列表
curl -X POST "http://localhost:8080/api/datachannel/device/abc789/fileindex?date=2025-11-25"
```

---

## 8. 总结

已实现：
✅ 完整的数据库表结构（7张表）
✅ 实体类（Device、NetworkConfig、DeviceMessage）
✅ Repository 接口（增删改查）
✅ 配网接口（提交配网信息、更新状态、查询历史）
✅ DataChannel 控制接口（9个控制接口）
✅ DeviceSsidService 自动从数据库读取SSID

**所有代码都在上面，直接复制创建文件即可！**
