package com.vyxentra.vehicle.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.vyxentra.vehicle.dto.response.LocationResponse;
import com.vyxentra.vehicle.entity.WebSocketConnection;
import com.vyxentra.vehicle.repository.WebSocketConnectionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArraySet;

@Slf4j
@Service
@RequiredArgsConstructor
public class WebSocketServiceImpl implements WebSocketService {

    private final WebSocketConnectionRepository connectionRepository;
    private final ObjectMapper objectMapper;

    // In-memory storage for active sessions
    private final Map<String, WebSocketSession> activeSessions = new ConcurrentHashMap<>();
    private final Map<String, String> sessionUserMap = new ConcurrentHashMap<>();
    private final Map<String, CopyOnWriteArraySet<String>> trackingSubscribers = new ConcurrentHashMap<>();

    @Value("${tracking.websocket.heartbeat-timeout:30}")
    private int heartbeatTimeoutSeconds;

    @Override
    public void registerSession(String userId, String userType, String trackingSessionId, WebSocketSession session) {
        log.info("Registering WebSocket session for user: {}, tracking: {}", userId, trackingSessionId);

        activeSessions.put(session.getId(), session);
        sessionUserMap.put(session.getId(), userId);

        // Save to database
        WebSocketConnection connection = WebSocketConnection.builder()
                .sessionId(session.getId())
                .userId(userId)
                .userType(userType)
                .trackingSessionId(trackingSessionId)
                .connectedAt(LocalDateTime.now())
                .lastHeartbeatAt(LocalDateTime.now())
                .clientIp(getClientIp(session))
                .userAgent(getUserAgent(session))
                .build();

        connectionRepository.save(connection);

        // Add to tracking subscribers
        if (trackingSessionId != null) {
            trackingSubscribers
                    .computeIfAbsent(trackingSessionId, k -> new CopyOnWriteArraySet<>())
                    .add(session.getId());
        }
    }

    @Override
    public void unregisterSession(String sessionId) {
        WebSocketSession session = activeSessions.remove(sessionId);
        String userId = sessionUserMap.remove(sessionId);

        if (userId != null) {
            // Update database
            connectionRepository.findBySessionId(sessionId).ifPresent(conn -> {
                conn.setDisconnectedAt(LocalDateTime.now());
                connectionRepository.save(conn);
            });

            // Remove from tracking subscribers
            trackingSubscribers.values().forEach(set -> set.remove(sessionId));
        }

        log.debug("Unregistered WebSocket session: {}", sessionId);
    }

    @Override
    public void sendLocationUpdate(String trackingSessionId, LocationResponse location) {
        sendToTrackingSession(trackingSessionId, "LOCATION_UPDATE", location);
    }

    @Override
    public void sendETAUpdate(String trackingSessionId, Integer eta, String reason) {
        Map<String, Object> data = Map.of(
                "eta", eta,
                "reason", reason
        );
        sendToTrackingSession(trackingSessionId, "ETA_UPDATE", data);
    }

    @Override
    public void sendTrackingPaused(String trackingSessionId) {
        sendToTrackingSession(trackingSessionId, "TRACKING_PAUSED", null);
    }

    @Override
    public void sendTrackingResumed(String trackingSessionId) {
        sendToTrackingSession(trackingSessionId, "TRACKING_RESUMED", null);
    }

    @Override
    public void sendTrackingEnded(String trackingSessionId) {
        sendToTrackingSession(trackingSessionId, "TRACKING_ENDED", null);
    }

    @Override
    public void handleHeartbeat(String sessionId) {
        connectionRepository.updateHeartbeat(sessionId, LocalDateTime.now());

        WebSocketSession session = activeSessions.get(sessionId);
        if (session != null && session.isOpen()) {
            try {
                session.sendMessage(new TextMessage("{\"type\":\"HEARTBEAT_ACK\"}"));
            } catch (Exception e) {
                log.error("Error sending heartbeat ack to session {}: {}", sessionId, e.getMessage());
            }
        }
    }

    @Override
    public long getActiveConnectionsCount(String trackingSessionId) {
        return connectionRepository.countActiveByTrackingSessionId(trackingSessionId);
    }

    @Override
    public List<String> getConnectedUsers(String trackingSessionId) {
        return connectionRepository.findByTrackingSessionId(trackingSessionId).stream()
                .filter(conn -> conn.getDisconnectedAt() == null)
                .map(WebSocketConnection::getUserId)
                .toList();
    }

    @Override
    public void broadcastToTrackingSession(String trackingSessionId, String message) {
        CopyOnWriteArraySet<String> subscriberIds = trackingSubscribers.get(trackingSessionId);
        if (subscriberIds == null || subscriberIds.isEmpty()) {
            return;
        }

        for (String sessionId : subscriberIds) {
            WebSocketSession session = activeSessions.get(sessionId);
            if (session != null && session.isOpen()) {
                try {
                    session.sendMessage(new TextMessage(message));
                } catch (Exception e) {
                    log.error("Error broadcasting to session {}: {}", sessionId, e.getMessage());
                    // Remove dead session
                    subscriberIds.remove(sessionId);
                }
            } else {
                subscriberIds.remove(sessionId);
            }
        }
    }

    private void sendToTrackingSession(String trackingSessionId, String type, Object data) {
        try {
            Map<String, Object> message = Map.of(
                    "type", type,
                    "data", data,
                    "timestamp", LocalDateTime.now().toString()
            );
            String jsonMessage = objectMapper.writeValueAsString(message);
            broadcastToTrackingSession(trackingSessionId, jsonMessage);
        } catch (Exception e) {
            log.error("Error sending {} to tracking session {}: {}", type, trackingSessionId, e.getMessage());
        }
    }

    private String getClientIp(WebSocketSession session) {
        try {
            return session.getRemoteAddress().toString();
        } catch (Exception e) {
            return "unknown";
        }
    }

    private String getUserAgent(WebSocketSession session) {
        try {
            List<String> userAgent = session.getHandshakeHeaders().get("User-Agent");
            if (userAgent != null && !userAgent.isEmpty()) {
                return userAgent.get(0);
            }
        } catch (Exception e) {
            // Ignore
        }
        return "unknown";
    }
}
