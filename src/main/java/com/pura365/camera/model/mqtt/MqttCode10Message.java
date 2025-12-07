package com.pura365.camera.model.mqtt;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * CODE 10/138: MQTT 已连接消息
 * 摄像头订阅后发布，或断开再连接时发布
 */
public class MqttCode10Message extends MqttBaseMessage {
    @JsonProperty("uid")
    private String uid;
    
    @JsonProperty("status")
    private Integer status; // 0: 正常连接, 1: 配网后连接
    
    public String getUid() { return uid; }
    public void setUid(String uid) { this.uid = uid; }
    
    public Integer getStatus() { return status; }
    public void setStatus(Integer status) { this.status = status; }
    
    @Override
    public String toString() {
        return "MqttCode10Message{" +
                "code=" + getCode() +
                ", uid='" + uid + '\'' +
                ", time=" + getTime() +
                ", status=" + status +
                '}';
    }
}
