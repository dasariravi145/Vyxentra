package com.vyxentra.vehicle.controller;


import com.vyxentra.vehicle.service.BookingService;
import com.vyxentra.vehicle.service.PaymentWebhookService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/v1/webhooks/bookings")
@RequiredArgsConstructor
public class WebhookController {

    private final BookingService bookingService;
    private final PaymentWebhookService paymentWebhookService;

    /**
     * Payment success webhook from payment service
     */
    @PostMapping("/payment/success")
    public ResponseEntity<String> handlePaymentSuccess(@RequestBody Map<String, Object> payload) {
        log.info("Received payment success webhook");

        try {
            String bookingId = (String) payload.get("bookingId");
            String paymentId = (String) payload.get("paymentId");
            Double amount = Double.parseDouble(payload.get("amount").toString());

            paymentWebhookService.processPaymentSuccess(bookingId, paymentId, amount);

            return ResponseEntity.ok("Webhook processed successfully");
        } catch (Exception e) {
            log.error("Error processing payment webhook: {}", e.getMessage());
            return ResponseEntity.badRequest().body("Error processing webhook: " + e.getMessage());
        }
    }

    /**
     * Payment failure webhook from payment service
     */
    @PostMapping("/payment/failed")
    public ResponseEntity<String> handlePaymentFailed(@RequestBody Map<String, Object> payload) {
        log.info("Received payment failed webhook");

        try {
            String bookingId = (String) payload.get("bookingId");
            String paymentId = (String) payload.get("paymentId");
            String reason = (String) payload.get("reason");

            paymentWebhookService.processPaymentFailed(bookingId, paymentId, reason);

            return ResponseEntity.ok("Webhook processed successfully");
        } catch (Exception e) {
            log.error("Error processing payment webhook: {}", e.getMessage());
            return ResponseEntity.badRequest().body("Error processing webhook: " + e.getMessage());
        }
    }

    /**
     * Provider assignment webhook from dispatch service
     */
    @PostMapping("/provider/assigned")
    public ResponseEntity<String> handleProviderAssigned(@RequestBody Map<String, Object> payload) {
        log.info("Received provider assigned webhook");

        try {
            String bookingId = (String) payload.get("bookingId");
            String providerId = (String) payload.get("providerId");
            String employeeId = (String) payload.get("employeeId");

            // Update booking with assigned provider/employee
            bookingService.assignEmployee(bookingId, employeeId, providerId);

            return ResponseEntity.ok("Webhook processed successfully");
        } catch (Exception e) {
            log.error("Error processing provider assigned webhook: {}", e.getMessage());
            return ResponseEntity.badRequest().body("Error processing webhook: " + e.getMessage());
        }
    }

    /**
     * Tracking update webhook from tracking service
     */
    @PostMapping("/tracking/update")
    public ResponseEntity<String> handleTrackingUpdate(@RequestBody Map<String, Object> payload) {
        log.info("Received tracking update webhook");

        try {
            String bookingId = (String) payload.get("bookingId");
            Double latitude = (Double) payload.get("latitude");
            Double longitude = (Double) payload.get("longitude");
            Integer etaMinutes = (Integer) payload.get("etaMinutes");

            // Update tracking information
            // This would need to be implemented

            return ResponseEntity.ok("Webhook processed successfully");
        } catch (Exception e) {
            log.error("Error processing tracking webhook: {}", e.getMessage());
            return ResponseEntity.badRequest().body("Error processing webhook: " + e.getMessage());
        }
    }
}
