package com.vyxentra.vehicle.controller;


import com.vyxentra.vehicle.dto.response.ApiResponse;
import com.vyxentra.vehicle.dto.response.RevenueReportResponse;
import com.vyxentra.vehicle.service.AnalyticsService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/v1/admin/analytics")
@PreAuthorize("hasRole('ADMIN')")
@RequiredArgsConstructor
public class AnalyticsController {

    private final AnalyticsService analyticsService;

    @GetMapping("/revenue/daily")
    public ResponseEntity<ApiResponse<RevenueReportResponse>> getDailyRevenue(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        log.info("Getting daily revenue for date: {}", date);
        RevenueReportResponse response = analyticsService.getDailyRevenue(date);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/revenue/range")
    public ResponseEntity<ApiResponse<RevenueReportResponse>> getRevenueRange(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fromDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate toDate,@RequestParam(defaultValue = "daily") String interval) {
        log.info("Getting revenue from {} to {}", fromDate, toDate);
        RevenueReportResponse response = analyticsService.getRevenueRange(fromDate, toDate,interval);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/bookings/status")
    public ResponseEntity<ApiResponse<Map<String, Long>>> getBookingStatusDistribution() {
        log.info("Getting booking status distribution");
        Map<String, Long> distribution = analyticsService.getBookingStatusDistribution();
        return ResponseEntity.ok(ApiResponse.success(distribution));
    }

    @GetMapping("/growth")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getGrowthMetrics() {
        log.info("Getting growth metrics");
        Map<String, Object> metrics = analyticsService.getGrowthMetrics();
        return ResponseEntity.ok(ApiResponse.success(metrics));
    }
}
