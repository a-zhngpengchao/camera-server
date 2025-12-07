package com.pura365.camera.domain;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

import java.util.Date;

/**
 * 设备生产批次实体，对应表 device_production_batch
 */
@TableName("device_production_batch")
public class DeviceProductionBatch {

    @TableId(type = IdType.AUTO)
    private Long id;

    /** 批次号 */
    private String batchNo;

    /** 网络+镜头配置(第1-2位) */
    private String networkLens;

    /** 设备形态(第3位) */
    private String deviceForm;

    /** 特殊要求(第4位) */
    private String specialReq;

    /** 装机商代码(第5位) */
    private String assemblerCode;

    /** 销售商代码(第6-7位) */
    private String vendorCode;

    /** 预留位(第8位) */
    private String reserved;

    /** 生产数量 */
    private Integer quantity;

    /** 起始序列号 */
    private Integer startSerial;

    /** 结束序列号 */
    private Integer endSerial;

    /** 状态: pending/producing/completed */
    private String status;

    /** 备注 */
    private String remark;

    /** 创建人 */
    private String createdBy;

    private Date createdAt;

    private Date updatedAt;

    // Getters and Setters

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getBatchNo() {
        return batchNo;
    }

    public void setBatchNo(String batchNo) {
        this.batchNo = batchNo;
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

    public String getReserved() {
        return reserved;
    }

    public void setReserved(String reserved) {
        this.reserved = reserved;
    }

    public Integer getQuantity() {
        return quantity;
    }

    public void setQuantity(Integer quantity) {
        this.quantity = quantity;
    }

    public Integer getStartSerial() {
        return startSerial;
    }

    public void setStartSerial(Integer startSerial) {
        this.startSerial = startSerial;
    }

    public Integer getEndSerial() {
        return endSerial;
    }

    public void setEndSerial(Integer endSerial) {
        this.endSerial = endSerial;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getRemark() {
        return remark;
    }

    public void setRemark(String remark) {
        this.remark = remark;
    }

    public String getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(String createdBy) {
        this.createdBy = createdBy;
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

    /**
     * 生成设备ID前缀 (前8位)
     */
    public String getDeviceIdPrefix() {
        return networkLens + deviceForm + specialReq + assemblerCode + vendorCode + reserved;
    }
}
