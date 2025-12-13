package com.pura365.camera.controller.internal;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.pura365.camera.domain.CloudPlan;
import com.pura365.camera.domain.CloudSubscription;
import com.pura365.camera.domain.CloudVideo;
import com.pura365.camera.domain.UserDevice;
import com.pura365.camera.model.ApiResponse;
import com.pura365.camera.model.cloud.ClaimFreeCloudRequest;
import com.pura365.camera.model.cloud.CloudSubscribeRequest;
import com.pura365.camera.repository.CloudPlanRepository;
import com.pura365.camera.repository.CloudSubscriptionRepository;
import com.pura365.camera.repository.CloudVideoRepository;
import com.pura365.camera.repository.UserDeviceRepository;
import com.pura365.camera.service.CloudStorageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * 云存储相关接口
 *
 * - GET  /cloud/plans             获取云存储套餐列表
 * - POST /cloud/subscribe         创建云存储订阅支付订单
 * - GET  /cloud/videos            查询云存储视频列表
 * - GET  /cloud/subscription/{deviceId} 查询设备当前云存储订阅状态
 */
@Tag(name = "云服务接口", description = "云服务相关内部接口")
@RestController
@RequestMapping("/api/internal/cloud")
public class CloudController {

    @Autowired
    private CloudPlanRepository cloudPlanRepository;

    @Autowired
    private CloudSubscriptionRepository cloudSubscriptionRepository;

    @Autowired
    private CloudVideoRepository cloudVideoRepository;

    @Autowired
    private UserDeviceRepository userDeviceRepository;
    
    @Autowired
    private com.pura365.camera.repository.DeviceRepository deviceRepository;
    
    @Autowired
    private CloudStorageService cloudStorageService;

    private final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * 获取云存储套餐列表 - GET /cloud/plans
     */
    @Operation(summary = "获取云存储套餐列表", description = "列出所有可用的云存储套餐")
    @GetMapping("/plans")
    public ApiResponse<List<Map<String, Object>>> getCloudPlans(@RequestAttribute("currentUserId") Long currentUserId) {
        List<CloudPlan> plans = cloudPlanRepository.selectList(new QueryWrapper<>());
        List<Map<String, Object>> list = new ArrayList<>();
        if (plans != null) {
            for (CloudPlan plan : plans) {
                Map<String, Object> item = new HashMap<>();
                // 对外优先使用 planId，没有则退回自增 id
                item.put("id", plan.getPlanId() != null ? plan.getPlanId() : String.valueOf(plan.getId()));
                item.put("name", plan.getName());
                item.put("description", plan.getDescription());
                item.put("storage_days", plan.getStorageDays());
                item.put("price", plan.getPrice());
                item.put("original_price", plan.getOriginalPrice());
                item.put("period", plan.getPeriod());
                item.put("features", parseFeatures(plan.getFeatures()));
                list.add(item);
            }
        }
        return ApiResponse.success(list);
    }

    /**
     * 订阅云存储 - POST /cloud/subscribe
     * 这里只创建支付订单，不直接写入 CloudSubscription（支付成功后再写入）。
     */
    @Operation(summary = "订阅云存储", description = "创建云存储套餐的支付订单")
    @PostMapping("/subscribe")
    public ApiResponse<Map<String, Object>> subscribe(@RequestAttribute("currentUserId") Long currentUserId,
                                                      @RequestBody CloudSubscribeRequest request) {
        String deviceId = request.getDeviceId();
        String planId = request.getPlanId();
        String paymentMethod = request.getPaymentMethod();
        if (deviceId == null || deviceId.isEmpty() || planId == null || planId.isEmpty()) {
            return ApiResponse.error(400, "device_id 和 plan_id 不能为空");
        }
        if (paymentMethod == null || paymentMethod.isEmpty()) {
            paymentMethod = "wechat"; // 默认微信
        }
        // 校验设备归属
        if (!hasUserDevice(currentUserId, deviceId)) {
            return ApiResponse.error(403, "无权操作该设备");
        }
        // 查套餐
        CloudPlan plan = findPlanByPlanId(planId);
        if (plan == null) {
            return ApiResponse.error(404, "云存储套餐不存在");
        }

        BigDecimal amount = plan.getPrice();
        if (amount == null) {
            amount = BigDecimal.ZERO;
        }

        // 这里原项目中是通过 PaymentOrder 表统一处理支付，这里我们只返回一个模拟的订单信息
        Map<String, Object> paymentInfo = new HashMap<>();
        String orderId = "order_" + System.currentTimeMillis();
        paymentInfo.put("order_id", orderId);
        paymentInfo.put("amount", amount);
        paymentInfo.put("currency", "CNY");
        paymentInfo.put("payment_method", paymentMethod);

        if ("wechat".equalsIgnoreCase(paymentMethod)) {
            paymentInfo.put("prepay_id", "mock_prepay_" + orderId);
            paymentInfo.put("sign", "mock_sign_" + orderId);
        }

        return ApiResponse.success(paymentInfo);
    }

    /**
     * 云存储视频列表 - GET /cloud/videos
     * 直接从S3云存储查询视频文件列表
     */
    @Operation(summary = "云存储视频列表", description = "分页查询某设备的云存储视频")
    @GetMapping("/videos")
    public ApiResponse<Map<String, Object>> listCloudVideos(@RequestAttribute("currentUserId") Long currentUserId,
                                                            @RequestParam("device_id") String deviceId,
                                                            @RequestParam(value = "date", required = false) String date,
                                                            @RequestParam(value = "page", required = false, defaultValue = "1") int page,
                                                            @RequestParam(value = "page_size", required = false, defaultValue = "20") int pageSize) {
        if (deviceId == null || deviceId.isEmpty()) {
            return ApiResponse.error(400, "device_id 不能为空");
        }
        if (!hasUserDevice(currentUserId, deviceId)) {
            return ApiResponse.error(403, "无权查看该设备");
        }
        if (page < 1) page = 1;
        if (pageSize <= 0) pageSize = 20;

        // 直接从云存储查询视频列表
        List<Map<String, Object>> allVideos = cloudStorageService.listVideosFromCloud(deviceId, date);
        
        // 手动分页
        int total = allVideos.size();
        int fromIndex = (page - 1) * pageSize;
        int toIndex = Math.min(fromIndex + pageSize, total);
        
        List<Map<String, Object>> list;
        if (fromIndex >= total) {
            list = new ArrayList<>();
        } else {
            list = allVideos.subList(fromIndex, toIndex);
        }
        
        // 移除内部使用的排序字段
        for (Map<String, Object> video : list) {
            video.remove("created_at_date");
        }

        Map<String, Object> data = new HashMap<>();
        data.put("list", list);
        data.put("total", total);
        data.put("page", page);
        data.put("page_size", pageSize);
        return ApiResponse.success(data);
    }

    /**
     * 获取设备云存储订阅状态 - GET /cloud/subscription/{deviceId}
     */
    @Operation(summary = "获取云存订阅状态", description = "获取某设备当前云存储订阅信息")
    @GetMapping("/subscription/{deviceId}")
    public ApiResponse<Map<String, Object>> getSubscription(@RequestAttribute("currentUserId") Long currentUserId,
                                                            @PathVariable("deviceId") String deviceId) {
        if (!hasUserDevice(currentUserId, deviceId)) {
            return ApiResponse.error(403, "无权查看该设备");
        }
        QueryWrapper<CloudSubscription> qw = new QueryWrapper<>();
        qw.lambda().eq(CloudSubscription::getUserId, currentUserId)
                .eq(CloudSubscription::getDeviceId, deviceId)
                .orderByDesc(CloudSubscription::getExpireAt)
                .last("limit 1");
        CloudSubscription sub = cloudSubscriptionRepository.selectOne(qw);

        Map<String, Object> data = new HashMap<>();
        boolean isSubscribed = sub != null && (sub.getExpireAt() == null || sub.getExpireAt().after(new Date()));
        data.put("is_subscribed", isSubscribed);
        if (isSubscribed && sub != null) {
            data.put("plan_id", sub.getPlanId());
            data.put("plan_name", sub.getPlanName());
            data.put("expire_at", sub.getExpireAt() != null ? formatIsoTime(sub.getExpireAt()) : null);
            data.put("auto_renew", sub.getAutoRenew() != null && sub.getAutoRenew() == 1);
        } else {
            data.put("plan_id", null);
            data.put("plan_name", null);
            data.put("expire_at", null);
            data.put("auto_renew", false);
        }
        return ApiResponse.success(data);
    }
    
    /**
     * 领取免费7天云存储 - POST /cloud/claim-free
     */
    @Operation(summary = "领取免费7天云存储", description = "用户首次领取7天免费云存储")
    @PostMapping("/claim-free")
    public ApiResponse<Map<String, Object>> claimFreeTrial(@RequestAttribute("currentUserId") Long currentUserId,
                                                           @RequestBody ClaimFreeCloudRequest request) {
        String deviceId = request.getDeviceId();
        
        if (deviceId == null || deviceId.isEmpty()) {
            return ApiResponse.error(400, "device_id不能为空");
        }
        
        // 验证设备归属
        if (!hasUserDevice(currentUserId, deviceId)) {
            return ApiResponse.error(403, "无权操作该设备");
        }
        
        // 检查设备是否已领取
        com.pura365.camera.domain.Device device = deviceRepository.selectById(deviceId);
        if (device == null) {
            return ApiResponse.error(404, "设备不存在");
        }
        
        if (device.getFreeCloudClaimed() != null && device.getFreeCloudClaimed() == 1) {
            return ApiResponse.error(400, "该设备已领取过免费云存储");
        }
        
        // 创建7天免费订阅
        CloudSubscription subscription = new CloudSubscription();
        subscription.setUserId(currentUserId);
        subscription.setDeviceId(deviceId);
        subscription.setPlanId("free-trial-7d");
        subscription.setPlanName("7天免费试用");
        
        // 设置7天后过期
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.DAY_OF_MONTH, 7);
        subscription.setExpireAt(calendar.getTime());
        subscription.setAutoRenew(0);
        subscription.setCreatedAt(new Date());
        subscription.setUpdatedAt(new Date());
        
        cloudSubscriptionRepository.insert(subscription);
        
        // 标记设备已领取
        device.setFreeCloudClaimed(1);
        deviceRepository.updateById(device);
        
        Map<String, Object> result = new HashMap<>();
        result.put("claimed", true);
        result.put("expire_at", formatIsoTime(subscription.getExpireAt()));
        
        return ApiResponse.success(result);
    }

    // ===== 私有辅助方法 =====

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

    private boolean hasUserDevice(Long userId, String deviceId) {
        QueryWrapper<UserDevice> qw = new QueryWrapper<>();
        qw.lambda().eq(UserDevice::getUserId, userId)
                .eq(UserDevice::getDeviceId, deviceId);
        Integer count = userDeviceRepository.selectCount(qw).intValue();
        return count != null && count > 0;
    }

    private List<String> parseFeatures(String features) {
        if (features == null || features.trim().isEmpty()) {
            return Collections.emptyList();
        }
        String f = features.trim();
        try {
            if (f.startsWith("[")) {
                return objectMapper.readValue(f, new TypeReference<List<String>>() {});
            }
        } catch (Exception ignore) {
        }
        String[] arr = f.split(",");
        List<String> list = new ArrayList<>();
        for (String s : arr) {
            if (s != null) {
                String t = s.trim();
                if (!t.isEmpty()) {
                    list.add(t);
                }
            }
        }
        return list;
    }

    private String formatIsoTime(Date date) {
        if (date == null) return null;
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
        sdf.setTimeZone(TimeZone.getTimeZone("UTC"));
        return sdf.format(date);
    }
}