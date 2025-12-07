package com.pura365.camera.controller.device;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.pura365.camera.domain.LiveStream;
import com.pura365.camera.domain.UserDevice;
import com.pura365.camera.model.ApiResponse;
import com.pura365.camera.repository.LiveStreamRepository;
import com.pura365.camera.repository.UserDeviceRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.text.SimpleDateFormat;
import java.util.*;

/**
 * 直播流相关接口
 *
 * 对应 api_spec.md 中的接口:
 * - POST /devices/{id}/stream/start
 * - POST /devices/{id}/stream/stop
 */
@Tag(name = "视频流管理", description = "设备直播流启动和停止接口")
@RestController
@RequestMapping("/api/device/devices")
public class StreamController {

    @Autowired
    private LiveStreamRepository liveStreamRepository;

    @Autowired
    private UserDeviceRepository userDeviceRepository;

    private final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * 开始直播流 - POST /devices/{id}/stream/start
     */
    @Operation(summary = "开始直播流", description = "启动指定设备的直播流")
    @PostMapping("/{id}/stream/start")
    public ApiResponse<Map<String, Object>> startStream(@RequestAttribute("currentUserId") Long currentUserId,
                                                        @PathVariable("id") String deviceId,
                                                        @RequestBody(required = false) Map<String, String> body) {
        if (!hasUserDevice(currentUserId, deviceId)) {
            return ApiResponse.error(403, "无权操作该设备");
        }
        String quality = body != null && body.get("quality") != null ? body.get("quality") : "hd";
        String protocol = body != null && body.get("protocol") != null ? body.get("protocol") : "webrtc";
        if (!"webrtc".equalsIgnoreCase(protocol)) {
            return ApiResponse.error(400, "当前仅支持 protocol = webrtc");
        }

        String streamId = "stream_" + System.currentTimeMillis();
        Date now = new Date();
        Date expiresAt = new Date(System.currentTimeMillis() + 60 * 60 * 1000L); // 默认 1 小时有效

        // 根据信令服务约定构造 WebRTC 信令 URL，前端按文档调用 /api/webrtc 即可
        String signalingUrl = ServletUriComponentsBuilder.fromCurrentContextPath()
                .path("/api/webrtc")
                .toUriString();

        List<Map<String, Object>> iceServers = new ArrayList<>();
        Map<String, Object> stun = new HashMap<>();
        stun.put("urls", "stun:stun.l.google.com:19302");
        iceServers.add(stun);

        LiveStream live = new LiveStream();
        live.setStreamId(streamId);
        live.setDeviceId(deviceId);
        live.setUserId(currentUserId);
        live.setProtocol(protocol);
        live.setQuality(quality);
        live.setSignalingUrl(signalingUrl);
        live.setCreatedAt(now);
        live.setExpiresAt(expiresAt);
        try {
            live.setIceServers(objectMapper.writeValueAsString(iceServers));
        } catch (JsonProcessingException e) {
            live.setIceServers(null);
        }
        liveStreamRepository.insert(live);

        Map<String, Object> data = new HashMap<>();
        data.put("stream_id", streamId);
        data.put("protocol", protocol);
        data.put("signaling_url", signalingUrl);
        data.put("ice_servers", iceServers);
        data.put("expires_at", formatIsoTime(expiresAt));
        return ApiResponse.success(data);
    }

    /**
     * 停止直播流 - POST /devices/{id}/stream/stop
     */
    @Operation(summary = "停止直播流", description = "停止指定设备的直播流")
    @PostMapping("/{id}/stream/stop")
    public ApiResponse<Void> stopStream(@RequestAttribute("currentUserId") Long currentUserId,
                                        @PathVariable("id") String deviceId,
                                        @RequestBody Map<String, String> body) {
        if (!hasUserDevice(currentUserId, deviceId)) {
            return ApiResponse.error(403, "无权操作该设备");
        }
        String streamId = body != null ? body.get("stream_id") : null;
        if (streamId == null || streamId.isEmpty()) {
            return ApiResponse.error(400, "stream_id 不能为空");
        }
        QueryWrapper<LiveStream> qw = new QueryWrapper<>();
        qw.lambda().eq(LiveStream::getStreamId, streamId)
                .eq(LiveStream::getDeviceId, deviceId)
                .eq(LiveStream::getUserId, currentUserId)
                .last("limit 1");
        LiveStream live = liveStreamRepository.selectOne(qw);
        if (live != null) {
            live.setExpiresAt(new Date());
            liveStreamRepository.updateById(live);
        }
        return ApiResponse.success("直播流已停止", null);
    }

    private boolean hasUserDevice(Long userId, String deviceId) {
        QueryWrapper<UserDevice> qw = new QueryWrapper<>();
        qw.lambda().eq(UserDevice::getUserId, userId)
                .eq(UserDevice::getDeviceId, deviceId);
        Integer count = userDeviceRepository.selectCount(qw).intValue();
        return count != null && count > 0;
    }

    private String formatIsoTime(Date date) {
        if (date == null) return null;
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
        sdf.setTimeZone(TimeZone.getTimeZone("UTC"));
        return sdf.format(date);
    }
}