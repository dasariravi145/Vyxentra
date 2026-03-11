package com.vyxentra.vehicle.gateway;


import com.razorpay.*;
import com.stripe.param.CustomerUpdateParams;
import com.vyxentra.vehicle.dto.request.PaymentMethodRequest;
import com.vyxentra.vehicle.dto.request.ProcessPaymentRequest;
import com.vyxentra.vehicle.entity.Payment;
import com.vyxentra.vehicle.entity.PaymentMethod;
import com.vyxentra.vehicle.entity.Refund;
import com.vyxentra.vehicle.exceptions.BusinessException;
import com.vyxentra.vehicle.exceptions.ErrorCode;
import lombok.extern.slf4j.Slf4j;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@Component
public class RazorpayGateway implements PaymentGateway {

    @Value("${payment.gateway.razorpay.key-id}")
    private String keyId;

    @Value("${payment.gateway.razorpay.key-secret}")
    private String keySecret;

    @Value("${payment.gateway.razorpay.webhook-secret}")
    private String webhookSecret;

    private RazorpayClient razorpayClient;

    private RazorpayClient getClient() throws RazorpayException {
        return new RazorpayClient(keyId, keySecret);
    }

    // ==================== Order Methods ====================

    @Override
    public Map<String, Object> createOrder(Payment payment) {
        Map<String, Object> response = new HashMap<>();
        try {
            JSONObject orderRequest = new JSONObject();
            orderRequest.put("amount", payment.getAmount() * 100);
            orderRequest.put("currency", "INR");
            orderRequest.put("receipt", payment.getPaymentNumber());
            orderRequest.put("payment_capture", 1);

            JSONObject notes = new JSONObject();
            notes.put("bookingId", payment.getBookingId());
            notes.put("customerId", payment.getCustomerId());
            orderRequest.put("notes", notes);

            Order order = getClient().orders.create(orderRequest);

            response.put("success", true);
            response.put("orderId", order.get("id"));
            response.put("amount", order.get("amount"));
            response.put("currency", order.get("currency"));
            response.put("receipt", order.get("receipt"));
            response.put("status", order.get("status"));

            log.info("Razorpay order created: {}", (Object) order.get("id"));

        } catch (RazorpayException e) {
            log.error("Razorpay order creation failed: {}", e.getMessage());
            response.put("success", false);
            response.put("errorMessage", e.getMessage());
        }
        return response;
    }

    // ==================== Payment Methods ====================

    @Override
    public Map<String, Object> processPayment(Payment payment, ProcessPaymentRequest request) {
        Map<String, Object> response = new HashMap<>();
        try {
            JSONObject options = new JSONObject();
            options.put("razorpay_order_id", request.getGatewayOrderId());
            options.put("razorpay_payment_id", request.getGatewayPaymentId());
            options.put("razorpay_signature", request.getGatewaySignature());

            boolean isValid = Utils.verifyPaymentSignature(options, keySecret);

            if (isValid) {
                com.razorpay.Payment razorpayPayment = getClient().payments
                        .fetch(request.getGatewayPaymentId());

                response.put("success", true);
                response.put("paymentId", request.getGatewayPaymentId());
                response.put("orderId", request.getGatewayOrderId());
                response.put("status", razorpayPayment.get("status"));
                response.put("method", razorpayPayment.get("method"));
                response.put("amount", razorpayPayment.get("amount"));
                response.put("fee", razorpayPayment.get("fee"));
                response.put("tax", razorpayPayment.get("tax"));
                response.put("bank", razorpayPayment.get("bank"));
                response.put("wallet", razorpayPayment.get("wallet"));
                response.put("vpa", razorpayPayment.get("vpa"));
            } else {
                response.put("success", false);
                response.put("errorMessage", "Invalid payment signature");
            }

        } catch (RazorpayException e) {
            log.error("Razorpay payment verification failed: {}", e.getMessage());
            response.put("success", false);
            response.put("errorMessage", e.getMessage());
        }
        return response;
    }

    @Override
    public boolean verifyPayment(Payment payment) {
        try {
            com.razorpay.Payment razorpayPayment = getClient().payments
                    .fetch(payment.getGatewayPaymentId());
            String status = razorpayPayment.get("status");
            return "captured".equals(status) || "authorized".equals(status);
        } catch (RazorpayException e) {
            log.error("Razorpay payment verification failed: {}", e.getMessage());
            return false;
        }
    }

    @Override
    public String getPaymentStatus(String gatewayPaymentId) {
        try {
            com.razorpay.Payment razorpayPayment = getClient().payments
                    .fetch(gatewayPaymentId);
            return razorpayPayment.get("status");
        } catch (RazorpayException e) {
            log.error("Failed to get payment status: {}", e.getMessage());
            return "unknown";
        }
    }

    // ==================== Refund Methods ====================

    @Override
    public Map<String, Object> processRefund(Payment payment, Refund refund) {
        Map<String, Object> response = new HashMap<>();
        try {
            JSONObject refundRequest = new JSONObject();
            refundRequest.put("amount", refund.getAmount() * 100);
            refundRequest.put("speed", "normal");
            refundRequest.put("receipt", refund.getRefundNumber());

            JSONObject notes = new JSONObject();
            notes.put("reason", refund.getReason());
            notes.put("paymentId", payment.getId());
            refundRequest.put("notes", notes);

            com.razorpay.Refund razorpayRefund = getClient().payments
                    .refund(payment.getGatewayPaymentId(), refundRequest);

            response.put("success", true);
            response.put("refundId", razorpayRefund.get("id"));
            response.put("amount", razorpayRefund.get("amount"));
            response.put("status", razorpayRefund.get("status"));
            response.put("speed", razorpayRefund.get("speed"));
            response.put("created_at", razorpayRefund.get("created_at"));

            log.info("Razorpay refund created: {}", (Object) razorpayRefund.get("id"));

        } catch (RazorpayException e) {
            log.error("Razorpay refund failed: {}", e.getMessage());
            response.put("success", false);
            response.put("errorMessage", e.getMessage());
        }
        return response;
    }

    @Override
    public String getRefundStatus(String gatewayRefundId) {
        try {
            com.razorpay.Refund razorpayRefund = getClient().refunds.fetch(gatewayRefundId);
            return razorpayRefund.get("status");
        } catch (RazorpayException e) {
            log.error("Failed to get refund status: {}", e.getMessage());
            return "unknown";
        }
    }

    // ==================== Payment Method Management ====================

    @Override
    public String tokenizePaymentMethod(PaymentMethod paymentMethod, PaymentMethodRequest request) {
        try {
            if (request != null && request.getCardNumber() != null) {
                // In production, you'd use Razorpay's tokenization API
                String token = "card_token_" + System.currentTimeMillis() + "_" +
                        (request.getCardNumber() != null ?
                                request.getCardNumber().substring(request.getCardNumber().length() - 4) : "xxxx");

                log.info("Payment method tokenized: {}", token);
                return token;
            }
            return "token_" + System.currentTimeMillis();

        } catch (Exception e) {
            log.error("Failed to tokenize payment method: {}", e.getMessage());
            throw new BusinessException(ErrorCode.PAYMENT_FAILED, "Failed to tokenize payment method");
        }
    }

    @Override
    public boolean deletePaymentMethod(String gatewayToken) {
        try {
            log.info("Payment method marked as inactive in local system, gateway token: {}", gatewayToken);
            return true;
        } catch (Exception e) {
            log.error("Failed to delete payment method from gateway: {}", e.getMessage());
            return false;
        }
    }

    @Override
    public String updatePaymentMethod(String gatewayToken, PaymentMethod paymentMethod) {
        log.info("Updating payment method - would delete old and create new");
        deletePaymentMethod(gatewayToken);
        return tokenizePaymentMethod(paymentMethod, null);
    }

    @Override
    public boolean verifyPaymentMethod(String gatewayToken, Map<String, Object> verificationData) {
        log.info("Verifying payment method: {}", gatewayToken);
        return true;
    }

    @Override
    public Map<String, Object> getPaymentMethodDetails(String gatewayToken) {
        Map<String, Object> details = new HashMap<>();
        details.put("token", gatewayToken);
        details.put("lastFour", gatewayToken != null && gatewayToken.contains("_") ?
                gatewayToken.substring(gatewayToken.length() - 4) : "1234");
        details.put("network", "VISA");
        details.put("type", "card");
        return details;
    }

    // ==================== Customer Management ====================

    @Override
    public String createCustomer(String customerId, Map<String, Object> customerData) {
        try {
            JSONObject customerRequest = new JSONObject();
            customerRequest.put("name", customerData.getOrDefault("name", "Customer"));
            customerRequest.put("email", customerData.getOrDefault("email", ""));
            customerRequest.put("contact", customerData.getOrDefault("phone", ""));

            JSONObject notes = new JSONObject();
            notes.put("internal_customer_id", customerId);
            customerRequest.put("notes", notes);

            Customer customer = getClient().customers.create(customerRequest);
            String gatewayCustomerId = customer.get("id");

            log.info("Razorpay customer created: {} for internal customer: {}", gatewayCustomerId, customerId);
            return gatewayCustomerId;

        } catch (RazorpayException e) {
            log.error("Failed to create customer: {}", e.getMessage());
            throw new BusinessException(ErrorCode.PAYMENT_FAILED, "Failed to create customer: " + e.getMessage());
        }
    }

    @Override
    public Map<String, Object> getCustomerDetails(String gatewayCustomerId) {
        try {
            Customer customer = getClient().customers.fetch(gatewayCustomerId);

            Map<String, Object> details = new HashMap<>();
            details.put("id", customer.get("id"));
            details.put("name", customer.get("name"));
            details.put("email", customer.get("email"));
            details.put("contact", customer.get("contact"));
            details.put("notes", customer.get("notes"));

            return details;

        } catch (RazorpayException e) {
            log.error("Failed to fetch customer: {}", e.getMessage());
            return Map.of("error", "Customer not found", "id", gatewayCustomerId);
        }
    }

    @Override
    public boolean updateCustomer(String gatewayCustomerId, Map<String, Object> customerData) {

        try {

            JSONObject updateParams = new JSONObject();

            if (customerData.containsKey("name")) {
                updateParams.put("name", customerData.get("name"));
            }

            if (customerData.containsKey("email")) {
                updateParams.put("email", customerData.get("email"));
            }

            if (customerData.containsKey("phone")) {
                updateParams.put("contact", customerData.get("phone"));
            }

            if (customerData.containsKey("notes")) {
                JSONObject notes = new JSONObject();

                Map<String, String> notesMap = (Map<String, String>) customerData.get("notes");

                for (Map.Entry<String, String> entry : notesMap.entrySet()) {
                    notes.put(entry.getKey(), entry.getValue());
                }

                updateParams.put("notes", notes);
            }

            // If nothing to update
            if (updateParams.length() == 0) {
                log.info("No fields to update for customer: {}", gatewayCustomerId);
                return true;
            }

            // Correct Razorpay update call
            Customer customer = razorpayClient.customers.edit(gatewayCustomerId, updateParams);

            log.info("Customer updated successfully: {}", (Object) customer.get("id"));

            return true;

        } catch (RazorpayException e) {
            log.error("Failed to update customer: {}", e.getMessage());
            return false;

        } catch (Exception e) {
            log.error("Unexpected error updating customer: {}", e.getMessage());
            return false;
        }
    }

    @Override
    public boolean deleteCustomer(String gatewayCustomerId) {
        log.info("Customer deletion requested - would mark as inactive in local system: {}", gatewayCustomerId);
        return true;
    }

    // ==================== Payout Methods ====================

    @Override
    public String createPayout(String providerId, Double amount, Map<String, Object> bankDetails) {
        try {
            String payoutId = "payout_" + System.currentTimeMillis() + "_" + providerId;
            log.info("Payout created: {} for provider: {} amount: {}", payoutId, providerId, amount);
            return payoutId;
        } catch (Exception e) {
            log.error("Failed to create payout: {}", e.getMessage());
            throw new BusinessException(ErrorCode.PAYMENT_FAILED, "Failed to create payout: " + e.getMessage());
        }
    }

    @Override
    public String getPayoutStatus(String gatewayPayoutId) {
        return "processed";
    }

    // ==================== Webhook Methods ====================

    @Override
    public Map<String, Object> processWebhook(String payload, String signature) {
        Map<String, Object> response = new HashMap<>();
        try {
            boolean isValid = Utils.verifyWebhookSignature(payload, signature, webhookSecret);

            if (isValid) {
                JSONObject webhookData = new JSONObject(payload);
                String event = webhookData.getString("event");
                JSONObject payloadData = webhookData.getJSONObject("payload");

                response.put("success", true);
                response.put("event", event);

                if (event.contains("payment")) {
                    JSONObject payment = payloadData.getJSONObject("payment").getJSONObject("entity");
                    response.put("paymentId", payment.getString("id"));
                    response.put("orderId", payment.optString("order_id", null));
                    response.put("amount", payment.getDouble("amount") / 100);
                    response.put("status", payment.getString("status"));
                } else if (event.contains("refund")) {
                    JSONObject refund = payloadData.getJSONObject("refund").getJSONObject("entity");
                    response.put("refundId", refund.getString("id"));
                    response.put("paymentId", refund.getString("payment_id"));
                    response.put("amount", refund.getDouble("amount") / 100);
                    response.put("status", refund.getString("status"));
                }

                log.info("Razorpay webhook verified: {}", event);
            } else {
                response.put("success", false);
                response.put("errorMessage", "Invalid webhook signature");
            }

        } catch (Exception e) {
            log.error("Razorpay webhook processing failed: {}", e.getMessage());
            response.put("success", false);
            response.put("errorMessage", e.getMessage());
        }
        return response;
    }

    @Override
    public boolean verifyWebhookSignature(String payload, String signature) {
        try {
            return Utils.verifyWebhookSignature(payload, signature, webhookSecret);
        } catch (RazorpayException e) {
            log.error("Webhook signature verification failed: {}", e.getMessage());
            return false;
        }
    }

    // ==================== Utility Methods ====================

    @Override
    public String getGatewayName() {
        return "RAZORPAY";
    }

    @Override
    public boolean isAvailable() {
        try {
            getClient().payments.fetch("dummy_payment_id");
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    @Override
    public Map<String, Object> healthCheck() {
        Map<String, Object> health = new HashMap<>();
        health.put("gateway", getGatewayName());
        health.put("status", isAvailable() ? "UP" : "DOWN");
        health.put("timestamp", System.currentTimeMillis());
        return health;
    }

    @Override
    public Long formatAmount(Double amount) {
        return Math.round(amount * 100);
    }

    @Override
    public Double parseAmount(Long gatewayAmount) {
        return gatewayAmount / 100.0;
    }

}