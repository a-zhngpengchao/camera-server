package com.pura365.camera.model.share;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

/**
 * 生成分享码请求
 */
@Data
public class ShareGenerateRequest {

    /**
     * 设备ID
     */
    @JsonProperty("device_id")
    private String deviceId;

    /**
     * 分享权限
     * view_only - 仅查看（只能观看直播和录像）
     * full_control - 完全控制（可以控制云台、对讲等）
     */
    @JsonProperty("permission")
    private String permission;

    /**
     * 分享目标账号
     * 
     * 前端输入一个对方账号或其绑定的邮箱/手机号，用于限定分享对象：
     * - 可以是登录账号 username
     * - 或绑定的 email
     * - 或绑定的 phone
     * 如果不填写，则表示生成一个任何人扫码都可以使用的分享码（当前逻辑保持不变）。
     */
    @JsonProperty("target_account")
    private String targetAccount;
}
