package com.pura365.camera.domain;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

@TableName("cloud_plan")
public class CloudPlan {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String planId;

    private String name;

    private String description;

    private Integer storageDays;

    private java.math.BigDecimal price;

    private java.math.BigDecimal originalPrice;

    private String period;

    private String features;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getPlanId() {
        return planId;
    }

    public void setPlanId(String planId) {
        this.planId = planId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Integer getStorageDays() {
        return storageDays;
    }

    public void setStorageDays(Integer storageDays) {
        this.storageDays = storageDays;
    }

    public java.math.BigDecimal getPrice() {
        return price;
    }

    public void setPrice(java.math.BigDecimal price) {
        this.price = price;
    }

    public java.math.BigDecimal getOriginalPrice() {
        return originalPrice;
    }

    public void setOriginalPrice(java.math.BigDecimal originalPrice) {
        this.originalPrice = originalPrice;
    }

    public String getPeriod() {
        return period;
    }

    public void setPeriod(String period) {
        this.period = period;
    }

    public String getFeatures() {
        return features;
    }

    public void setFeatures(String features) {
        this.features = features;
    }
}