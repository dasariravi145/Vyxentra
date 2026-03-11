package com.vyxentra.vehicle.client;

import org.springframework.stereotype.Component;

@Component
class NotificationServiceClientFallback implements NotificationServiceClient {

    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(NotificationServiceClientFallback.class);

    @Override
    public void sendNotification(String userId, String message, String type) {
        log.error("Fallback: sendNotification failed for user: {}", userId);
    }
}
