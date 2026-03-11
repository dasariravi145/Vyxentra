package com.vyxentra.vehicle.gateway;


import com.vyxentra.vehicle.exceptions.BusinessException;
import com.vyxentra.vehicle.exceptions.ErrorCode;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;


import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Component
public class PaymentGatewayFactory {

    private final Map<String, PaymentGateway> gatewayMap = new ConcurrentHashMap<>();
    private String defaultGateway;

    private final RazorpayGateway razorpayGateway;
    private final StripeGateway stripeGateway;
    private final PayUGateway payUGateway;

    public PaymentGatewayFactory(
            RazorpayGateway razorpayGateway,
            StripeGateway stripeGateway,
            PayUGateway payUGateway) {
        this.razorpayGateway = razorpayGateway;
        this.stripeGateway = stripeGateway;
        this.payUGateway = payUGateway;
    }

    @Value("${payment.gateway.default:RAZORPAY}")
    private String defaultGatewayName;

    @PostConstruct
    public void init() {
        // Register all available gateways
        registerGateway("RAZORPAY", razorpayGateway);
        registerGateway("STRIPE", stripeGateway);
        registerGateway("PAYU", payUGateway);

        // Set default gateway
        this.defaultGateway = defaultGatewayName;

        log.info("PaymentGatewayFactory initialized with gateways: {}, default: {}",
                gatewayMap.keySet(), defaultGateway);
    }

    /**
     * Register a payment gateway implementation
     *
     * @param gatewayName The name of the gateway (e.g., "RAZORPAY", "STRIPE")
     * @param gateway The gateway implementation
     */
    public void registerGateway(String gatewayName, PaymentGateway gateway) {
        if (gatewayName == null || gatewayName.trim().isEmpty()) {
            throw new IllegalArgumentException("Gateway name cannot be null or empty");
        }
        if (gateway == null) {
            throw new IllegalArgumentException("Gateway implementation cannot be null");
        }

        gatewayMap.put(gatewayName.toUpperCase(), gateway);
        log.debug("Registered payment gateway: {}", gatewayName);
    }

    /**
     * Get payment gateway by name
     *
     * @param gatewayName The name of the gateway
     * @return The gateway implementation
     * @throws com.vyxentra.vehicle.exceptions.BusinessException if gateway not found
     */
    public PaymentGateway getGateway(String gatewayName) {
        if (gatewayName == null || gatewayName.trim().isEmpty()) {
            return getDefaultGateway();
        }

        PaymentGateway gateway = gatewayMap.get(gatewayName.toUpperCase());
        if (gateway == null) {
            log.error("Payment gateway not found: {}, available gateways: {}",
                    gatewayName, gatewayMap.keySet());
            throw new BusinessException(ErrorCode.PAYMENT_GATEWAY_NOT_FOUND,
                    "Payment gateway not found: " + gatewayName);
        }

        log.debug("Retrieved payment gateway: {}", gatewayName);
        return gateway;
    }

    /**
     * Get the default payment gateway
     *
     * @return The default gateway implementation
     * @throws BusinessException if default gateway not found
     */
    public PaymentGateway getDefaultGateway() {
        if (defaultGateway == null) {
            defaultGateway = "RAZORPAY"; // Fallback default
        }

        PaymentGateway gateway = gatewayMap.get(defaultGateway.toUpperCase());
        if (gateway == null) {
            log.error("Default payment gateway not found: {}, available gateways: {}",
                    defaultGateway, gatewayMap.keySet());
            throw new BusinessException(ErrorCode.PAYMENT_GATEWAY_NOT_FOUND,
                    "Default payment gateway not found: " + defaultGateway);
        }

        return gateway;
    }

    /**
     * Set the default payment gateway
     *
     * @param gatewayName The name of the gateway to set as default
     */
    public void setDefaultGateway(String gatewayName) {
        if (!gatewayMap.containsKey(gatewayName.toUpperCase())) {
            throw new BusinessException(ErrorCode.PAYMENT_GATEWAY_NOT_FOUND,
                    "Cannot set default gateway. Gateway not found: " + gatewayName);
        }
        this.defaultGateway = gatewayName.toUpperCase();
        log.info("Default payment gateway set to: {}", defaultGateway);
    }

    /**
     * Get all registered gateway names
     *
     * @return Set of gateway names
     */
    public java.util.Set<String> getRegisteredGateways() {
        return gatewayMap.keySet();
    }

    /**
     * Check if a gateway is registered
     *
     * @param gatewayName The name of the gateway
     * @return true if registered
     */
    public boolean isGatewayRegistered(String gatewayName) {
        return gatewayMap.containsKey(gatewayName.toUpperCase());
    }

    /**
     * Get gateway health status
     *
     * @return Map of gateway health statuses
     */
    public Map<String, Object> getGatewayHealth() {
        Map<String, Object> health = new HashMap<>();

        for (Map.Entry<String, PaymentGateway> entry : gatewayMap.entrySet()) {
            try {
                PaymentGateway gateway = entry.getValue();
                health.put(entry.getKey(), Map.of(
                        "status", gateway.isAvailable() ? "UP" : "DOWN",
                        "name", gateway.getGatewayName()
                ));
            } catch (Exception e) {
                health.put(entry.getKey(), Map.of(
                        "status", "DOWN",
                        "error", e.getMessage()
                ));
            }
        }

        health.put("default", defaultGateway);

        return health;
    }

    /**
     * Reload gateway configuration
     */
    public void reloadGateways() {
        log.info("Reloading payment gateways");
        // Clear and re-register
        gatewayMap.clear();
        init();
    }
}