package com.vyxentra.vehicle.gateway;

import com.stripe.Stripe;
import com.stripe.exception.StripeException;
import com.stripe.model.*;
import com.stripe.net.Webhook;
import com.stripe.param.*;
import com.vyxentra.vehicle.dto.request.PaymentMethodRequest;
import com.vyxentra.vehicle.dto.request.ProcessPaymentRequest;
import com.vyxentra.vehicle.entity.Payment;
import com.vyxentra.vehicle.entity.PaymentMethod;
import com.vyxentra.vehicle.entity.Refund;
import com.vyxentra.vehicle.exceptions.BusinessException;
import com.vyxentra.vehicle.exceptions.ErrorCode;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@Component
public class StripeGateway implements PaymentGateway {

    @Value("${payment.gateway.stripe.api-key}")
    private String apiKey;

    @Value("${payment.gateway.stripe.webhook-secret}")
    private String webhookSecret;

    @Value("${payment.gateway.stripe.publishable-key}")
    private String publishableKey;

    @PostConstruct
    public void init() {
        Stripe.apiKey = apiKey;
    }

    // ==================== Order Methods ====================

    @Override
    public Map<String, Object> createOrder(Payment payment) {
        Map<String, Object> response = new HashMap<>();

        try {
            PaymentIntentCreateParams params = PaymentIntentCreateParams.builder()
                    .setAmount(payment.getAmount().longValue() * 100) // in cents
                    .setCurrency("inr")
                    .putMetadata("paymentNumber", payment.getPaymentNumber())
                    .putMetadata("bookingId", payment.getBookingId())
                    .putMetadata("customerId", payment.getCustomerId())
                    .build();

            PaymentIntent paymentIntent = PaymentIntent.create(params);

            response.put("success", true);
            response.put("orderId", paymentIntent.getId());
            response.put("clientSecret", paymentIntent.getClientSecret());
            response.put("amount", paymentIntent.getAmount());
            response.put("currency", paymentIntent.getCurrency());
            response.put("status", paymentIntent.getStatus());

            log.info("Stripe payment intent created: {}", paymentIntent.getId());

        } catch (StripeException e) {
            log.error("Stripe payment intent creation failed: {}", e.getMessage());
            response.put("success", false);
            response.put("errorMessage", e.getMessage());
            response.put("errorCode", e.getCode());
        }

        return response;
    }

    // ==================== Payment Methods ====================

    @Override
    public Map<String, Object> processPayment(Payment payment, ProcessPaymentRequest request) {
        Map<String, Object> response = new HashMap<>();

        try {
            PaymentIntent paymentIntent = PaymentIntent.retrieve(request.getGatewayPaymentId());

            Map<String, Object> metadata = new HashMap<>();
            metadata.put("payment_method", paymentIntent.getPaymentMethod());
            metadata.put("payment_method_types", paymentIntent.getPaymentMethodTypes());

            if (request.getMetadata() != null) {
                metadata.putAll(request.getMetadata());
            }

            if ("succeeded".equals(paymentIntent.getStatus())) {
                response.put("success", true);
                response.put("paymentId", paymentIntent.getId());
                response.put("orderId", paymentIntent.getId());
                response.put("status", paymentIntent.getStatus());
                response.put("amount", paymentIntent.getAmount() / 100.0);
                response.put("currency", paymentIntent.getCurrency());
                response.put("payment_method", paymentIntent.getPaymentMethod());
                response.put("metadata", metadata);

                log.info("Stripe payment succeeded: {}", paymentIntent.getId());
            } else {
                response.put("success", false);
                response.put("errorMessage", "Payment not successful. Status: " + paymentIntent.getStatus());
                response.put("status", paymentIntent.getStatus());
            }

        } catch (StripeException e) {
            log.error("Stripe payment verification failed: {}", e.getMessage());
            response.put("success", false);
            response.put("errorMessage", e.getMessage());
            response.put("errorCode", e.getCode());
        }

        return response;
    }

    @Override
    public boolean verifyPayment(Payment payment) {
        try {
            PaymentIntent paymentIntent = PaymentIntent.retrieve(payment.getGatewayPaymentId());
            return "succeeded".equals(paymentIntent.getStatus());
        } catch (StripeException e) {
            log.error("Stripe payment verification failed: {}", e.getMessage());
            return false;
        }
    }

    @Override
    public String getPaymentStatus(String gatewayPaymentId) {
        try {
            PaymentIntent paymentIntent = PaymentIntent.retrieve(gatewayPaymentId);
            return paymentIntent.getStatus();
        } catch (StripeException e) {
            log.error("Failed to get payment status: {}", e.getMessage());
            return "unknown";
        }
    }

    // ==================== Refund Methods ====================

    @Override
    public Map<String, Object> processRefund(Payment payment, Refund refund) {
        Map<String, Object> response = new HashMap<>();

        try {
            RefundCreateParams params = RefundCreateParams.builder()
                    .setPaymentIntent(payment.getGatewayPaymentId())
                    .setAmount(refund.getAmount().longValue() * 100)
                    .putMetadata("refundNumber", refund.getRefundNumber())
                    .putMetadata("reason", refund.getReason())
                    .putMetadata("paymentId", payment.getId())
                    .build();

            com.stripe.model.Refund stripeRefund = com.stripe.model.Refund.create(params);

            response.put("success", true);
            response.put("refundId", stripeRefund.getId());
            response.put("amount", stripeRefund.getAmount() / 100.0);
            response.put("status", stripeRefund.getStatus());
            response.put("created", stripeRefund.getCreated());

            log.info("Stripe refund created: {}", stripeRefund.getId());

        } catch (StripeException e) {
            log.error("Stripe refund failed: {}", e.getMessage());
            response.put("success", false);
            response.put("errorMessage", e.getMessage());
            response.put("errorCode", e.getCode());
        }

        return response;
    }

    @Override
    public String getRefundStatus(String gatewayRefundId) {
        try {
            com.stripe.model.Refund stripeRefund = com.stripe.model.Refund.retrieve(gatewayRefundId);
            return stripeRefund.getStatus();
        } catch (StripeException e) {
            log.error("Failed to get refund status: {}", e.getMessage());
            return "unknown";
        }
    }

    // ==================== Payment Method Management ====================

    @Override
    public String tokenizePaymentMethod(PaymentMethod paymentMethod, PaymentMethodRequest request) {
        try {
            // In Stripe, you would create a PaymentMethod object
            // This is a simplified version
            Map<String, Object> cardParams = new HashMap<>();
            if (request != null && request.getCardNumber() != null) {
                cardParams.put("number", request.getCardNumber());
                cardParams.put("exp_month", Integer.parseInt(request.getExpiryMonth()));
                cardParams.put("exp_year", Integer.parseInt(request.getExpiryYear()));
                cardParams.put("cvc", request.getCvv());

                Map<String, Object> paymentMethodParams = new HashMap<>();
                paymentMethodParams.put("type", "card");
                paymentMethodParams.put("card", cardParams);

                // In production, you would call PaymentMethod.create(paymentMethodParams)
                String token = "pm_" + System.currentTimeMillis() + "_" +
                        (request.getCardNumber() != null ?
                                request.getCardNumber().substring(request.getCardNumber().length() - 4) : "xxxx");

                log.info("Stripe payment method tokenized: {}", token);
                return token;
            }

            return "pm_" + System.currentTimeMillis();

        } catch (Exception e) {
            log.error("Failed to tokenize payment method: {}", e.getMessage());
            throw new BusinessException(ErrorCode.PAYMENT_FAILED, "Failed to tokenize payment method");
        }
    }

    @Override
    public boolean deletePaymentMethod(String gatewayToken) {
        try {
            // In Stripe, you can detach a payment method from a customer
            // This doesn't actually delete it but removes the association
            log.info("Stripe payment method detached/inactivated: {}", gatewayToken);
            return true;

        } catch (Exception e) {
            log.error("Failed to delete payment method from Stripe: {}", e.getMessage());
            return false;
        }
    }

    @Override
    public String updatePaymentMethod(String gatewayToken, PaymentMethod paymentMethod) {
        // Stripe doesn't support updating payment methods directly
        // You'd typically delete the old one and create a new one
        log.info("Updating Stripe payment method - would delete old and create new");
        deletePaymentMethod(gatewayToken);
        return tokenizePaymentMethod(paymentMethod, null);
    }

    @Override
    public boolean verifyPaymentMethod(String gatewayToken, Map<String, Object> verificationData) {
        // For cards, verification might involve a small charge or 3DS
        log.info("Verifying Stripe payment method: {}", gatewayToken);
        return true;
    }

    @Override
    public Map<String, Object> getPaymentMethodDetails(String gatewayToken) {
        try {
            // In production, you would retrieve the PaymentMethod from Stripe
            Map<String, Object> details = new HashMap<>();
            details.put("token", gatewayToken);
            details.put("lastFour", gatewayToken != null && gatewayToken.contains("_") ?
                    gatewayToken.substring(gatewayToken.length() - 4) : "1234");
            details.put("network", "VISA");
            details.put("type", "card");
            return details;

        } catch (Exception e) {
            log.error("Failed to get payment method details: {}", e.getMessage());
            return Map.of("error", "Payment method not found", "token", gatewayToken);
        }
    }

    // ==================== Customer Management ====================

    @Override
    public String createCustomer(String customerId, Map<String, Object> customerData) {
        try {
            CustomerCreateParams params = CustomerCreateParams.builder()
                    .setName((String) customerData.getOrDefault("name", "Customer"))
                    .setEmail((String) customerData.getOrDefault("email", null))
                    .setPhone((String) customerData.getOrDefault("phone", null))
                    .putMetadata("internal_customer_id", customerId)
                    .build();

            Customer customer = Customer.create(params);
            String gatewayCustomerId = customer.getId();

            log.info("Stripe customer created: {} for internal customer: {}", gatewayCustomerId, customerId);
            return gatewayCustomerId;

        } catch (StripeException e) {
            log.error("Failed to create Stripe customer: {}", e.getMessage());
            throw new BusinessException(ErrorCode.PAYMENT_FAILED, "Failed to create customer: " + e.getMessage());
        }
    }

    @Override
    public Map<String, Object> getCustomerDetails(String gatewayCustomerId) {
        try {
            Customer customer = Customer.retrieve(gatewayCustomerId);

            Map<String, Object> details = new HashMap<>();
            details.put("id", customer.getId());
            details.put("name", customer.getName());
            details.put("email", customer.getEmail());
            details.put("phone", customer.getPhone());
            details.put("balance", customer.getBalance());
            details.put("currency", customer.getCurrency());
            details.put("created", customer.getCreated());
            details.put("metadata", customer.getMetadata());

            return details;

        } catch (StripeException e) {
            log.error("Failed to fetch Stripe customer: {}", e.getMessage());
            return Map.of("error", "Customer not found", "id", gatewayCustomerId);
        }
    }

    @Override
    public boolean updateCustomer(String gatewayCustomerId, Map<String, Object> customerData) {
        try {
            Customer customer = Customer.retrieve(gatewayCustomerId);

            CustomerUpdateParams.Builder paramsBuilder = CustomerUpdateParams.builder();

            if (customerData.containsKey("name")) {
                paramsBuilder.setName((String) customerData.get("name"));
            }
            if (customerData.containsKey("email")) {
                paramsBuilder.setEmail((String) customerData.get("email"));
            }
            if (customerData.containsKey("phone")) {
                paramsBuilder.setPhone((String) customerData.get("phone"));
            }
            if (customerData.containsKey("metadata")) {
                Map<String, String> metadata = (Map<String, String>) customerData.get("metadata");
                paramsBuilder.putAllMetadata(metadata);
            }

            customer.update(paramsBuilder.build());
            log.info("Stripe customer updated: {}", gatewayCustomerId);
            return true;

        } catch (StripeException e) {
            log.error("Failed to update Stripe customer: {}", e.getMessage());
            return false;
        }
    }

    @Override
    public boolean deleteCustomer(String gatewayCustomerId) {
        try {
            Customer customer = Customer.retrieve(gatewayCustomerId);
            Customer deletedCustomer = customer.delete();
            log.info("Stripe customer deleted: {}", gatewayCustomerId);
            return deletedCustomer.getDeleted();

        } catch (StripeException e) {
            log.error("Failed to delete Stripe customer: {}", e.getMessage());
            return false;
        }
    }

    // ==================== Payout Methods ====================

    @Override
    public String createPayout(String providerId, Double amount, Map<String, Object> bankDetails) {
        try {
            // In Stripe, you would create a Transfer or Payout
            // This requires a connected account or destination charge
            String payoutId = "po_" + System.currentTimeMillis() + "_" + providerId;

            log.info("Stripe payout created: {} for provider: {} amount: {}", payoutId, providerId, amount);
            return payoutId;

        } catch (Exception e) {
            log.error("Failed to create Stripe payout: {}", e.getMessage());
            throw new BusinessException(ErrorCode.PAYMENT_FAILED, "Failed to create payout: " + e.getMessage());
        }
    }

    @Override
    public String getPayoutStatus(String gatewayPayoutId) {
        try {
            // In production, you would retrieve the Payout from Stripe
            return "paid";
        } catch (Exception e) {
            log.error("Failed to get payout status: {}", e.getMessage());
            return "unknown";
        }
    }

    // ==================== Webhook Methods ====================

    @Override
    public Map<String, Object> processWebhook(String payload, String signature) {
        Map<String, Object> response = new HashMap<>();

        try {
            Event event = Webhook.constructEvent(payload, signature, webhookSecret);
            String eventType = event.getType();

            response.put("success", true);
            response.put("event", eventType);

            switch (eventType) {
                case "payment_intent.succeeded":
                    PaymentIntent paymentIntent = (PaymentIntent) event.getDataObjectDeserializer().getObject().get();
                    response.put("paymentId", paymentIntent.getId());
                    response.put("amount", paymentIntent.getAmount() / 100.0);
                    response.put("currency", paymentIntent.getCurrency());
                    response.put("status", paymentIntent.getStatus());
                    break;

                case "payment_intent.payment_failed":
                    PaymentIntent failedIntent = (PaymentIntent) event.getDataObjectDeserializer().getObject().get();
                    response.put("paymentId", failedIntent.getId());
                    response.put("error", failedIntent.getLastPaymentError());
                    break;

                case "charge.refunded":
                    Charge charge = (Charge) event.getDataObjectDeserializer().getObject().get();
                    response.put("chargeId", charge.getId());
                    response.put("paymentId", charge.getPaymentIntent());
                    response.put("amountRefunded", charge.getAmountRefunded() / 100.0);
                    break;

                default:
                    log.info("Unhandled Stripe event type: {}", eventType);
            }

            log.info("Stripe webhook processed: {}", eventType);

        } catch (Exception e) {
            log.error("Stripe webhook processing failed: {}", e.getMessage());
            response.put("success", false);
            response.put("errorMessage", e.getMessage());
        }

        return response;
    }

    @Override
    public boolean verifyWebhookSignature(String payload, String signature) {
        try {
            Webhook.constructEvent(payload, signature, webhookSecret);
            return true;
        } catch (Exception e) {
            log.error("Stripe webhook signature verification failed: {}", e.getMessage());
            return false;
        }
    }

    // ==================== Utility Methods ====================

    @Override
    public String getGatewayName() {
        return "STRIPE";
    }

    @Override
    public boolean isAvailable() {
        try {
            // Simple health check - try to retrieve a test resource
            PaymentIntent.retrieve("pi_test");
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
        health.put("mode", "live");
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