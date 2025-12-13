package com.pura365.camera.controller.internal;

import com.pura365.camera.model.ApiResponse;
import com.pura365.camera.model.share.ShareBindRequest;
import com.pura365.camera.model.share.ShareGenerateRequest;
import com.pura365.camera.model.share.SharePermissionUpdateRequest;
import com.pura365.camera.model.share.ShareRevokeRequest;
import com.pura365.camera.service.DeviceShareService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 设备分享接口
 *
 * - POST /share/generate     生成分享码（二维码内容）
 * - POST /share/bind         通过分享码绑定设备（扫码后调用）
 * - GET  /share/list         获取设备的分享列表
 * - POST /share/revoke       取消分享
 * - POST /share/permission   更新分享权限
 */
@Tag(name = "设备分享", description = "设备分享相关接口")
@RestController
@RequestMapping("/api/internal/share")
public class DeviceShareController {

    private static final Logger log = LoggerFactory.getLogger(DeviceShareController.class);

    @Autowired
    private DeviceShareService deviceShareService;

    /**
     * 生成分享码
     * POST /share/generate
     */
    @Operation(summary = "生成分享码", description = "生成设备分享码，用于生成二维码")
    @PostMapping("/generate")
    public ApiResponse<Map<String, Object>> generateShareCode(
            @RequestAttribute("currentUserId") Long currentUserId,
            @RequestBody ShareGenerateRequest request) {

        String deviceId = request.getDeviceId();
        String permission = request.getPermission();
        String targetAccount = request.getTargetAccount();

        if (deviceId == null || deviceId.isEmpty()) {
            return ApiResponse.error(400, "device_id不能为空");
        }

        // 验证是设备拥有者
        if (!deviceShareService.isDeviceOwner(currentUserId, deviceId)) {
            return ApiResponse.error(403, "只有设备拥有者才能分享设备");
        }

        try {
            Map<String, Object> result = deviceShareService.generateShareCode(currentUserId, deviceId, permission, targetAccount);
            return ApiResponse.success(result);
        } catch (Exception e) {
            log.error("生成分享码失败", e);
            return ApiResponse.error(500, e.getMessage());
        }
    }

    /**
     * 通过分享码绑定设备（扫码后调用）
     * POST /share/bind
     */
    @Operation(summary = "扫码绑定设备", description = "通过分享码绑定设备")
    @PostMapping("/bind")
    public ApiResponse<Map<String, Object>> bindByShareCode(
            @RequestAttribute("currentUserId") Long currentUserId,
            @RequestBody ShareBindRequest request) {

        String shareCode = request.getShareCode();

        if (shareCode == null || shareCode.isEmpty()) {
            return ApiResponse.error(400, "share_code不能为空");
        }

        // 如果是PURA365_SHARE:前缀格式，提取分享码
        if (shareCode.startsWith("PURA365_SHARE:")) {
            shareCode = shareCode.substring("PURA365_SHARE:".length());
        }

        try {
            Map<String, Object> result = deviceShareService.bindByShareCode(currentUserId, shareCode);
            return ApiResponse.success(result);
        } catch (RuntimeException e) {
            return ApiResponse.error(400, e.getMessage());
        } catch (Exception e) {
            log.error("绑定设备失败", e);
            return ApiResponse.error(500, "绑定失败");
        }
    }

    /**
     * 获取设备的分享列表
     * GET /share/list?device_id=xxx
     */
    @Operation(summary = "获取分享列表", description = "获取设备的分享用户列表")
    @GetMapping("/list")
    public ApiResponse<List<Map<String, Object>>> getShareList(
            @RequestAttribute("currentUserId") Long currentUserId,
            @RequestParam("device_id") String deviceId) {

        if (deviceId == null || deviceId.isEmpty()) {
            return ApiResponse.error(400, "device_id不能为空");
        }

        // 验证是设备拥有者
        if (!deviceShareService.isDeviceOwner(currentUserId, deviceId)) {
            return ApiResponse.error(403, "只有设备拥有者才能查看分享列表");
        }

        try {
            List<Map<String, Object>> list = deviceShareService.getShareList(currentUserId, deviceId);
            return ApiResponse.success(list);
        } catch (Exception e) {
            log.error("获取分享列表失败", e);
            return ApiResponse.error(500, "获取失败");
        }
    }

    /**
     * 取消分享（移除某用户的访问权限）
     * POST /share/revoke
     */
    @Operation(summary = "取消分享", description = "移除某用户对设备的访问权限")
    @PostMapping("/revoke")
    public ApiResponse<Map<String, Object>> revokeShare(
            @RequestAttribute("currentUserId") Long currentUserId,
            @RequestBody ShareRevokeRequest request) {

        String deviceId = request.getDeviceId();
        Long targetUserId = request.getUserId();

        if (deviceId == null || deviceId.isEmpty()) {
            return ApiResponse.error(400, "device_id不能为空");
        }
        if (targetUserId == null) {
            return ApiResponse.error(400, "user_id不能为空");
        }

        try {
            boolean success = deviceShareService.revokeShare(currentUserId, deviceId, targetUserId);
            if (success) {
                return ApiResponse.success(null);
            } else {
                return ApiResponse.error(404, "分享记录不存在");
            }
        } catch (RuntimeException e) {
            return ApiResponse.error(403, e.getMessage());
        } catch (Exception e) {
            log.error("取消分享失败", e);
            return ApiResponse.error(500, "操作失败");
        }
    }

    /**
     * 更新分享权限
     * POST /share/permission
     */
    @Operation(summary = "更新分享权限", description = "修改某用户对设备的权限")
    @PostMapping("/permission")
    public ApiResponse<Map<String, Object>> updatePermission(
            @RequestAttribute("currentUserId") Long currentUserId,
            @RequestBody SharePermissionUpdateRequest request) {

        String deviceId = request.getDeviceId();
        Long targetUserId = request.getUserId();
        String permission = request.getPermission();

        if (deviceId == null || deviceId.isEmpty()) {
            return ApiResponse.error(400, "device_id不能为空");
        }
        if (targetUserId == null) {
            return ApiResponse.error(400, "user_id不能为空");
        }
        if (permission == null || permission.isEmpty()) {
            return ApiResponse.error(400, "permission不能为空");
        }

        try {
            boolean success = deviceShareService.updatePermission(currentUserId, deviceId, targetUserId, permission);
            if (success) {
                return ApiResponse.success(null);
            } else {
                return ApiResponse.error(500, "更新失败");
            }
        } catch (RuntimeException e) {
            return ApiResponse.error(400, e.getMessage());
        } catch (Exception e) {
            log.error("更新权限失败", e);
            return ApiResponse.error(500, "操作失败");
        }
    }

    /**
     * 检查当前用户对设备的权限
     * GET /share/check-permission?device_id=xxx
     */
    @Operation(summary = "检查设备权限", description = "检查当前用户对设备的权限")
    @GetMapping("/check-permission")
    public ApiResponse<Map<String, Object>> checkPermission(
            @RequestAttribute("currentUserId") Long currentUserId,
            @RequestParam("device_id") String deviceId) {

        if (deviceId == null || deviceId.isEmpty()) {
            return ApiResponse.error(400, "device_id不能为空");
        }

        String permission = deviceShareService.checkPermission(currentUserId, deviceId);

        java.util.Map<String, Object> result = new java.util.HashMap<>();
        result.put("device_id", deviceId);
        result.put("permission", permission);
        result.put("can_view", permission != null);
        result.put("can_control", "owner".equals(permission) || "full_control".equals(permission));
        result.put("is_owner", "owner".equals(permission));

        return ApiResponse.success(result);
    }
}
