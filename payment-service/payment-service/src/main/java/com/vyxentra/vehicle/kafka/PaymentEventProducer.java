package com.vyxentra.vehicle.kafka;


import com.fasterxml.jackson.databind.ObjectMapper;
import com.vyxentra.vehicle.entity.Payment;
import com.vyxentra.vehicle.entity.Payout;
import com.vyxentra.vehicle.entity.Refund;
import com.vyxentra.vehicle.entity.WalletTransaction;
import com.vyxentra.vehicle.utils.PaymentConstants;
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
public class PaymentEventProducer {

    private final KafkaTemplate<String, Object> kafkaTemplate;
    private final ObjectMapper objectMapper;

    // ==================== Payment Events ====================

    /**
     * Publish payment success event
     */
    public void publishPaymentSuccess(Payment payment) {
        Map<String, Object> event = createBasePaymentEvent(payment, "PAYMENT_SUCCESS");
        event.put("amount", payment.getAmount());
        event.put("status", payment.getStatus());
        event.put("paymentMethod", payment.getPaymentMethod());
        event.put("gatewayPaymentId", payment.getGatewayPaymentId());

        publishEvent(PaymentConstants.PAYMENT_SUCCESS_TOPIC, event);
    }

    /**
     * Publish payment failed event
     */
    public void publishPaymentFailed(Payment payment, String reason) {
        Map<String, Object> event = createBasePaymentEvent(payment, "PAYMENT_FAILED");
        event.put("amount", payment.getAmount());
        event.put("reason", reason);
        event.put("errorCode", payment.getErrorCode());

        publishEvent(PaymentConstants.PAYMENT_FAILED_TOPIC, event);
    }

    /**
     * Publish payment created event
     */
    public void publishPaymentCreated(Payment payment) {
        Map<String, Object> event = createBasePaymentEvent(payment, "PAYMENT_CREATED");
        event.put("amount", payment.getAmount());
        event.put("status", payment.getStatus());

        publishEvent("payment.created", event);
    }

    /**
     * Publish payment processing event
     */
    public void publishPaymentProcessing(Payment payment) {
        Map<String, Object> event = createBasePaymentEvent(payment, "PAYMENT_PROCESSING");
        event.put("amount", payment.getAmount());

        publishEvent("payment.processing", event);
    }

    // ==================== Refund Events ====================

    /**
     * Publish refund processed event
     */
    public void publishRefundProcessed(Refund refund) {
        Map<String, Object> event = new HashMap<>();
        event.put("eventId", UUID.randomUUID().toString());
        event.put("eventType", "REFUND_PROCESSED");
        event.put("refundId", refund.getId());
        event.put("refundNumber", refund.getRefundNumber());
        event.put("paymentId", refund.getPayment().getId());
        event.put("bookingId", refund.getBookingId());
        event.put("amount", refund.getAmount());
        event.put("status", refund.getStatus());
        event.put("reason", refund.getReason());
        event.put("timestamp", Instant.now().toString());

        publishEvent(PaymentConstants.REFUND_PROCESSED_TOPIC, event);
    }

    /**
     * Publish refund failed event
     */
    public void publishRefundFailed(Refund refund, String reason) {
        Map<String, Object> event = new HashMap<>();
        event.put("eventId", UUID.randomUUID().toString());
        event.put("eventType", "REFUND_FAILED");
        event.put("refundId", refund.getId());
        event.put("paymentId", refund.getPayment().getId());
        event.put("amount", refund.getAmount());
        event.put("reason", reason);
        event.put("timestamp", Instant.now().toString());

        publishEvent("refund.failed", event);
    }

    // ==================== Wallet Events ====================

    /**
     * Publish wallet credited event
     */
    public void publishWalletCredited(WalletTransaction transaction) {
        Map<String, Object> event = new HashMap<>();
        event.put("eventId", UUID.randomUUID().toString());
        event.put("eventType", "WALLET_CREDITED");
        event.put("walletId", transaction.getWallet().getId());
        event.put("userId", transaction.getWallet().getUserId());
        event.put("transactionId", transaction.getId());
        event.put("amount", transaction.getAmount());
        event.put("balance", transaction.getBalanceAfter());
        event.put("referenceId", transaction.getReferenceId());
        event.put("referenceType", transaction.getReferenceType());
        event.put("timestamp", Instant.now().toString());

        publishEvent("wallet.credited", event);
    }

    /**
     * Publish wallet debited event
     */
    public void publishWalletDebited(WalletTransaction transaction) {
        Map<String, Object> event = new HashMap<>();
        event.put("eventId", UUID.randomUUID().toString());
        event.put("eventType", "WALLET_DEBITED");
        event.put("walletId", transaction.getWallet().getId());
        event.put("userId", transaction.getWallet().getUserId());
        event.put("transactionId", transaction.getId());
        event.put("amount", transaction.getAmount());
        event.put("balance", transaction.getBalanceAfter());
        event.put("referenceId", transaction.getReferenceId());
        event.put("referenceType", transaction.getReferenceType());
        event.put("timestamp", Instant.now().toString());

        publishEvent("wallet.debited", event);
    }

    // ==================== PAYOUT EVENTS ====================

    /**
     * Publish payout created event
     */
    public void publishPayoutCreated(Payout payout) {
        Map<String, Object> event = createBasePayoutEvent(payout, "PAYOUT_CREATED");
        event.put("amount", payout.getNetAmount());
        event.put("totalAmount", payout.getTotalAmount());
        event.put("commission", payout.getCommissionDeducted());
        event.put("tax", payout.getTaxDeducted());
        event.put("periodStart", payout.getPeriodStart().toString());
        event.put("periodEnd", payout.getPeriodEnd().toString());
        event.put("bookingCount", payout.getBookingIds() != null ? payout.getBookingIds().size() : 0);

        publishEvent("payout.created", event);
    }

    /**
     * Publish payout processing event
     */
    public void publishPayoutProcessing(Payout payout) {
        Map<String, Object> event = createBasePayoutEvent(payout, "PAYOUT_PROCESSING");
        event.put("amount", payout.getNetAmount());
        event.put("gatewayPayoutId", payout.getGatewayPayoutId());
        event.put("requestedAt", payout.getRequestedAt() != null ? payout.getRequestedAt().toString() : null);

        publishEvent("payout.processing", event);
    }

    /**
     * Publish payout success event
     */
    public void publishPayoutSuccess(Payout payout) {
        Map<String, Object> event = createBasePayoutEvent(payout, "PAYOUT_SUCCESS");
        event.put("amount", payout.getNetAmount());
        event.put("gatewayPayoutId", payout.getGatewayPayoutId());
        event.put("gatewayReference", payout.getGatewayReference());
        event.put("processedAt", payout.getProcessedAt() != null ? payout.getProcessedAt().toString() : null);
        event.put("completedAt", payout.getCompletedAt() != null ? payout.getCompletedAt().toString() : null);

        publishEvent(PaymentConstants.PAYOUT_PROCESSED_TOPIC, event);
    }

    /**
     * Publish payout failed event
     */
    public void publishPayoutFailed(Payout payout, String reason, String failureCode) {
        Map<String, Object> event = createBasePayoutEvent(payout, "PAYOUT_FAILED");
        event.put("amount", payout.getNetAmount());
        event.put("reason", reason);
        event.put("failureCode", failureCode);
        event.put("retryCount", payout.getRetryCount());

        publishEvent("payout.failed", event);
    }

    /**
     * Publish payout retry event
     */
    public void publishPayoutRetry(Payout payout, int attemptNumber) {
        Map<String, Object> event = createBasePayoutEvent(payout, "PAYOUT_RETRY");
        event.put("amount", payout.getNetAmount());
        event.put("attemptNumber", attemptNumber);
        event.put("maxRetries", payout.getMaxRetryCount());

        publishEvent("payout.retry", event);
    }

    /**
     * Publish payout cancelled event
     */
    public void publishPayoutCancelled(Payout payout, String reason) {
        Map<String, Object> event = createBasePayoutEvent(payout, "PAYOUT_CANCELLED");
        event.put("amount", payout.getNetAmount());
        event.put("reason", reason);

        publishEvent("payout.cancelled", event);
    }

    /**
     * Publish payout settled event
     */
    public void publishPayoutSettled(Payout payout, String settlementReference) {
        Map<String, Object> event = createBasePayoutEvent(payout, "PAYOUT_SETTLED");
        event.put("amount", payout.getNetAmount());
        event.put("settlementReference", settlementReference);
        event.put("settlementDate", payout.getActualSettlementDate() != null ?
                payout.getActualSettlementDate().toString() : null);

        publishEvent("payout.settled", event);
    }

    // ==================== Helper Methods ====================

    /**
     * Create base payment event map
     */
    private Map<String, Object> createBasePaymentEvent(Payment payment, String eventType) {
        Map<String, Object> event = new HashMap<>();
        event.put("eventId", UUID.randomUUID().toString());
        event.put("eventType", eventType);
        event.put("paymentId", payment.getId());
        event.put("paymentNumber", payment.getPaymentNumber());
        event.put("bookingId", payment.getBookingId());
        event.put("customerId", payment.getCustomerId());
        event.put("providerId", payment.getProviderId());
        event.put("timestamp", Instant.now().toString());
        return event;
    }

    /**
     * Create base payout event map
     */
    private Map<String, Object> createBasePayoutEvent(Payout payout, String eventType) {
        Map<String, Object> event = new HashMap<>();
        event.put("eventId", UUID.randomUUID().toString());
        event.put("eventType", eventType);
        event.put("payoutId", payout.getId());
        event.put("payoutNumber", payout.getPayoutNumber());
        event.put("providerId", payout.getProviderId());
        event.put("providerName", payout.getProviderName());
        event.put("status", payout.getStatus());
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