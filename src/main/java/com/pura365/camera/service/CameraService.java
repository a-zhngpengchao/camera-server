package com.pura365.camera.service;

import com.pura365.camera.domain.Device;
import com.pura365.camera.model.GetInfoRequest;
import com.pura365.camera.model.GetInfoResponse;
import com.pura365.camera.model.ResetDeviceRequest;
import com.pura365.camera.model.SendMsgRequest;
import com.pura365.camera.repository.DeviceRepository;
import com.pura365.camera.util.TimeValidator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class CameraService {
    
    private static final Logger log = LoggerFactory.getLogger(CameraService.class);
    
    @Autowired
    private DeviceRepository deviceRepository;
    
    /**
     * 获取设备信息
     * 设备调用此接口时，自动新增或更新设备记录
     */
    public GetInfoResponse getDeviceInfo(GetInfoRequest info) {
        log.info("设备请求配置信息 - ID: {}, MAC: {}, Region: {}", info.getId(), info.getMac(), info.getRegion());
        
        // 查找或创建设备记录
        try {
            Device device = deviceRepository.selectById(info.getId());
            if (device == null) {
                // 设备不存在，创建新记录
                device = new Device();
                device.setId(info.getId());
                device.setMac(info.getMac() != null ? info.getMac() : "UNKNOWN");
                device.setRegion(info.getRegion());
                device.setStatus(0); // 默认离线，等待 MQTT 连接后更新为在线
                device.setEnabled(1);
                device.setCreatedAt(LocalDateTime.now());
                device.setUpdatedAt(LocalDateTime.now());
                deviceRepository.insert(device);
                log.info("新设备入库成功 - ID: {}, MAC: {}", info.getId(), info.getMac());
            } else {
                // 设备已存在，更新信息
                boolean needUpdate = false;
                if (info.getMac() != null && !info.getMac().isEmpty() && !info.getMac().equals(device.getMac())) {
                    device.setMac(info.getMac());
                    needUpdate = true;
                }
                if (info.getRegion() != null && !info.getRegion().equals(device.getRegion())) {
                    device.setRegion(info.getRegion());
                    needUpdate = true;
                }
                if (needUpdate) {
                    device.setUpdatedAt(LocalDateTime.now());
                    deviceRepository.updateById(device);
                    log.info("设备信息已更新 - ID: {}, MAC: {}, Region: {}", info.getId(), info.getMac(), info.getRegion());
                }
            }
        } catch (Exception e) {
            log.error("设备入库/更新失败 - ID: {}", info.getId(), e);
        }
        
        // 构建响应
        GetInfoResponse response = new GetInfoResponse();
        response.setDeviceID(info.getId());
        response.setDeviceEnable(true);
        
        // MQTT 配置
        response.setMqttHostname("mqtts://cam.pura365.cn:8883");
        response.setMqttUser("camera_test");
        response.setMqttPass("123456");
        
        // 以下配置可以后续从数据库读取
        // response.setCloudStorage(1);
        // response.setS3Hostname("s3.pura365.com");
        // response.setS3Region("us-east-1");
        // response.setS3AccessKey("your-access-key");
        // response.setS3SecretKey("your-secret-key");
        // response.setGPTHostname("ai.pura365.com");
        // response.setGPTKey("gpt-access-key");
        
        return response;
    }

    /**
     * 重置设备
     * 清除该设备的历史数据，包括:
     * 1. APP中该设备的分享信息
     * 2. 之前APP的已连接信息
     * 3. 云存储中的历史数据
     * 
     * TODO: 后续需要实现真实的数据清理逻辑
     */
    public int resetDevice(ResetDeviceRequest request) {
        log.info("重置设备 - ID: {}, MAC: {}", request.getId(), request.getMac());
        
        // TODO: 实现以下逻辑
        // 1. 验证设备序列号和MAC地址是否匹配
        // 2. 清除设备分享信息（从数据库删除相关记录）
        // 3. 清除APP连接信息
        // 4. 调用云存储API清除历史数据
        // 5. 记录操作日志
        
        // 模拟成功
        log.info("设备重置成功 - ID: {}", request.getId());
        return 0; // 0表示成功
    }
    
    /**
     * 处理摄像头发送的消息通知
     * 用于接收事件信息或AI结果
     * 
     * TODO: 后续需要实现消息推送、存储等逻辑
     */
    public void handleMessage(SendMsgRequest request) {
        log.info("收到摄像头消息 - Topic: {}, Title: {}, Msg: {}", 
                request.getTopic(), request.getTitle(), request.getMsg());
        
        // TODO: 实现以下逻辑
        // 1. 将消息存储到数据库（事件表/告警表）
        // 2. 根据topic判断消息类型并分类处理
        // 3. 如果是告警消息，推送给相关用户（APP推送、短信、邮件等）
        // 4. 如果是AI结果，关联到对应的事件记录
        // 5. 记录消息处理日志
        
        log.info("消息处理完成 - Topic: {}", request.getTopic());
    }
    
    /**
     * 验证时间戳是否有效
     * @deprecated 使用 TimeValidator.isValid() 替代
     */
    @Deprecated
    public boolean validateRequest(Long exp) {
        return TimeValidator.isValid(exp);
    }
}
