package com.pura365.camera.controller.device;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.pura365.camera.domain.*;
import com.pura365.camera.model.ApiResponse;
import com.pura365.camera.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.text.SimpleDateFormat;
import java.util.*;

/**
 * App 侧设备相关接口
 * 
 * 包含：
 * - 设备列表
 * - 设备详情
 * - 添加设备
 * - 删除设备
 * - 更新设备信息
 * - 设备设置
 * - 本地录像列表
 * - 云台控制
 */
@Tag(name = "设备管理", description = "设备增删改查、设置等管理接口")
@RestController
@RequestMapping("/api/device/devices")
public class DeviceController {

    @Autowired
    private DeviceRepository deviceRepository;

    @Autowired
    private UserDeviceRepository userDeviceRepository;

    @Autowired
    private DeviceSettingsRepository deviceSettingsRepository;

    @Autowired
    private CloudSubscriptionRepository cloudSubscriptionRepository;

    @Autowired
    private LocalVideoRepository localVideoRepository;

    @Autowired
    private DeviceRecordRepository deviceRecordRepository;

    @Autowired
    private DeviceBindingRepository deviceBindingRepository;

    @Autowired
    private com.pura365.camera.service.MqttMessageService mqttMessageService;

    /**
     * 设备列表 - GET /devices
     */
    @GetMapping
    public ApiResponse<List<Map<String, Object>>> listDevices(@RequestAttribute("currentUserId") Long currentUserId) {
        QueryWrapper<UserDevice> qw = new QueryWrapper<>();
        qw.lambda().eq(UserDevice::getUserId, currentUserId);
        List<UserDevice> bindings = userDeviceRepository.selectList(qw);
        List<Map<String, Object>> list = new ArrayList<>();
        if (bindings != null) {
            for (UserDevice ud : bindings) {
                Device device = deviceRepository.selectById(ud.getDeviceId());
                if (device == null) continue;
                Map<String, Object> item = buildDeviceListItem(currentUserId, device);
                list.add(item);
            }
        }
        return ApiResponse.success(list);
    }

    /**
     * 设备详情 - GET /devices/{id}/info
     */
    @GetMapping("/{id}/info")
    public ApiResponse<Map<String, Object>> getDeviceInfo(@RequestAttribute("currentUserId") Long currentUserId,
                                                          @PathVariable("id") String deviceId) {
        Device device = deviceRepository.selectById(deviceId);
        if (device == null) {
            return ApiResponse.error(404, "设备不存在");
        }
        Map<String, Object> data = buildDeviceDetail(currentUserId, device);
        return ApiResponse.success(data);
    }

    /**
     * 添加设备 - POST /devices/add
     */
    @PostMapping("/add")
    public ApiResponse<Map<String, Object>> addDevice(@RequestAttribute("currentUserId") Long currentUserId,
                                                      @RequestBody Map<String, String> body) {
        String deviceId = body.get("device_id");
        String name = body.get("name");
        String wifiSsid = body.get("wifi_ssid");
        String wifiPassword = body.get("wifi_password");
        if (deviceId == null || deviceId.isEmpty()) {
            return ApiResponse.error(400, "device_id 不能为空");
        }
        // 创建设备（如果不存在）
        Device device = deviceRepository.selectById(deviceId);
        if (device == null) {
            device = new Device();
            device.setId(deviceId);
            device.setName(name);
            device.setSsid(wifiSsid);
            device.setStatus(0); // 默认离线
            device.setEnabled(1);
            deviceRepository.insert(device);
        } else {
            if (name != null && !name.isEmpty()) {
                device.setName(name);
                deviceRepository.updateById(device);
            }
        }
        // 绑定用户与设备
        bindUserDeviceOnce(currentUserId, deviceId);
        // 可选：记录绑定信息（视为已成功）
        DeviceBinding binding = new DeviceBinding();
        binding.setDeviceId(deviceId);
        binding.setDeviceSn(deviceId);
        binding.setUserId(currentUserId);
        binding.setWifiSsid(wifiSsid);
        binding.setWifiPassword(wifiPassword);
        binding.setStatus("success");
        binding.setProgress(100);
        binding.setMessage("绑定成功");
        deviceBindingSafeInsert(binding);

        Map<String, Object> data = new HashMap<>();
        data.put("id", device.getId());
        data.put("name", device.getName());
        data.put("model", null); // 暂无型号字段
        data.put("status", device.getStatus() != null && device.getStatus() == 1 ? "online" : "offline");
        return ApiResponse.success(data);
    }

    /**
     * 删除/解绑设备 - DELETE /devices/{id}
     */
    @DeleteMapping("/{id}")
    public ApiResponse<Void> deleteDevice(@RequestAttribute("currentUserId") Long currentUserId,
                                          @PathVariable("id") String deviceId) {
        QueryWrapper<UserDevice> qw = new QueryWrapper<>();
        qw.lambda().eq(UserDevice::getUserId, currentUserId)
                .eq(UserDevice::getDeviceId, deviceId);
        userDeviceRepository.delete(qw);
        return ApiResponse.success(null);
    }

    /**
     * 更新设备信息 - 目前只支持修改名称
     */
    @PutMapping("/{id}")
    public ApiResponse<Map<String, Object>> updateDevice(@RequestAttribute("currentUserId") Long currentUserId,
                                                         @PathVariable("id") String deviceId,
                                                         @RequestBody Map<String, String> body) {
        Device device = deviceRepository.selectById(deviceId);
        if (device == null) {
            return ApiResponse.error(404, "设备不存在");
        }
        String name = body.get("name");
        if (name != null && !name.isEmpty()) {
            device.setName(name);
            deviceRepository.updateById(device);
        }
        Map<String, Object> data = new HashMap<>();
        data.put("id", device.getId());
        data.put("name", device.getName());
        return ApiResponse.success(data);
    }

    /**
     * 更新设备设置 - PUT /devices/{id}/settings
     */
    @PutMapping("/{id}/settings")
    public ApiResponse<Map<String, Object>> updateDeviceSettings(@RequestAttribute("currentUserId") Long currentUserId,
                                                                 @PathVariable("id") String deviceId,
                                                                 @RequestBody Map<String, Object> body) {
        // 确保当前用户有这个设备
        if (!hasUserDevice(currentUserId, deviceId)) {
            return ApiResponse.error(403, "无权操作该设备");
        }
        DeviceSettings settings = getOrCreateSettings(deviceId);
        if (body.containsKey("motion_detection")) {
            settings.setMotionDetection(boolToInt(body.get("motion_detection"))); 
        }
        if (body.containsKey("night_vision")) {
            settings.setNightVision(boolToInt(body.get("night_vision")));
        }
        if (body.containsKey("audio_enabled")) {
            settings.setAudioEnabled(boolToInt(body.get("audio_enabled")));
        }
        if (body.containsKey("flip_image")) {
            settings.setFlipImage(boolToInt(body.get("flip_image")));
        }
        if (body.containsKey("sensitivity")) {
            Object s = body.get("sensitivity");
            settings.setSensitivity(s != null ? s.toString() : null);
        }
        if (settings.getId() == null) {
            deviceSettingsRepository.insert(settings);
        } else {
            deviceSettingsRepository.updateById(settings);
        }
        Map<String, Object> data = new HashMap<>();
        data.put("motion_detection", settings.getMotionDetection() != null && settings.getMotionDetection() == 1);
        data.put("night_vision", settings.getNightVision() != null && settings.getNightVision() == 1);
        data.put("audio_enabled", settings.getAudioEnabled() != null && settings.getAudioEnabled() == 1);
        data.put("flip_image", settings.getFlipImage() != null && settings.getFlipImage() == 1);
        data.put("sensitivity", settings.getSensitivity());
        return ApiResponse.success(data);
    }

    /**
     * 本地录像列表 - GET /devices/{id}/local-videos
     */
    @GetMapping("/{id}/local-videos")
    public ApiResponse<Map<String, Object>> listLocalVideos(@RequestAttribute("currentUserId") Long currentUserId,
                                                            @PathVariable("id") String deviceId,
                                                            @RequestParam(value = "date", required = false) String date,
                                                            @RequestParam(value = "page", required = false, defaultValue = "1") int page,
                                                            @RequestParam(value = "page_size", required = false, defaultValue = "20") int pageSize) {
        if (!hasUserDevice(currentUserId, deviceId)) {
            return ApiResponse.error(403, "无权查看该设备");
        }
        if (page < 1) page = 1;
        if (pageSize <= 0) pageSize = 20;
        int offset = (page - 1) * pageSize;

        QueryWrapper<LocalVideo> qw = new QueryWrapper<>();
        qw.lambda().eq(LocalVideo::getDeviceId, deviceId);
        if (date != null && !date.isEmpty()) {
            // 简单按日期前缀过滤 created_at 的日期部分
            qw.apply("DATE(created_at) = {0}", date);
        }
        qw.orderByDesc("created_at");

        int total = localVideoRepository.selectCount(qw).intValue();
        List<LocalVideo> rows = localVideoRepository.selectList(qw.last("limit " + offset + "," + pageSize));

        List<Map<String, Object>> list = new ArrayList<>();
        if (rows != null) {
            for (LocalVideo v : rows) {
                Map<String, Object> item = new HashMap<>();
                item.put("id", v.getVideoId());
                item.put("device_id", v.getDeviceId());
                item.put("type", v.getType());
                item.put("title", v.getTitle());
                item.put("thumbnail_url", v.getThumbnail());
                item.put("video_url", v.getVideoUrl());
                item.put("duration", v.getDuration());
                item.put("size", v.getSize());
                if (v.getCreatedAt() != null) {
                    item.put("created_at", formatIsoTime(v.getCreatedAt()));
                }
                list.add(item);
            }
        }

        Map<String, Object> data = new HashMap<>();
        data.put("list", list);
        data.put("total", total);
        data.put("page", page);
        data.put("page_size", pageSize);
        return ApiResponse.success(data);
    }

    /**
     * 云台控制 - POST /devices/{id}/ptz
     *
     * RequestBody: { "direction": "up", "speed": 5 }
     * direction: up/down/left/right/stop
     */
    @PostMapping("/{id}/ptz")
    public ApiResponse<Void> ptzControl(@RequestAttribute("currentUserId") Long currentUserId,
                                        @PathVariable("id") String deviceId,
                                        @RequestBody Map<String, Object> body) {
        if (!hasUserDevice(currentUserId, deviceId)) {
            return ApiResponse.error(403, "无权操作该设备");
        }
        Object dirObj = body.get("direction");
        if (dirObj == null) {
            return ApiResponse.error(400, "direction 不能为空");
        }
        String direction = dirObj.toString();
        // 通过 MQTT 发送一个 DataChannel 控制指令（与 DataChannelController 使用同一 CODE 99 协议）
        try {
            Map<String, Object> msg = new HashMap<>();
            msg.put("code", 99);
            msg.put("time", com.pura365.camera.util.TimeValidator.getCurrentTimestamp());
            msg.put("command", direction);
            mqttMessageService.sendToDevice(deviceId, msg, null);
            return ApiResponse.success(null);
        } catch (Exception e) {
            return ApiResponse.error(500, "发送 PTZ 指令失败: " + e.getMessage());
        }
    }

    // ===== 辅助方法 =====

    private Map<String, Object> buildDeviceListItem(Long userId, Device device) {
        Map<String, Object> m = new HashMap<>();
        m.put("id", device.getId());
        m.put("name", device.getName());
        m.put("model", null); // 当前未有型号字段
        m.put("status", device.getStatus() != null && device.getStatus() == 1 ? "online" : "offline");
        // 云存状态
        CloudSubscription sub = findActiveSubscription(userId, device.getId());
        boolean hasCloud = sub != null && (sub.getExpireAt() == null || sub.getExpireAt().after(new Date()));
        m.put("has_cloud_storage", hasCloud);
        if (hasCloud && sub.getExpireAt() != null) {
            m.put("cloud_expire_at", formatIsoTime(sub.getExpireAt()));
        } else {
            m.put("cloud_expire_at", null);
        }
        m.put("thumbnail_url", null); // 暂无缩略图字段
        if (device.getLastOnlineTime() != null) {
            m.put("last_online_at", device.getLastOnlineTime().toString());
        } else {
            m.put("last_online_at", null);
        }
        return m;
    }

    private Map<String, Object> buildDeviceDetail(Long userId, Device device) {
        Map<String, Object> m = new HashMap<>();
        m.put("id", device.getId());
        m.put("name", device.getName());
        m.put("model", null);
        m.put("firmware_version", device.getFirmwareVersion());
        m.put("status", device.getStatus() != null && device.getStatus() == 1 ? "online" : "offline");

        CloudSubscription sub = findActiveSubscription(userId, device.getId());
        boolean hasCloud = sub != null && (sub.getExpireAt() == null || sub.getExpireAt().after(new Date()));
        m.put("has_cloud_storage", hasCloud);
        if (hasCloud && sub.getExpireAt() != null) {
            m.put("cloud_expire_at", formatIsoTime(sub.getExpireAt()));
        } else {
            m.put("cloud_expire_at", null);
        }
        m.put("thumbnail_url", null);
        m.put("wifi_ssid", device.getSsid());
        m.put("wifi_signal", null); // 目前未存RSSI，可后续接设备上报

        Map<String, Object> sd = new HashMap<>();
        sd.put("total", 0);
        sd.put("used", 0);
        sd.put("available", 0);
        m.put("sd_card", sd);

        DeviceSettings settings = getOrCreateSettings(device.getId());
        Map<String, Object> set = new HashMap<>();
        set.put("motion_detection", settings.getMotionDetection() != null && settings.getMotionDetection() == 1);
        set.put("night_vision", settings.getNightVision() != null && settings.getNightVision() == 1);
        set.put("audio_enabled", settings.getAudioEnabled() != null && settings.getAudioEnabled() == 1);
        set.put("flip_image", settings.getFlipImage() != null && settings.getFlipImage() == 1);
        set.put("sensitivity", settings.getSensitivity());
        m.put("settings", set);

        if (device.getLastOnlineTime() != null) {
            m.put("last_online_at", device.getLastOnlineTime().toString());
        } else {
            m.put("last_online_at", null);
        }
        return m;
    }

    private CloudSubscription findActiveSubscription(Long userId, String deviceId) {
        QueryWrapper<CloudSubscription> qw = new QueryWrapper<>();
        qw.lambda().eq(CloudSubscription::getUserId, userId)
                .eq(CloudSubscription::getDeviceId, deviceId)
                .orderByDesc(CloudSubscription::getExpireAt)
                .last("limit 1");
        return cloudSubscriptionRepository.selectOne(qw);
    }

    private boolean hasUserDevice(Long userId, String deviceId) {
        QueryWrapper<UserDevice> qw = new QueryWrapper<>();
        qw.lambda().eq(UserDevice::getUserId, userId)
                .eq(UserDevice::getDeviceId, deviceId);
        Integer count = userDeviceRepository.selectCount(qw).intValue();
        return count != null && count > 0;
    }

    private void bindUserDeviceOnce(Long userId, String deviceId) {
        QueryWrapper<UserDevice> qw = new QueryWrapper<>();
        qw.lambda().eq(UserDevice::getUserId, userId)
                .eq(UserDevice::getDeviceId, deviceId);
        if (userDeviceRepository.selectCount(qw) == 0) {
            UserDevice ud = new UserDevice();
            ud.setUserId(userId);
            ud.setDeviceId(deviceId);
            ud.setRole("owner");
            userDeviceRepository.insert(ud);
        }
    }

    private DeviceSettings getOrCreateSettings(String deviceId) {
        QueryWrapper<DeviceSettings> qw = new QueryWrapper<>();
        qw.lambda().eq(DeviceSettings::getDeviceId, deviceId).last("limit 1");
        DeviceSettings settings = deviceSettingsRepository.selectOne(qw);
        if (settings == null) {
            settings = new DeviceSettings();
            settings.setDeviceId(deviceId);
            settings.setMotionDetection(0);
            settings.setNightVision(0);
            settings.setAudioEnabled(1);
            settings.setFlipImage(0);
            settings.setSensitivity("medium");
        }
        return settings;
    }

    private int boolToInt(Object value) {
        if (value == null) return 0;
        if (value instanceof Boolean) {
            return (Boolean) value ? 1 : 0;
        }
        return Boolean.parseBoolean(value.toString()) ? 1 : 0;
    }

    private String formatIsoTime(Date date) {
        if (date == null) return null;
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
        sdf.setTimeZone(TimeZone.getTimeZone("UTC"));
        return sdf.format(date);
    }

    private void deviceBindingSafeInsert(DeviceBinding binding) {
        try {
            // 直接插入绑定记录（前端正常不会高频重复调用）
            deviceBindingRepository.insert(binding);
        } catch (Exception ignore) {
            // 如果插入失败（例如唯一性冲突等），这里简单忽略，不影响主流程
        }
    }
}
