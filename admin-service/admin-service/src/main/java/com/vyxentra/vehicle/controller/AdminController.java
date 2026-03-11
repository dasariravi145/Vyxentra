package com.vyxentra.vehicle.controller;

import com.vyxentra.vehicle.dto.request.DateRangeRequest;
import com.vyxentra.vehicle.dto.response.*;
import com.vyxentra.vehicle.service.AdminService;
import com.vyxentra.vehicle.service.AuditService;
import com.vyxentra.vehicle.service.DashboardService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;

@Slf4j
@RestController
@RequestMapping("/api/v1/admin")
@RequiredArgsConstructor
public class AdminController {

    private final DashboardService dashboardService;
    private final AuditService auditService;
    private final AdminService adminService;

    @GetMapping("/dashboard")
    public ResponseEntity<ApiResponse<DashboardResponse>> getDashboard(
            @RequestHeader("X-User-ID") String adminId) {
        log.info("Getting dashboard for admin: {}", adminId);
        DashboardResponse response = dashboardService.getDashboard();
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/dashboard/refresh")
    public ResponseEntity<ApiResponse<DashboardResponse>> refreshDashboard(
            @RequestHeader("X-User-ID") String adminId) {
        log.info("Refreshing dashboard by admin: {}", adminId);
        DashboardResponse response = dashboardService.refreshDashboard();
        return ResponseEntity.ok(ApiResponse.success(response, "Dashboard refreshed"));
    }

    @GetMapping("/audit-logs")
    public ResponseEntity<ApiResponse<PageResponse<AuditLogResponse>>> getAuditLogs(
            @RequestHeader("X-User-ID") String adminId,
            @Valid DateRangeRequest dateRange,
            @RequestParam(required = false) String eventType,
            @RequestParam(required = false) String userId,
            @RequestParam(required = false) String resourceType,
            @RequestParam(required = false) Boolean success,
            @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
        log.info("Getting audit logs for admin: {}", adminId);
        PageResponse<AuditLogResponse> response = auditService.getAuditLogs(
                dateRange, eventType, userId, resourceType, success, pageable);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/audit-logs/export")
    public ResponseEntity<byte[]> exportAuditLogs(
            @RequestHeader("X-User-ID") String adminId,
            @Valid DateRangeRequest dateRange,
            @RequestParam(defaultValue = "excel") String format) {
        log.info("Exporting audit logs for admin: {} in format: {}", adminId, format);
        byte[] data = auditService.exportAuditLogs(dateRange, format);

        String filename = "audit-logs-" + LocalDateTime.now() + "." + format;
        return ResponseEntity.ok()
                .header("Content-Disposition", "attachment; filename=" + filename)
                .body(data);
    }

    @GetMapping("/health")
    public ResponseEntity<ApiResponse<SystemHealthResponse>> getSystemHealth(
            @RequestHeader("X-User-ID") String adminId) {
        log.info("Getting system health for admin: {}", adminId);
        SystemHealthResponse response = adminService.getSystemHealth();
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @PostMapping("/cache/clear")
    public ResponseEntity<ApiResponse<Void>> clearCache(
            @RequestHeader("X-User-ID") String adminId,
            @RequestParam(required = false) String cacheName) {
        log.info("Clearing cache {} by admin: {}", cacheName, adminId);
        adminService.clearCache(cacheName);
        return ResponseEntity.ok(ApiResponse.success(null, "Cache cleared"));
    }

    @GetMapping("/metrics")
    public ResponseEntity<ApiResponse<Object>> getMetrics(
            @RequestHeader("X-User-ID") String adminId) {
        log.info("Getting metrics for admin: {}", adminId);
        Object metrics = adminService.getMetrics();
        return ResponseEntity.ok(ApiResponse.success(metrics));
    }
}
