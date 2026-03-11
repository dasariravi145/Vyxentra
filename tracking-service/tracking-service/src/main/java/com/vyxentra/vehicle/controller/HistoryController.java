package com.vyxentra.vehicle.controller;

import com.vyxentra.vehicle.dto.TrackingStats;
import com.vyxentra.vehicle.dto.response.ApiResponse;
import com.vyxentra.vehicle.dto.response.ETAUpdateResponse;
import com.vyxentra.vehicle.dto.response.TrackingInfoResponse;
import com.vyxentra.vehicle.service.TrackingService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/v1/tracking/history")
@RequiredArgsConstructor
public class HistoryController {

    private final TrackingService trackingService;

    @GetMapping("/user/{userId}")
    public ResponseEntity<ApiResponse<List<TrackingInfoResponse>>> getUserTrackingHistory(
            @PathVariable String userId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime from,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime to) {
        log.info("Getting tracking history for user: {} from {} to {}", userId, from, to);
        List<TrackingInfoResponse> history = trackingService.getUserTrackingHistory(userId, from, to);
        return ResponseEntity.ok(ApiResponse.success(history));
    }

    @GetMapping("/eta/{bookingId}")
    public ResponseEntity<ApiResponse<List<ETAUpdateResponse>>> getETAHistory(
            @PathVariable String bookingId) {
        log.info("Getting ETA history for booking: {}", bookingId);
        List<ETAUpdateResponse> history = trackingService.getETAHistory(bookingId);
        return ResponseEntity.ok(ApiResponse.success(history));
    }

    @GetMapping("/stats/{bookingId}")
    public ResponseEntity<ApiResponse<TrackingStats>> getTrackingStats(
            @PathVariable String bookingId) {
        log.info("Getting tracking stats for booking: {}", bookingId);
        TrackingStats stats = trackingService.getTrackingStats(bookingId);
        return ResponseEntity.ok(ApiResponse.success(stats));
    }
}
