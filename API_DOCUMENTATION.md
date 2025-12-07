# API 文档说明

## 修复内容总结

已完成以下修复工作：

### 1. 添加了 Swagger/OpenAPI 依赖
- 在 `pom.xml` 中添加了 `springdoc-openapi-starter-webmvc-ui` 依赖
- 版本: 2.2.0

### 2. 修复了所有 Controller 的中文乱码
- 所有中文注释和错误消息的乱码已修复为正确的中文

### 3. 统一了 RequestMapping 路径规范
所有 Controller 的路径已按模块统一规范：

#### Admin 模块 (`/api/admin/*`)
- `/api/admin/device-id` - 设备ID工具
- `/api/admin/vendors` - 经销商管理
- `/api/admin/device-production` - 设备生产管理

#### App 模块 (`/api/app/*`)
- `/api/app` - App通用接口
- `/api/app/auth` - 用户认证
- `/api/app/messages` - 消息管理
- `/api/app/user` - 用户信息
- `/api/app/payment` - 支付管理

#### Device 模块 (`/api/device/*`)
- `/api/device/devices` - 设备管理
- `/api/device/devices` - 视频流管理（StreamController）
- 其他设备相关接口

#### Internal 模块 (`/api/internal/*`)
- `/` - 摄像头内部接口（CameraController，保持根路径）
- `/api/internal/cloud` - 云服务接口
- `/api/internal/datachannel` - 数据通道
- `/api/internal/mqtt` - MQTT控制
- `/api/internal/webrtc` - WebRTC调试

### 4. 添加了 Swagger 注解
所有 Controller 类都添加了：
- `@Tag` 注解：用于分组和描述
- `@Operation` 注解：用于描述具体的API操作（部分已添加）

## 使用方法

### 1. 启动应用
```bash
mvn clean install
mvn spring-boot:run
```

### 2. 访问 Swagger UI
启动应用后，在浏览器中访问：
- Swagger UI: `http://localhost:8080/swagger-ui.html`
- OpenAPI JSON: `http://localhost:8080/v3/api-docs`

### 3. 在 Apifox 中导入
1. 打开 Apifox
2. 选择"导入" -> "OpenAPI/Swagger"
3. 输入URL: `http://localhost:8080/v3/api-docs`
4. 或者下载 JSON 文件后导入

### 4. 同步更新
- 代码更新后，重启应用
- Apifox 中重新导入或点击"同步"按钮
- 所有接口会自动更新

## Swagger UI 配置

已在 `application.properties` 中添加了以下配置：
```properties
springdoc.api-docs.path=/v3/api-docs
springdoc.swagger-ui.path=/swagger-ui.html
springdoc.swagger-ui.tags-sorter=alpha
springdoc.swagger-ui.operations-sorter=alpha
springdoc.swagger-ui.display-request-duration=true
```

## 注意事项

1. **端口配置**: 默认端口为 8080，如需修改请更新 `application.properties` 中的 `server.port`
2. **认证**: 部分接口需要认证，在 Apifox 中测试时需要先登录获取 token
3. **环境变量**: 生产环境URL需要在 `OpenApiConfig.java` 中修改

## 文件结构

```
src/main/java/com/pura365/camera/
├── config/
│   └── OpenApiConfig.java          # Swagger/OpenAPI 配置
├── controller/
│   ├── admin/                      # 管理后台接口
│   │   ├── DeviceIdToolController.java
│   │   ├── DeviceProductionController.java
│   │   └── VendorController.java
│   ├── app/                        # App端接口
│   │   ├── AppController.java
│   │   ├── AuthController.java
│   │   ├── MessageController.java
│   │   ├── UserController.java
│   │   └── PaymentController.java
│   ├── device/                     # 设备接口
│   │   ├── DeviceController.java
│   │   ├── StreamController.java
│   │   ├── NetworkConfigController.java
│   │   ├── PairConfigController.java
│   │   ├── VideoPlaybackController.java
│   │   └── WifiController.java
│   └── internal/                   # 内部接口
│       ├── CameraController.java
│       ├── CloudController.java
│       ├── DataChannelController.java
│       ├── MqttControlController.java
│       └── WebRtcDebugController.java
```

## 后续优化建议

1. 为每个接口方法添加 `@Operation` 注解以提供更详细的描述
2. 使用 `@ApiResponse` 注解定义返回状态码和响应示例
3. 使用 `@Schema` 注解为请求/响应模型添加详细说明
4. 添加请求参数的验证注解（`@Valid`, `@NotNull` 等）

## 技术栈

- Spring Boot 2.x
- SpringDoc OpenAPI 2.2.0
- Swagger UI 集成
- Apifox 兼容
