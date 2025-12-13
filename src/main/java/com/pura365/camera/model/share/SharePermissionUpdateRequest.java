package com.pura365.camera.model.share;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

/**
 * 更新分享权限请求
 */
@Data
public class SharePermissionUpdateRequest {

    /**
     * 设备ID
     */
    @JsonProperty("device_id")
    private String deviceId;

    /**
     * 目标用户ID（被更新权限的用户）
     */
    @JsonProperty("user_id")
    private Long userId;

    /**
     * 新的权限
     * view_only - 仅查看
     * full_control - 完全控制
     */
    @JsonProperty("permission")
    private String permission;
}
