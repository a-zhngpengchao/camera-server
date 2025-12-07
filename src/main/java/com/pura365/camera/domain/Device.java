package com.pura365.camera.domain;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

import java.time.LocalDateTime;

@TableName("device")
public class Device {
    
    @TableId(value = "id", type = IdType.INPUT)
    private String id; // 设备序列号
    
    @TableField("mac")
    private String mac;
    
    @TableField("ssid")
    private String ssid; // WiFi SSID
    
    @TableField("region")
    private String region;
    
    @TableField("name")
    private String name;
    
    @TableField("firmware_version")
    private String firmwareVersion;
    
    @TableField("status")
    private Integer status; // 0-离线 1-在线
    
    @TableField("enabled")
    private Integer enabled; // 0-禁用 1-启用
    
    // 云存储配置
    @TableField("cloud_storage")
    private Integer cloudStorage;
    
    @TableField("s3_hostname")
    private String s3Hostname;
    
    @TableField("s3_region")
    private String s3Region;
    
    @TableField("s3_access_key")
    private String s3AccessKey;
    
    @TableField("s3_secret_key")
    private String s3SecretKey;
    
    // MQTT配置
    @TableField("mqtt_hostname")
    private String mqttHostname;
    
    @TableField("mqtt_username")
    private String mqttUsername;
    
    @TableField("mqtt_password")
    private String mqttPassword;
    
    // AI配置
    @TableField("ai_enabled")
    private Integer aiEnabled;
    
    @TableField("gpt_hostname")
    private String gptHostname;
    
    @TableField("gpt_key")
    private String gptKey;
    
    @TableField("last_online_time")
    private LocalDateTime lastOnlineTime;
    
    @TableField("created_at")
    private LocalDateTime createdAt;
    
    @TableField("updated_at")
    private LocalDateTime updatedAt;
    
    // Getters and Setters
    public String getId() {
        return id;
    }
    
    public void setId(String id) {
        this.id = id;
    }
    
    public String getMac() {
        return mac;
    }
    
    public void setMac(String mac) {
        this.mac = mac;
    }
    
    public String getSsid() {
        return ssid;
    }
    
    public void setSsid(String ssid) {
        this.ssid = ssid;
    }
    
    public String getRegion() {
        return region;
    }
    
    public void setRegion(String region) {
        this.region = region;
    }
    
    public String getName() {
        return name;
    }
    
    public void setName(String name) {
        this.name = name;
    }
    
    public String getFirmwareVersion() {
        return firmwareVersion;
    }
    
    public void setFirmwareVersion(String firmwareVersion) {
        this.firmwareVersion = firmwareVersion;
    }
    
    public Integer getStatus() {
        return status;
    }
    
    public void setStatus(Integer status) {
        this.status = status;
    }
    
    public Integer getEnabled() {
        return enabled;
    }
    
    public void setEnabled(Integer enabled) {
        this.enabled = enabled;
    }
    
    public Integer getCloudStorage() {
        return cloudStorage;
    }
    
    public void setCloudStorage(Integer cloudStorage) {
        this.cloudStorage = cloudStorage;
    }
    
    public String getS3Hostname() {
        return s3Hostname;
    }
    
    public void setS3Hostname(String s3Hostname) {
        this.s3Hostname = s3Hostname;
    }
    
    public String getS3Region() {
        return s3Region;
    }
    
    public void setS3Region(String s3Region) {
        this.s3Region = s3Region;
    }
    
    public String getS3AccessKey() {
        return s3AccessKey;
    }
    
    public void setS3AccessKey(String s3AccessKey) {
        this.s3AccessKey = s3AccessKey;
    }
    
    public String getS3SecretKey() {
        return s3SecretKey;
    }
    
    public void setS3SecretKey(String s3SecretKey) {
        this.s3SecretKey = s3SecretKey;
    }
    
    public String getMqttHostname() {
        return mqttHostname;
    }
    
    public void setMqttHostname(String mqttHostname) {
        this.mqttHostname = mqttHostname;
    }
    
    public String getMqttUsername() {
        return mqttUsername;
    }
    
    public void setMqttUsername(String mqttUsername) {
        this.mqttUsername = mqttUsername;
    }
    
    public String getMqttPassword() {
        return mqttPassword;
    }
    
    public void setMqttPassword(String mqttPassword) {
        this.mqttPassword = mqttPassword;
    }
    
    public Integer getAiEnabled() {
        return aiEnabled;
    }
    
    public void setAiEnabled(Integer aiEnabled) {
        this.aiEnabled = aiEnabled;
    }
    
    public String getGptHostname() {
        return gptHostname;
    }
    
    public void setGptHostname(String gptHostname) {
        this.gptHostname = gptHostname;
    }
    
    public String getGptKey() {
        return gptKey;
    }
    
    public void setGptKey(String gptKey) {
        this.gptKey = gptKey;
    }
    
    public LocalDateTime getLastOnlineTime() {
        return lastOnlineTime;
    }
    
    public void setLastOnlineTime(LocalDateTime lastOnlineTime) {
        this.lastOnlineTime = lastOnlineTime;
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
