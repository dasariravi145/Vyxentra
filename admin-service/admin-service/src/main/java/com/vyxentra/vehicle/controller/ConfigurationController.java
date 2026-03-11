package com.vyxentra.vehicle.controller;


import com.vyxentra.vehicle.dto.response.ApiResponse;
import com.vyxentra.vehicle.dto.response.SystemConfigResponse;
import com.vyxentra.vehicle.service.AdminService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/v1/admin/config")
@RequiredArgsConstructor
public class ConfigurationController {

    private final AdminService adminService;

    @GetMapping
    public ResponseEntity<ApiResponse<List<SystemConfigResponse>>> getAllConfigs(
            @RequestHeader("X-User-ID") String adminId) {
        log.info("Getting all system configs for admin: {}", adminId);
        List<SystemConfigResponse> responses = adminService.getAllConfigs();
        return ResponseEntity.ok(ApiResponse.success(responses));
    }

    @GetMapping("/{key}")
    public ResponseEntity<ApiResponse<SystemConfigResponse>> getConfig(
            @RequestHeader("X-User-ID") String adminId,
            @PathVariable String key) {
        log.info("Getting config for key: {} by admin: {}", key, adminId);
        SystemConfigResponse response = adminService.getConfig(key);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @PutMapping("/{key}")
    public ResponseEntity<ApiResponse<SystemConfigResponse>> updateConfig(
            @RequestHeader("X-User-ID") String adminId,
            @PathVariable String key,
            @RequestBody Map<String, Object> request) {
        log.info("Updating config for key: {} by admin: {}", key, adminId);
        SystemConfigResponse response = adminService.updateConfig(adminId, key, request);
        return ResponseEntity.ok(ApiResponse.success(response, "Configuration updated"));
    }

    @PostMapping("/{key}/reset")
    public ResponseEntity<ApiResponse<SystemConfigResponse>> resetConfig(
            @RequestHeader("X-User-ID") String adminId,
            @PathVariable String key) {
        log.info("Resetting config for key: {} by admin: {}", key, adminId);
        SystemConfigResponse response = adminService.resetConfig(adminId, key);
        return ResponseEntity.ok(ApiResponse.success(response, "Configuration reset to default"));
    }

    @GetMapping("/export")
    public ResponseEntity<Map<String, Object>> exportAllConfigs(
            @RequestHeader("X-User-ID") String adminId) {
        log.info("Exporting all configs by admin: {}", adminId);
        Map<String, Object> configs = adminService.exportAllConfigs();
        return ResponseEntity.ok(configs);
    }

    @PostMapping("/import")
    public ResponseEntity<ApiResponse<Void>> importConfigs(
            @RequestHeader("X-User-ID") String adminId,
            @RequestBody Map<String, Object> configs) {
        log.info("Importing configs by admin: {}", adminId);
        adminService.importConfigs(adminId, configs);
        return ResponseEntity.ok(ApiResponse.success(null, "Configurations imported"));
    }
}