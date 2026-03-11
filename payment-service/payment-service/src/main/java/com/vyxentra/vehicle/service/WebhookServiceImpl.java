package com.vyxentra.vehicle.service;

import com.razorpay.Utils;
import com.stripe.exception.SignatureVerificationException;
import com.stripe.model.Event;
import com.stripe.model.PaymentIntent;
import com.stripe.net.Webhook;
import com.vyxentra.vehicle.dto.WebhookResult;
import com.vyxentra.vehicle.dto.response.PaymentResponse;
import com.vyxentra.vehicle.entity.Payment;
import com.vyxentra.vehicle.entity.Refund;
import com.vyxentra.vehicle.enums.PaymentStatus;
import com.vyxentra.vehicle.exception.PaymentException;
import com.vyxentra.vehicle.exceptions.ErrorCode;
import com.vyxentra.vehicle.kafka.PaymentEventProducer;
import com.vyxentra.vehicle.repository.PaymentRepository;
import com.vyxentra.vehicle.repository.RefundRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class WebhookServiceImpl implements WebhookService {

    private final PaymentRepository paymentRepository;
    private final RefundRepository refundRepository;
    private final PaymentEventProducer eventProducer;
    private final NotificationService notificationService;

    @Value("${payment.gateway.razorpay.webhook-secret}")
    private String razorpayWebhookSecret;

    @Value("${payment.gateway.stripe.webhook-secret}")
    private String stripeWebhookSecret;

    @Value("${payment.gateway.payu.merchant-key}")
    private String payuMerchantKey;

    @Value("${payment.gateway.payu.merchant-salt}")
    private String payuMerchantSalt;

    // ================= RAZORPAY =================

    @Override
    @Transactional
    public WebhookResult processRazorpayWebhook(String payload, String signature) {

        try {

            boolean valid = Utils.verifyWebhookSignature(payload, signature, razorpayWebhookSecret);

            if (!valid) {
                return WebhookResult.builder()
                        .success(false)
                        .message("Invalid Razorpay signature")
                        .build();
            }

            JSONObject json = new JSONObject(payload);
            String eventType = json.getString("event");
            JSONObject payloadObj = json.getJSONObject("payload");

            switch (eventType) {

                case "payment.captured":
                case "payment.authorized":
                    handleRazorpayPaymentSuccess(payloadObj);
                    break;

                case "payment.failed":
                    handleRazorpayPaymentFailed(payloadObj);
                    break;

                case "refund.created":
                case "refund.processed":
                    handleRazorpayRefund(payloadObj);
                    break;

                default:
                    log.info("Unhandled Razorpay event {}", eventType);
            }

            return WebhookResult.builder()
                    .success(true)
                    .eventType(eventType)
                    .build();

        } catch (Exception e) {

            log.error("Error processing Razorpay webhook", e);

            return WebhookResult.builder()
                    .success(false)
                    .message(e.getMessage())
                    .build();
        }
    }

    // ================= STRIPE =================

    @Override
    @Transactional
    public WebhookResult processStripeWebhook(String payload, String signature) {

        try {

            Event event = Webhook.constructEvent(payload, signature, stripeWebhookSecret);
            String eventType = event.getType();

            switch (eventType) {

                case "payment_intent.succeeded":
                    handleStripePaymentSuccess(event);
                    break;

                case "payment_intent.payment_failed":
                    handleStripePaymentFailed(event);
                    break;

                case "charge.refunded":
                    handleStripeRefund(event);
                    break;

                default:
                    log.info("Unhandled Stripe event {}", eventType);
            }

            return WebhookResult.builder()
                    .success(true)
                    .eventType(eventType)
                    .build();

        } catch (SignatureVerificationException e) {

            return WebhookResult.builder()
                    .success(false)
                    .message("Invalid Stripe signature")
                    .build();

        } catch (Exception e) {

            log.error("Stripe webhook error", e);

            return WebhookResult.builder()
                    .success(false)
                    .message(e.getMessage())
                    .build();
        }
    }

    // ================= PAYU =================

    @Override
    @Transactional
    public WebhookResult processPayUWebhook(Map<String, String> payload) {

        try {

            String hash = payload.get("hash");

            if (!verifyPayUHash(payload, hash)) {
                return WebhookResult.builder()
                        .success(false)
                        .message("Invalid PayU signature")
                        .build();
            }

            String status = payload.get("status");

            if ("success".equalsIgnoreCase(status)) {
                handlePayUPaymentSuccess(payload);
            } else {
                handlePayUPaymentFailed(payload);
            }

            return WebhookResult.builder()
                    .success(true)
                    .eventType("payment." + status)
                    .build();

        } catch (Exception e) {

            log.error("PayU webhook error", e);

            return WebhookResult.builder()
                    .success(false)
                    .message(e.getMessage())
                    .build();
        }
    }

    // ================= CASHFREE =================

    @Override
    @Transactional
    public WebhookResult processCashfreeWebhook(String payload, String signature) {

        log.info("Cashfree webhook received");

        return WebhookResult.builder()
                .success(true)
                .message("Cashfree webhook processed")
                .build();
    }

    // ================= SIGNATURE VERIFY =================

    @Override
    public boolean verifyWebhookSignature(String payload, String signature, String gateway) {

        try {

            switch (gateway.toUpperCase()) {

                case "RAZORPAY":
                    return Utils.verifyWebhookSignature(payload, signature, razorpayWebhookSecret);

                case "STRIPE":
                    try {
                        Webhook.constructEvent(payload, signature, stripeWebhookSecret);
                        return true;
                    } catch (SignatureVerificationException e) {
                        return false;
                    }

                case "PAYU":
                    return true;

                default:
                    return false;
            }

        } catch (Exception e) {

            log.error("Signature verification error", e);
            return false;
        }
    }

    // ================= SUCCESS PAYMENT =================

    @Override
    @Transactional
    public PaymentResponse handleSuccessfulPayment(
            String gatewayPaymentId,
            String gatewayOrderId,
            Double amount,
            Map<String, Object> metadata) {

        Optional<Payment> paymentOpt =
                paymentRepository.findByGatewayOrderId(gatewayOrderId);

        if (paymentOpt.isEmpty()) {
            throw new PaymentException(null, ErrorCode.PAYMENT_NOT_FOUND);
        }

        Payment payment = paymentOpt.get();

        payment.setStatus(PaymentStatus.SUCCESS.name());
        payment.setGatewayPaymentId(gatewayPaymentId);
        payment.setUpdatedAt(LocalDateTime.now());

        payment = paymentRepository.save(payment);

        eventProducer.publishPaymentSuccess(payment);

        notificationService.sendPaymentSuccessNotification(
                payment.getCustomerId(),
                payment.getBookingId(),
                payment.getAmount());

        return mapToPaymentResponse(payment);
    }

    // ================= FAILED PAYMENT =================

    @Override
    @Transactional
    public void handleFailedPayment(
            String gatewayPaymentId,
            String gatewayOrderId,
            String failureReason,
            Map<String, Object> metadata) {

        Optional<Payment> paymentOpt =
                paymentRepository.findByGatewayOrderId(gatewayOrderId);

        if (paymentOpt.isEmpty()) return;

        Payment payment = paymentOpt.get();

        payment.setStatus(PaymentStatus.FAILED.name());
        payment.setErrorMessage(failureReason);
        payment.setUpdatedAt(LocalDateTime.now());

        paymentRepository.save(payment);

        eventProducer.publishPaymentFailed(payment, failureReason);
    }

    // ================= REFUND HANDLER =================

    @Override
    @Transactional
    public void handleRefundWebhook(
            String gatewayRefundId,
            String gatewayPaymentId,
            String status,
            Map<String, Object> metadata) {

        Optional<Refund> refundOpt =
                refundRepository.findByGatewayRefundId(gatewayRefundId);

        if (refundOpt.isEmpty()) return;

        Refund refund = refundOpt.get();

        if ("processed".equalsIgnoreCase(status) || "success".equalsIgnoreCase(status)) {
            refund.setStatus(PaymentStatus.SUCCESS.name());
            refund.setProcessedAt(LocalDateTime.now());
        } else {
            refund.setStatus(PaymentStatus.FAILED.name());
        }

        refundRepository.save(refund);
    }

    // ================= RAZORPAY HANDLERS =================

    private void handleRazorpayPaymentSuccess(JSONObject payload) {

        JSONObject payment = payload.getJSONObject("payment").getJSONObject("entity");

        String paymentId = payment.getString("id");
        String orderId = payment.getString("order_id");
        double amount = payment.getDouble("amount") / 100;

        handleSuccessfulPayment(paymentId, orderId, amount, new HashMap<>());
    }

    private void handleRazorpayPaymentFailed(JSONObject payload) {

        JSONObject payment = payload.getJSONObject("payment").getJSONObject("entity");

        String paymentId = payment.getString("id");
        String orderId = payment.getString("order_id");
        String error = payment.optString("error_description");

        handleFailedPayment(paymentId, orderId, error, null);
    }

    private void handleRazorpayRefund(JSONObject payload) {

        JSONObject refund = payload.getJSONObject("refund").getJSONObject("entity");

        String refundId = refund.getString("id");
        String paymentId = refund.getString("payment_id");
        String status = refund.getString("status");

        handleRefundWebhook(refundId, paymentId, status, new HashMap<>());
    }

    // ================= STRIPE HANDLERS =================

    private void handleStripePaymentSuccess(Event event) {

        PaymentIntent intent =
                (PaymentIntent) event.getDataObjectDeserializer().getObject().orElse(null);

        if (intent == null) return;

        String orderId = intent.getMetadata().get("order_id");
        double amount = intent.getAmount() / 100.0;

        handleSuccessfulPayment(intent.getId(), orderId, amount, new HashMap<>());
    }

    private void handleStripePaymentFailed(Event event) {

        PaymentIntent intent =
                (PaymentIntent) event.getDataObjectDeserializer().getObject().orElse(null);

        if (intent == null) return;

        String orderId = intent.getMetadata().get("order_id");

        String error = intent.getLastPaymentError() != null
                ? intent.getLastPaymentError().getMessage()
                : "Unknown error";

        handleFailedPayment(intent.getId(), orderId, error, null);
    }

    private void handleStripeRefund(Event event) {

        log.info("Stripe refund webhook received");

        // Implementation depends on Stripe refund payload usage
    }

    // ================= PAYU HANDLERS =================

    private void handlePayUPaymentSuccess(Map<String, String> payload) {

        String txnId = payload.get("txnid");
        String paymentId = payload.get("mihpayid");
        double amount = Double.parseDouble(payload.get("amount"));

        handleSuccessfulPayment(paymentId, txnId, amount, new HashMap<>());
    }

    private void handlePayUPaymentFailed(Map<String, String> payload) {

        String txnId = payload.get("txnid");
        String error = payload.get("error_Message");

        handleFailedPayment(null, txnId, error, null);
    }

    // ================= PAYU HASH =================

    private boolean verifyPayUHash(Map<String, String> payload, String receivedHash) {

        try {

            StringBuilder hashString = new StringBuilder();

            hashString.append(payuMerchantKey).append("|")
                    .append(payload.get("txnid")).append("|")
                    .append(payload.get("amount")).append("|")
                    .append(payload.get("productinfo")).append("|")
                    .append(payload.get("firstname")).append("|")
                    .append(payload.get("email")).append("|||||||||||")
                    .append(payuMerchantSalt);

            String calculatedHash = calculateHash(hashString.toString());

            return calculatedHash.equals(receivedHash);

        } catch (Exception e) {

            log.error("PayU hash verification failed", e);
            return false;
        }
    }

    private String calculateHash(String input) throws NoSuchAlgorithmException {

        MessageDigest md = MessageDigest.getInstance("SHA-512");
        byte[] bytes = md.digest(input.getBytes());

        StringBuilder sb = new StringBuilder();

        for (byte b : bytes) {
            sb.append(String.format("%02x", b));
        }

        return sb.toString();
    }

    // ================= RESPONSE MAPPER =================

    private PaymentResponse mapToPaymentResponse(Payment payment) {

        return PaymentResponse.builder()
                .paymentId(payment.getId())
                .paymentNumber(payment.getPaymentNumber())
                .bookingId(payment.getBookingId())
                .customerId(payment.getCustomerId())
                .amount(payment.getAmount())
                .commissionAmount(payment.getCommissionAmount())
                .providerAmount(payment.getProviderAmount())
                .paymentMethod(payment.getPaymentMethod())
                .status(PaymentStatus.valueOf(payment.getStatus()))
                .paymentType(payment.getPaymentType())
                .createdAt(payment.getCreatedAt())
                .build();
    }
}