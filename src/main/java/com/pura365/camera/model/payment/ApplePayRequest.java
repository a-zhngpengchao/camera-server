package com.pura365.camera.model.payment;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;

/**
 * Apple Pay 支付请求
 */
@Schema(description = "Apple Pay 支付请求")
public class ApplePayRequest {

    /**
     * 订单ID
     */
    @Schema(description = "订单ID", required = true)
    @JsonProperty("order_id")
    private String orderId;

    /**
     * Apple 支付凭证
     * 客户端从 Apple Pay SDK 获取的支付令牌
     */
    @Schema(description = "Apple 支付凭证，从 Apple Pay SDK 获取")
    @JsonProperty("payment_token")
    private String paymentToken;

    public String getOrderId() {
        return orderId;
    }

    public void setOrderId(String orderId) {
        this.orderId = orderId;
    }

    public String getPaymentToken() {
        return paymentToken;
    }

    public void setPaymentToken(String paymentToken) {
        this.paymentToken = paymentToken;
    }
}
