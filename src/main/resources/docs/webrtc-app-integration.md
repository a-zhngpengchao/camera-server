# Camera WebRTC APP 对接文档

本文档给 APP / 前端同事，用来对接摄像头 WebRTC 直播与控制功能。

主要内容：

- 信令 HTTP 接口（通过后端转发 MQTT）
- APP 侧 WebRTC 配置约定（ICE / TURN）
- DataChannel 文本控制指令
- `fileindex` 二进制回复格式
- 一个 Android 方向的参考实现思路

---

## 1. 总体流程概览

1. APP 生成会话 ID：`sid`（建议使用时间戳或 UUID）。
2. APP 调用 HTTP，通知后端通过 MQTT 让摄像头生成 WebRTC Offer：
   - `POST /api/internal/mqtt/device/{deviceId}/webrtc/offer?sid={sid}&rtcServer={server,user,pass}`
3. APP 轮询后端，获取当前 `sid` 对应的 WebRTC Offer：
   - `GET /api/internal/webrtc/offer/{sid}`
4. APP 使用 Offer 创建 `RTCPeerConnection`，设置 remote description。
5. APP 创建 Answer，设置 local description，然后通过 HTTP 通知后端转发给摄像头：
   - `POST /api/internal/mqtt/device/{deviceId}/webrtc/answer?sid={sid}&sdp={answerSdp}`
6. 双方通过 ICE Candidate 建立连接：
   - 本地 candidate：APP 从 `onIceCandidate` 回调里，通过 HTTP 发给后端 → MQTT 转发给摄像头。
   - 远端 candidate：摄像头通过 MQTT 上报，后端缓存；APP 轮询接口获取并 `addIceCandidate`。
7. WebRTC 连接建立后：
   - 视频通过 WebRTC 媒体流播放；
   - 控制指令走 WebRTC DataChannel 文本消息；
   - TF 卡文件列表(`fileindex`)通过 DataChannel 二进制返回。

---

## 2. 信令 HTTP 接口

### 2.1 请求设备生成 Offer

- **Method**: `POST`
- **URL**: `/api/internal/mqtt/device/{deviceId}/webrtc/offer`
- **Query 参数**：
  - `sid` (string)：本次 WebRTC 会话 ID。
  - `rtcServer` (string)：WebRTC 服务器信息，格式：`server,user,pass`
    - 例如：`8.129.3.8:3478,test_rtc,test123456`
- **行为**：
  - 后端通过 MQTT 向摄像头发送 CODE 23 消息，让摄像头生成 WebRTC Offer 并上报。
  - APP 不需要关心响应体内容，只需保证调用成功。

### 2.2 轮询获取 Offer

- **Method**: `GET`
- **URL**: `/api/internal/webrtc/offer/{sid}`
- **返回 JSON**：

```json
{
  "found": true,
  "sid": "1764951659227",
  "sdp": "v=0...",   
  "status": 1,
  "time": 1764951688
}
```

- 说明：
  - `found = false`：还没收到该 `sid` 对应的 Offer，APP 需要 1 秒左右轮询一次。
  - `sdp`：完整的 SDP 字符串，用于构造 `SessionDescription`。

### 2.3 轮询获取远端 ICE Candidates

- **Method**: `GET`
- **URL**: `/api/internal/webrtc/candidates/{sid}`
- **返回 JSON**：

```json
{
  "found": true,
  "candidates": [
    "candidate:...",
    "candidate:..."
  ]
}
```

- 说明：
  - 每次调用会返回并**清空**缓存的候选（drain），APP 应该：
    - 对返回的每一条字符串 `c` 调用 `addIceCandidate`；
    - 然后继续 1 秒左右轮询一次。

### 2.4 发送 Answer 给设备

- **Method**: `POST`
- **URL**: `/api/internal/mqtt/device/{deviceId}/webrtc/answer`
- **Query 参数**：
  - `sid`：会话 ID，与请求 Offer 时保持一致。
  - `sdp`：APP 生成的 Answer SDP，**URL encoded**。
- **行为**：
  - 后端通过 MQTT 发送 CODE 24 消息到摄像头。

### 2.5 发送本地 ICE Candidate 给设备

- **Method**: `POST`
- **URL**: `/api/internal/mqtt/device/{deviceId}/webrtc/candidate`
- **Query 参数**：
  - `sid`：会话 ID。
  - `candidate`：从 WebRTC `onIceCandidate` 拿到的 candidate line，**URL encoded**。
- **行为**：
  - 后端通过 MQTT 发送 CODE 25 消息到摄像头。

---

## 3. APP 侧 WebRTC 配置约定

### 3.1 `rtcServer` 字符串格式

- 字符串格式：`server,user,pass`
  - 例：`8.129.3.8:3478,test_rtc,test123456`
- APP 解析逻辑：
  1. 用逗号分割为三段：`server`, `user`, `pass`。
  2. 如果 `server` 没有 `stun:` 或 `turn:` 前缀：
     - 默认作为 TURN 地址使用：`turn:{server}`。
  3. 如果以 `turn:` 开头，则：
     - `urls = turn:{server}`
     - `username = user`
     - `credential = pass`

### 3.2 ICE / TURN 配置建议

以浏览器 / Android 为例：

- **IceServer**：
  - `urls = "turn:8.129.3.8:3478"`
  - `username = user`
  - `credential = pass`
- **策略**：
  - 为避免复杂的 NAT 情况下 host/srflx 不可达，建议：
    - 如果配置中包含 TURN，则设置 `iceTransportPolicy = relay`（或对应平台的等价配置），**强制只走 TURN 中继**。

---

## 4. DataChannel 文本控制指令

WebRTC 连接建立后，摄像头会创建一个 DataChannel，APP 需要：

- 在 `onDataChannel` / `ondatachannel` 回调中拿到 `DataChannel` 实例；
- 在状态变为 `open` 后，通过 `send(text)` 发送控制字符串。

### 4.1 文本指令列表

| 指令                         | 说明                         |
|------------------------------|------------------------------|
| `left`                       | 云台左转                    |
| `right`                      | 云台右转                    |
| `up`                         | 云台上转                    |
| `down`                       | 云台下转                    |
| `mute`                       | 静音                         |
| `unmute`                     | 取消静音                     |
| `live`                       | 结束回放，切回直播          |
| `replay <ts> <ch>`           | TF 卡回放，时间戳+视频通道  |
| `fileindex YYYY-mm-dd`      | 获取指定日期的 TF 卡文件列表 |

说明：

- `replay`：
  - `ts`：整型时间戳（秒），表示回放起始时间点；
  - `ch`：视频通道索引（针对多目摄像头）。
- `fileindex`：
  - `YYYY-mm-dd`：日期字符串，摄像头返回该天所有录像文件的列表（二进制形式）。

APP 发送时使用 UTF-8 文本，示例：

```text
"left"
"replay 1700000000 0"
"fileindex 2025-12-06"
```

---

## 5. `fileindex` 二进制回复格式

摄像头通过 DataChannel 回复 TF 卡文件列表时，数据格式如下：

- 第 1 字节：ASCII 字符 `'f'` (`0x66`) 作为标识；
- 之后为 N 个 `fileIndex_T` 结构体，每个 **18 字节**：

```c
typedef struct{
    char filename[12];      // 文件名 (ASCII, 0 结尾)
    char del_flag;          // 是否已删除 (0: 未删除, 非0: 已删除)
    unsigned short duration;// 视频时长(秒), 小端序
    char reserved;          // 保留
    // 余下 2 字节保留位，总长度 18 字节
} fileIndex_T;
```

### 5.1 解析步骤（伪代码）

1. 读取第 1 字节：
   - 若不等于 `'f'`，则该包不是 `fileindex` 回复，可按普通二进制消息处理或忽略。
2. 从第 2 字节开始，每 18 字节解析一个 `fileIndex_T`：
   - `filename[0..11]`：
     - 遇到 `0x00` 认为字符串结束；
     - 使用 ASCII 解码为文件名。
   - `del_flag`：`0` 表示未删除，非 0 表示已删除。
   - `duration`：读取 2 字节，**小端序**，单位为秒。
   - `reserved`：1 字节，可忽略。
   - 最后 2 字节：保留，目前可忽略。

解析结果可映射到类似结构：

```json
{
  "filename": "20251206_120000.h264",
  "deleted": false,
  "durationSeconds": 60,
  "reserved": 0
}
```

APP 侧可以将这些结果展示为文件列表，用于选择某个录像文件进行回放（再通过 `replay` 指令指定时间点和通道）。

---

## 6. Android 参考实现思路（简版）

> 这里只给思路和关键点，真正的代码可参照仓库中 `webrtc-demo.html` 的逻辑，或根据项目实际封装。

1. **创建 PeerConnectionFactory**：
   - 使用官方 `google-webrtc` 库，初始化 `PeerConnectionFactory`。
2. **构造 ICE/TURN 配置**：
   - 从 `rtcServer` 字符串解析得到 `server,user,pass`；
   - 构造 `IceServer(urls="turn:server", username=user, credential=pass)`；
   - 若存在 TURN，则将 `RTCConfiguration.iceTransportsType` 设为 `RELAY`。
3. **请求 Offer 并设置 remote SDP**：
   - 调用 2.1 的 HTTP 接口请求 Offer；
   - 轮询 2.2，拿到 `sdp` 后：
     - `pc.setRemoteDescription(SessionDescription.Type.OFFER, sdp)`。
4. **创建 Answer 并发送给设备**：
   - `pc.createAnswer` → `pc.setLocalDescription`；
   - 通过 2.4 的 HTTP 接口发送 Answer SDP。
5. **ICE Candidate 交换**：
   - 在 `onIceCandidate` 回调里，将 `candidate.sdp` 通过 2.5 接口发送给设备；
   - 启动定时任务轮询 2.3 接口，获取 `candidates[]`：
     - 对每个字符串 `c` 构造 `IceCandidate`（`sdp = c`），执行 `addIceCandidate`。
6. **DataChannel 控制**：
   - 在 `PeerConnection.Observer.onDataChannel` 中拿到 `DataChannel` 实例；
   - 在 `DataChannel.Observer.onStateChange` 中检测状态为 `OPEN`；
   - 通过 `dataChannel.send(Buffer(ByteBuffer.wrap("left".toByteArray()), false))` 等方式发送文本指令。
7. **`fileindex` 解析**：
   - 在 `DataChannel.Observer.onMessage` 中，当 `buffer.binary == true` 时：
     - 将 `ByteBuffer` 读为 `ByteArray`；
     - 按第 5 节说明解析；
     - 将解析出的列表回调给上层 UI，用于展示 TF 卡文件列表。

---

## 7. iOS / 其他平台说明

- iOS (Swift)：可以使用 `libwebrtc` 或 WebRTC 封装库，整体流程与 Android 完全一致：
  - 使用 `RTCPeerConnection`、`RTCDataChannel`；
  - 同样的 HTTP 信令接口；
  - 同样的 DataChannel 文本指令与 `fileindex` 二进制格式。
- Flutter / React Native 等跨平台方案：
  - 只要底层使用标准 WebRTC（`RTCPeerConnection` + `RTCDataChannel`），照此文档实现即可。

如需具体平台（iOS / Flutter 等）的示例代码，可在实现时再补充对应章节。