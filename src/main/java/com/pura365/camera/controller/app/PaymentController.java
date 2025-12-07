package com.pura365.camera.controller.app;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.pura365.camera.domain.CloudPlan;
import com.pura365.camera.domain.PaymentOrder;
import com.pura365.camera.domain.PaymentWechat;
import com.pura365.camera.domain.UserDevice;
import com.pura365.camera.model.ApiResponse;
import com.pura365.camera.repository.CloudPlanRepository;
import com.pura365.camera.repository.PaymentOrderRepository;
import com.pura365.camera.repository.PaymentWechatRepository;
import com.pura365.camera.repository.UserDeviceRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * 支付相关接口
 * 
 * 包含：
 * - 创建支付订单
 * - 查询支付状态
 * - 微信支付
 * - PayPal支付
 * - Apple Pay
 */
@Tag(name = "支付管理", description = "订单创建、支付相关接口")
@RestController
@RequestMapping("/api/app/payment")
public class PaymentController {

    @Autowired
    private PaymentOrderRepository paymentOrderRepository;

    @Autowired
    private PaymentWechatRepository paymentWechatRepository;

    @Autowired
    private CloudPlanRepository cloudPlanRepository;

    @Autowired
    private UserDeviceRepository userDeviceRepository;

    /**
     * 创建支付订单 - POST /payment/create
     */
    @PostMapping("/create")
    public ApiResponse<Map<String, Object>> createOrder(@RequestAttribute("currentUserId") Long currentUserId,
                                                        @RequestBody Map<String, String> body) {
        String productType = body.get("product_type");
        String productId = body.get("product_id");
        String deviceId = body.get("device_id");
        String paymentMethod = body.get("payment_method");

        if (productType == null || productType.isEmpty() || productId == null || productId.isEmpty()) {
            return ApiResponse.error(400, "product_type 和 product_id 不能为空");
        }
        if (deviceId == null || deviceId.isEmpty()) {
            return ApiResponse.error(400, "device_id 不能为空");
        }
        if (paymentMethod == null || paymentMethod.isEmpty()) {
            paymentMethod = "wechat"; // 默认微信
        }
        // 校验设备归属
        if (!hasUserDevice(currentUserId, deviceId)) {
            return ApiResponse.error(403, "无权操作该设备");
        }

        BigDecimal amount;
        String currency = "CNY";

        if ("cloud_storage".equals(productType)) {
            CloudPlan plan = findPlanByPlanId(productId);
            if (plan == null) {
                return ApiResponse.error(404, "云存储套餐不存在");
            }
            amount = plan.getPrice() != null ? plan.getPrice() : BigDecimal.ZERO;
        } else {
            return ApiResponse.error(400, "暂不支持的商品类型: " + productType);
        }

        PaymentOrder order = new PaymentOrder();
        order.setOrderId(generateOrderId());
        order.setUserId(currentUserId);
        order.setDeviceId(deviceId);
        order.setProductType(productType);
        order.setProductId(productId);
        order.setAmount(amount);
        order.setCurrency(currency);
        order.setStatus("pending");
        order.setPaymentMethod(paymentMethod);
        order.setCreatedAt(new Date());
        paymentOrderRepository.insert(order);

        Map<String, Object> data = new HashMap<>();
        data.put("order_id", order.getOrderId());
        data.put("amount", order.getAmount());
        data.put("currency", order.getCurrency());
        data.put("created_at", formatIsoTime(order.getCreatedAt()));
        return ApiResponse.success(data);
    }

    /**
     * 查询支付状态 - GET /payment/{id}/status
     */
    @GetMapping("/{id}/status")
    public ApiResponse<Map<String, Object>> getOrderStatus(@RequestAttribute("currentUserId") Long currentUserId,
                                                           @PathVariable("id") String orderId) {
        PaymentOrder order = findOrderByOrderId(orderId);
        if (order == null || order.getUserId() == null || !order.getUserId().equals(currentUserId)) {
            return ApiResponse.error(404, "订单不存在");
        }
        Map<String, Object> data = new HashMap<>();
        data.put("order_id", order.getOrderId());
        data.put("status", order.getStatus());
        data.put("amount", order.getAmount());
        data.put("paid_at", order.getPaidAt() != null ? formatIsoTime(order.getPaidAt()) : null);
        return ApiResponse.success(data);
    }

    /**
     * 微信支付参数 - POST /payment/wechat
     */
    @PostMapping("/wechat")
    public ApiResponse<Map<String, Object>> wechatPay(@RequestAttribute("currentUserId") Long currentUserId,
                                                      @RequestBody Map<String, String> body) {
        String orderId = body.get("order_id");
        if (orderId == null || orderId.isEmpty()) {
            return ApiResponse.error(400, "order_id 不能为空");
        }
        PaymentOrder order = findOrderByOrderId(orderId);
        if (order == null || order.getUserId() == null || !order.getUserId().equals(currentUserId)) {
            return ApiResponse.error(404, "订单不存在");
        }

        // 简单保存一条微信支付预下单记录（mock）
        PaymentWechat pw = new PaymentWechat();
        pw.setOrderId(order.getOrderId());
        pw.setPrepayId("mock_prepay_" + order.getOrderId());
        pw.setRawResponse("{}");
        pw.setCreatedAt(new Date());
        paymentWechatRepository.insert(pw);

        Map<String, Object> data = new HashMap<>();
        data.put("appid", "wx_app_id_mock");
        data.put("partnerid", "partner_mock");
        data.put("prepayid", pw.getPrepayId());
        data.put("package", "Sign=WXPay");
        data.put("noncestr", UUID.randomUUID().toString().replace("-", ""));
        data.put("timestamp", String.valueOf(System.currentTimeMillis() / 1000));
        data.put("sign", "mock_sign_" + order.getOrderId());
        return ApiResponse.success(data);
    }

    /**
     * PayPal 支付 - POST /payment/paypal
     */
    @PostMapping("/paypal")
    public ApiResponse<Map<String, Object>> paypalPay(@RequestAttribute("currentUserId") Long currentUserId,
                                                      @RequestBody Map<String, String> body) {
        String orderId = body.get("order_id");
        if (orderId == null || orderId.isEmpty()) {
            return ApiResponse.error(400, "order_id 不能为空");
        }
        PaymentOrder order = findOrderByOrderId(orderId);
        if (order == null || order.getUserId() == null || !order.getUserId().equals(currentUserId)) {
            return ApiResponse.error(404, "订单不存在");
        }
        Map<String, Object> data = new HashMap<>();
        data.put("approval_url", "https://www.paypal.com/checkout?token=" + order.getOrderId());
        data.put("paypal_order_id", "paypal_" + order.getOrderId());
        return ApiResponse.success(data);
    }

    /**
     * Apple Pay 支付 - POST /payment/apple
     */
    @PostMapping("/apple")
    public ApiResponse<Map<String, Object>> applePay(@RequestAttribute("currentUserId") Long currentUserId,
                                                     @RequestBody Map<String, String> body) {
        String orderId = body.get("order_id");
        String paymentToken = body.get("payment_token");
        if (orderId == null || orderId.isEmpty()) {
            return ApiResponse.error(400, "order_id 不能为空");
        }
        PaymentOrder order = findOrderByOrderId(orderId);
        if (order == null || order.getUserId() == null || !order.getUserId().equals(currentUserId)) {
            return ApiResponse.error(404, "订单不存在");
        }

        // 简单模拟支付成功，更新订单状态
        order.setStatus("paid");
        order.setThirdOrderId("apple_" + order.getOrderId());
        order.setPaidAt(new Date());
        order.setUpdatedAt(new Date());
        paymentOrderRepository.updateById(order);

        Map<String, Object> data = new HashMap<>();
        data.put("transaction_id", order.getThirdOrderId());
        data.put("status", "completed");
        return ApiResponse.success(data);
    }

    // ===== 辅助方法 =====

    private CloudPlan findPlanByPlanId(String planId) {
        QueryWrapper<CloudPlan> qw = new QueryWrapper<>();
        qw.lambda().eq(CloudPlan::getPlanId, planId).last("limit 1");
        CloudPlan plan = cloudPlanRepository.selectOne(qw);
        if (plan == null) {
            try {
                Long id = Long.parseLong(planId);
                plan = cloudPlanRepository.selectById(id);
            } catch (NumberFormatException ignore) {
            }
        }
        return plan;
    }

    private PaymentOrder findOrderByOrderId(String orderId) {
        QueryWrapper<PaymentOrder> qw = new QueryWrapper<>();
        qw.lambda().eq(PaymentOrder::getOrderId, orderId).last("limit 1");
        return paymentOrderRepository.selectOne(qw);
    }

    private boolean hasUserDevice(Long userId, String deviceId) {
        QueryWrapper<UserDevice> qw = new QueryWrapper<>();
        qw.lambda().eq(UserDevice::getUserId, userId)
                .eq(UserDevice::getDeviceId, deviceId);
        Integer count = userDeviceRepository.selectCount(qw).intValue();
        return count != null && count > 0;
    }

    private String generateOrderId() {
        return "order_" + System.currentTimeMillis();
    }

    private String formatIsoTime(Date date) {
        if (date == null) return null;
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
        sdf.setTimeZone(TimeZone.getTimeZone("UTC"));
        return sdf.format(date);
    }
}
