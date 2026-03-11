package com.vyxentra.vehicle.controller;


import com.vyxentra.vehicle.dto.request.SendPushRequest;
import com.vyxentra.vehicle.dto.response.ApiResponse;
import com.vyxentra.vehicle.dto.response.NotificationResponse;
import com.vyxentra.vehicle.entity.PushDevice;
import com.vyxentra.vehicle.service.PushNotificationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/v1/notifications/push")
@RequiredArgsConstructor
public class PushController {

    private final PushNotificationService pushService;

    @PostMapping("/send")
    public ResponseEntity<ApiResponse<NotificationResponse>> sendPush(
            @RequestHeader("X-User-ID") String userId,
            @Valid @RequestBody SendPushRequest request) {
        log.info("Sending push notification to user: {}", userId);
        NotificationResponse response = pushService.sendPush(userId, request);
        return ResponseEntity.ok(ApiResponse.success(response, "Push notification sent"));
    }

    @PostMapping("/broadcast")
    public ResponseEntity<ApiResponse<Void>> broadcastPush(
            @RequestBody SendPushRequest request,
            @RequestParam(required = false) String userType) {
        log.info("Broadcasting push notification to {} users", userType != null ? userType : "all");
        pushService.broadcastPush(request, userType);
        return ResponseEntity.ok(ApiResponse.success(null, "Push broadcast queued"));
    }

    @PostMapping("/device/register")
    public ResponseEntity<ApiResponse<Void>> registerDevice(
            @RequestHeader("X-User-ID") String userId,
            @RequestParam String deviceToken,
            @RequestParam String platform,
            @RequestParam(required = false) String deviceModel,
            @RequestParam(required = false) String appVersion) {
        log.info("Registering device for user: {}", userId);
        pushService.registerDevice(userId, deviceToken, platform, deviceModel, appVersion);
        return ResponseEntity.ok(ApiResponse.success(null, "Device registered"));
    }

    @PostMapping("/device/unregister")
    public ResponseEntity<ApiResponse<Void>> unregisterDevice(
            @RequestHeader("X-User-ID") String userId,
            @RequestParam String deviceToken) {
        log.info("Unregistering device for user: {}", userId);
        pushService.unregisterDevice(userId, deviceToken);
        return ResponseEntity.ok(ApiResponse.success(null, "Device unregistered"));
    }

    @GetMapping("/devices")
    public ResponseEntity<ApiResponse<List<PushDevice>>> getUserDevices(
            @RequestHeader("X-User-ID") String userId) {
        log.info("Getting devices for user: {}", userId);
        List<PushDevice> devices = pushService.getUserDevices(userId);
        return ResponseEntity.ok(ApiResponse.success(devices));
    }
}
