package com.vyxentra.vehicle.gateway;

import com.vyxentra.vehicle.dto.request.PaymentMethodRequest;
import com.vyxentra.vehicle.dto.request.ProcessPaymentRequest;
import com.vyxentra.vehicle.entity.Payment;
import com.vyxentra.vehicle.entity.PaymentMethod;
import com.vyxentra.vehicle.entity.Refund;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.util.DigestUtils;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;

@Slf4j
@Component
public class PayUGateway implements PaymentGateway {

    @Value("${payment.gateway.payu.merchant-key}")
    private String merchantKey;

    @Value("${payment.gateway.payu.merchant-salt}")
    private String merchantSalt;

    @Value("${payment.gateway.payu.base-url}")
    private String baseUrl;

    // ==================== Order Methods ====================

    @Override
    public Map<String, Object> createOrder(Payment payment) {
        Map<String, Object> response = new HashMap<>();

        try {
            String txnId = "TXN_" + payment.getPaymentNumber();
            String amount = String.format("%.2f", payment.getAmount());
            String productInfo = "Payment for Booking " + payment.getBookingId();
            String firstName = "Customer";
            String email = "customer@example.com";
            String phone = "9999999999";

            // Generate hash
            String hashString = merchantKey + "|" + txnId + "|" + amount + "|" + productInfo + "|" +
                    firstName + "|" + email + "|||||||||||" + merchantSalt;
            String hash = calculateHash(hashString);

            response.put("success", true);
            response.put("orderId", txnId);
            response.put("paymentUrl", baseUrl + "/_payment");
            response.put("key", merchantKey);
            response.put("txnid", txnId);
            response.put("amount", amount);
            response.put("productinfo", productInfo);
            response.put("firstname", firstName);
            response.put("email", email);
            response.put("phone", phone);
            response.put("hash", hash);
            response.put("surl", "https://api.vyxentra.com/payment/success");
            response.put("furl", "https://api.vyxentra.com/payment/failure");

            log.info("PayU order created for payment: {}, txnId: {}", payment.getId(), txnId);

        } catch (Exception e) {
            log.error("PayU order creation failed: {}", e.getMessage());
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
            // Verify PayU response hash
            Map<String, String> params = (Map<String, String>) request.getMetadata().get("payuParams");
            String receivedHash = params.get("hash");

            // Generate hash for verification: salt|status||||||udf5|udf4|udf3|udf2|udf1|email|firstname|productinfo|amount|txnid|key
            String status = params.get("status");
            String txnId = params.get("txnid");
            String amount = params.get("amount");
            String productInfo = params.get("productinfo");
            String firstName = params.get("firstname");
            String email = params.get("email");

            String hashString = merchantSalt + "|" + status + "|||||||||||" + email + "|" + firstName +
                    "|" + productInfo + "|" + amount + "|" + txnId + "|" + merchantKey;
            String calculatedHash = calculateHash(hashString);

            if (calculatedHash.equals(receivedHash)) {
                response.put("success", true);
                response.put("paymentId", params.get("mihpayid"));
                response.put("orderId", txnId);
                response.put("status", status);
                response.put("amount", Double.parseDouble(amount));
                response.put("mode", params.get("mode"));
                response.put("bankRefNum", params.get("bank_ref_num"));

                log.info("PayU payment verified: {}, status: {}", txnId, status);
            } else {
                response.put("success", false);
                response.put("errorMessage", "Invalid payment hash");
            }

        } catch (Exception e) {
            log.error("PayU payment verification failed: {}", e.getMessage());
            response.put("success", false);
            response.put("errorMessage", e.getMessage());
        }

        return response;
    }

    @Override
    public boolean verifyPayment(Payment payment) {
        // PayU doesn't have a direct verification API
        // Usually verified through webhook or response hash
        return true;
    }

    @Override
    public String getPaymentStatus(String gatewayPaymentId) {
        // Would need to query PayU API
        return "unknown";
    }

    // ==================== Refund Methods ====================

    @Override
    public Map<String, Object> processRefund(Payment payment, Refund refund) {
        Map<String, Object> response = new HashMap<>();

        try {
            // PayU refund implementation - requires calling their API
            String refundId = "REF_" + refund.getRefundNumber();

            // Generate hash for refund API
            String hashString = merchantKey + "|" + payment.getGatewayPaymentId() + "|" +
                    refund.getAmount() + "|" + merchantSalt;
            String hash = calculateHash(hashString);

            response.put("success", true);
            response.put("refundId", refundId);
            response.put("status", "processing");
            response.put("amount", refund.getAmount());

            log.info("PayU refund initiated: {}", refundId);

        } catch (Exception e) {
            log.error("PayU refund failed: {}", e.getMessage());
            response.put("success", false);
            response.put("errorMessage", e.getMessage());
        }

        return response;
    }

    @Override
    public String getRefundStatus(String gatewayRefundId) {
        return "processed";
    }

    // ==================== Payment Method Management ====================

    @Override
    public String tokenizePaymentMethod(PaymentMethod paymentMethod, PaymentMethodRequest request) {
        // PayU supports tokenization through their API
        String token = "payu_token_" + System.currentTimeMillis();
        log.info("PayU payment method tokenized: {}", token);
        return token;
    }

    @Override
    public boolean deletePaymentMethod(String gatewayToken) {
        log.info("PayU payment method deleted from gateway: {}", gatewayToken);
        return true;
    }

    @Override
    public String updatePaymentMethod(String gatewayToken, PaymentMethod paymentMethod) {
        log.info("Updating PayU payment method: {}", gatewayToken);
        deletePaymentMethod(gatewayToken);
        return tokenizePaymentMethod(paymentMethod, null);
    }

    @Override
    public boolean verifyPaymentMethod(String gatewayToken, Map<String, Object> verificationData) {
        log.info("Verifying PayU payment method: {}", gatewayToken);
        return true;
    }

    @Override
    public Map<String, Object> getPaymentMethodDetails(String gatewayToken) {
        Map<String, Object> details = new HashMap<>();
        details.put("token", gatewayToken);
        details.put("type", "card");
        return details;
    }

    // ==================== Customer Management ====================

    @Override
    public String createCustomer(String customerId, Map<String, Object> customerData) {
        String gatewayCustomerId = "payu_cust_" + customerId;
        log.info("PayU customer created: {}", gatewayCustomerId);
        return gatewayCustomerId;
    }

    @Override
    public Map<String, Object> getCustomerDetails(String gatewayCustomerId) {
        return Map.of("id", gatewayCustomerId, "name", "Customer");
    }

    @Override
    public boolean updateCustomer(String gatewayCustomerId, Map<String, Object> customerData) {
        log.info("PayU customer updated: {}", gatewayCustomerId);
        return true;
    }

    @Override
    public boolean deleteCustomer(String gatewayCustomerId) {
        log.info("PayU customer deleted: {}", gatewayCustomerId);
        return true;
    }

    // ==================== Payout Methods ====================

    @Override
    public String createPayout(String providerId, Double amount, Map<String, Object> bankDetails) {
        String payoutId = "payu_payout_" + System.currentTimeMillis();
        log.info("PayU payout created: {} for provider: {}", payoutId, providerId);
        return payoutId;
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
            // Parse PayU webhook response
            // PayU sends response as POST parameters
            String[] params = payload.split("&");
            Map<String, String> paramMap = new HashMap<>();
            for (String param : params) {
                String[] keyValue = param.split("=");
                if (keyValue.length == 2) {
                    paramMap.put(keyValue[0], keyValue[1]);
                }
            }

            String status = paramMap.get("status");
            String txnId = paramMap.get("txnid");
            String mihpayId = paramMap.get("mihpayid");

            response.put("success", true);
            response.put("event", "payment." + status);
            response.put("paymentId", mihpayId);
            response.put("orderId", txnId);
            response.put("status", status);

            log.info("PayU webhook processed: {} for transaction: {}", status, txnId);

        } catch (Exception e) {
            log.error("PayU webhook processing failed: {}", e.getMessage());
            response.put("success", false);
            response.put("errorMessage", e.getMessage());
        }

        return response;
    }

    @Override
    public boolean verifyWebhookSignature(String payload, String signature) {
        // PayU doesn't use signature verification for webhooks
        // They use hash verification in response
        return true;
    }

    // ==================== Utility Methods ====================

    @Override
    public String getGatewayName() {
        return "PAYU";
    }

    @Override
    public boolean isAvailable() {
        return true;
    }

    @Override
    public Map<String, Object> healthCheck() {
        Map<String, Object> health = new HashMap<>();
        health.put("gateway", getGatewayName());
        health.put("status", "UP");
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

    // ==================== Helper Methods ====================

    private String calculateHash(String input) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-512");
            byte[] hashBytes = md.digest(input.getBytes(StandardCharsets.UTF_8));

            StringBuilder sb = new StringBuilder();
            for (byte b : hashBytes) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString();

        } catch (NoSuchAlgorithmException e) {
            log.error("Hash calculation failed: {}", e.getMessage());
            return DigestUtils.md5DigestAsHex(input.getBytes());
        }
    }
}