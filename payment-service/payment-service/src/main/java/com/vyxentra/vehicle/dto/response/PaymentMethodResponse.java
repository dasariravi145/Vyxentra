package com.vyxentra.vehicle.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class PaymentMethodResponse {

    private String methodId;
    private String userId;
    private String userType; // CUSTOMER, PROVIDER

    private String methodType; // CARD, UPI, NETBANKING
    private String displayName; // Display name for the method

    // Card specific fields
    private String lastFour;
    private String cardType; // CREDIT, DEBIT
    private String cardNetwork; // VISA, MASTERCARD, RUPAY, AMEX
    private String expiryMonth;
    private String expiryYear;
    private String cardHolderName;
    private String cardIssuer;
    private String cardIssuerCountry;
    private Boolean isInternational;

    // UPI specific fields
    private String vpa; // Virtual Payment Address
    private String upiId;
    private String upiAppName; // Google Pay, PhonePe, etc.

    // Bank account specific
    private String bankName;
    private String accountNumber;
    private String ifscCode;
    private String accountType; // SAVINGS, CURRENT

    // Common fields
    private Boolean isDefault;
    private Boolean isActive;
    private Boolean isVerified;
    private Instant verifiedAt;
    private String logoUrl;

    // Metadata
    private String gatewayToken;
    private String gatewayCustomerId;
    private String gatewayPaymentMethodId;

    // Timestamps
    private Instant createdAt;
    private Instant updatedAt;
    private Instant lastUsedAt;

    // Risk/fraud related
    private Boolean isExpired;
    private Boolean is3dsEnabled;
    private String riskLevel; // LOW, MEDIUM, HIGH

    /**
     * Get masked display string for UI
     */
    public String getDisplayValue() {
        if ("CARD".equals(methodType)) {
            return cardNetwork + " •••• " + lastFour;
        } else if ("UPI".equals(methodType)) {
            return vpa;
        } else if ("NETBANKING".equals(methodType)) {
            return bankName;
        }
        return displayName;
    }

    /**
     * Get expiry status
     */
    public boolean isExpiringSoon() {
        if (expiryMonth == null || expiryYear == null) {
            return false;
        }

        int currentYear = java.time.Year.now().getValue();
        int currentMonth = java.time.LocalDate.now().getMonthValue();

        int expYear = Integer.parseInt(expiryYear);
        int expMonth = Integer.parseInt(expiryMonth);

        // If expired
        if (expYear < currentYear || (expYear == currentYear && expMonth < currentMonth)) {
            return true;
        }

        // If expiring within 2 months
        if (expYear == currentYear && (expMonth - currentMonth) <= 2) {
            return true;
        }

        return expYear == currentYear + 1 && expMonth <= 2;
    }

    /**
     * Builder with convenience methods
     */
    public static class PaymentMethodResponseBuilder {

        public PaymentMethodResponseBuilder maskSensitiveData() {
            if (this.accountNumber != null && this.accountNumber.length() > 4) {
                this.accountNumber = "XXXX" + this.accountNumber.substring(this.accountNumber.length() - 4);
            }
            if (this.vpa != null) {
                // Mask part of UPI for security if needed
            }
            return this;
        }

        public PaymentMethodResponseBuilder forCard(String lastFour, String cardNetwork, String cardType) {
            this.lastFour = lastFour;
            this.cardNetwork = cardNetwork;
            this.cardType = cardType;
            this.methodType = "CARD";
            this.displayName = cardNetwork + " •••• " + lastFour;
            return this;
        }

        public PaymentMethodResponseBuilder forUPI(String vpa, String upiAppName) {
            this.vpa = vpa;
            this.upiAppName = upiAppName;
            this.methodType = "UPI";
            this.displayName = vpa;
            return this;
        }

        public PaymentMethodResponseBuilder forNetbanking(String bankName, String accountNumber) {
            this.bankName = bankName;
            this.accountNumber = accountNumber;
            this.methodType = "NETBANKING";
            this.displayName = bankName;
            return this;
        }
    }
}
