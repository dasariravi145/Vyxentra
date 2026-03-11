package com.vyxentra.vehicle.kafka;

import com.vyxentra.vehicle.service.PaymentService;
import com.vyxentra.vehicle.service.RefundService;
import com.vyxentra.vehicle.service.WalletService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
public class PaymentEventConsumer {

    private final PaymentService paymentService;
    private final RefundService refundService;
    private final WalletService walletService;

    @KafkaListener(topics = "booking.completed", groupId = "payment-service")
    public void handleBookingCompleted(Map<String, Object> event) {
        log.info("Received booking completed event: {}", event);

        String bookingId = (String) event.get("bookingId");
        Double amount = (Double) event.get("amount");
        String customerId = (String) event.get("customerId");

        // Create payment for completed booking
        // paymentService.createPaymentForBooking(bookingId, amount, customerId);
    }

    @KafkaListener(topics = "booking.cancelled", groupId = "payment-service")
    public void handleBookingCancelled(Map<String, Object> event) {
        log.info("Received booking cancelled event: {}", event);

        String bookingId = (String) event.get("bookingId");
        String reason = (String) event.get("reason");

        // Process refund if payment was made
        // refundService.processRefundForCancelledBooking(bookingId, reason);
    }

    @KafkaListener(topics = "user.deactivated", groupId = "payment-service")
    public void handleUserDeactivated(Map<String, Object> event) {
        log.info("Received user deactivated event: {}", event);

        String userId = (String) event.get("userId");

        // Block wallet for deactivated user
        // walletService.blockWalletForUser(userId);
    }
}
