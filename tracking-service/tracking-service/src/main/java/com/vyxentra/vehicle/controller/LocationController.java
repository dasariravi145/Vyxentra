package com.vyxentra.vehicle.controller;

import com.vyxentra.vehicle.dto.request.LocationUpdateRequest;
import com.vyxentra.vehicle.dto.response.ApiResponse;
import com.vyxentra.vehicle.dto.response.LocationResponse;
import com.vyxentra.vehicle.dto.response.PathResponse;
import com.vyxentra.vehicle.service.LocationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/v1/tracking/locations")
@RequiredArgsConstructor
public class LocationController {

    private final LocationService locationService;

    @PostMapping
    public ResponseEntity<ApiResponse<Void>> updateLocation(
            @RequestHeader("X-User-ID") String userId,
            @Valid @RequestBody LocationUpdateRequest request) {
        log.info("Location update from user: {} at ({}, {})", userId,
                request.getLatitude(), request.getLongitude());
        locationService.updateLocation(userId, request);
        return ResponseEntity.ok(ApiResponse.success(null, "Location updated"));
    }

    @GetMapping("/current/{entityId}")
    public ResponseEntity<ApiResponse<LocationResponse>> getCurrentLocation(
            @PathVariable String entityId,
            @RequestParam String entityType) {
        log.info("Getting current location for {}: {}", entityType, entityId);
        LocationResponse response = locationService.getCurrentLocation(entityId, entityType);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/history/{entityId}")
    public ResponseEntity<ApiResponse<List<LocationResponse>>> getLocationHistory(
            @PathVariable String entityId,
            @RequestParam String entityType,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime from,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime to) {
        log.info("Getting location history for {}: {} from {} to {}", entityType, entityId, from, to);
        List<LocationResponse> history = locationService.getLocationHistory(entityId, entityType, from, to);
        return ResponseEntity.ok(ApiResponse.success(history));
    }

    @GetMapping("/path/{bookingId}")
    public ResponseEntity<ApiResponse<PathResponse>> getTrackingPath(
            @PathVariable String bookingId) {
        log.info("Getting tracking path for booking: {}", bookingId);
        PathResponse response = locationService.getTrackingPath(bookingId);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/active")
    public ResponseEntity<ApiResponse<List<String>>> getActiveTrackings(
            @RequestParam(required = false) String providerId) {
        log.info("Getting active trackings for provider: {}", providerId);
        List<String> activeBookings = locationService.getActiveTrackings(providerId);
        return ResponseEntity.ok(ApiResponse.success(activeBookings));
    }

    @DeleteMapping("/history/cleanup")
    public ResponseEntity<ApiResponse<Void>> cleanupOldLocations() {
        log.info("Cleaning up old location history");
        locationService.cleanupOldLocations();
        return ResponseEntity.ok(ApiResponse.success(null, "Cleanup completed"));
    }
}
