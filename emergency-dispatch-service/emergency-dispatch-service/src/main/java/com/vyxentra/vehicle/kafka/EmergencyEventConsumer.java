package com.vyxentra.vehicle.kafka;


import com.vyxentra.vehicle.service.EmergencyDispatchService;
import com.vyxentra.vehicle.service.ProviderMatchingService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
public class EmergencyEventConsumer {

    private final EmergencyDispatchService emergencyDispatchService;
    private final ProviderMatchingService providerMatchingService;

    @KafkaListener(topics = "provider.location.updated", groupId = "emergency-dispatch-service")
    public void handleProviderLocationUpdate(Map<String, Object> event) {
        log.info("Received provider location update event");

        try {
            String providerId = (String) event.get("providerId");
            Double latitude = (Double) event.get("latitude");
            Double longitude = (Double) event.get("longitude");

            providerMatchingService.updateProviderLocation(providerId, latitude, longitude);

            log.info("Updated location for provider: {}", providerId);
        } catch (Exception e) {
            log.error("Error processing provider location update: {}", e.getMessage());
        }
    }

    @KafkaListener(topics = "provider.status.changed", groupId = "emergency-dispatch-service")
    public void handleProviderStatusChange(Map<String, Object> event) {
        log.info("Received provider status change event");

        try {
            String providerId = (String) event.get("providerId");
            String status = (String) event.get("status");

            if ("OFFLINE".equals(status) || "BUSY".equals(status)) {
                providerMatchingService.markProviderUnavailable(providerId);
            } else if ("AVAILABLE".equals(status)) {
                providerMatchingService.markProviderAvailable(providerId);
            }

            log.info("Updated status for provider: {} to {}", providerId, status);
        } catch (Exception e) {
            log.error("Error processing provider status change: {}", e.getMessage());
        }
    }

    @KafkaListener(topics = "booking.completed", groupId = "emergency-dispatch-service")
    public void handleBookingCompleted(Map<String, Object> event) {
        log.info("Received booking completed event");

        try {
            String bookingId = (String) event.get("bookingId");
            String providerId = (String) event.get("providerId");

            // Mark provider as available again
            providerMatchingService.markProviderAvailable(providerId);

            log.info("Marked provider {} as available after completing booking", providerId);
        } catch (Exception e) {
            log.error("Error processing booking completed: {}", e.getMessage());
        }
    }
}
