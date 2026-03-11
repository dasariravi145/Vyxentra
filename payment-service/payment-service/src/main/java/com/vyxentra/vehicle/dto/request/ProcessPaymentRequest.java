package com.vyxentra.vehicle.dto.request;


import com.vyxentra.vehicle.dto.PaymentMethodDetails;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProcessPaymentRequest {

    @NotBlank(message = "Gateway payment ID is required")
    private String gatewayPaymentId;

    private String gatewayOrderId;

    private String gatewaySignature;

    private String otp; // For card payments requiring OTP

    private String status; // SUCCESS, FAILED

    /**
     * Additional metadata from the payment gateway
     * Can store:
     * - Payment method details (card type, last4, etc.)
     * - Bank details
     * - UPI details
     * - Response parameters
     * - Any gateway-specific data
     */
    private Map<String, Object> metadata;

    /**
     * Payment method details (for tokenized payments)
     */
    private PaymentMethodDetails paymentMethodDetails;

    /**
     * Convenience method to get a metadata value
     */
    public Object getMetadataValue(String key) {
        if (metadata == null) {
            return null;
        }
        return metadata.get(key);
    }

    /**
     * Convenience method to add metadata
     */
    public void addMetadata(String key, Object value) {
        if (metadata == null) {
            metadata = new java.util.HashMap<>();
        }
        metadata.put(key, value);
    }

    /**
     * Check if payment was successful
     */
    public boolean isSuccess() {
        return "SUCCESS".equalsIgnoreCase(status);
    }

    /**
     * Check if payment failed
     */
    public boolean isFailed() {
        return "FAILED".equalsIgnoreCase(status);
    }


}
