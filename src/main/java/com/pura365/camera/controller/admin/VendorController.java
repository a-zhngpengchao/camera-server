package com.pura365.camera.controller.admin;

import com.pura365.camera.domain.Vendor;
import com.pura365.camera.model.ApiResponse;
import com.pura365.camera.service.DeviceProductionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 经销商管理接口
 */
@Tag(name = "经销商管理", description = "经销商信息管理相关接口")
@RestController
@RequestMapping("/api/admin/vendors")
public class VendorController {

    @Autowired
    private DeviceProductionService productionService;

    /**
     * 获取启用的经销商列表
     */
    @Operation(summary = "获取启用的经销商列表", description = "获取所有状态为启用的经销商")
    @GetMapping
    public ApiResponse<List<Vendor>> listVendors() {
        return ApiResponse.success(productionService.listVendors());
    }

    /**
     * 获取所有经销商列表(包含禁用的)
     */
    @Operation(summary = "获取所有经销商", description = "获取所有经销商列表(包含已禁用的)")
    @GetMapping("/all")
    public ApiResponse<List<Vendor>> listAllVendors() {
        return ApiResponse.success(productionService.listAllVendors());
    }

    /**
     * 获取单个经销商详情
     */
    @Operation(summary = "获取经销商详情", description = "根据ID获取经销商详细信息")
    @GetMapping("/{id}")
    public ApiResponse<Vendor> getVendor(@PathVariable Long id) {
        Vendor vendor = productionService.getVendorById(id);
        if (vendor == null) {
            return ApiResponse.error(404, "经销商不存在");
        }
        return ApiResponse.success(vendor);
    }

    /**
     * 新增经销商
     * 请求体:{
     *   "vendorCode": "02",
     *   "vendorName": "经销商名称",
     *   "contactPerson": "联系人",
     *   "contactPhone": "13800138000",
     *   "address": "地址",
     *   "status": 1
     * }
     */
    @Operation(summary = "新增经销商", description = "创建新的经销商信息")
    @PostMapping
    public ApiResponse<Vendor> createVendor(@RequestBody Vendor vendor) {
        try {
            Vendor created = productionService.createVendor(vendor);
            return ApiResponse.success(created);
        } catch (RuntimeException e) {
            return ApiResponse.error(400, e.getMessage());
        }
    }

    /**
     * 更新经销商信息
     */
    @Operation(summary = "更新经销商", description = "更新经销商信息")
    @PutMapping("/{id}")
    public ApiResponse<Vendor> updateVendor(@PathVariable Long id, @RequestBody Vendor vendor) {
        try {
            vendor.setId(id);
            Vendor updated = productionService.updateVendor(vendor);
            return ApiResponse.success(updated);
        } catch (RuntimeException e) {
            return ApiResponse.error(400, e.getMessage());
        }
    }

    /**
     * 删除经销商
     */
    @Operation(summary = "删除经销商", description = "删除指定的经销商")
    @DeleteMapping("/{id}")
    public ApiResponse<Void> deleteVendor(@PathVariable Long id) {
        try {
            productionService.deleteVendor(id);
            return ApiResponse.success(null);
        } catch (RuntimeException e) {
            return ApiResponse.error(400, e.getMessage());
        }
    }

    /**
     * 启用/禁用经销商
     * 请求体:{ "status": 1 } 或 { "status": 0 }
     */
    @Operation(summary = "更新经销商状态", description = "启用或禁用经销商")
    @PutMapping("/{id}/status")
    public ApiResponse<Void> updateVendorStatus(@PathVariable Long id, @RequestBody Map<String, Integer> body) {
        try {
            Integer status = body.get("status");
            if (status == null) {
                return ApiResponse.error(400, "status 不能为空");
            }
            productionService.updateVendorStatus(id, status);
            return ApiResponse.success(null);
        } catch (RuntimeException e) {
            return ApiResponse.error(400, e.getMessage());
        }
    }
}