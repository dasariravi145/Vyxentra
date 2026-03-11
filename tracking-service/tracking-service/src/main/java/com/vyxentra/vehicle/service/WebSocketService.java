package com.vyxentra.vehicle.service;

import com.vyxentra.vehicle.dto.response.LocationResponse;
import org.springframework.web.socket.WebSocketSession;

import java.util.List;

public interface WebSocketService {

    void registerSession(String userId, String userType, String trackingSessionId, WebSocketSession session);

    void unregisterSession(String sessionId);

    void sendLocationUpdate(String trackingSessionId, LocationResponse location);

    void sendETAUpdate(String trackingSessionId, Integer eta, String reason);

    void sendTrackingPaused(String trackingSessionId);

    void sendTrackingResumed(String trackingSessionId);

    void sendTrackingEnded(String trackingSessionId);

    void handleHeartbeat(String sessionId);

    long getActiveConnectionsCount(String trackingSessionId);

    List<String> getConnectedUsers(String trackingSessionId);

    void broadcastToTrackingSession(String trackingSessionId, String message);
}
