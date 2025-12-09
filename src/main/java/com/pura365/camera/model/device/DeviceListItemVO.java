package com.pura365.camera.model.device;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;

/**
 * 设备列表项响应
 *
 * @author camera-server
 */
@Schema(description = "设备列表项")
public class DeviceListItemVO {

    /**
     * 设备ID（序列号）
     */
    @JsonProperty("id")
    @Schema(description = "设备ID")
    private String id;

    /**
     * 设备名称
     */
    @JsonProperty("name")
    @Schema(description = "设备名称")
    private String name;

    /**
     * 设备型号
     */
    @JsonProperty("model")
    @Schema(description = "设备型号")
    private String model;

    /**
     * 设备状态：online/offline
     */
    @JsonProperty("status")
    @Schema(description = "设备状态", example = "online")
    private String status;

    /**
     * 是否有云存储
     */
    @JsonProperty("has_cloud_storage")
    @Schema(description = "是否有云存储")
    private Boolean hasCloudStorage;

    /**
     * 云存储到期时间（ISO8601格式）
     */
    @JsonProperty("cloud_expire_at")
    @Schema(description = "云存储到期时间")
    private String cloudExpireAt;

    /**
     * 缩略图URL
     */
    @JsonProperty("thumbnail_url")
    @Schema(description = "缩略图URL")
    private String thumbnailUrl;

    /**
     * 最后在线时间
     */
    @JsonProperty("last_online_at")
    @Schema(description = "最后在线时间")
    private String lastOnlineAt;

    // Getters and Setters

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getModel() {
        return model;
    }

    public void setModel(String model) {
        this.model = model;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Boolean getHasCloudStorage() {
        return hasCloudStorage;
    }

    public void setHasCloudStorage(Boolean hasCloudStorage) {
        this.hasCloudStorage = hasCloudStorage;
    }

    public String getCloudExpireAt() {
        return cloudExpireAt;
    }

    public void setCloudExpireAt(String cloudExpireAt) {
        this.cloudExpireAt = cloudExpireAt;
    }

    public String getThumbnailUrl() {
        return thumbnailUrl;
    }

    public void setThumbnailUrl(String thumbnailUrl) {
        this.thumbnailUrl = thumbnailUrl;
    }

    public String getLastOnlineAt() {
        return lastOnlineAt;
    }

    public void setLastOnlineAt(String lastOnlineAt) {
        this.lastOnlineAt = lastOnlineAt;
    }
}
