package com.vyxentra.vehicle.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PaymentMethodRequest {

    @NotBlank(message = "Method type is required")
    private String methodType; // CARD, UPI, NETBANKING

    // Card details
    @Pattern(regexp = "^[0-9]{16}$", message = "Invalid card number")
    private String cardNumber;

    private String cardHolderName;

    @Pattern(regexp = "^(0[1-9]|1[0-2])$", message = "Invalid expiry month")
    private String expiryMonth;

    @Pattern(regexp = "^[0-9]{4}$", message = "Invalid expiry year")
    private String expiryYear;

    private String cvv; // Not stored, only used for validation

    // UPI details
    @Pattern(regexp = "^[a-zA-Z0-9._-]+@[a-zA-Z0-9]+$", message = "Invalid UPI ID")
    private String upiId;

    // Bank account details
    private String accountNumber;
    private String ifscCode;
    private String accountHolderName;
    private String bankName;
    private String accountType; // SAVINGS, CURRENT

    // Common
    private Boolean isDefault;
    private Boolean saveForFuture;
    private String billingAddress;
    private String billingZipCode;
    private String billingCity;
    private String billingCountry;

    // Device info for fraud detection
    private String deviceId;
    private String ipAddress;
    private String userAgent;

    // Metadata
    private Map<String, Object> metadata;
}
