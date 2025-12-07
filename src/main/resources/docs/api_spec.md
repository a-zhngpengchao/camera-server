# 智能摄像头App API接口规范

## 基础信息

- **Base URL**: `https://api.smartcamera.com` (生产) / `https://api-dev.smartcamera.com` (开发)
- **协议**: HTTPS
- **数据格式**: JSON
- **认证方式**: Bearer Token (在 Header 中传递 `Authorization: Bearer <token>`)

## 通用响应格式

```json
{
  "code": 0,           // 0 表示成功，其他为错误码
  "message": "success", // 响应消息
  "data": {}           // 响应数据
}
```

## 错误码定义

| 错误码 | 说明 |
|--------|------|
| 0 | 成功 |
| 400 | 请求参数错误 |
| 401 | 未授权/Token过期 |
| 403 | 无权限访问 |
| 404 | 资源不存在 |
| 500 | 服务器内部错误 |

---

## 一、认证模块

### 1.1 微信登录

**POST** `/auth/login/wechat`

**Request Body:**
```json
{
  "code": "微信授权code"
}
```

**Response:**
```json
{
  "code": 0,
  "data": {
    "token": "eyJhbGciOiJIUzI1NiIs...",
    "refresh_token": "refresh_token_xxx",
    "expires_in": 7200,
    "user": {
      "id": "user_001",
      "phone": "158****9999",
      "nickname": "用户昵称",
      "avatar": "https://xxx/avatar.jpg"
    }
  }
}
```

### 1.2 Apple登录

**POST** `/auth/login/apple`

**Request Body:**
```json
{
  "identity_token": "Apple identityToken"
}
```

**Response:** 同微信登录

### 1.3 Google登录

**POST** `/auth/login/google`

**Request Body:**
```json
{
  "id_token": "Google idToken"
}
```

**Response:** 同微信登录

### 1.4 发送短信验证码

**POST** `/auth/sms/send`

**Request Body:**
```json
{
  "phone": "15888889999"
}
```

**Response:**
```json
{
  "code": 0,
  "message": "验证码已发送"
}
```

### 1.5 短信验证码登录

**POST** `/auth/login/sms`

**Request Body:**
```json
{
  "phone": "15888889999",
  "code": "123456"
}
```

**Response:** 同微信登录

### 1.6 刷新Token

**POST** `/auth/token/refresh`

**Request Body:**
```json
{
  "refresh_token": "refresh_token_xxx"
}
```

**Response:**
```json
{
  "code": 0,
  "data": {
    "token": "new_token_xxx",
    "refresh_token": "new_refresh_token_xxx",
    "expires_in": 7200
  }
}
```

### 1.7 登出

**POST** `/auth/logout`

**Headers:** `Authorization: Bearer <token>`

**Response:**
```json
{
  "code": 0,
  "message": "登出成功"
}
```

---

## 二、用户模块

### 2.1 获取用户信息

**GET** `/user/info`

**Headers:** `Authorization: Bearer <token>`

**Response:**
```json
{
  "code": 0,
  "data": {
    "id": "user_001",
    "phone": "15888889999",
    "nickname": "用户昵称",
    "avatar": "https://xxx/avatar.jpg",
    "email": "user@example.com",
    "created_at": "2025-01-01T00:00:00Z"
  }
}
```

### 2.2 更新用户信息

**PUT** `/user/update`

**Headers:** `Authorization: Bearer <token>`

**Request Body:**
```json
{
  "nickname": "新昵称",
  "avatar": "https://xxx/new_avatar.jpg"
}
```

**Response:**
```json
{
  "code": 0,
  "data": {
    "id": "user_001",
    "phone": "15888889999",
    "nickname": "新昵称",
    "avatar": "https://xxx/new_avatar.jpg"
  }
}
```

### 2.3 上传头像

**POST** `/user/avatar`

**Headers:** `Authorization: Bearer <token>`

**Content-Type:** `multipart/form-data`

**Request Body:**
```
file: [图片文件]
```

**Response:**
```json
{
  "code": 0,
  "data": {
    "url": "https://xxx/uploaded_avatar.jpg"
  }
}
```

---

## 三、设备模块

### 3.1 获取设备列表

**GET** `/devices`

**Headers:** `Authorization: Bearer <token>`

**Response:**
```json
{
  "code": 0,
  "data": [
    {
      "id": "device_001",
      "name": "客厅",
      "model": "SC-1080P",
      "status": "online",
      "has_cloud_storage": true,
      "cloud_expire_at": "2025-12-31T23:59:59Z",
      "thumbnail_url": "https://xxx/thumbnail.jpg",
      "last_online_at": "2025-09-06T10:00:00Z"
    },
    {
      "id": "device_002",
      "name": "卧室",
      "model": "SC-2K",
      "status": "offline",
      "has_cloud_storage": false,
      "thumbnail_url": null,
      "last_online_at": "2025-09-05T18:30:00Z"
    }
  ]
}
```

### 3.2 获取设备详情 (新增)

**GET** `/devices/{id}/info`

**Headers:** `Authorization: Bearer <token>`

**Response:**
```json
{
  "code": 0,
  "data": {
    "id": "device_001",
    "name": "客厅",
    "model": "SC-1080P",
    "firmware_version": "v2.1.3",
    "status": "online",
    "has_cloud_storage": true,
    "cloud_expire_at": "2025-12-31T23:59:59Z",
    "thumbnail_url": "https://xxx/thumbnail.jpg",
    "wifi_ssid": "ACCGE-5G",
    "wifi_signal": 85,
    "sd_card": {
      "total": 32000000000,
      "used": 12500000000,
      "available": 19500000000
    },
    "settings": {
      "motion_detection": true,
      "night_vision": true,
      "audio_enabled": true,
      "flip_image": false,
      "sensitivity": "medium"
    },
    "last_online_at": "2025-09-06T10:00:00Z"
  }
}
```

### 3.3 添加设备

**POST** `/devices/add`

**Headers:** `Authorization: Bearer <token>`

**Request Body:**
```json
{
  "device_id": "设备序列号或二维码内容",
  "name": "客厅摄像头",
  "wifi_ssid": "WiFi名称",
  "wifi_password": "WiFi密码"
}
```

**Response:**
```json
{
  "code": 0,
  "data": {
    "id": "device_003",
    "name": "客厅摄像头",
    "model": "SC-1080P",
    "status": "online"
  }
}
```

### 3.4 删除设备

**DELETE** `/devices/{id}`

**Headers:** `Authorization: Bearer <token>`

**Response:**
```json
{
  "code": 0,
  "message": "设备已删除"
}
```

### 3.5 更新设备信息

**PUT** `/devices/{id}`

**Headers:** `Authorization: Bearer <token>`

**Request Body:**
```json
{
  "name": "新设备名称"
}
```

**Response:**
```json
{
  "code": 0,
  "data": {
    "id": "device_001",
    "name": "新设备名称"
  }
}
```

### 3.6 更新设备设置 (新增)

**PUT** `/devices/{id}/settings`

**Headers:** `Authorization: Bearer <token>`

**Request Body:**
```json
{
  "motion_detection": true,
  "night_vision": true,
  "audio_enabled": false,
  "flip_image": true,
  "sensitivity": "high"
}
```

**Response:**
```json
{
  "code": 0,
  "data": {
    "motion_detection": true,
    "night_vision": true,
    "audio_enabled": false,
    "flip_image": true,
    "sensitivity": "high"
  }
}
```

### 3.7 获取本地视频列表 (新增)

**GET** `/devices/{id}/local-videos`

**Headers:** `Authorization: Bearer <token>`

**Query Parameters:**
| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| date | string | 否 | 日期筛选，格式: YYYY-MM-DD |
| page | int | 否 | 页码，默认1 |
| page_size | int | 否 | 每页数量，默认20 |

**Response:**
```json
{
  "code": 0,
  "data": {
    "list": [
      {
        "id": "local_video_001",
        "device_id": "device_001",
        "type": "alarm",
        "title": "检测到移动",
        "thumbnail_url": "https://xxx/thumb.jpg",
        "video_url": "https://xxx/video.mp4",
        "duration": 30,
        "size": 15000000,
        "created_at": "2025-09-06T19:15:00Z"
      }
    ],
    "total": 50,
    "page": 1,
    "page_size": 20
  }
}
```

### 3.8 云台控制 (新增)

**POST** `/devices/{id}/ptz`

**Headers:** `Authorization: Bearer <token>`

**Request Body:**
```json
{
  "direction": "up",
  "speed": 5
}
```

| direction 值 | 说明 |
|--------------|------|
| up | 向上 |
| down | 向下 |
| left | 向左 |
| right | 向右 |
| stop | 停止 |

**Response:**
```json
{
  "code": 0,
  "message": "操作成功"
}
```

### 3.9 截图 (新增)

**POST** `/devices/{id}/screenshot`

**Headers:** `Authorization: Bearer <token>`

**Response:**
```json
{
  "code": 0,
  "data": {
    "url": "https://xxx/screenshot_20250906_191500.jpg",
    "created_at": "2025-09-06T19:15:00Z"
  }
}
```

### 3.10 开始录制 (新增)

**POST** `/devices/{id}/record/start`

**Headers:** `Authorization: Bearer <token>`

**Response:**
```json
{
  "code": 0,
  "data": {
    "record_id": "record_001",
    "started_at": "2025-09-06T19:15:00Z"
  }
}
```

### 3.11 停止录制 (新增)

**POST** `/devices/{id}/record/stop`

**Headers:** `Authorization: Bearer <token>`

**Request Body:**
```json
{
  "record_id": "record_001"
}
```

**Response:**
```json
{
  "code": 0,
  "data": {
    "record_id": "record_001",
    "video_url": "https://xxx/record_001.mp4",
    "duration": 120,
    "size": 50000000,
    "ended_at": "2025-09-06T19:17:00Z"
  }
}
```

---

## 四、消息模块

### 4.1 获取消息列表

**GET** `/messages`

**Headers:** `Authorization: Bearer <token>`

**Query Parameters:**
| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| device_id | string | 否 | 设备ID筛选 |
| date | string | 否 | 日期筛选，格式: YYYY-MM-DD |
| type | string | 否 | 消息类型: alarm/system/promotion |
| page | int | 否 | 页码，默认1 |
| page_size | int | 否 | 每页数量，默认20 |

**Response:**
```json
{
  "code": 0,
  "data": {
    "list": [
      {
        "id": "msg_001",
        "type": "alarm",
        "title": "检测到移动",
        "content": "客厅摄像头检测到异常移动",
        "device_id": "device_001",
        "device_name": "客厅",
        "thumbnail_url": "https://xxx/thumb.jpg",
        "video_url": "https://xxx/video.mp4",
        "is_read": false,
        "created_at": "2025-09-06T19:15:00Z"
      }
    ],
    "total": 100,
    "page": 1,
    "page_size": 20
  }
}
```

### 4.2 标记消息已读

**POST** `/messages/{id}/read`

**Headers:** `Authorization: Bearer <token>`

**Response:**
```json
{
  "code": 0,
  "message": "标记成功"
}
```

### 4.3 删除消息

**DELETE** `/messages/{id}`

**Headers:** `Authorization: Bearer <token>`

**Response:**
```json
{
  "code": 0,
  "message": "删除成功"
}
```

### 4.4 获取未读消息数量

**GET** `/messages/unread/count`

**Headers:** `Authorization: Bearer <token>`

**Response:**
```json
{
  "code": 0,
  "data": {
    "count": 5
  }
}
```

---

## 五、云存储模块

### 5.1 获取套餐列表

**GET** `/cloud/plans`

**Headers:** `Authorization: Bearer <token>`

**Response:**
```json
{
  "code": 0,
  "data": [
    {
      "id": "plan_001",
      "name": "7天循环存储",
      "description": "所有数据，循环存储7天",
      "storage_days": 7,
      "price": 98.00,
      "original_price": 198.00,
      "period": "month",
      "features": ["全天候录制", "移动侦测", "高清存储"]
    },
    {
      "id": "plan_002",
      "name": "30天循环存储",
      "description": "所有数据，循环存储30天",
      "storage_days": 30,
      "price": 298.00,
      "original_price": 498.00,
      "period": "month",
      "features": ["全天候录制", "移动侦测", "高清存储", "优先客服"]
    }
  ]
}
```

### 5.2 订阅云存储

**POST** `/cloud/subscribe`

**Headers:** `Authorization: Bearer <token>`

**Request Body:**
```json
{
  "device_id": "device_001",
  "plan_id": "plan_001",
  "payment_method": "wechat"
}
```

**Response:**
```json
{
  "code": 0,
  "data": {
    "order_id": "order_001",
    "payment_info": {
      "prepay_id": "wx_prepay_xxx",
      "sign": "签名信息"
    }
  }
}
```

### 5.3 获取云存储视频列表

**GET** `/cloud/videos`

**Headers:** `Authorization: Bearer <token>`

**Query Parameters:**
| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| device_id | string | 是 | 设备ID |
| date | string | 否 | 日期筛选，格式: YYYY-MM-DD |
| page | int | 否 | 页码，默认1 |
| page_size | int | 否 | 每页数量，默认20 |

**Response:**
```json
{
  "code": 0,
  "data": {
    "list": [
      {
        "id": "cloud_video_001",
        "device_id": "device_001",
        "type": "alarm",
        "title": "检测到移动",
        "thumbnail_url": "https://xxx/thumb.jpg",
        "video_url": "https://xxx/video.mp4",
        "duration": 30,
        "created_at": "2025-09-06T19:15:00Z"
      }
    ],
    "total": 100,
    "page": 1,
    "page_size": 20
  }
}
```

### 5.4 获取设备云存储订阅状态 (新增)

**GET** `/cloud/subscription/{deviceId}`

**Headers:** `Authorization: Bearer <token>`

**Response:**
```json
{
  "code": 0,
  "data": {
    "is_subscribed": true,
    "plan_id": "plan_001",
    "plan_name": "7天循环存储",
    "expire_at": "2025-12-31T23:59:59Z",
    "auto_renew": true
  }
}
```

---

## 六、支付模块

### 6.1 创建支付订单

**POST** `/payment/create`

**Headers:** `Authorization: Bearer <token>`

**Request Body:**
```json
{
  "product_type": "cloud_storage",
  "product_id": "plan_001",
  "device_id": "device_001",
  "payment_method": "wechat"
}
```

**Response:**
```json
{
  "code": 0,
  "data": {
    "order_id": "order_001",
    "amount": 98.00,
    "currency": "CNY",
    "created_at": "2025-09-06T19:15:00Z"
  }
}
```

### 6.2 查询支付状态

**GET** `/payment/{id}/status`

**Headers:** `Authorization: Bearer <token>`

**Response:**
```json
{
  "code": 0,
  "data": {
    "order_id": "order_001",
    "status": "paid",
    "amount": 98.00,
    "paid_at": "2025-09-06T19:16:00Z"
  }
}
```

| status 值 | 说明 |
|-----------|------|
| pending | 待支付 |
| paid | 已支付 |
| failed | 支付失败 |
| cancelled | 已取消 |
| refunded | 已退款 |

### 6.3 微信支付 (新增)

**POST** `/payment/wechat`

**Headers:** `Authorization: Bearer <token>`

**Request Body:**
```json
{
  "order_id": "order_001"
}
```

**Response:**
```json
{
  "code": 0,
  "data": {
    "appid": "wx_app_id",
    "partnerid": "商户ID",
    "prepayid": "预支付ID",
    "package": "Sign=WXPay",
    "noncestr": "随机字符串",
    "timestamp": "1630934400",
    "sign": "签名"
  }
}
```

### 6.4 PayPal支付 (新增)

**POST** `/payment/paypal`

**Headers:** `Authorization: Bearer <token>`

**Request Body:**
```json
{
  "order_id": "order_001"
}
```

**Response:**
```json
{
  "code": 0,
  "data": {
    "approval_url": "https://www.paypal.com/checkout?token=xxx",
    "paypal_order_id": "paypal_order_xxx"
  }
}
```

### 6.5 Apple Pay支付 (新增)

**POST** `/payment/apple`

**Headers:** `Authorization: Bearer <token>`

**Request Body:**
```json
{
  "order_id": "order_001",
  "payment_token": "Apple Pay token"
}
```

**Response:**
```json
{
  "code": 0,
  "data": {
    "transaction_id": "apple_txn_xxx",
    "status": "completed"
  }
}
```

---

## 七、视频流模块 (新增)

### 7.1 获取直播流

**POST** `/devices/{id}/stream/start`

**Headers:** `Authorization: Bearer <token>`

**Request Body:**
```json
{
  "quality": "hd",
  "protocol": "webrtc"
}
```

| quality 值 | 说明 |
|------------|------|
| sd | 标清 480p |
| hd | 高清 720p |
| fhd | 全高清 1080p |

| protocol 值 | 说明 |
|-------------|------|
| webrtc | WebRTC协议 |
| hls | HLS协议 |
| rtmp | RTMP协议 |

**Response:**
```json
{
  "code": 0,
  "data": {
    "stream_id": "stream_001",
    "protocol": "webrtc",
    "signaling_url": "wss://signaling.smartcamera.com/ws",
    "ice_servers": [
      {
        "urls": "stun:stun.smartcamera.com:3478"
      },
      {
        "urls": "turn:turn.smartcamera.com:3478",
        "username": "user",
        "credential": "password"
      }
    ],
    "expires_at": "2025-09-06T20:15:00Z"
  }
}
```

### 7.2 停止直播流

**POST** `/devices/{id}/stream/stop`

**Headers:** `Authorization: Bearer <token>`

**Request Body:**
```json
{
  "stream_id": "stream_001"
}
```

**Response:**
```json
{
  "code": 0,
  "message": "直播流已停止"
}
```

### 7.3 获取回放视频流

**GET** `/videos/{id}/playback`

**Headers:** `Authorization: Bearer <token>`

**Response:**
```json
{
  "code": 0,
  "data": {
    "video_url": "https://xxx/video.mp4",
    "hls_url": "https://xxx/video.m3u8",
    "duration": 120,
    "expires_at": "2025-09-06T20:15:00Z"
  }
}
```

---

## 八、WiFi配网模块 (新增)

### 8.1 扫描WiFi列表

**GET** `/wifi/scan`

**说明:** 此接口通常在设备端实现，通过蓝牙与设备通信获取。如需服务端支持，可返回历史连接的WiFi列表。

**Headers:** `Authorization: Bearer <token>`

**Response:**
```json
{
  "code": 0,
  "data": [
    {
      "ssid": "ACCGE-5G",
      "signal": 85,
      "security": "WPA2",
      "is_connected": true
    },
    {
      "ssid": "HomeNetwork",
      "signal": 70,
      "security": "WPA2",
      "is_connected": false
    }
  ]
}
```

### 8.2 绑定设备

**POST** `/devices/bind`

**Headers:** `Authorization: Bearer <token>`

**Request Body:**
```json
{
  "device_sn": "设备序列号",
  "device_name": "客厅摄像头",
  "wifi_ssid": "ACCGE-5G",
  "wifi_password": "password123"
}
```

**Response:**
```json
{
  "code": 0,
  "data": {
    "id": "device_003",
    "name": "客厅摄像头",
    "status": "binding",
    "binding_progress": 0
  }
}
```

### 8.3 查询绑定进度 (新增)

**GET** `/devices/{id}/binding-status`

**Headers:** `Authorization: Bearer <token>`

**Response:**
```json
{
  "code": 0,
  "data": {
    "status": "success",
    "progress": 100,
    "message": "设备绑定成功"
  }
}
```

| status 值 | 说明 |
|-----------|------|
| binding | 绑定中 |
| success | 绑定成功 |
| failed | 绑定失败 |

---

## 九、其他模块

### 9.1 检查应用版本

**GET** `/app/version`

**Query Parameters:**
| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| platform | string | 是 | ios/android |
| current_version | string | 是 | 当前版本号 |

**Response:**
```json
{
  "code": 0,
  "data": {
    "latest_version": "1.1.0",
    "min_version": "1.0.0",
    "download_url": "https://xxx/app.apk",
    "release_notes": "1. 修复已知问题\n2. 性能优化",
    "force_update": false
  }
}
```

### 9.2 提交反馈

**POST** `/feedback`

**Headers:** `Authorization: Bearer <token>`

**Request Body:**
```json
{
  "content": "反馈内容",
  "contact": "联系方式（可选）",
  "images": ["https://xxx/img1.jpg", "https://xxx/img2.jpg"]
}
```

**Response:**
```json
{
  "code": 0,
  "data": {
    "feedback_id": "feedback_001",
    "created_at": "2025-09-06T19:15:00Z"
  }
}
```

---

## 附录

### A. 设备状态定义

| 状态 | 说明 |
|------|------|
| online | 在线 |
| offline | 离线 |
| upgrading | 升级中 |

### B. 消息类型定义

| 类型 | 说明 |
|------|------|
| alarm | 报警消息（移动侦测等） |
| system | 系统消息 |
| promotion | 推广消息 |

### C. 视频类型定义

| 类型 | 说明 |
|------|------|
| alarm | 报警触发录像 |
| manual | 手动录制 |
| scheduled | 定时录制 |
| continuous | 连续录制 |
