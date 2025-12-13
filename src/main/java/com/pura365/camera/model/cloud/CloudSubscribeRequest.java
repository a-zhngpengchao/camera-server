package com.pura365.camera.model.cloud;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

/**
 * 订阅云存储请求
 */
@Data
public class CloudSubscribeRequest {

    /**
     * 设备ID
     */
    @JsonProperty("device_id")
    private String deviceId;

    /**
     * 套餐ID
     */
    @JsonProperty("plan_id")
    private String planId;

    /**
     * 支付方式
     * wechat - 微信支付
     * alipay - 支付宝
     * apple - Apple Pay
     * google - Google Pay
     */
    @JsonProperty("payment_method")
    private String paymentMethod;
}
