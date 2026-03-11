package com.vyxentra.vehicle.kafka;


import com.vyxentra.vehicle.entity.EmergencyAssignment;
import com.vyxentra.vehicle.entity.EmergencyRequest;
import com.vyxentra.vehicle.enums.EmergencyType;
import com.vyxentra.vehicle.utils.EmergencyConstants;
import com.fasterxml.jackson.databind.ObjectMapper;
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
public class EmergencyEventProducer {

    private final KafkaTemplate<String, Object> kafkaTemplate;
    private final ObjectMapper objectMapper;

    /**
     * Publish event when emergency is triggered
     */
    public void publishEmergencyTriggered(EmergencyRequest request) {
        Map<String, Object> event = createBaseEvent(request, EmergencyConstants.EMERGENCY_TRIGGERED_TOPIC);
        event.put("emergencyType", request.getEmergencyType());
        event.put("vehicleType", request.getVehicleType());
        event.put("location", Map.of(
                "lat", request.getLocationLat(),
                "lng", request.getLocationLng(),
                "address", request.getLocationAddress()
        ));

        // Add petrol-specific fields
        if (request.getEmergencyType() == EmergencyType.PETROL_EMERGENCY) {
            event.put("fuelType", request.getFuelType());
            event.put("quantity", request.getQuantityLiters());
        }

        publishEvent(EmergencyConstants.EMERGENCY_TRIGGERED_TOPIC, event);
    }

    /**
     * Publish event when emergency is assigned to a provider
     */
    public void publishEmergencyAssigned(EmergencyRequest request, EmergencyAssignment assignment) {
        Map<String, Object> event = createBaseEvent(request, EmergencyConstants.EMERGENCY_ASSIGNED_TOPIC);
        event.put("assignmentId", assignment.getId());
        event.put("providerId", assignment.getProviderId());
        event.put("providerName", assignment.getProviderName());
        event.put("etaMinutes", assignment.getEtaMinutes());
        event.put("distanceKm", assignment.getDistanceKm());
        event.put("bookingId", assignment.getBookingId());

        publishEvent(EmergencyConstants.EMERGENCY_ASSIGNED_TOPIC, event);
    }

    /**
     * Publish event when emergency is completed
     */
    public void publishEmergencyCompleted(EmergencyRequest request, EmergencyAssignment assignment) {
        Map<String, Object> event = createBaseEvent(request, EmergencyConstants.EMERGENCY_COMPLETED_TOPIC);
        event.put("assignmentId", assignment.getId());
        event.put("providerId", assignment.getProviderId());
        event.put("bookingId", assignment.getBookingId());
        event.put("totalAmount", request.getTotalAmount());
        event.put("completedAt", Instant.now().toString());

        publishEvent(EmergencyConstants.EMERGENCY_COMPLETED_TOPIC, event);
    }

    /**
     * Publish event when emergency expires
     */
    public void publishEmergencyExpired(EmergencyRequest request) {
        Map<String, Object> event = createBaseEvent(request, EmergencyConstants.EMERGENCY_EXPIRED_TOPIC);
        event.put("expiryTime", request.getExpiryTime().toString());
        event.put("searchRadiusKm", request.getCurrentRadiusKm());

        publishEvent(EmergencyConstants.EMERGENCY_EXPIRED_TOPIC, event);
    }

    /**
     * Publish event when emergency is cancelled by customer
     */
    public void publishEmergencyCancelled(EmergencyRequest request, String reason) {
        Map<String, Object> event = createBaseEvent(request, "emergency.cancelled");
        event.put("reason", reason);
        event.put("cancelledAt", Instant.now().toString());
        event.put("status", request.getStatus());

        publishEvent("emergency.cancelled", event);
    }

    /**
     * Publish event when provider arrives at location
     */
    public void publishProviderArrived(EmergencyRequest request, EmergencyAssignment assignment) {
        Map<String, Object> event = createBaseEvent(request, "provider.arrived");
        event.put("assignmentId", assignment.getId());
        event.put("providerId", assignment.getProviderId());
        event.put("arrivedAt", Instant.now().toString());

        publishEvent("provider.arrived", event);
    }

    /**
     * Publish event when service is started
     */
    public void publishServiceStarted(EmergencyRequest request, EmergencyAssignment assignment) {
        Map<String, Object> event = createBaseEvent(request, "service.started");
        event.put("assignmentId", assignment.getId());
        event.put("providerId", assignment.getProviderId());
        /*event.put("startedAt", assignment.getStartedAt() != null ?
                assignment.getStartedAt().toString() : Instant.now().toString());*/

        publishEvent("service.started", event);
    }

    /**
     * Publish event for provider location update
     */
    public void publishProviderLocationUpdate(String providerId, Double latitude, Double longitude) {
        Map<String, Object> event = new HashMap<>();
        event.put("eventId", UUID.randomUUID().toString());
        event.put("eventType", "provider.location.updated");
        event.put("providerId", providerId);
        event.put("latitude", latitude);
        event.put("longitude", longitude);
        event.put("timestamp", Instant.now().toString());

        publishEvent(EmergencyConstants.PROVIDER_LOCATION_TOPIC, event);
    }

    /**
     * Publish event for provider availability change
     */
    public void publishProviderAvailabilityChanged(String providerId, boolean available, String reason) {
        Map<String, Object> event = new HashMap<>();
        event.put("eventId", UUID.randomUUID().toString());
        event.put("eventType", "provider.availability.changed");
        event.put("providerId", providerId);
        event.put("available", available);
        event.put("reason", reason);
        event.put("timestamp", Instant.now().toString());

        publishEvent(EmergencyConstants.PROVIDER_AVAILABILITY_TOPIC, event);
    }

    /**
     * Create base event map from emergency request
     */
    private Map<String, Object> createBaseEvent(EmergencyRequest request, String eventType) {
        Map<String, Object> event = new HashMap<>();
        event.put("eventId", UUID.randomUUID().toString());
        event.put("eventType", eventType);
        event.put("requestId", request.getId());
        event.put("requestNumber", request.getRequestNumber());
        event.put("customerId", request.getCustomerId());
        event.put("emergencyType", request.getEmergencyType());
        event.put("vehicleType", request.getVehicleType());
        event.put("status", request.getStatus());
        event.put("timestamp", Instant.now().toString());
        return event;
    }

    /**
     * Publish event to Kafka topic
     */
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