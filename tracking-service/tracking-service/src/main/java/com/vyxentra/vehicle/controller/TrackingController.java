package com.vyxentra.vehicle.controller;

import com.vyxentra.vehicle.dto.request.TrackingSubscribeRequest;
import com.vyxentra.vehicle.dto.response.ApiResponse;
import com.vyxentra.vehicle.dto.response.TrackingInfoResponse;
import com.vyxentra.vehicle.service.TrackingService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/api/v1/tracking")
@RequiredArgsConstructor
public class TrackingController {

    private final TrackingService trackingService;

    @PostMapping("/subscribe")
    public ResponseEntity<ApiResponse<TrackingInfoResponse>> subscribeToTracking(
            @RequestHeader("X-User-ID") String userId,
            @Valid @RequestBody TrackingSubscribeRequest request) {
        log.info("User {} subscribing to tracking for booking: {}", userId, request.getBookingId());
        TrackingInfoResponse response = trackingService.subscribeToTracking(userId, request);
        return ResponseEntity.ok(ApiResponse.success(response, "Subscribed to tracking"));
    }

    @DeleteMapping("/unsubscribe/{bookingId}")
    public ResponseEntity<ApiResponse<Void>> unsubscribeFromTracking(
            @RequestHeader("X-User-ID") String userId,
            @PathVariable String bookingId) {
        log.info("User {} unsubscribing from tracking for booking: {}", userId, bookingId);
        trackingService.unsubscribeFromTracking(userId, bookingId);
        return ResponseEntity.ok(ApiResponse.success(null, "Unsubscribed from tracking"));
    }

    @GetMapping("/session/{bookingId}")
    public ResponseEntity<ApiResponse<TrackingInfoResponse>> getTrackingSession(
            @PathVariable String bookingId) {
        log.info("Getting tracking session for booking: {}", bookingId);
        TrackingInfoResponse response = trackingService.getTrackingSession(bookingId);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/session/{bookingId}/status")
    public ResponseEntity<ApiResponse<String>> getTrackingStatus(
            @PathVariable String bookingId) {
        log.info("Getting tracking status for booking: {}", bookingId);
        String status = trackingService.getTrackingStatus(bookingId);
        return ResponseEntity.ok(ApiResponse.success(status));
    }

    @PostMapping("/session/{bookingId}/pause")
    public ResponseEntity<ApiResponse<Void>> pauseTracking(
            @RequestHeader("X-User-ID") String userId,
            @PathVariable String bookingId) {
        log.info("Pausing tracking for booking: {} by user: {}", bookingId, userId);
        trackingService.pauseTracking(userId, bookingId);
        return ResponseEntity.ok(ApiResponse.success(null, "Tracking paused"));
    }

    @PostMapping("/session/{bookingId}/resume")
    public ResponseEntity<ApiResponse<Void>> resumeTracking(
            @RequestHeader("X-User-ID") String userId,
            @PathVariable String bookingId) {
        log.info("Resuming tracking for booking: {} by user: {}", bookingId, userId);
        trackingService.resumeTracking(userId, bookingId);
        return ResponseEntity.ok(ApiResponse.success(null, "Tracking resumed"));
    }

    @GetMapping("/eta/{bookingId}")
    public ResponseEntity<ApiResponse<Integer>> getCurrentETA(
            @PathVariable String bookingId) {
        log.info("Getting current ETA for booking: {}", bookingId);
        Integer eta = trackingService.getCurrentETA(bookingId);
        return ResponseEntity.ok(ApiResponse.success(eta));
    }
}
