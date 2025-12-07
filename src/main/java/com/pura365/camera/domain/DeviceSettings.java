package com.pura365.camera.domain;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

import java.util.Date;

@TableName("device_settings")
public class DeviceSettings {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String deviceId;

    private Integer motionDetection;

    private Integer nightVision;

    private Integer audioEnabled;

    private Integer flipImage;

    private String sensitivity;

    private Date createdAt;

    private Date updatedAt;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getDeviceId() {
        return deviceId;
    }

    public void setDeviceId(String deviceId) {
        this.deviceId = deviceId;
    }

    public Integer getMotionDetection() {
        return motionDetection;
    }

    public void setMotionDetection(Integer motionDetection) {
        this.motionDetection = motionDetection;
    }

    public Integer getNightVision() {
        return nightVision;
    }

    public void setNightVision(Integer nightVision) {
        this.nightVision = nightVision;
    }

    public Integer getAudioEnabled() {
        return audioEnabled;
    }

    public void setAudioEnabled(Integer audioEnabled) {
        this.audioEnabled = audioEnabled;
    }

    public Integer getFlipImage() {
        return flipImage;
    }

    public void setFlipImage(Integer flipImage) {
        this.flipImage = flipImage;
    }

    public String getSensitivity() {
        return sensitivity;
    }

    public void setSensitivity(String sensitivity) {
        this.sensitivity = sensitivity;
    }

    public Date getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Date createdAt) {
        this.createdAt = createdAt;
    }

    public Date getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(Date updatedAt) {
        this.updatedAt = updatedAt;
    }
}