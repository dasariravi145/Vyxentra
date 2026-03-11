package com.vyxentra.vehicle.websocket;


import com.fasterxml.jackson.databind.ObjectMapper;
import com.vyxentra.vehicle.entity.WebSocketConnection;
import com.vyxentra.vehicle.repository.WebSocketConnectionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Component
@RequiredArgsConstructor
public class TrackingWebSocketHandler extends TextWebSocketHandler {

    private final WebSocketConnectionRepository connectionRepository;
    private final LocationBroadcastService broadcastService;
    private final ObjectMapper objectMapper;

    // Store active sessions in memory
    private final Map<String, WebSocketSession> sessions = new ConcurrentHashMap<>();
    private final Map<String, String> sessionUserMap = new ConcurrentHashMap<>();

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        log.info("WebSocket connection established: {}", session.getId());

        // Extract user info from query parameters
        String query = session.getUri().getQuery();
        String userId = null;
        String userType = null;
        String bookingId = null;

        if (query != null) {
            String[] params = query.split("&");
            for (String param : params) {
                String[] keyValue = param.split("=");
                if (keyValue.length == 2) {
                    switch (keyValue[0]) {
                        case "userId":
                            userId = keyValue[1];
                            break;
                        case "userType":
                            userType = keyValue[1];
                            break;
                        case "bookingId":
                            bookingId = keyValue[1];
                            break;
                    }
                }
            }
        }

        if (userId != null) {
            sessions.put(session.getId(), session);
            sessionUserMap.put(session.getId(), userId);

            // Save to database
            WebSocketConnection connection = WebSocketConnection.builder()
                    .sessionId(session.getId())
                    .userId(userId)
                    .userType(userType)
                    .trackingSessionId(bookingId)
                    .connectedAt(LocalDateTime.now())
                    .build();
            connectionRepository.save(connection);

            // Send confirmation
            session.sendMessage(new TextMessage("{\"type\":\"CONNECTED\",\"message\":\"Connected to tracking service\"}"));
        } else {
            session.close(CloseStatus.BAD_DATA);
        }
    }

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
        String payload = message.getPayload();
        log.debug("Received message: {}", payload);

        try {
            Map<String, Object> messageMap = objectMapper.readValue(payload, Map.class);
            String type = (String) messageMap.get("type");

            switch (type) {
                case "SUBSCRIBE":
                    handleSubscribe(session, messageMap);
                    break;
                case "UNSUBSCRIBE":
                    handleUnsubscribe(session, messageMap);
                    break;
                case "HEARTBEAT":
                    handleHeartbeat(session);
                    break;
                default:
                    log.warn("Unknown message type: {}", type);
            }
        } catch (Exception e) {
            log.error("Error handling message: {}", e.getMessage());
            session.sendMessage(new TextMessage("{\"type\":\"ERROR\",\"message\":\"Invalid message format\"}"));
        }
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
        log.info("WebSocket connection closed: {} with status: {}", session.getId(), status);

        String userId = sessionUserMap.get(session.getId());
        if (userId != null) {
            sessions.remove(session.getId());
            sessionUserMap.remove(session.getId());

            // Update database
            connectionRepository.findBySessionId(session.getId()).ifPresent(conn -> {
                conn.setDisconnectedAt(LocalDateTime.now());
                connectionRepository.save(conn);
            });
        }
    }

    private void handleSubscribe(WebSocketSession session, Map<String, Object> message) {
        String bookingId = (String) message.get("bookingId");
        if (bookingId != null) {
            broadcastService.addSubscriber(bookingId, session);
            log.info("Session {} subscribed to booking: {}", session.getId(), bookingId);
        }
    }

    private void handleUnsubscribe(WebSocketSession session, Map<String, Object> message) {
        String bookingId = (String) message.get("bookingId");
        if (bookingId != null) {
            broadcastService.removeSubscriber(bookingId, session);
            log.info("Session {} unsubscribed from booking: {}", session.getId(), bookingId);
        }
    }

    private void handleHeartbeat(WebSocketSession session) {
        connectionRepository.findBySessionId(session.getId()).ifPresent(conn -> {
            conn.setLastHeartbeatAt(LocalDateTime.now());
            connectionRepository.save(conn);
        });
    }

    public void sendToUser(String userId, String message) {
        sessions.values().stream()
                .filter(s -> userId.equals(sessionUserMap.get(s.getId())))
                .forEach(s -> {
                    try {
                        s.sendMessage(new TextMessage(message));
                    } catch (Exception e) {
                        log.error("Error sending message to user {}: {}", userId, e.getMessage());
                    }
                });
    }
}
