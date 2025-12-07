package com.pura365.camera.domain;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

import java.util.Date;

/**
 * 生产设备实体，对应表 manufactured_device
 */
@TableName("manufactured_device")
public class ManufacturedDevice {

    @TableId(type = IdType.AUTO)
    private Long id;

    /** 完整设备ID(16位) */
    private String deviceId;

    /** 关联批次ID */
    private Long batchId;

    /** 网络+镜头(第1-2位) */
    private String networkLens;

    /** 设备形态(第3位) */
    private String deviceForm;

    /** 特殊要求(第4位) */
    private String specialReq;

    /** 装机商代码(第5位) */
    private String assemblerCode;

    /** 销售商代码(第6-7位) */
    private String vendorCode;

    /** 序列号(第9-16位) */
    private String serialNo;

    /** MAC地址 */
    private String macAddress;

    /** 状态: manufactured/activated/bound */
    private String status;

    /** 生产时间 */
    private Date manufacturedAt;

    /** 激活时间 */
    private Date activatedAt;

    private Date createdAt;

    private Date updatedAt;

    // Getters and Setters

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

    public Long getBatchId() {
        return batchId;
    }

    public void setBatchId(Long batchId) {
        this.batchId = batchId;
    }

    public String getNetworkLens() {
        return networkLens;
    }

    public void setNetworkLens(String networkLens) {
        this.networkLens = networkLens;
    }

    public String getDeviceForm() {
        return deviceForm;
    }

    public void setDeviceForm(String deviceForm) {
        this.deviceForm = deviceForm;
    }

    public String getSpecialReq() {
        return specialReq;
    }

    public void setSpecialReq(String specialReq) {
        this.specialReq = specialReq;
    }

    public String getAssemblerCode() {
        return assemblerCode;
    }

    public void setAssemblerCode(String assemblerCode) {
        this.assemblerCode = assemblerCode;
    }

    public String getVendorCode() {
        return vendorCode;
    }

    public void setVendorCode(String vendorCode) {
        this.vendorCode = vendorCode;
    }

    public String getSerialNo() {
        return serialNo;
    }

    public void setSerialNo(String serialNo) {
        this.serialNo = serialNo;
    }

    public String getMacAddress() {
        return macAddress;
    }

    public void setMacAddress(String macAddress) {
        this.macAddress = macAddress;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Date getManufacturedAt() {
        return manufacturedAt;
    }

    public void setManufacturedAt(Date manufacturedAt) {
        this.manufacturedAt = manufacturedAt;
    }

    public Date getActivatedAt() {
        return activatedAt;
    }

    public void setActivatedAt(Date activatedAt) {
        this.activatedAt = activatedAt;
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
