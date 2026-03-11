package com.vyxentra.vehicle.controller;


import com.vyxentra.vehicle.dto.response.ApiResponse;
import com.vyxentra.vehicle.dto.response.RevenueReportResponse;
import com.vyxentra.vehicle.service.AnalyticsService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/v1/admin/dashboard")
@RequiredArgsConstructor
public class DashboardController {

    private final AnalyticsService analyticsService;

    @GetMapping("/revenue/daily")
    public ResponseEntity<ApiResponse<RevenueReportResponse>> getDailyRevenue(
            @RequestHeader("X-User-ID") String adminId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        log.info("Getting daily revenue for date: {} by admin: {}", date, adminId);
        RevenueReportResponse response = analyticsService.getDailyRevenue(date);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/revenue/range")
    public ResponseEntity<ApiResponse<RevenueReportResponse>> getRevenueRange(
            @RequestHeader("X-User-ID") String adminId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fromDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate toDate,
            @RequestParam(defaultValue = "daily") String interval) {
        log.info("Getting revenue from {} to {} by admin: {}", fromDate, toDate, adminId);
        RevenueReportResponse response = analyticsService.getRevenueRange(fromDate, toDate, interval);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/bookings/status")
    public ResponseEntity<ApiResponse<Map<String, Long>>> getBookingStatusDistribution(
            @RequestHeader("X-User-ID") String adminId) {
        log.info("Getting booking status distribution by admin: {}", adminId);
        Map<String, Long> distribution = analyticsService.getBookingStatusDistribution();
        return ResponseEntity.ok(ApiResponse.success(distribution));
    }

    @GetMapping("/providers/top")
    public ResponseEntity<ApiResponse<Object>> getTopProviders(
            @RequestHeader("X-User-ID") String adminId,
            @RequestParam(defaultValue = "10") int limit) {
        log.info("Getting top {} providers by admin: {}", limit, adminId);
        Object providers = analyticsService.getTopProviders(limit);
        return ResponseEntity.ok(ApiResponse.success(providers));
    }

    @GetMapping("/customers/active")
    public ResponseEntity<ApiResponse<Long>> getActiveCustomers(
            @RequestHeader("X-User-ID") String adminId,
            @RequestParam(defaultValue = "30") int days) {
        log.info("Getting active customers in last {} days by admin: {}", days, adminId);
        Long count = analyticsService.getActiveCustomers(days);
        return ResponseEntity.ok(ApiResponse.success(count));
    }

    @GetMapping("/growth")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getGrowthMetrics(
            @RequestHeader("X-User-ID") String adminId) {
        log.info("Getting growth metrics by admin: {}", adminId);
        Map<String, Object> metrics = analyticsService.getGrowthMetrics();
        return ResponseEntity.ok(ApiResponse.success(metrics));
    }
}