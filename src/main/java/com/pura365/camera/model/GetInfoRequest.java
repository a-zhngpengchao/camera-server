package com.pura365.camera.model;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

public class GetInfoRequest {
    //@NotBlank(message = "序列号不能为空")
    private String id;
    
    //@NotNull(message = "时间戳不能为空")
    private Long exp;
    
    //@NotBlank(message = "MAC地址不能为空")
    private String mac;
    
    private String region;
    
    // 可选：WiFi SSID（用于MQTT消息加解密）
    private String ssid;
    
    // getters and setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    
    public Long getExp() { return exp; }
    public void setExp(Long exp) { this.exp = exp; }
    
    public String getMac() { return mac; }
    public void setMac(String mac) { this.mac = mac; }
    
    public String getRegion() { return region; }
    public void setRegion(String region) { this.region = region; }
    
    public String getSsid() { return ssid; }
    public void setSsid(String ssid) { this.ssid = ssid; }
}
