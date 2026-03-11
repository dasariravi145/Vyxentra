package com.vyxentra.vehicle.controller;


import com.vyxentra.vehicle.dto.request.NotificationPreferenceRequest;
import com.vyxentra.vehicle.dto.response.ApiResponse;
import com.vyxentra.vehicle.dto.response.NotificationHistoryResponse;
import com.vyxentra.vehicle.dto.response.NotificationPreferenceResponse;
import com.vyxentra.vehicle.dto.response.PageResponse;
import com.vyxentra.vehicle.service.NotificationService;
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
import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/v1/notifications")
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationService notificationService;

    @GetMapping("/history")
    public ResponseEntity<ApiResponse<PageResponse<NotificationHistoryResponse>>> getNotificationHistory(
            @RequestHeader("X-User-ID") String userId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime fromDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime toDate,
            @RequestParam(required = false) String type,
            @RequestParam(required = false) String status,
            @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
        log.info("Getting notification history for user: {}", userId);
        PageResponse<NotificationHistoryResponse> response = notificationService.getNotificationHistory(
                userId, fromDate, toDate, type, status, pageable);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/unread/count")
    public ResponseEntity<ApiResponse<Long>> getUnreadCount(
            @RequestHeader("X-User-ID") String userId) {
        log.info("Getting unread count for user: {}", userId);
        Long count = notificationService.getUnreadCount(userId);
        return ResponseEntity.ok(ApiResponse.success(count));
    }

    @PostMapping("/{notificationId}/read")
    public ResponseEntity<ApiResponse<Void>> markAsRead(
            @PathVariable String notificationId,
            @RequestHeader("X-User-ID") String userId) {
        log.info("Marking notification {} as read for user: {}", notificationId, userId);
        notificationService.markAsRead(notificationId, userId);
        return ResponseEntity.ok(ApiResponse.success(null, "Notification marked as read"));
    }

    @PostMapping("/read-all")
    public ResponseEntity<ApiResponse<Void>> markAllAsRead(
            @RequestHeader("X-User-ID") String userId) {
        log.info("Marking all notifications as read for user: {}", userId);
        notificationService.markAllAsRead(userId);
        return ResponseEntity.ok(ApiResponse.success(null, "All notifications marked as read"));
    }

    @DeleteMapping("/{notificationId}")
    public ResponseEntity<ApiResponse<Void>> deleteNotification(
            @PathVariable String notificationId,
            @RequestHeader("X-User-ID") String userId) {
        log.info("Deleting notification {} for user: {}", notificationId, userId);
        notificationService.deleteNotification(notificationId, userId);
        return ResponseEntity.ok(ApiResponse.success(null, "Notification deleted"));
    }

    // Notification Preferences
    @GetMapping("/preferences")
    public ResponseEntity<ApiResponse<NotificationPreferenceResponse>> getPreferences(
            @RequestHeader("X-User-ID") String userId) {
        log.info("Getting notification preferences for user: {}", userId);
        NotificationPreferenceResponse response = notificationService.getPreferences(userId);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @PutMapping("/preferences")
    public ResponseEntity<ApiResponse<NotificationPreferenceResponse>> updatePreferences(
            @RequestHeader("X-User-ID") String userId,
            @Valid @RequestBody NotificationPreferenceRequest request) {
        log.info("Updating notification preferences for user: {}", userId);
        NotificationPreferenceResponse response = notificationService.updatePreferences(userId, request);
        return ResponseEntity.ok(ApiResponse.success(response, "Preferences updated"));
    }

    @PostMapping("/preferences/quiet-hours")
    public ResponseEntity<ApiResponse<Void>> setQuietHours(
            @RequestHeader("X-User-ID") String userId,
            @RequestParam String startTime,
            @RequestParam String endTime,
            @RequestParam boolean enabled) {
        log.info("Setting quiet hours for user: {} - {} to {}", userId, startTime, endTime);
        notificationService.setQuietHours(userId, startTime, endTime, enabled);
        return ResponseEntity.ok(ApiResponse.success(null, "Quiet hours updated"));
    }
}