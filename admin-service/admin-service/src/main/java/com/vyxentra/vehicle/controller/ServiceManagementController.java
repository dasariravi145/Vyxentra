package com.vyxentra.vehicle.controller;

import com.vyxentra.vehicle.dto.request.CommissionUpdateRequest;
import com.vyxentra.vehicle.dto.request.ServiceConfigRequest;
import com.vyxentra.vehicle.dto.response.ApiResponse;
import com.vyxentra.vehicle.dto.response.CommissionConfigResponse;
import com.vyxentra.vehicle.dto.response.ServiceConfigResponse;
import com.vyxentra.vehicle.service.AdminService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/v1/admin/services")
@RequiredArgsConstructor
public class ServiceManagementController {

    private final AdminService adminService;

    @GetMapping("/config")
    public ResponseEntity<ApiResponse<List<ServiceConfigResponse>>> getAllServiceConfigs(
            @RequestHeader("X-User-ID") String adminId) {
        log.info("Getting all service configs for admin: {}", adminId);
        List<ServiceConfigResponse> responses = adminService.getAllServiceConfigs();
        return ResponseEntity.ok(ApiResponse.success(responses));
    }

    @GetMapping("/config/{serviceType}")
    public ResponseEntity<ApiResponse<ServiceConfigResponse>> getServiceConfig(
            @RequestHeader("X-User-ID") String adminId,
            @PathVariable String serviceType) {
        log.info("Getting service config for: {} by admin: {}", serviceType, adminId);
        ServiceConfigResponse response = adminService.getServiceConfig(serviceType);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @PutMapping("/config/{serviceType}")
    public ResponseEntity<ApiResponse<ServiceConfigResponse>> updateServiceConfig(
            @RequestHeader("X-User-ID") String adminId,
            @PathVariable String serviceType,
            @Valid @RequestBody ServiceConfigRequest request) {
        log.info("Updating service config for: {} by admin: {}", serviceType, adminId);
        ServiceConfigResponse response = adminService.updateServiceConfig(adminId, serviceType, request);
        return ResponseEntity.ok(ApiResponse.success(response, "Service config updated"));
    }

    @PostMapping("/config/{serviceType}/toggle")
    public ResponseEntity<ApiResponse<Void>> toggleService(
            @RequestHeader("X-User-ID") String adminId,
            @PathVariable String serviceType,
            @RequestParam boolean active) {
        log.info("Toggling service: {} to {} by admin: {}", serviceType, active, adminId);
        adminService.toggleService(adminId, serviceType, active);
        return ResponseEntity.ok(ApiResponse.success(null,
                active ? "Service activated" : "Service deactivated"));
    }

    @GetMapping("/commission")
    public ResponseEntity<ApiResponse<List<CommissionConfigResponse>>> getCommissionConfigs(
            @RequestHeader("X-User-ID") String adminId) {
        log.info("Getting commission configs for admin: {}", adminId);
        List<CommissionConfigResponse> responses = adminService.getCommissionConfigs();
        return ResponseEntity.ok(ApiResponse.success(responses));
    }

    @PutMapping("/commission/{providerType}")
    public ResponseEntity<ApiResponse<CommissionConfigResponse>> updateCommission(
            @RequestHeader("X-User-ID") String adminId,
            @PathVariable String providerType,
            @Valid @RequestBody CommissionUpdateRequest request) {
        log.info("Updating commission for: {} by admin: {}", providerType, adminId);
        CommissionConfigResponse response = adminService.updateCommission(adminId, providerType, request);
        return ResponseEntity.ok(ApiResponse.success(response, "Commission updated"));
    }
}
