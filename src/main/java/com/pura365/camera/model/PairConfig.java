package com.pura365.camera.model;

public class PairConfig {
    private String ssid;       // WiFi SSID
    private String password;   // WiFi 密码，可为空
    private String timezone;   // 时区，如 +8
    private String region;     // 区域，如 cn/us

    public String getSsid() { return ssid; }
    public void setSsid(String ssid) { this.ssid = ssid; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }

    public String getTimezone() { return timezone; }
    public void setTimezone(String timezone) { this.timezone = timezone; }

    public String getRegion() { return region; }
    public void setRegion(String region) { this.region = region; }
}
