package com.pura365.camera.controller.device;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.pura365.camera.domain.Device;
import com.pura365.camera.domain.DeviceBinding;
import com.pura365.camera.domain.UserDevice;
import com.pura365.camera.domain.WifiHistory;
import com.pura365.camera.model.ApiResponse;
import com.pura365.camera.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.*;

/**
 * WiFi 配网 & 设备绑定相关接口
 *
 * - GET /wifi/scan
 * - POST /devices/bind
 * - GET /devices/{id}/binding-status
 */
@Tag(name = "WiFi管理", description = "WiFi配置相关接口")
@RestController
@RequestMapping("/api/device")
public class WifiController {

    @Autowired
    private WifiHistoryRepository wifiHistoryRepository;

    @Autowired
    private DeviceRepository deviceRepository;

    @Autowired
    private UserDeviceRepository userDeviceRepository;

    @Autowired
    private DeviceBindingRepository deviceBindingRepository;

    /**
     * WiFi 列表 - GET /wifi/scan
     *
     * 目前从 wifi_history 返回当前用户最近使用的 WiFi 记录
     */
    @Operation(summary = "扫描 WiFi 列表", description = "返回当前用户最近使用的 WiFi 记录（wifi_history）")
    @GetMapping("/wifi/scan")
    public ApiResponse<List<Map<String, Object>>> scanWifi(@RequestAttribute("currentUserId") Long currentUserId) {
        QueryWrapper<WifiHistory> qw = new QueryWrapper<>();
        qw.lambda().eq(WifiHistory::getUserId, currentUserId)
                .orderByDesc(WifiHistory::getLastUsedAt)
                .last("limit 20");
        List<WifiHistory> list = wifiHistoryRepository.selectList(qw);
        List<Map<String, Object>> result = new ArrayList<>();
        if (list != null) {
            for (WifiHistory w : list) {
                Map<String, Object> m = new HashMap<>();
                m.put("ssid", w.getSsid());
                m.put("signal", w.getSignal());
                m.put("security", w.getSecurity());
                m.put("is_connected", w.getIsConnected() != null && w.getIsConnected() == 1);
                result.add(m);
            }
        }
        return ApiResponse.success(result);
    }

    /**
     * 设备绑定 - POST /devices/bind
     *
     * RequestBody 示例:
     * {
     *   "device_sn": "设备序列号",
     *   "device_name": "客厅摄像头",
     *   "wifi_ssid": "ACCGE-5G",
     *   "wifi_password": "password123"
     * }
     */
    @Operation(summary = "设备绑定", description = "绑定设备并记录 WiFi 信息")
    @PostMapping("/devices/bind")
    public ApiResponse<Map<String, Object>> bindDevice(@RequestAttribute("currentUserId") Long currentUserId,
                                                       @RequestBody Map<String, String> body) {
        String deviceSn = body.get("device_sn");
        String deviceName = body.get("device_name");
        String wifiSsid = body.get("wifi_ssid");
        String wifiPassword = body.get("wifi_password");
        if (deviceSn == null || deviceSn.isEmpty()) {
            return ApiResponse.error(400, "device_sn 不能为空");
        }
        // 创建设备（如果不存在）
        Device device = deviceRepository.selectById(deviceSn);
        if (device == null) {
            device = new Device();
            device.setId(deviceSn);
            device.setName(deviceName);
            device.setSsid(wifiSsid);
            device.setStatus(0);
            device.setEnabled(1);
            deviceRepository.insert(device);
        } else {
            if (deviceName != null && !deviceName.isEmpty()) {
                device.setName(deviceName);
                deviceRepository.updateById(device);
            }
        }
        // 绑定关系
        QueryWrapper<UserDevice> qw = new QueryWrapper<>();
        qw.lambda().eq(UserDevice::getUserId, currentUserId)
                .eq(UserDevice::getDeviceId, deviceSn);
        if (userDeviceRepository.selectCount(qw) == 0) {
            UserDevice ud = new UserDevice();
            ud.setUserId(currentUserId);
            ud.setDeviceId(deviceSn);
            ud.setRole("owner");
            userDeviceRepository.insert(ud);
        }
        // 创建 binding 记录，初始状态 binding
        DeviceBinding binding = new DeviceBinding();
        binding.setDeviceId(deviceSn);
        binding.setDeviceSn(deviceSn);
        binding.setUserId(currentUserId);
        binding.setWifiSsid(wifiSsid);
        binding.setWifiPassword(wifiPassword);
        binding.setStatus("binding");
        binding.setProgress(0);
        binding.setMessage("正在配置WiFi");
        deviceBindingRepository.insert(binding);

        // 新增 wifi_history 记录
        if (wifiSsid != null && !wifiSsid.isEmpty()) {
            WifiHistory history = new WifiHistory();
            history.setUserId(currentUserId);
            history.setSsid(wifiSsid);
            history.setSignal(null);
            history.setSecurity("WPA2");
            history.setIsConnected(0);
            history.setLastUsedAt(new Date());
            wifiHistoryRepository.insert(history);
        }

        Map<String, Object> data = new HashMap<>();
        data.put("id", device.getId());
        data.put("name", device.getName());
        data.put("status", "binding");
        data.put("binding_progress", 0);
        return ApiResponse.success(data);
    }

    /**
     * 查询绑定进度 - GET /devices/{id}/binding-status
     */
    @Operation(summary = "查询绑定进度", description = "查询设备绑定进度和状态")
    @GetMapping("/devices/{id}/binding-status")
    public ApiResponse<Map<String, Object>> getBindingStatus(@RequestAttribute("currentUserId") Long currentUserId,
                                                             @PathVariable("id") String deviceId) {
        QueryWrapper<DeviceBinding> qw = new QueryWrapper<>();
        qw.lambda().eq(DeviceBinding::getUserId, currentUserId)
                .eq(DeviceBinding::getDeviceId, deviceId)
                .orderByDesc(DeviceBinding::getCreatedAt)
                .last("limit 1");
        DeviceBinding binding = deviceBindingRepository.selectOne(qw);
        Map<String, Object> data = new HashMap<>();
        if (binding == null) {
            data.put("status", "binding");
            data.put("progress", 0);
            data.put("message", "未找到绑定记录");
        } else {
            data.put("status", binding.getStatus());
            data.put("progress", binding.getProgress());
            data.put("message", binding.getMessage());
        }
        return ApiResponse.success(data);
    }
}