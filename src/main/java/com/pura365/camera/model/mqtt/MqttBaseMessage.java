package com.pura365.camera.model.mqtt;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * MQTT 消息基类
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class MqttBaseMessage {
    @JsonProperty("code")
    private Integer code;
    
    @JsonProperty("time")
    private Long time;
    
    public Integer getCode() { return code; }
    public void setCode(Integer code) { this.code = code; }
    
    public Long getTime() { return time; }
    public void setTime(Long time) { this.time = time; }
    
    @Override
    public String toString() {
        return "MqttBaseMessage{" +
                "code=" + code +
                ", time=" + time +
                '}';
    }
}
