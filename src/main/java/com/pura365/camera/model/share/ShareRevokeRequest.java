package com.pura365.camera.model.share;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

/**
 * 取消分享请求
 */
@Data
public class ShareRevokeRequest {

    /**
     * 设备ID
     */
    @JsonProperty("device_id")
    private String deviceId;

    /**
     * 被取消分享的用户ID
     */
    @JsonProperty("user_id")
    private Long userId;
}
