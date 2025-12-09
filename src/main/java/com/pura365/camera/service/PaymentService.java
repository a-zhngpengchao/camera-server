package com.pura365.camera.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.pura365.camera.domain.CloudPlan;
import com.pura365.camera.domain.PaymentOrder;
import com.pura365.camera.domain.PaymentWechat;
import com.pura365.camera.domain.UserDevice;
import com.pura365.camera.model.payment.*;
import com.pura365.camera.repository.CloudPlanRepository;
import com.pura365.camera.repository.PaymentOrderRepository;
import com.pura365.camera.repository.PaymentWechatRepository;
import com.pura365.camera.repository.UserDeviceRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.UUID;

/**
 * 支付服务
 * 
 * 处理订单创建、支付渠道对接等业务逻辑
 */
@Service
public class PaymentService {

    private static final DateTimeFormatter ISO_FORMATTER =
            DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss'Z'").withZone(ZoneOffset.UTC);

    /** 默认支付方式 */
    private static final String DEFAULT_PAYMENT_METHOD = "wechat";

    /** 默认货币 */
    private static final String DEFAULT_CURRENCY = "CNY";

    /** 商品类型: 云存储 */
    private static final String PRODUCT_TYPE_CLOUD_STORAGE = "cloud_storage";

    @Autowired
    private PaymentOrderRepository paymentOrderRepository;

    @Autowired
    private PaymentWechatRepository paymentWechatRepository;

    @Autowired
    private CloudPlanRepository cloudPlanRepository;

    @Autowired
    private UserDeviceRepository userDeviceRepository;

    /**
     * 创建支付订单
     *
     * @param userId  用户ID
     * @param request 创建订单请求
     * @return 订单信息，失败返回 null 并设置 errorMessage
     */
    public CreateOrderResult createOrder(Long userId, CreateOrderRequest request) {
        CreateOrderResult result = new CreateOrderResult();

        // 参数校验
        if (!StringUtils.hasText(request.getProductType()) || !StringUtils.hasText(request.getProductId())) {
            result.setErrorCode(400);
            result.setErrorMessage("product_type 和 product_id 不能为空");
            return result;
        }
        if (!StringUtils.hasText(request.getDeviceId())) {
            result.setErrorCode(400);
            result.setErrorMessage("device_id 不能为空");
            return result;
        }

        // 校验设备归属
        if (!hasUserDevice(userId, request.getDeviceId())) {
            result.setErrorCode(403);
            result.setErrorMessage("无权操作该设备");
            return result;
        }

        // 根据商品类型获取价格
        BigDecimal amount;
        if (PRODUCT_TYPE_CLOUD_STORAGE.equals(request.getProductType())) {
            CloudPlan plan = findPlanByPlanId(request.getProductId());
            if (plan == null) {
                result.setErrorCode(404);
                result.setErrorMessage("云存储套餐不存在");
                return result;
            }
            amount = plan.getPrice() != null ? plan.getPrice() : BigDecimal.ZERO;
        } else {
            result.setErrorCode(400);
            result.setErrorMessage("暂不支持的商品类型: " + request.getProductType());
            return result;
        }

        // 创建订单
        String paymentMethod = StringUtils.hasText(request.getPaymentMethod())
                ? request.getPaymentMethod() : DEFAULT_PAYMENT_METHOD;

        PaymentOrder order = new PaymentOrder();
        order.setOrderId(generateOrderId());
        order.setUserId(userId);
        order.setDeviceId(request.getDeviceId());
        order.setProductType(request.getProductType());
        order.setProductId(request.getProductId());
        order.setAmount(amount);
        order.setCurrency(DEFAULT_CURRENCY);
        order.setStatus("pending");
        order.setPaymentMethod(paymentMethod);
        order.setCreatedAt(new Date());
        paymentOrderRepository.insert(order);

        // 构建响应
        OrderVO vo = new OrderVO();
        vo.setOrderId(order.getOrderId());
        vo.setAmount(order.getAmount());
        vo.setCurrency(order.getCurrency());
        vo.setCreatedAt(formatIsoTime(order.getCreatedAt()));

        result.setSuccess(true);
        result.setOrder(vo);
        return result;
    }

    /**
     * 查询订单状态
     *
     * @param userId  用户ID
     * @param orderId 订单ID
     * @return 订单信息，不存在或无权限返回 null
     */
    public OrderVO getOrderStatus(Long userId, String orderId) {
        PaymentOrder order = getOrderByIdAndUser(orderId, userId);
        if (order == null) {
            return null;
        }

        OrderVO vo = new OrderVO();
        vo.setOrderId(order.getOrderId());
        vo.setStatus(order.getStatus());
        vo.setAmount(order.getAmount());
        vo.setPaidAt(order.getPaidAt() != null ? formatIsoTime(order.getPaidAt()) : null);
        return vo;
    }

    /**
     * 发起微信支付
     * 
     * @param userId  用户ID
     * @param orderId 订单ID
     * @return 微信支付参数，订单不存在返回 null
     */
    public WechatPayVO wechatPay(Long userId, String orderId) {
        PaymentOrder order = getOrderByIdAndUser(orderId, userId);
        if (order == null) {
            return null;
        }

        // 创建微信预支付记录 (mock)
        PaymentWechat pw = new PaymentWechat();
        pw.setOrderId(order.getOrderId());
        pw.setPrepayId("mock_prepay_" + order.getOrderId());
        pw.setRawResponse("{}");
        pw.setCreatedAt(new Date());
        paymentWechatRepository.insert(pw);

        // 返回支付参数
        WechatPayVO vo = new WechatPayVO();
        vo.setAppid("wx_app_id_mock");
        vo.setPartnerid("partner_mock");
        vo.setPrepayid(pw.getPrepayId());
        vo.setPackageValue("Sign=WXPay");
        vo.setNoncestr(UUID.randomUUID().toString().replace("-", ""));
        vo.setTimestamp(String.valueOf(System.currentTimeMillis() / 1000));
        vo.setSign("mock_sign_" + order.getOrderId());
        return vo;
    }

    /**
     * 发起 PayPal 支付
     *
     * @param userId  用户ID
     * @param orderId 订单ID
     * @return PayPal 支付参数，订单不存在返回 null
     */
    public PaypalPayVO paypalPay(Long userId, String orderId) {
        PaymentOrder order = getOrderByIdAndUser(orderId, userId);
        if (order == null) {
            return null;
        }

        PaypalPayVO vo = new PaypalPayVO();
        vo.setApprovalUrl("https://www.paypal.com/checkout?token=" + order.getOrderId());
        vo.setPaypalOrderId("paypal_" + order.getOrderId());
        return vo;
    }

    /**
     * Apple Pay 支付
     *
     * @param userId  用户ID
     * @param request Apple Pay 请求
     * @return 支付结果，订单不存在返回 null
     */
    public ApplePayVO applePay(Long userId, ApplePayRequest request) {
        PaymentOrder order = getOrderByIdAndUser(request.getOrderId(), userId);
        if (order == null) {
            return null;
        }

        // 模拟支付成功，更新订单状态
        order.setStatus("paid");
        order.setThirdOrderId("apple_" + order.getOrderId());
        order.setPaidAt(new Date());
        order.setUpdatedAt(new Date());
        paymentOrderRepository.updateById(order);

        ApplePayVO vo = new ApplePayVO();
        vo.setTransactionId(order.getThirdOrderId());
        vo.setStatus("completed");
        return vo;
    }

    // ============== 私有方法 ==============

    /**
     * 根据订单ID和用户ID获取订单（权限校验）
     */
    private PaymentOrder getOrderByIdAndUser(String orderId, Long userId) {
        PaymentOrder order = findOrderByOrderId(orderId);
        if (order == null || order.getUserId() == null || !order.getUserId().equals(userId)) {
            return null;
        }
        return order;
    }

    /**
     * 根据订单ID查询订单
     */
    private PaymentOrder findOrderByOrderId(String orderId) {
        LambdaQueryWrapper<PaymentOrder> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(PaymentOrder::getOrderId, orderId).last("LIMIT 1");
        return paymentOrderRepository.selectOne(wrapper);
    }

    /**
     * 根据套餐ID查询云存储套餐
     */
    private CloudPlan findPlanByPlanId(String planId) {
        // 先按业务 planId 查询
        LambdaQueryWrapper<CloudPlan> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(CloudPlan::getPlanId, planId).last("LIMIT 1");
        CloudPlan plan = cloudPlanRepository.selectOne(wrapper);

        // 如果找不到，尝试按数据库主键查询
        if (plan == null) {
            try {
                Long id = Long.parseLong(planId);
                plan = cloudPlanRepository.selectById(id);
            } catch (NumberFormatException ignored) {
            }
        }
        return plan;
    }

    /**
     * 检查用户是否拥有设备
     */
    private boolean hasUserDevice(Long userId, String deviceId) {
        LambdaQueryWrapper<UserDevice> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(UserDevice::getUserId, userId)
               .eq(UserDevice::getDeviceId, deviceId);
        return paymentOrderRepository.selectCount(
                new LambdaQueryWrapper<PaymentOrder>().apply("1=0")) >= 0
                && userDeviceRepository.selectCount(wrapper) > 0;
    }

    /**
     * 生成订单ID
     */
    private String generateOrderId() {
        return "order_" + System.currentTimeMillis();
    }

    /**
     * 格式化时间为 ISO 8601 格式
     */
    private String formatIsoTime(Date date) {
        if (date == null) return null;
        return ISO_FORMATTER.format(date.toInstant());
    }

    /**
     * 创建订单结果
     */
    public static class CreateOrderResult {
        private boolean success;
        private OrderVO order;
        private int errorCode;
        private String errorMessage;

        public boolean isSuccess() {
            return success;
        }

        public void setSuccess(boolean success) {
            this.success = success;
        }

        public OrderVO getOrder() {
            return order;
        }

        public void setOrder(OrderVO order) {
            this.order = order;
        }

        public int getErrorCode() {
            return errorCode;
        }

        public void setErrorCode(int errorCode) {
            this.errorCode = errorCode;
        }

        public String getErrorMessage() {
            return errorMessage;
        }

        public void setErrorMessage(String errorMessage) {
            this.errorMessage = errorMessage;
        }
    }
}
