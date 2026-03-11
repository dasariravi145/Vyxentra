package com.vyxentra.vehicle.service;


import com.vyxentra.vehicle.dto.WebhookResult;
import com.vyxentra.vehicle.dto.response.PaymentResponse;

import java.util.Map;

public interface WebhookService {

    WebhookResult processRazorpayWebhook(String payload, String signature);

    WebhookResult processStripeWebhook(String payload, String signature);

    WebhookResult processPayUWebhook(Map<String, String> payload);

    WebhookResult processCashfreeWebhook(String payload, String signature);

    boolean verifyWebhookSignature(String payload, String signature, String gateway);

    PaymentResponse handleSuccessfulPayment(String gatewayPaymentId,
                                            String gatewayOrderId,
                                            Double amount,
                                            Map<String, Object> metadata);

    void handleFailedPayment(String gatewayPaymentId,
                             String gatewayOrderId,
                             String failureReason,
                             Map<String, Object> metadata);

    void handleRefundWebhook(String gatewayRefundId,
                             String gatewayPaymentId,
                             String status,
                             Map<String, Object> metadata);
}