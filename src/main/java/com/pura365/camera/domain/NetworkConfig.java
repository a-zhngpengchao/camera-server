package com.pura365.camera.domain;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

import java.time.LocalDateTime;

@TableName("network_config")
public class NetworkConfig {
    
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;
    
    @TableField("device_id")
    private String deviceId;
    
    @TableField("ssid")
    private String ssid;
    
    @TableField("password")
    private String password;
    
    @TableField("timezone")
    private String timezone;
    
    @TableField("region")
    private String region;
    
    @TableField("ip_address")
    private String ipAddress;
    
    @TableField("config_status")
    private Integer configStatus; // 0-配网中 1-成功 2-失败
    
    @TableField("config_method")
    private String configMethod; // qrcode/ble/audio
    
    @TableField("config_source")
    private String configSource;
    
    @TableField("created_at")
    private LocalDateTime createdAt;
    
    @TableField("updated_at")
    private LocalDateTime updatedAt;
    
    // Getters and Setters
    public Long getId() {
        return id;
    }
    
    public void setId(Long id) {
        this.id = id;
    }
    
    public String getDeviceId() {
        return deviceId;
    }
    
    public void setDeviceId(String deviceId) {
        this.deviceId = deviceId;
    }
    
    public String getSsid() {
        return ssid;
    }
    
    public void setSsid(String ssid) {
        this.ssid = ssid;
    }
    
    public String getPassword() {
        return password;
    }
    
    public void setPassword(String password) {
        this.password = password;
    }
    
    public String getTimezone() {
        return timezone;
    }
    
    public void setTimezone(String timezone) {
        this.timezone = timezone;
    }
    
    public String getRegion() {
        return region;
    }
    
    public void setRegion(String region) {
        this.region = region;
    }
    
    public String getIpAddress() {
        return ipAddress;
    }
    
    public void setIpAddress(String ipAddress) {
        this.ipAddress = ipAddress;
    }
    
    public Integer getConfigStatus() {
        return configStatus;
    }
    
    public void setConfigStatus(Integer configStatus) {
        this.configStatus = configStatus;
    }
    
    public String getConfigMethod() {
        return configMethod;
    }
    
    public void setConfigMethod(String configMethod) {
        this.configMethod = configMethod;
    }
    
    public String getConfigSource() {
        return configSource;
    }
    
    public void setConfigSource(String configSource) {
        this.configSource = configSource;
    }
    
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
    
    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
    
    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }
    
    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
}
