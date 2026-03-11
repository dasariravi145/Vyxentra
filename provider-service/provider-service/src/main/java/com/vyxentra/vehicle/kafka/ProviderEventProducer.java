package com.vyxentra.vehicle.kafka;


import com.vyxentra.vehicle.entity.Provider;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

@Slf4j
@Component
@RequiredArgsConstructor
public class ProviderEventProducer {

    private final KafkaTemplate<String, Object> kafkaTemplate;

    public void publishProviderRegistered(Provider provider) {
        Map<String, Object> event = createBaseEvent(provider, "PROVIDER_REGISTERED");
        publishEvent("provider.registered", event);
    }

    public void publishProviderApproved(Provider provider, String approvedBy) {
        Map<String, Object> event = createBaseEvent(provider, "PROVIDER_APPROVED");
        event.put("approvedBy", approvedBy);
        event.put("approvedAt", Instant.now().toString());
        publishEvent("provider.approved", event);
    }

    public void publishProviderRejected(Provider provider, String rejectedBy, String reason) {
        Map<String, Object> event = createBaseEvent(provider, "PROVIDER_REJECTED");
        event.put("rejectedBy", rejectedBy);
        event.put("reason", reason);
        publishEvent("provider.rejected", event);
    }

    public void publishProviderSuspended(Provider provider, String suspendedBy, String reason) {
        Map<String, Object> event = createBaseEvent(provider, "PROVIDER_SUSPENDED");
        event.put("suspendedBy", suspendedBy);
        event.put("reason", reason);
        publishEvent("provider.suspended", event);
    }

    public void publishProviderActivated(Provider provider, String activatedBy) {
        Map<String, Object> event = createBaseEvent(provider, "PROVIDER_ACTIVATED");
        event.put("activatedBy", activatedBy);
        publishEvent("provider.activated", event);
    }

    public void publishProviderUpdated(Provider provider) {
        Map<String, Object> event = createBaseEvent(provider, "PROVIDER_UPDATED");
        publishEvent("provider.updated", event);
    }

    private Map<String, Object> createBaseEvent(Provider provider, String eventType) {
        Map<String, Object> event = new HashMap<>();
        event.put("eventId", UUID.randomUUID().toString());
        event.put("eventType", eventType);
        event.put("providerId", provider.getId());
        event.put("userId", provider.getUserId());
        event.put("businessName", provider.getBusinessName());
        event.put("email", provider.getEmail());
        event.put("phone", provider.getPhone());
        event.put("status", provider.getStatus().name());
        event.put("timestamp", Instant.now().toString());
        return event;
    }

    private void publishEvent(String topic, Map<String, Object> event) {
        CompletableFuture<SendResult<String, Object>> future = kafkaTemplate.send(topic, event);

        future.whenComplete((result, ex) -> {
            if (ex == null) {
                log.debug("Event published successfully to {}: {}", topic, event.get("eventId"));
            } else {
                log.error("Failed to publish event to {}: {}", topic, ex.getMessage());
            }
        });
    }
}
