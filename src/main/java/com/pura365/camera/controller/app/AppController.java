package com.pura365.camera.controller.app;

import com.pura365.camera.model.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * App 通用接口
 * 包含:
 * - 版本检查
 * - 提交反馈
 */
@Tag(name = "App通用接口", description = "App版本检查、反馈等通用接口")
@RestController
@RequestMapping("/api/app")
public class AppController {

    /**
     * 版本检查
     * GET /api/app/version?platform=ios&current_version=1.0.0
     */
    @Operation(summary = "版本检查", description = "检查App是否需要更新")
    @GetMapping("/version")
    public ApiResponse<Map<String, Object>> checkVersion(
            @RequestParam String platform,
            @RequestParam(name = "current_version") String currentVersion) {
        
        if (platform == null || currentVersion == null) {
            return ApiResponse.error(400, "platform 和 current_version 不能为空");
        }

        Map<String, Object> data = new HashMap<>();
        data.put("latest_version", "1.0.1");
        data.put("min_version", "1.0.0");
        data.put("download_url", "https://example.com/download");
        data.put("release_notes", "修复了一些问题");
        data.put("force_update", false);
        return ApiResponse.success(data);
    }

    /**
     * 提交反馈
     * POST /feedback
     * Body: {
     *   "content": "反馈内容",
     *   "contact": "联系方式(可选)",
     *   "images": ["图片URL1", "图片URL2"]
     * }
     */
    @Operation(summary = "提交反馈", description = "用户提交反馈意见")
    @PostMapping("/feedback")
    public ApiResponse<Map<String, Object>> submitFeedback(
            @RequestAttribute("currentUserId") Long currentUserId,
            @RequestBody Map<String, Object> body) {
        
        String content = (String) body.get("content");
        if (content == null || content.trim().isEmpty()) {
            return ApiResponse.error(400, "content 不能为空");
        }

        String contact = (String) body.get("contact");
        Object images = body.get("images");

        // TODO: 保存反馈到数据库
        Long feedbackId = System.currentTimeMillis();

        Map<String, Object> result = new HashMap<>();
        result.put("feedback_id", feedbackId);
        result.put("created_at", new java.text.SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'")
                .format(new java.util.Date()));
        
        return ApiResponse.success(result);
    }
}