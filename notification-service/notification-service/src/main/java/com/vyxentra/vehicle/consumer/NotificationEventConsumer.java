package com.vyxentra.vehicle.consumer;


import com.vyxentra.vehicle.constants.ServiceConstants;
import com.vyxentra.vehicle.dto.request.SendPushRequest;
import com.vyxentra.vehicle.event.UserEvent;
import com.vyxentra.vehicle.events.*;
import com.vyxentra.vehicle.service.EmailService;
import com.vyxentra.vehicle.service.PushNotificationService;
import com.vyxentra.vehicle.service.SMSService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
public class NotificationEventConsumer {

    private final EmailService emailService;
    private final SMSService smsService;
    private final PushNotificationService pushService;

    @KafkaListener(topics = ServiceConstants.BOOKING_CREATED_TOPIC, groupId = "notification-service")
    public void handleBookingCreated(BookingEvent event) {
        log.info("Received booking created event for booking: {}", event.getBookingId());

        // Send confirmation to customer
        Map<String, Object> templateData = new HashMap<>();
        templateData.put("bookingId", event.getBookingId());
        templateData.put("scheduledTime", event.getScheduledTime());
        templateData.put("location", event.getLocation());

        emailService.sendTemplateEmail(
                event.getCustomerId(),
                "customer@example.com", // Would get from user service
                "booking-confirmation",
                templateData
        );

        // Send SMS for urgent bookings
        if (event.isEmergency()) {
            smsService.sendTemplateSMS(
                    event.getCustomerId(),
                    "+919876543210", // Would get from user service
                    "emergency-created",
                    templateData
            );
        }
    }

    @KafkaListener(topics = ServiceConstants.EMERGENCY_TRIGGERED_TOPIC, groupId = "notification-service")
    public void handleEmergencyTriggered(EmergencyEvent event) {
        log.info("Received emergency triggered event: {}", event.getEmergencyId());

        // Notify nearby providers (would be done via push)
        SendPushRequest pushRequest = SendPushRequest.builder()
                .title("Emergency Request")
                .body("New " + event.getEmergencyType() + " emergency in your area")
                .data(Map.of(
                        "emergencyId", event.getEmergencyId(),
                        "type", event.getEmergencyType().name(),
                        "location", event.getLatitude() + "," + event.getLongitude()
                ))
                .priority(10)
                .referenceId(event.getEmergencyId())
                .referenceType("EMERGENCY")
                .build();

        pushService.broadcastPush(pushRequest, "PROVIDER");
    }

    @KafkaListener(topics = ServiceConstants.EMERGENCY_TRIGGERED_TOPIC, groupId = "notification-service")
    public void handleEmergencyAssigned(EmergencyEvent event) {
        log.info("Received emergency assigned event: {}", event.getEmergencyId());

        // Notify customer
        Map<String, Object> templateData = new HashMap<>();
        templateData.put("providerName", event.getProviderId());
        templateData.put("etaMinutes", 15); // Would get from tracking service

        smsService.sendTemplateSMS(
                event.getCustomerId(),
                "+919876543210",
                "emergency-assigned",
                templateData
        );

        // Push notification to customer
        SendPushRequest pushRequest = SendPushRequest.builder()
                .title("Provider Assigned")
                .body("Your provider is on the way. ETA: 15 minutes")
                .data(Map.of(
                        "emergencyId", event.getEmergencyId(),
                        "trackingUrl", "/track/" + event.getEmergencyId()
                ))
                .referenceId(event.getEmergencyId())
                .referenceType("EMERGENCY")
                .build();

        pushService.sendPush(event.getCustomerId(), pushRequest);
    }

    @KafkaListener(topics = ServiceConstants.DAMAGE_REPORTED_TOPIC, groupId = "notification-service")
    public void handleDamageReported(DamageEvent event) {
        log.info("Received damage reported event for booking: {}", event.getBookingId());

        // Notify customer
        Map<String, Object> templateData = new HashMap<>();
        templateData.put("bookingId", event.getBookingId());
        templateData.put("totalAmount", event.getTotalAmount());
        templateData.put("approvalDeadline", "24 hours");

        emailService.sendTemplateEmail(
                "customer", // Would get customer ID
                "customer@example.com",
                "damage-reported",
                templateData
        );

        // Push notification
        SendPushRequest pushRequest = SendPushRequest.builder()
                .title("Damage Report Requires Approval")
                .body("Additional damage found during service. Please review and approve.")
                .data(Map.of(
                        "bookingId", event.getBookingId(),
                        "reportId", event.getDamageReportId()
                ))
                .referenceId(event.getBookingId())
                .referenceType("BOOKING")
                .build();

        pushService.sendPush("customer", pushRequest);
    }

    @KafkaListener(topics = ServiceConstants.PAYMENT_SUCCESS_TOPIC, groupId = "notification-service")
    public void handlePaymentSuccess(PaymentEvent event) {
        log.info("Received payment success event for booking: {}", event.getBookingId());

        // Send receipt
        Map<String, Object> templateData = new HashMap<>();
        templateData.put("bookingId", event.getBookingId());
        templateData.put("amount", event.getAmount());
        templateData.put("paymentId", event.getPaymentId());

        emailService.sendTemplateEmail(
                event.getCustomerId(),
                "customer@example.com",
                "payment-success",
                templateData
        );

        // SMS for high-value transactions
        BigDecimal threshold = BigDecimal.valueOf(5000);

        if (event.getAmount().compareTo(threshold) > 0) {
            smsService.sendTemplateSMS(
                    event.getCustomerId(),
                    "+919876543210",
                    "payment-success",
                    templateData
            );
        }
    }

    @KafkaListener(topics = ServiceConstants.PROVIDER_APPROVED_TOPIC, groupId = "notification-service")
    public void handleProviderApproved(ProviderEvent event) {
        log.info("Received provider approved event for provider: {}", event.getProviderId());

        // Notify provider
        Map<String, Object> templateData = new HashMap<>();
        templateData.put("businessName", event.getBusinessName());

        emailService.sendTemplateEmail(
                event.getProviderId(),
                "provider@example.com",
                "provider-approved",
                templateData
        );

        // Welcome SMS
        smsService.sendTemplateSMS(
                event.getProviderId(),
                "+919876543210",
                "provider-welcome",
                templateData
        );
    }

    @KafkaListener(topics = "user.registered", groupId = "notification-service")
    public void handleUserRegistered(UserEvent event) {
        log.info("Received user registered event for user: {}", event.getUserId());

        // Welcome email
        Map<String, Object> templateData = new HashMap<>();
        templateData.put("name", event.getFullName());

        emailService.sendTemplateEmail(
                event.getUserId(),
                event.getEmail(),
                "welcome",
                templateData
        );
    }
}
