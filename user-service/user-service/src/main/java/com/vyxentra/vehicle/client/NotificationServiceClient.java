package com.vyxentra.vehicle.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient(name = "notification-service", path = "/api/v1/notifications", fallback = NotificationServiceClientFallback.class)
public interface NotificationServiceClient {

    @PostMapping("/send")
    void sendNotification(@RequestParam String userId, @RequestParam String message, @RequestParam String type);
}
