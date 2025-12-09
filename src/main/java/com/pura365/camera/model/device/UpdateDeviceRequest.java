package com.pura365.camera.model.device;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;

/**
 * 更新设备信息请求参数
 *
 * @author camera-server
 */
@Schema(description = "更新设备请求")
public class UpdateDeviceRequest {

    /**
     * 设备名称
     */
    @JsonProperty("name")
    @Schema(description = "设备名称", example = "卧室摄像头")
    private String name;

    // Getters and Setters

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
