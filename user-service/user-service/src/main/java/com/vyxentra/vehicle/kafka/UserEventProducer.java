package com.vyxentra.vehicle.kafka;


import com.vyxentra.vehicle.events.BaseEvent;
import com.vyxentra.vehicle.utils.CorrelationIdUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Component;

import java.util.concurrent.CompletableFuture;

@Slf4j
@Component
@RequiredArgsConstructor
public class UserEventProducer {

    private final KafkaTemplate<String, com.vyxentra.vehicle.events.BaseEvent> kafkaTemplate;

    public void publishUserProfileUpdated(String userId, String email, String fullName) {
        com.vyxentra.vehicle.event.UserEventProducer event = com.vyxentra.vehicle.event.UserEventProducer.builder()
                .userId(userId)
                .email(email)
                .fullName(fullName)
                .eventType("USER_PROFILE_UPDATED")
                .correlationId(CorrelationIdUtil.getCurrentCorrelationId())
                .source("user-service")
                .build();

        publishEvent("user.profile.updated", event);
    }

    public void publishUserDeactivated(String userId) {
        com.vyxentra.vehicle.event.UserEventProducer event = com.vyxentra.vehicle.event.UserEventProducer.builder()
                .userId(userId)
                .eventType("USER_DEACTIVATED")
                .correlationId(CorrelationIdUtil.getCurrentCorrelationId())
                .source("user-service")
                .build();

        publishEvent("user.deactivated", event);
    }

    public void publishAddressAdded(String userId, String addressId) {
        com.vyxentra.vehicle.event.UserEventProducer event = com.vyxentra.vehicle.event.UserEventProducer.builder()
                .userId(userId)
                .addressId(addressId)
                .eventType("ADDRESS_ADDED")
                .correlationId(CorrelationIdUtil.getCurrentCorrelationId())
                .source("user-service")
                .build();

        publishEvent("user.address.added", event);
    }

    public void publishAddressUpdated(String userId, String addressId) {
        com.vyxentra.vehicle.event.UserEventProducer event = com.vyxentra.vehicle.event.UserEventProducer.builder()
                .userId(userId)
                .addressId(addressId)
                .eventType("ADDRESS_UPDATED")
                .correlationId(CorrelationIdUtil.getCurrentCorrelationId())
                .source("user-service")
                .build();

        publishEvent("user.address.updated", event);
    }

    public void publishAddressDeleted(String userId, String addressId) {
        com.vyxentra.vehicle.event.UserEventProducer event = com.vyxentra.vehicle.event.UserEventProducer.builder()
                .userId(userId)
                .addressId(addressId)
                .eventType("ADDRESS_DELETED")
                .correlationId(CorrelationIdUtil.getCurrentCorrelationId())
                .source("user-service")
                .build();

        publishEvent("user.address.deleted", event);
    }

    public void publishDefaultAddressChanged(String userId, String addressId) {
        com.vyxentra.vehicle.event.UserEventProducer event = com.vyxentra.vehicle.event.UserEventProducer.builder()
                .userId(userId)
                .addressId(addressId)
                .eventType("DEFAULT_ADDRESS_CHANGED")
                .correlationId(CorrelationIdUtil.getCurrentCorrelationId())
                .source("user-service")
                .build();

        publishEvent("user.address.default", event);
    }

    public void publishVehicleAdded(String userId, String vehicleId) {
        com.vyxentra.vehicle.event.UserEventProducer event = com.vyxentra.vehicle.event.UserEventProducer.builder()
                .userId(userId)
                .vehicleId(vehicleId)
                .eventType("VEHICLE_ADDED")
                .correlationId(CorrelationIdUtil.getCurrentCorrelationId())
                .source("user-service")
                .build();

        publishEvent("user.vehicle.added", event);
    }

    public void publishVehicleUpdated(String userId, String vehicleId) {
        com.vyxentra.vehicle.event.UserEventProducer event = com.vyxentra.vehicle.event.UserEventProducer.builder()
                .userId(userId)
                .vehicleId(vehicleId)
                .eventType("VEHICLE_UPDATED")
                .correlationId(CorrelationIdUtil.getCurrentCorrelationId())
                .source("user-service")
                .build();

        publishEvent("user.vehicle.updated", event);
    }

    public void publishVehicleDeleted(String userId, String vehicleId) {
        com.vyxentra.vehicle.event.UserEventProducer event = com.vyxentra.vehicle.event.UserEventProducer.builder()
                .userId(userId)
                .vehicleId(vehicleId)
                .eventType("VEHICLE_DELETED")
                .correlationId(CorrelationIdUtil.getCurrentCorrelationId())
                .source("user-service")
                .build();

        publishEvent("user.vehicle.deleted", event);
    }

    public void publishDefaultVehicleChanged(String userId, String vehicleId) {
        com.vyxentra.vehicle.event.UserEventProducer event = com.vyxentra.vehicle.event.UserEventProducer.builder()
                .userId(userId)
                .vehicleId(vehicleId)
                .eventType("DEFAULT_VEHICLE_CHANGED")
                .correlationId(CorrelationIdUtil.getCurrentCorrelationId())
                .source("user-service")
                .build();

        publishEvent("user.vehicle.default", event);
    }

    private void publishEvent(String topic, BaseEvent event) {
        CompletableFuture<SendResult<String, BaseEvent>> future = kafkaTemplate.send(topic, event);

        future.whenComplete((result, ex) -> {
            if (ex == null) {
                log.debug("Event published successfully to {}: {}", topic, event.getEventId());
            } else {
                log.error("Failed to publish event to {}: {}", topic, ex.getMessage());
            }
        });
    }
}
