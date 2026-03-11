package com.vyxentra.vehicle.gateway;


import com.vyxentra.vehicle.dto.request.PaymentMethodRequest;
import com.vyxentra.vehicle.dto.request.ProcessPaymentRequest;
import com.vyxentra.vehicle.entity.Payment;
import com.vyxentra.vehicle.entity.PaymentMethod;
import com.vyxentra.vehicle.entity.Refund;

import java.util.Map;

public interface PaymentGateway {

    // ==================== Order Methods ====================
    Map<String, Object> createOrder(Payment payment);

    // ==================== Payment Methods ====================
    Map<String, Object> processPayment(Payment payment, ProcessPaymentRequest request);
    boolean verifyPayment(Payment payment);
    String getPaymentStatus(String gatewayPaymentId);

    // ==================== Refund Methods ====================
    Map<String, Object> processRefund(Payment payment, Refund refund);
    String getRefundStatus(String gatewayRefundId);

    // ==================== Payment Method Management ====================
    String tokenizePaymentMethod(PaymentMethod paymentMethod, PaymentMethodRequest request);
    boolean deletePaymentMethod(String gatewayToken);
    String updatePaymentMethod(String gatewayToken, PaymentMethod paymentMethod);
    boolean verifyPaymentMethod(String gatewayToken, Map<String, Object> verificationData);
    Map<String, Object> getPaymentMethodDetails(String gatewayToken);

    // ==================== Customer Management ====================
    String createCustomer(String customerId, Map<String, Object> customerData);
    Map<String, Object> getCustomerDetails(String gatewayCustomerId);
    boolean updateCustomer(String gatewayCustomerId, Map<String, Object> customerData);
    boolean deleteCustomer(String gatewayCustomerId);

    // ==================== Payout Methods ====================
    String createPayout(String providerId, Double amount, Map<String, Object> bankDetails);
    String getPayoutStatus(String gatewayPayoutId);

    // ==================== Webhook Methods ====================
    Map<String, Object> processWebhook(String payload, String signature);
    boolean verifyWebhookSignature(String payload, String signature);

    // ==================== Utility Methods ====================
    String getGatewayName();
    boolean isAvailable();
    Map<String, Object> healthCheck();
    Long formatAmount(Double amount);
    Double parseAmount(Long gatewayAmount);
}
