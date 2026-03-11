package com.vyxentra.vehicle.websocket;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.server.HandshakeInterceptor;

import java.net.URI;
import java.util.Map;

@Slf4j
@Component
public class WebSocketAuthenticator implements HandshakeInterceptor {

    @Override
    public boolean beforeHandshake(ServerHttpRequest request, ServerHttpResponse response,
                                   WebSocketHandler wsHandler, Map<String, Object> attributes) {
        URI uri = request.getURI();
        String query = uri.getQuery();

        if (query != null) {
            String[] params = query.split("&");
            for (String param : params) {
                String[] keyValue = param.split("=");
                if (keyValue.length == 2) {
                    attributes.put(keyValue[0], keyValue[1]);
                    log.debug("Added attribute: {} = {}", keyValue[0], keyValue[1]);
                }
            }
        }

        // Validate required attributes
        if (!attributes.containsKey("userId") || !attributes.containsKey("userType")) {
            log.warn("WebSocket handshake rejected: missing userId or userType");
            return false;
        }

        // Add handshake time
        attributes.put("handshakeTime", System.currentTimeMillis());

        return true;
    }

    @Override
    public void afterHandshake(ServerHttpRequest request, ServerHttpResponse response,
                               WebSocketHandler wsHandler, Exception exception) {
        log.debug("WebSocket handshake completed");
    }
}
