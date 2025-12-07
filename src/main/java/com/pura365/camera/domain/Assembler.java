package com.pura365.camera.domain;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

import java.util.Date;

/**
 * 装机商实体，对应表 assembler
 */
@TableName("assembler")
public class Assembler {

    @TableId(type = IdType.AUTO)
    private Long id;

    /** 装机商代码(1位) */
    private String assemblerCode;

    /** 装机商名称 */
    private String assemblerName;

    /** 联系人 */
    private String contactPerson;

    /** 联系电话 */
    private String contactPhone;

    /** 状态 0-禁用 1-启用 */
    private Integer status;

    private Date createdAt;

    private Date updatedAt;

    // Getters and Setters

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getAssemblerCode() {
        return assemblerCode;
    }

    public void setAssemblerCode(String assemblerCode) {
        this.assemblerCode = assemblerCode;
    }

    public String getAssemblerName() {
        return assemblerName;
    }

    public void setAssemblerName(String assemblerName) {
        this.assemblerName = assemblerName;
    }

    public String getContactPerson() {
        return contactPerson;
    }

    public void setContactPerson(String contactPerson) {
        this.contactPerson = contactPerson;
    }

    public String getContactPhone() {
        return contactPhone;
    }

    public void setContactPhone(String contactPhone) {
        this.contactPhone = contactPhone;
    }

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
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
