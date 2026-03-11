package com.vyxentra.vehicle.controller;


import com.vyxentra.vehicle.dto.request.AddonRequest;
import com.vyxentra.vehicle.dto.response.AddonResponse;
import com.vyxentra.vehicle.dto.response.ApiResponse;
import com.vyxentra.vehicle.service.AddonService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/v1/catalog/addons")
@RequiredArgsConstructor
public class AddonController {

    private final AddonService addonService;

    @GetMapping("/service/{serviceId}")
    public ResponseEntity<ApiResponse<List<AddonResponse>>> getAddonsForService(
            @PathVariable String serviceId,
            @RequestParam(required = false) Boolean active) {
        log.info("Getting addons for service: {}, active: {}", serviceId, active);
        List<AddonResponse> responses = addonService.getAddonsForService(serviceId, active);
        return ResponseEntity.ok(ApiResponse.success(responses));
    }

    @GetMapping("/{addonId}")
    public ResponseEntity<ApiResponse<AddonResponse>> getAddon(
            @PathVariable String addonId) {
        log.info("Getting addon: {}", addonId);
        AddonResponse response = addonService.getAddon(addonId);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @PostMapping("/admin/service/{serviceId}")
    public ResponseEntity<ApiResponse<AddonResponse>> createAddon(
            @RequestHeader("X-User-ID") String adminId,
            @PathVariable String serviceId,
            @Valid @RequestBody AddonRequest request) {
        log.info("Creating addon for service: {} by admin: {}", serviceId, adminId);
        AddonResponse response = addonService.createAddon(adminId, serviceId, request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(response, "Addon created successfully"));
    }

    @PutMapping("/admin/{addonId}")
    public ResponseEntity<ApiResponse<AddonResponse>> updateAddon(
            @RequestHeader("X-User-ID") String adminId,
            @PathVariable String addonId,
            @Valid @RequestBody AddonRequest request) {
        log.info("Updating addon: {} by admin: {}", addonId, adminId);
        AddonResponse response = addonService.updateAddon(adminId, addonId, request);
        return ResponseEntity.ok(ApiResponse.success(response, "Addon updated successfully"));
    }

    @DeleteMapping("/admin/{addonId}")
    public ResponseEntity<ApiResponse<Void>> deleteAddon(
            @RequestHeader("X-User-ID") String adminId,
            @PathVariable String addonId) {
        log.info("Deleting addon: {} by admin: {}", addonId, adminId);
        addonService.deleteAddon(adminId, addonId);
        return ResponseEntity.ok(ApiResponse.success(null, "Addon deleted successfully"));
    }

    @PatchMapping("/admin/{addonId}/toggle")
    public ResponseEntity<ApiResponse<Void>> toggleAddonStatus(
            @RequestHeader("X-User-ID") String adminId,
            @PathVariable String addonId,
            @RequestParam boolean active) {
        log.info("Toggling addon: {} to {} by admin: {}", addonId, active, adminId);
        addonService.toggleAddonStatus(adminId, addonId, active);
        return ResponseEntity.ok(ApiResponse.success(null,
                active ? "Addon activated" : "Addon deactivated"));
    }

    @PostMapping("/admin/reorder/{serviceId}")
    public ResponseEntity<ApiResponse<Void>> reorderAddons(
            @RequestHeader("X-User-ID") String adminId,
            @PathVariable String serviceId,
            @RequestBody List<String> addonIds) {
        log.info("Reordering addons for service: {} by admin: {}", serviceId, adminId);
        addonService.reorderAddons(adminId, serviceId, addonIds);
        return ResponseEntity.ok(ApiResponse.success(null, "Addons reordered successfully"));
    }
}