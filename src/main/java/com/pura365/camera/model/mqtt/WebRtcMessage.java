package com.pura365.camera.model.mqtt;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * WebRTC 相关消息（CODE 23-25）
 */
public class WebRtcMessage extends MqttBaseMessage {
    @JsonProperty("uid")
    private String uid;
    
    @JsonProperty("sid")
    private String sid; // Peer ID
    
    @JsonProperty("sdp")
    private String sdp; // Offer/Answer SDP
    
    @JsonProperty("candidate")
    private String candidate; // ICE Candidate
    
    @JsonProperty("rtc")
    private String rtc; // WebRTC服务器信息: server,user,pass
    
    @JsonProperty("status")
    private Integer status; // 0: 失败, 1: 成功
    
    // Getters and Setters
    public String getUid() { return uid; }
    public void setUid(String uid) { this.uid = uid; }
    
    public String getSid() { return sid; }
    public void setSid(String sid) { this.sid = sid; }
    
    public String getSdp() { return sdp; }
    public void setSdp(String sdp) { this.sdp = sdp; }
    
    public String getCandidate() { return candidate; }
    public void setCandidate(String candidate) { this.candidate = candidate; }
    
    public String getRtc() { return rtc; }
    public void setRtc(String rtc) { this.rtc = rtc; }
    
    public Integer getStatus() { return status; }
    public void setStatus(Integer status) { this.status = status; }
    
    @Override
    public String toString() {
        return "WebRtcMessage{" +
                "code=" + getCode() +
                ", uid='" + uid + '\'' +
                ", sid='" + sid + '\'' +
                ", time=" + getTime() +
                ", status=" + status +
                '}';
    }
}
