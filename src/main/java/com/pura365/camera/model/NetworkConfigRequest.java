package com.pura365.camera.model;

import javax.validation.constraints.NotBlank;

public class NetworkConfigRequest {
    
    @NotBlank(message = "设备ID不能为空")
    private String deviceId;
    
    @NotBlank(message = "SSID不能为空")
    private String ssid;
    
    private String password;
    
    private String timezone; // 如 +8
    
    private String region; // cn/us等
    
    private String configMethod; // qrcode/ble/audio
    
    private String configSource; // APP版本等
    
    // Getters and Setters
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
}
