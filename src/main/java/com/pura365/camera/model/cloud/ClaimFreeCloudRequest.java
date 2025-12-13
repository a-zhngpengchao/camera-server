package com.pura365.camera.model.cloud;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

/**
 * 领取免费云存储请求
 */
@Data
public class ClaimFreeCloudRequest {

    /**
     * 设备ID
     * 要领取免费云存储的设备
     */
    @JsonProperty("device_id")
    private String deviceId;
}
