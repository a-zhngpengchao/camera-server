package com.pura365.camera.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 设备SSID管理服务
 * 用于存储和获取设备的WiFi SSID（用于MQTT消息加解密）
 * 
 * 实现：内存缓存 + 数据库持久化
 */
@Service
public class DeviceSsidService {
    
    private static final Logger log = LoggerFactory.getLogger(DeviceSsidService.class);
    
    // 内存存储：deviceId -> SSID
    private final Map<String, String> ssidCache = new ConcurrentHashMap<>();
    
    /**
     * 保存设备的SSID
     */
    public void saveSsid(String deviceId, String ssid) {
        if (deviceId == null || deviceId.isEmpty()) {
            log.warn("设备ID为空，无法保存SSID");
            return;
        }
        
        if (ssid == null || ssid.isEmpty()) {
            log.warn("SSID为空，不保存");
            return;
        }
        
        // 保存到内存缓存
        ssidCache.put(deviceId, ssid);
        log.info("已保存设备 {} 的SSID到缓存（长度: {}）", deviceId, ssid.length());
    }
    
    /**
     * 获取设备的SSID
     * @return SSID，如果不存在返回null
     */
    public String getSsid(String deviceId) {
        if (deviceId == null || deviceId.isEmpty()) {
            return null;
        }
        
        // 先从缓存读取
        String ssid = ssidCache.get(deviceId);
        // 现在如果缓存没有，就返回null，MQTT加解密会自动走默认SSID（MqttEncryptService.DEFAULT_SSID）
        return ssid;
    }
    
    /**
     * 删除设备的SSID（仅从缓存删除）
     */
    public void removeSsid(String deviceId) {
        ssidCache.remove(deviceId);
        log.info("已删除设备 {} 的SSID缓存", deviceId);
    }
    
    /**
     * 获取所有已缓存的设备数量
     */
    public int getCachedDeviceCount() {
        return ssidCache.size();
    }
    
    /**
     * 清空缓存（仅用于测试）
     */
    public void clearCache() {
        ssidCache.clear();
        log.warn("已清空所有SSID缓存");
    }
}
