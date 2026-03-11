package com.vyxentra.vehicle.websocket;


import com.fasterxml.jackson.databind.ObjectMapper;
import com.vyxentra.vehicle.dto.response.LocationResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArraySet;

@Slf4j
@Service
@RequiredArgsConstructor
public class LocationBroadcastService {

    private final RedisTemplate<String, String> redisTemplate;
    private final ObjectMapper objectMapper;

    // In-memory subscriber tracking (would use Redis in distributed setup)
    private final Map<String, Set<WebSocketSession>> subscribers = new ConcurrentHashMap<>();

    public void addSubscriber(String bookingId, WebSocketSession session) {
        subscribers.computeIfAbsent(bookingId, k -> new CopyOnWriteArraySet<>()).add(session);
    }

    public void removeSubscriber(String bookingId, WebSocketSession session) {
        Set<WebSocketSession> sessions = subscribers.get(bookingId);
        if (sessions != null) {
            sessions.remove(session);
            if (sessions.isEmpty()) {
                subscribers.remove(bookingId);
            }
        }
    }

    public void broadcastLocation(String bookingId, LocationResponse location) {
        Set<WebSocketSession> sessions = subscribers.get(bookingId);
        if (sessions == null || sessions.isEmpty()) {
            return;
        }

        try {
            String message = objectMapper.writeValueAsString(Map.of(
                    "type", "LOCATION_UPDATE",
                    "data", location,
                    "timestamp", LocalDateTime.now().toString()
            ));

            for (WebSocketSession session : sessions) {
                if (session.isOpen()) {
                    try {
                        session.sendMessage(new TextMessage(message));
                    } catch (IOException e) {
                        log.error("Error broadcasting to session {}: {}", session.getId(), e.getMessage());
                        // Remove dead session
                        sessions.remove(session);
                    }
                } else {
                    sessions.remove(session);
                }
            }
        } catch (Exception e) {
            log.error("Error broadcasting location: {}", e.getMessage());
        }
    }

    public void broadcastETAUpdate(String bookingId, Integer eta, String reason) {
        Set<WebSocketSession> sessions = subscribers.get(bookingId);
        if (sessions == null || sessions.isEmpty()) {
            return;
        }

        try {
            String message = objectMapper.writeValueAsString(Map.of(
                    "type", "ETA_UPDATE",
                    "data", Map.of("eta", eta, "reason", reason),
                    "timestamp", LocalDateTime.now().toString()
            ));

            for (WebSocketSession session : sessions) {
                if (session.isOpen()) {
                    try {
                        session.sendMessage(new TextMessage(message));
                    } catch (IOException e) {
                        sessions.remove(session);
                    }
                } else {
                    sessions.remove(session);
                }
            }
        } catch (Exception e) {
            log.error("Error broadcasting ETA: {}", e.getMessage());
        }
    }

    public void broadcastTrackingPaused(String bookingId) {
        broadcastStatus(bookingId, "PAUSED", "Tracking paused by provider");
    }

    public void broadcastTrackingResumed(String bookingId) {
        broadcastStatus(bookingId, "RESUMED", "Tracking resumed");
    }

    public void broadcastTrackingEnded(String bookingId) {
        broadcastStatus(bookingId, "ENDED", "Tracking ended");
    }

    private void broadcastStatus(String bookingId, String status, String message) {
        Set<WebSocketSession> sessions = subscribers.get(bookingId);
        if (sessions == null || sessions.isEmpty()) {
            return;
        }

        try {
            String jsonMessage = objectMapper.writeValueAsString(Map.of(
                    "type", "TRACKING_" + status,
                    "message", message,
                    "timestamp", LocalDateTime.now().toString()
            ));

            for (WebSocketSession session : sessions) {
                if (session.isOpen()) {
                    try {
                        session.sendMessage(new TextMessage(jsonMessage));
                    } catch (IOException e) {
                        sessions.remove(session);
                    }
                } else {
                    sessions.remove(session);
                }
            }
        } catch (Exception e) {
            log.error("Error broadcasting status: {}", e.getMessage());
        }
    }
}
