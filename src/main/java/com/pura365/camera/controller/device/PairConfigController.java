package com.pura365.camera.controller.device;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

import com.pura365.camera.model.PairConfig;
import com.pura365.camera.service.DeviceSsidService;
import com.pura365.camera.util.PairConfigUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@Tag(name = "配对配置", description = "设备配对相关接口")
@RestController
@RequestMapping("/api/device/network/pair")
public class PairConfigController {

    private static final Logger log = LoggerFactory.getLogger(PairConfigController.class);

    private final DeviceSsidService deviceSsidService;

    public PairConfigController(DeviceSsidService deviceSsidService) {
        this.deviceSsidService = deviceSsidService;
    }

    /**
     * 解析配网内容（来自二维码/音频/BLE 原始字符串），返回 ssid 等字段
     */
    @PostMapping(value = "/parse", consumes = MediaType.TEXT_PLAIN_VALUE)
    public ResponseEntity<?> parsePairContent(
            @RequestParam(required = false) String deviceId,
            @RequestBody String content) {

        try {
            PairConfig cfg = PairConfigUtil.parsePairString(content);
            log.info("解析配网内容成功, SSID={}, TZ={}, Region={}",
                    cfg.getSsid(), cfg.getTimezone(), cfg.getRegion());

            // 如果已经知道 deviceId，可以顺手把 SSID 保存进缓存（将来可配合数据库）?
            if (deviceId != null && !deviceId.isEmpty() && cfg.getSsid() != null) {
                deviceSsidService.saveSsid(deviceId, cfg.getSsid());
            }

            Map<String, Object> result = new HashMap<>();
            result.put("success", true);
            result.put("ssid", cfg.getSsid());
            result.put("password", cfg.getPassword());
            result.put("timezone", cfg.getTimezone());
            result.put("region", cfg.getRegion());
            return ResponseEntity.ok(result);

        } catch (Exception e) {
            log.warn("解析配网内容失败: {}", e.getMessage());
            Map<String, Object> result = new HashMap<>();
            result.put("success", false);
            result.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(result);
        }
    }
}
