# 摄像头 WebRTC 直播 & 云台控制 接口文档（基于现有实现）

## 1. 总体说明

- **后端基地址（开发环境示例）**：`http://{server-host}:8080`
- 当前这组接口都在 Spring Boot 工程 `camera-server` 中，路径前缀为 `/api/...`。
- 目前这组接口 **未接入统一登录鉴权**（没有校验 `Authorization`），前端可以直接调用。
  将来如果接入 JWT，可在 Header 统一加上：`Authorization: Bearer <token>`。

### 1.1 直播与控制整体流程（概览）

当前端要看实时画面并控制 PTZ（上下左右）时，流程如下：

1. 前端生成一个本地会话 ID：`sid = Date.now().toString()`。
2. 调用 `POST /api/mqtt/device/{deviceId}/webrtc/offer`：
   - 携带 `sid` 和 `rtcServer`（TURN/STUN 配置字符串）。
   - 后端通过 MQTT 通知摄像头发起 WebRTC（CODE 23）。
3. 前端轮询 `GET /api/webrtc/offer/{sid}`：
   - 直到拿到摄像头返回的 Offer SDP。
4. 前端创建 `RTCPeerConnection`：
   - 自己从 `rtcServer` 解析出 `iceServers`，传给 `new RTCPeerConnection(config)`。
5. 前端：
   - `setRemoteDescription(offer)` → `createAnswer()` → `setLocalDescription(answer)`；
   - 调 `POST /api/mqtt/device/{deviceId}/webrtc/answer` 上报 Answer（后端发 MQTT CODE 24 给设备）。
6. 前端在 `onicecandidate` 里，每有一个新的 candidate：
   - 调 `POST /api/mqtt/device/{deviceId}/webrtc/candidate` 上报本地 candidate（后端发 MQTT CODE 25 给设备）。
7. 前端每隔 1 秒轮询 `GET /api/webrtc/candidates/{sid}`：
   - 拿到设备端的 candidates，逐个 `pc.addIceCandidate(...)`。
8. WebRTC 连接成功后：
   - `ontrack` 里把流绑定到 `<video>`；
   - `ondatachannel` 里拿到 DataChannel，发送字符串 `"left" / "right" / "up" / "down"` 控制 PTZ。

> 回放接口目前代码中还没有 HTTP 实现，下文只文档化**现在已经实现并在自测页面使用的接口**。

---

## 2. WebRTC 信令接口（HTTP ⇆ 后端 ⇆ MQTT ⇆ 摄像头）

这些接口都定义在 `MqttControlController` 和 `WebRtcDebugController` 中，路径前缀分别为：

- `/api/mqtt/...`
- `/api/webrtc/...`

### 2.1 请求设备生成 WebRTC Offer

**POST** `/api/mqtt/device/{deviceId}/webrtc/offer`

- **说明**：
  向后端发起请求，让后端通过 MQTT CODE 23 通知设备：“请为会话 SID 生成 WebRTC Offer”。

- **Path 参数**：

| 名称     | 类型   | 必填 | 说明                  |
|----------|--------|------|-----------------------|
| deviceId | string | 是   | 设备唯一 ID（序列号） |

- **Query 参数**：

| 名称      | 类型   | 必填 | 说明                                                                                     |
|-----------|--------|------|------------------------------------------------------------------------------------------|
| sid       | string | 是   | 本次 WebRTC 会话 ID，建议前端用 `Date.now().toString()` 生成，全局唯一即可              |
| rtcServer | string | 是   | 传给设备的 TURN/STUN 配置字符串，格式：`server,user,pass`，例如 `8.129.3.8:3478,test_rtc,test123456` |

> **rtcServer 说明**：  
> - 这是后端 **原样透传** 给设备的 `rtc` 字段（MQTT JSON 中的 `"rtc"`）。  
> - 设备工程师要求目前不要带 `stun:` / `turn:` 前缀，所以这里请直接用 `8.129.3.8:3478,test_rtc,test123456` 这种形式。  
> - 前端自己在构造 `RTCPeerConnection` 时，可以把 `server` 解析成 `turn:8.129.3.8:3478` 并附带 `username`、`credential` 使用。

- **示例请求**：

```http
POST /api/mqtt/device/abc789/webrtc/offer?sid=1764215491310&rtcServer=8.129.3.8:3478,test_rtc,test123456
```

- **响应（成功）**：

```json
{
  "success": true,
  "message": "已请求WebRTC Offer"
}
```

- **响应（失败）**：

```json
{
  "success": false,
  "message": "错误原因"
}
```

---

### 2.2 轮询获取设备发回的 Offer（SDP）

**GET** `/api/webrtc/offer/{sid}`

- **说明**：  
  设备收到 MQTT CODE 23 后会通过 CODE 151 把 Offer 发送到后端。后端把最近一次 Offer 缓存在内存（`webrtcOfferCache`），前端通过这个接口轮询获取。

- **Path 参数**：

| 名称 | 类型   | 必填 | 说明                |
|------|--------|------|---------------------|
| sid  | string | 是   | 上一步传给设备的同一个会话 ID |

- **请求示例**：

```http
GET /api/webrtc/offer/1764215491310
```

- **响应（尚未收到 Offer）**：

```json
{
  "found": false
}
```

- **响应（已收到 Offer）**：

```json
{
  "found": true,
  "sid": "1764215491310",
  "sdp": "v=0\r\no=- 1495799811084970 ...",  
  "status": 1,
  "time": 1764215490
}
```

> 前端逻辑：  
> - 如果 `found = false`，0.5~1 秒后再调一次。  
> - `found = true` 时，用 `data.sdp` 作为 `RTCPeerConnection.setRemoteDescription({ type: 'offer', sdp: sdp })` 的 SDP。

---

### 2.3 上报前端生成的 Answer（SDP）

**POST** `/api/mqtt/device/{deviceId}/webrtc/answer`

- **说明**：  
  前端根据设备 Offer 创建 Answer SDP 后，通过此接口上报给后端；后端再经 MQTT CODE 24 发给设备。

- **Path 参数**：

| 名称     | 类型   | 必填 | 说明    |
|----------|--------|------|---------|
| deviceId | string | 是   | 设备 ID |

- **Query 参数**：

| 名称 | 类型   | 必填 | 说明                      |
|------|--------|------|---------------------------|
| sid  | string | 是   | 会话 ID（与 Offer 时一致） |
| sdp  | string | 是   | Answer SDP 文本（需 URL 编码） |

> **注意**：当前实现中 Answer 是用 **QueryString** 传的（`?sid=...&sdp=...`），前端要对 SDP 做 `encodeURIComponent`。

- **示例请求**：

```http
POST /api/mqtt/device/abc789/webrtc/answer?sid=1764215491310&sdp=v%3D0%0D%0Ao%3D-...
```

- **响应**：

```json
{
  "success": true,
  "message": "已发送WebRTC Answer"
}
```

---

### 2.4 上报前端本地 ICE Candidate（前端 → 设备）

**POST** `/api/mqtt/device/{deviceId}/webrtc/candidate`

- **说明**：  
  在 WebRTC 建立过程中，前端的 `RTCPeerConnection` 会持续产生本地 ICE candidate。每个 candidate 都通过本接口发给后端，再由后端通过 MQTT CODE 25 转发给设备。

- **Path 参数**：

| 名称     | 类型   | 必填 | 说明    |
|----------|--------|------|---------|
| deviceId | string | 是   | 设备 ID |

- **Query 参数**：

| 名称      | 类型   | 必填 | 说明                        |
|-----------|--------|------|-----------------------------|
| sid       | string | 是   | 会话 ID                     |
| candidate | string | 是   | 本地 ICE candidate（URL 编码） |

- **示例请求**：

```http
POST /api/mqtt/device/abc789/webrtc/candidate?sid=1764215491310&candidate=candidate%3A1349...
```

- **响应**：

```json
{
  "success": true,
  "message": "已发送WebRTC Candidate"
}
```

- **前端建议代码片段**：

```js
pc.onicecandidate = (event) => {
  if (event.candidate) {
    const candidate = encodeURIComponent(event.candidate.candidate);
    fetch(`/api/mqtt/device/${deviceId}/webrtc/candidate?sid=${sid}&candidate=${candidate}`, {
      method: 'POST'
    });
  }
};
```

---

### 2.5 轮询获取设备端 ICE Candidates（设备 → 前端）

**GET** `/api/webrtc/candidates/{sid}`

- **说明**：  
  设备通过 MQTT CODE 25 + 128 上报 Candidate，后端缓存到 `webrtcCandidateCache`。当前端调用该接口时，后端返回当前所有缓存的 candidate 并**清空缓存**（“取一次清一次”）。

- **Path 参数**：

| 名称 | 类型   | 必填 | 说明    |
|------|--------|------|---------|
| sid  | string | 是   | 会话 ID |

- **请求示例**：

```http
GET /api/webrtc/candidates/1764215491310
```

- **响应（暂无候选）**：

```json
{
  "found": false,
  "candidates": []
}
```

- **响应（有新的候选）**：

```json
{
  "found": true,
  "candidates": [
    "candidate:0 1 UDP 2129131263 192.168.31.36 59382 typ host",
    "candidate:2 1 UDP 1692923647 123.144.114.73 59382 typ srflx raddr 0.0.0.0 rport 0",
    "candidate:3 1 UDP 14359551 8.129.3.8 56091 typ relay raddr 0.0.0.0 rport 0"
  ]
}
```

- **前端建议逻辑**：

```js
async function pollRemoteCandidates() {
  const res = await fetch(`/api/webrtc/candidates/${sid}`);
  if (!res.ok) return;
  const data = await res.json();
  if (!data.found || !data.candidates) return;
  for (const c of data.candidates) {
    await pc.addIceCandidate(new RTCIceCandidate({ candidate: c }));
  }
}

// 每隔 1s 调一次
setInterval(pollRemoteCandidates, 1000);
```

---

## 3. WebRTC DataChannel 云台控制协议（现有实现）

云台控制目前**不走 HTTP**，是直接通过 WebRTC DataChannel 文本消息完成的，这一部分接口是“协议约定”，前端要在收到 DataChannel 后按此发送命令。

### 3.1 DataChannel 建立

- 由 **摄像头设备端** 在 WebRTC 会话中创建 DataChannel。
- 前端监听：

```js
let dataChannel = null;

pc.ondatachannel = (event) => {
  dataChannel = event.channel;
  console.log('DataChannel received:', dataChannel.label);
  dataChannel.onopen = () => console.log('DataChannel opened');
  dataChannel.onmessage = (e) => console.log('DataChannel message:', e.data);
  dataChannel.onclose = () => console.log('DataChannel closed');
};
```

前端只有在 `dataChannel.readyState === 'open'` 时才能发送 PTZ 命令。

### 3.2 PTZ 控制指令格式（当前代码）

- 当前自测页面中，发送的是**纯文本字符串**，没有 JSON 封装：
  - `"left"`
  - `"right"`
  - `"up"`
  - `"down"`

- 按钮对应的发送逻辑（来自 `webrtc-demo.html`）：

```js
function sendPtz(cmd) {
  if (!dataChannel || dataChannel.readyState !== 'open') {
    console.log('DataChannel not open, cannot send PTZ:', cmd);
    return;
  }
  dataChannel.send(cmd);
  console.log('Sent PTZ command:', cmd);
}

// 按钮绑定：sendPtz('left' / 'right' / 'up' / 'down')
```

- **说明**：
  - 设备固件已经对 `"left" / "right" / "up" / "down"` 这些字符串做了处理（你已经验证过可以控制摄像头）。
  - 如果后续设备支持 `"stop"`，前端也可以直接 `dataChannel.send("stop")`。

> 如果后面需要升级为 JSON 协议（例如 `{ "type": "ptz", "direction": "left" }`），只要前后端和设备一起改即可，当前正式文档按**现实可用的实现**写，仍然是 plain text。

---

## 4. 其他现有 HTTP 控制接口（非 PTZ，但和摄像头控制相关）

在 `MqttControlController` 中还有几组已经实现的 HTTP → MQTT 控制接口，可一并提供给前端使用（比如设备设置页）。

### 4.1 请求设备信息（CODE 11）

**POST** `/api/mqtt/device/{deviceId}/info`

- 说明：让设备上报一次自身信息（WiFi、固件版本等），具体字段由 MQTT 报文决定，目前后端只是转发并在日志中打印，后续可写入 DB。

- 响应：

```json
{
  "success": true,
  "message": "已发送请求"
}
```

### 4.2 格式化 TF 卡（CODE 12）

**POST** `/api/mqtt/device/{deviceId}/format`

- 说明：请求设备格式化 TF 卡。

- 响应：

```json
{
  "success": true,
  "message": "已发送格式化指令"
}
```

### 4.3 重启设备（CODE 13）

**POST** `/api/mqtt/device/{deviceId}/reboot`

- 响应：

```json
{
  "success": true,
  "message": "已发送重启指令"
}
```

### 4.4 设置画面旋转（CODE 26）

**POST** `/api/mqtt/device/{deviceId}/rotate?enable={0|1}`

- `enable = 0`：不旋转  
- `enable = 1`：旋转 180°

- 响应：

```json
{
  "success": true,
  "message": "已发送旋转设置"
}
```

### 4.5 设置白光灯开关（CODE 28）

**POST** `/api/mqtt/device/{deviceId}/whiteled?enable={0|1}`

- `enable = 0`：关闭  
- `enable = 1`：打开

- 响应：

```json
{
  "success": true,
  "message": "已发送白光灯设置"
}
```

---

## 5. 回放相关说明（基于当前代码状态）

- 目前代码里 **还没有** 专门针对“录像列表 / 回放播放地址”的 HTTP 接口实现（相应表结构也还没完全落地）。
- 如果前端现在就需要占位接口用来联调 UI，可参考 `src/main/resources/docs/api_spec.md` 中的视频/云存储部分定义，后端将按该文档后续扩展。