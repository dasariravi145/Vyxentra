package com.vyxentra.vehicle.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.Instant;
import java.util.Map;

@Entity
@Table(name = "payment_methods", indexes = {
        @Index(name = "idx_pm_user", columnList = "user_id"),
        @Index(name = "idx_pm_token", columnList = "gateway_token"),
        @Index(name = "idx_pm_type", columnList = "method_type"),
        @Index(name = "idx_pm_default", columnList = "is_default"),
        @Index(name = "idx_pm_active", columnList = "is_active")
})
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EntityListeners(AuditingEntityListener.class)
public class PaymentMethod {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @Column(name = "user_id", nullable = false)
    private String userId;

    @Column(name = "user_type", nullable = false)
    private String userType; // CUSTOMER, PROVIDER, EMPLOYEE

    @Column(name = "method_type", nullable = false)
    private String methodType; // CARD, UPI, NETBANKING, WALLET

    @Column(name = "display_name")
    private String displayName;

    // ==================== Card Specific Fields ====================

    @Column(name = "last_four")
    private String lastFour;

    @Column(name = "card_type")
    private String cardType; // CREDIT, DEBIT, PREPAID

    @Column(name = "card_network")
    private String cardNetwork; // VISA, MASTERCARD, RUPAY, AMEX, DISCOVER, DINERS, JCB

    @Column(name = "expiry_month")
    private String expiryMonth;

    @Column(name = "expiry_year")
    private String expiryYear;

    @Column(name = "card_holder_name")
    private String cardHolderName;

    @Column(name = "card_issuer")
    private String cardIssuer; // Bank name from BIN lookup

    @Column(name = "card_issuer_country")
    private String cardIssuerCountry;

    @Column(name = "is_international")
    private Boolean isInternational;

    @Column(name = "is_corporate")
    private Boolean isCorporate;

    // ==================== UPI Specific Fields ====================

    @Column(name = "vpa")
    private String vpa; // Virtual Payment Address (e.g., user@okhdfcbank)

    @Column(name = "upi_id")
    private String upiId; // Same as VPA, for compatibility

    @Column(name = "upi_app_name")
    private String upiAppName; // Google Pay, PhonePe, Paytm, etc.

    // ==================== Bank Account Specific Fields ====================

    @Column(name = "bank_name")
    private String bankName;

    @Column(name = "account_number")
    private String accountNumber;

    @Column(name = "ifsc_code")
    private String ifscCode;

    @Column(name = "account_type")
    private String accountType; // SAVINGS, CURRENT, SALARY

    @Column(name = "account_holder_name")
    private String accountHolderName;

    // ==================== Status Fields ====================

    @Column(name = "is_default")
    private Boolean isDefault;

    @Column(name = "is_active")
    private Boolean isActive;

    @Column(name = "is_verified")
    private Boolean isVerified;

    @Column(name = "verified_at")
    private Instant verifiedAt;

    @Column(name = "verification_method")
    private String verificationMethod; // OTP, MICRO_DEPOSIT, INSTANT

    @Column(name = "is_expired")
    private Boolean isExpired;

    @Column(name = "is_failing")
    private Boolean isFailing; // Flag for methods that are failing frequently

    @Column(name = "failure_count")
    private Integer failureCount;

    @Column(name = "last_failure_reason")
    private String lastFailureReason;

    @Column(name = "last_failure_at")
    private Instant lastFailureAt;

    // ==================== Gateway Fields ====================

    @Column(name = "gateway_token", unique = true)
    private String gatewayToken;

    @Column(name = "gateway_customer_id")
    private String gatewayCustomerId;

    @Column(name = "gateway_payment_method_id")
    private String gatewayPaymentMethodId;

    @Column(name = "gateway_response", length = 4000)
    private String gatewayResponse; // Store raw gateway response

    // ==================== BIN Lookup Fields ====================

    @Column(name = "bin_number")
    private String binNumber; // First 6 digits of card

    @Column(name = "bin_data", length = 4000)
    private String binData; // JSON string of BIN lookup data

    // ==================== Risk/Fraud Fields ====================

    @Column(name = "risk_level")
    private String riskLevel; // LOW, MEDIUM, HIGH

    @Column(name = "risk_score")
    private Integer riskScore;

    @Column(name = "fraud_flags")
    private String fraudFlags; // Comma-separated flags

    // ==================== 3DS Fields ====================

    @Column(name = "is_3ds_enabled")
    private Boolean is3dsEnabled;

    @Column(name = "three_ds_version")
    private String threeDsVersion;

    // ==================== Metadata ====================

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(columnDefinition = "jsonb")
    private Map<String, Object> metadata; // For any additional data

    // ==================== Usage Tracking ====================

    @Column(name = "successful_payments")
    private Integer successfulPayments;

    @Column(name = "failed_payments")
    private Integer failedPayments;

    @Column(name = "last_used_at")
    private Instant lastUsedAt;

    @Column(name = "last_success_at")
    private Instant lastSuccessAt;

    // ==================== Audit Timestamps ====================

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @LastModifiedDate
    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    // ==================== Helper Methods ====================

    @PrePersist
    protected void onCreate() {
        if (isActive == null) isActive = true;
        if (isDefault == null) isDefault = false;
        if (isVerified == null) isVerified = false;
        if (isExpired == null) isExpired = false;
        if (isFailing == null) isFailing = false;
        if (failureCount == null) failureCount = 0;
        if (successfulPayments == null) successfulPayments = 0;
        if (failedPayments == null) failedPayments = 0;
        if (riskLevel == null) riskLevel = "LOW";
        if (riskScore == null) riskScore = 0;
        if (is3dsEnabled == null) is3dsEnabled = false;
        if (isInternational == null) isInternational = false;
        if (isCorporate == null) isCorporate = false;
    }

    /**
     * Get masked display string for UI
     */
    public String getMaskedDisplayValue() {
        if ("CARD".equals(methodType)) {
            return (cardNetwork != null ? cardNetwork : "Card") + " •••• " + lastFour;
        } else if ("UPI".equals(methodType)) {
            if (vpa != null && vpa.contains("@")) {
                String[] parts = vpa.split("@");
                return "xxxx@" + parts[1];
            }
            return vpa;
        } else if ("NETBANKING".equals(methodType)) {
            return bankName;
        }
        return displayName;
    }

    /**
     * Check if card is expired
     */
    public boolean isCardExpired() {
        if (!"CARD".equals(methodType) || expiryYear == null || expiryMonth == null) {
            return false;
        }

        try {
            int expYear = Integer.parseInt(expiryYear);
            int expMonth = Integer.parseInt(expiryMonth);

            java.time.YearMonth expiry = java.time.YearMonth.of(expYear, expMonth);
            return expiry.isBefore(java.time.YearMonth.now());
        } catch (NumberFormatException e) {
            return true;
        }
    }

    /**
     * Check if card is expiring soon (within 2 months)
     */
    public boolean isExpiringSoon() {
        if (!"CARD".equals(methodType) || expiryYear == null || expiryMonth == null) {
            return false;
        }

        try {
            int expYear = Integer.parseInt(expiryYear);
            int expMonth = Integer.parseInt(expiryMonth);

            java.time.YearMonth expiry = java.time.YearMonth.of(expYear, expMonth);
            java.time.YearMonth twoMonthsLater = java.time.YearMonth.now().plusMonths(2);

            return expiry.isBefore(twoMonthsLater) && !expiry.isBefore(java.time.YearMonth.now());
        } catch (NumberFormatException e) {
            return false;
        }
    }

    /**
     * Increment failure count
     */
    public void recordFailure(String reason) {
        this.failureCount = (this.failureCount == null ? 1 : this.failureCount + 1);
        this.lastFailureReason = reason;
        this.lastFailureAt = Instant.now();

        if (this.failureCount >= 5) {
            this.isFailing = true;
        }
    }

    /**
     * Record successful payment
     */
    public void recordSuccess() {
        this.successfulPayments = (this.successfulPayments == null ? 1 : this.successfulPayments + 1);
        this.lastSuccessAt = Instant.now();
        this.lastUsedAt = Instant.now();
        this.failureCount = 0;
        this.isFailing = false;
    }

    /**
     * Get BIN number from card number (first 6 digits)
     */
    public void setBinFromCardNumber(String cardNumber) {
        if (cardNumber != null && cardNumber.length() >= 6) {
            this.binNumber = cardNumber.substring(0, 6);
        }
    }

    /**
     * Builder with convenience methods
     */
    public static class PaymentMethodBuilder {

        public PaymentMethodBuilder fromCard(String cardNumber, String expiryMonth,
                                             String expiryYear, String cardHolderName) {
            this.lastFour = cardNumber != null && cardNumber.length() >= 4 ?
                    cardNumber.substring(cardNumber.length() - 4) : null;
            this.binNumber = cardNumber != null && cardNumber.length() >= 6 ?
                    cardNumber.substring(0, 6) : null;
            this.expiryMonth = expiryMonth;
            this.expiryYear = expiryYear;
            this.cardHolderName = cardHolderName;
            this.methodType = "CARD";
            return this;
        }

        public PaymentMethodBuilder fromUpi(String upiId) {
            this.upiId = upiId;
            this.vpa = upiId;
            this.methodType = "UPI";

            // Detect UPI app
            if (upiId != null && upiId.contains("@")) {
                String domain = upiId.split("@")[1].toLowerCase();
                if (domain.contains("okhdfcbank") || domain.contains("okicici") ||
                        domain.contains("oksbi") || domain.contains("okaxis")) {
                    this.upiAppName = "Google Pay";
                } else if (domain.contains("ybl")) {
                    this.upiAppName = "PhonePe";
                } else if (domain.contains("paytm")) {
                    this.upiAppName = "Paytm";
                } else if (domain.contains("amazon")) {
                    this.upiAppName = "Amazon Pay";
                } else if (domain.contains("ibl")) {
                    this.upiAppName = "iMobile";
                } else {
                    this.upiAppName = "Other UPI App";
                }
            }
            return this;
        }

        public PaymentMethodBuilder fromBankAccount(String accountNumber, String ifscCode,
                                                    String accountHolderName, String accountType) {
            this.accountNumber = accountNumber;
            this.ifscCode = ifscCode;
            this.accountHolderName = accountHolderName;
            this.accountType = accountType;
            this.methodType = "NETBANKING";
            return this;
        }
    }
}
