package com.pura365.camera.controller.app;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.pura365.camera.domain.AppMessage;
import com.pura365.camera.domain.Device;
import com.pura365.camera.model.ApiResponse;
import com.pura365.camera.repository.AppMessageRepository;
import com.pura365.camera.repository.DeviceRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.text.SimpleDateFormat;
import java.util.*;

/**
 * 消息中心相关接口
 * 
 * 包含：
 * - 消息列表
 * - 标记已读
 * - 删除消息
 * - 未读数量
 */
@Tag(name = "消息管理", description = "用户消息查询、标记、删除等接口")
@RestController
@RequestMapping("/api/app/messages")
public class MessageController {

    @Autowired
    private AppMessageRepository appMessageRepository;

    @Autowired
    private DeviceRepository deviceRepository;

    /**
     * 消息列表 - GET /messages
     */
    @GetMapping
    public ApiResponse<Map<String, Object>> listMessages(@RequestAttribute("currentUserId") Long currentUserId,
                                                         @RequestParam(value = "device_id", required = false) String deviceId,
                                                         @RequestParam(value = "date", required = false) String date,
                                                         @RequestParam(value = "type", required = false) String type,
                                                         @RequestParam(value = "page", required = false, defaultValue = "1") int page,
                                                         @RequestParam(value = "page_size", required = false, defaultValue = "20") int pageSize) {
        if (page < 1) page = 1;
        if (pageSize <= 0) pageSize = 20;
        int offset = (page - 1) * pageSize;

        QueryWrapper<AppMessage> qw = new QueryWrapper<>();
        qw.lambda().eq(AppMessage::getUserId, currentUserId);
        if (deviceId != null && !deviceId.isEmpty()) {
            qw.lambda().eq(AppMessage::getDeviceId, deviceId);
        }
        if (type != null && !type.isEmpty()) {
            qw.lambda().eq(AppMessage::getType, type);
        }
        if (date != null && !date.isEmpty()) {
            // 按日期过滤 created_at 的日期部分
            qw.apply("DATE(created_at) = {0}", date);
        }
        qw.orderByDesc("created_at");

        int total = appMessageRepository.selectCount(qw).intValue();
        List<AppMessage> rows = appMessageRepository.selectList(qw.last("limit " + offset + "," + pageSize));

        Map<String, String> deviceNameCache = new HashMap<>();
        List<Map<String, Object>> list = new ArrayList<>();
        if (rows != null) {
            for (AppMessage msg : rows) {
                Map<String, Object> item = new HashMap<>();
                item.put("id", msg.getId());
                item.put("type", msg.getType());
                item.put("title", msg.getTitle());
                item.put("content", msg.getContent());
                item.put("device_id", msg.getDeviceId());

                String devId = msg.getDeviceId();
                String devName = null;
                if (devId != null && !devId.isEmpty()) {
                    devName = deviceNameCache.get(devId);
                    if (devName == null) {
                        Device d = deviceRepository.selectById(devId);
                        if (d != null) {
                            devName = d.getName();
                            deviceNameCache.put(devId, devName);
                        }
                    }
                }
                item.put("device_name", devName);
                item.put("thumbnail_url", msg.getThumbnailUrl());
                item.put("video_url", msg.getVideoUrl());
                item.put("is_read", msg.getIsRead() != null && msg.getIsRead() == 1);
                if (msg.getCreatedAt() != null) {
                    item.put("created_at", formatIsoTime(msg.getCreatedAt()));
                }
                list.add(item);
            }
        }

        Map<String, Object> data = new HashMap<>();
        data.put("list", list);
        data.put("total", total);
        data.put("page", page);
        data.put("page_size", pageSize);
        return ApiResponse.success(data);
    }

    /**
     * 标记消息已读 - POST /messages/{id}/read
     */
    @PostMapping("/{id}/read")
    public ApiResponse<Void> markMessageRead(@RequestAttribute("currentUserId") Long currentUserId,
                                             @PathVariable("id") Long id) {
        AppMessage msg = appMessageRepository.selectById(id);
        if (msg == null || msg.getUserId() == null || !msg.getUserId().equals(currentUserId)) {
            return ApiResponse.error(404, "消息不存在");
        }
        if (msg.getIsRead() == null || msg.getIsRead() == 0) {
            msg.setIsRead(1);
            appMessageRepository.updateById(msg);
        }
        return ApiResponse.success("标记成功", null);
    }

    /**
     * 删除消息 - DELETE /messages/{id}
     */
    @DeleteMapping("/{id}")
    public ApiResponse<Void> deleteMessage(@RequestAttribute("currentUserId") Long currentUserId,
                                           @PathVariable("id") Long id) {
        AppMessage msg = appMessageRepository.selectById(id);
        if (msg == null || msg.getUserId() == null || !msg.getUserId().equals(currentUserId)) {
            return ApiResponse.error(404, "消息不存在");
        }
        appMessageRepository.deleteById(id);
        return ApiResponse.success("删除成功", null);
    }

    /**
     * 获取未读消息数量 - GET /messages/unread/count
     */
    @GetMapping("/unread/count")
    public ApiResponse<Map<String, Object>> getUnreadCount(@RequestAttribute("currentUserId") Long currentUserId) {
        QueryWrapper<AppMessage> qw = new QueryWrapper<>();
        qw.lambda().eq(AppMessage::getUserId, currentUserId)
                .eq(AppMessage::getIsRead, 0);
        int count = appMessageRepository.selectCount(qw).intValue();
        Map<String, Object> data = new HashMap<>();
        data.put("count", count);
        return ApiResponse.success(data);
    }

    private String formatIsoTime(Date date) {
        if (date == null) return null;
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
        sdf.setTimeZone(TimeZone.getTimeZone("UTC"));
        return sdf.format(date);
    }
}
