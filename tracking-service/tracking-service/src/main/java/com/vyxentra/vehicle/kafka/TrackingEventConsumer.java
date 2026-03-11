package com.vyxentra.vehicle.kafka;

import com.vyxentra.vehicle.constants.ServiceConstants;
import com.vyxentra.vehicle.events.BookingEvent;
import com.vyxentra.vehicle.service.TrackingService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class TrackingEventConsumer {

    private final TrackingService trackingService;

    @KafkaListener(topics = ServiceConstants.BOOKING_CREATED_TOPIC, groupId = "tracking-service")
    public void handleBookingCreated(BookingEvent event) {
        log.info("Received booking created event: {}", event.getBookingId());

        // Create tracking session when booking is confirmed
        if ("CONFIRMED".equals(event.getStatus().name())) {
            trackingService.createTrackingSession(
                    event.getBookingId(),
                    event.getCustomerId(),
                    event.getProviderId(),
                    event.getLatitude(),
                    event.getLongitude(),
                    event.getLocation()
            );
        }
    }

    @KafkaListener(topics = ServiceConstants.SERVICE_STARTED_TOPIC, groupId = "tracking-service")
    public void handleServiceStarted(BookingEvent event) {
        log.info("Received service started event for booking: {}", event.getBookingId());
        // Activate tracking when service starts
    }

    @KafkaListener(topics = ServiceConstants.SERVICE_COMPLETED_TOPIC, groupId = "tracking-service")
    public void handleServiceCompleted(BookingEvent event) {
        log.info("Received service completed event for booking: {}", event.getBookingId());
        trackingService.endTrackingSession(event.getBookingId());
    }

    @KafkaListener(topics = ServiceConstants.EMERGENCY_TRIGGERED_TOPIC, groupId = "tracking-service")
    public void handleEmergencyTriggered(BookingEvent event) {
        log.info("Received emergency triggered event: {}", event.getBookingId());

        trackingService.createTrackingSession(
                event.getBookingId(),
                event.getCustomerId(),
                event.getProviderId(),
                event.getLatitude(),
                event.getLongitude(),
                event.getLocation()
        );
    }
}
