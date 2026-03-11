package com.vyxentra.vehicle.kafka;



import com.fasterxml.jackson.databind.ObjectMapper;
import com.vyxentra.vehicle.constants.BookingConstants;
import com.vyxentra.vehicle.entity.Booking;
import com.vyxentra.vehicle.entity.DamageReport;
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
public class BookingEventProducer {

    private final KafkaTemplate<String, Object> kafkaTemplate;
    private final ObjectMapper objectMapper;

    public void publishBookingCreated(Booking booking) {
        Map<String, Object> event = createBaseEvent(booking, BookingConstants.BOOKING_CREATED_TOPIC);
        publishEvent(BookingConstants.BOOKING_CREATED_TOPIC, event);
    }

    public void publishEmergencyTriggered(Booking booking) {
        Map<String, Object> event = createBaseEvent(booking, BookingConstants.EMERGENCY_TRIGGERED_TOPIC);
        event.put("emergencyType", booking.getEmergencyType());
        event.put("location", Map.of(
                "lat", booking.getLocationLat(),
                "lng", booking.getLocationLng(),
                "address", booking.getLocationAddress()
        ));
        publishEvent(BookingConstants.EMERGENCY_TRIGGERED_TOPIC, event);
    }

    public void publishDamageReported(Booking booking, DamageReport damageReport) {
        Map<String, Object> event = createBaseEvent(booking, BookingConstants.DAMAGE_REPORTED_TOPIC);
        event.put("damageReportId", damageReport.getId());
        event.put("totalAmount", damageReport.getTotalAmount());
        event.put("items", damageReport.getItems().size());
        publishEvent(BookingConstants.DAMAGE_REPORTED_TOPIC, event);
    }

    public void publishDamageApproved(Booking booking, DamageReport damageReport) {
        Map<String, Object> event = createBaseEvent(booking, BookingConstants.DAMAGE_APPROVED_TOPIC);
        event.put("damageReportId", damageReport.getId());
        event.put("approvedAmount", damageReport.getApprovedAmount());
        publishEvent(BookingConstants.DAMAGE_APPROVED_TOPIC, event);
    }

    public void publishDamageRejected(Booking booking, DamageReport damageReport, String reason) {
        Map<String, Object> event = createBaseEvent(booking, BookingConstants.DAMAGE_REJECTED_TOPIC);
        event.put("damageReportId", damageReport.getId());
        event.put("reason", reason);
        publishEvent(BookingConstants.DAMAGE_REJECTED_TOPIC, event);
    }

    public void publishServiceStarted(Booking booking) {
        Map<String, Object> event = createBaseEvent(booking, BookingConstants.SERVICE_STARTED_TOPIC);
        event.put("employeeId", booking.getEmployeeId());
        event.put("startTime", booking.getActualStartTime());
        publishEvent(BookingConstants.SERVICE_STARTED_TOPIC, event);
    }

    public void publishServiceCompleted(Booking booking) {
        Map<String, Object> event = createBaseEvent(booking, BookingConstants.SERVICE_COMPLETED_TOPIC);
        event.put("employeeId", booking.getEmployeeId());
        event.put("endTime", booking.getActualEndTime());
        publishEvent(BookingConstants.SERVICE_COMPLETED_TOPIC, event);
    }

    private Map<String, Object> createBaseEvent(Booking booking, String eventType) {
        Map<String, Object> event = new HashMap<>();
        event.put("eventId", UUID.randomUUID().toString());
        event.put("eventType", eventType);
        event.put("bookingId", booking.getId());
        event.put("bookingNumber", booking.getBookingNumber());
        event.put("customerId", booking.getCustomerId());
        event.put("providerId", booking.getProviderId());
        event.put("status", booking.getStatus());
        event.put("totalAmount", booking.getTotalAmount());
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