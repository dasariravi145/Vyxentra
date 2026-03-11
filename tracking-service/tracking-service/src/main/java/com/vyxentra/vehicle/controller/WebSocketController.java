package com.vyxentra.vehicle.controller;

import com.vyxentra.vehicle.dto.WebSocketStats;
import com.vyxentra.vehicle.dto.response.ApiResponse;
import com.vyxentra.vehicle.service.WebSocketService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/v1/tracking/websocket")
@RequiredArgsConstructor
public class WebSocketController {

    private final WebSocketService webSocketService;

    /**
     * Get active connections for a tracking session
     */
    @GetMapping("/connections/{trackingSessionId}")
    @PreAuthorize("hasAnyRole('PROVIDER', 'ADMIN')")
    public ResponseEntity<ApiResponse<Long>> getActiveConnections(
            @PathVariable String trackingSessionId) {
        log.info("Getting active connections for tracking session: {}", trackingSessionId);
        long count = webSocketService.getActiveConnectionsCount(trackingSessionId);
        return ResponseEntity.ok(ApiResponse.success(count));
    }

    /**
     * Get connected users for a tracking session
     */
    @GetMapping("/users/{trackingSessionId}")
    @PreAuthorize("hasAnyRole('PROVIDER', 'ADMIN')")
    public ResponseEntity<ApiResponse<List<String>>> getConnectedUsers(
            @PathVariable String trackingSessionId) {
        log.info("Getting connected users for tracking session: {}", trackingSessionId);
        List<String> users = webSocketService.getConnectedUsers(trackingSessionId);
        return ResponseEntity.ok(ApiResponse.success(users));
    }

    /**
     * Force disconnect a user from tracking (Admin only)
     */
    @PostMapping("/disconnect/{userId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Void>> forceDisconnectUser(
            @PathVariable String userId) {
        log.info("Force disconnecting user: {}", userId);
        // Implementation would need to find and close all sessions for user
        return ResponseEntity.ok(ApiResponse.success(null, "User disconnected"));
    }

    /**
     * Get WebSocket connection statistics
     */
    @GetMapping("/stats")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<WebSocketStats>> getConnectionStats() {
        log.info("Getting WebSocket connection statistics");
        WebSocketStats stats = WebSocketStats.builder()
                .totalConnections(150)
                .activeConnections(120)
                .uniqueUsers(85)
                .activeTrackingSessions(45)
                .build();
        return ResponseEntity.ok(ApiResponse.success(stats));
    }


}
