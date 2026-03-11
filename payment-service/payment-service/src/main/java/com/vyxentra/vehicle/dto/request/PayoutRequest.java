package com.vyxentra.vehicle.dto.request;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.vyxentra.vehicle.dto.BankAccountDetails;
import com.vyxentra.vehicle.dto.BookingDetail;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

/**
 * Request DTO for creating provider payouts
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class PayoutRequest {

    // ==================== Provider Information ====================

    @NotBlank(message = "Provider ID is required")
    private String providerId;

    private String providerName;

    @Email(message = "Invalid email format")
    private String providerEmail;

    @Pattern(regexp = "^\\+?[1-9][0-9]{7,14}$", message = "Invalid phone number")
    private String providerPhone;

    // ==================== Period Information ====================

    @NotNull(message = "Period start date is required")
    private LocalDate periodStart;

    @NotNull(message = "Period end date is required")
    private LocalDate periodEnd;

    // ==================== Booking Information ====================

    private List<String> bookingIds;

    private List<BookingDetail> bookingDetails;

    // ==================== Amount Information ====================

    @NotNull(message = "Total amount is required")
    @Positive(message = "Total amount must be positive")
    private Double totalAmount;

    @Min(value = 0, message = "Commission deducted cannot be negative")
    private Double commissionDeducted;

    @Min(value = 0, message = "Tax deducted cannot be negative")
    private Double taxDeducted;

    @Min(value = 0, message = "Processing fee cannot be negative")
    private Double processingFee;

    @Min(value = 0, message = "Gateway fee cannot be negative")
    private Double gatewayFee;

    @NotNull(message = "Net amount is required")
    @Positive(message = "Net amount must be positive")
    private Double netAmount;

    private String currency;

    // ==================== Commission Details ====================

    private Double commissionPercentage;
    private Double taxPercentage;
    private Double tdsPercentage;
    private Double gstPercentage;

    // ==================== Payment Information ====================

    private String paymentMethod; // BANK_TRANSFER, UPI, CHEQUE, CASH

    @Valid
    private BankAccountDetails bankAccount;

    private UpiDetails upiDetails;

    // ==================== Processing Options ====================

    private Boolean autoProcess; // Automatically process payout after creation

    private Boolean sendNotification;

    private Integer priority; // 1-Highest, 5-Lowest

    private String scheduleType; // IMMEDIATE, SCHEDULED

    private LocalDate scheduledDate;

    // ==================== Additional Information ====================

    private String notes;

    private String reason;

    private String reference;

    private Map<String, Object> metadata;

    private List<String> tags;

    // ==================== Tax Information ====================

    private TaxDetails taxDetails;

    // ==================== Nested Classes ====================




    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class UpiDetails {
        @NotBlank(message = "UPI ID is required")
        @Pattern(regexp = "^[a-zA-Z0-9._-]+@[a-zA-Z0-9]+$", message = "Invalid UPI ID format")
        private String upiId;

        private String vpa;

        private String upiAppName;

        private String qrCodeUrl;

        private Boolean isVerified;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TaxDetails {
        private String panNumber;

        @Pattern(regexp = "^[0-9]{2}[A-Z]{5}[0-9]{4}[A-Z]{1}[1-9A-Z]{1}[Z]{1}[0-9A-Z]{1}$",
                message = "Invalid GST number format")
        private String gstNumber;

        private String tanNumber;

        private Double tdsRate;

        private Double tdsAmount;

        private Double gstRate;

        private Double gstAmount;

        private String taxRegime;

        private Boolean isTaxDeducted;

        private Map<String, Object> taxMetadata;
    }

    // ==================== Validation Methods ====================

    /**
     * Validate that net amount equals total minus deductions
     */
    @AssertTrue(message = "Net amount must equal total amount minus deductions")
    public boolean isValidNetAmount() {
        if (totalAmount == null || netAmount == null) return true; // Skip if null, handled by @NotNull

        double deductions = (commissionDeducted != null ? commissionDeducted : 0) +
                (taxDeducted != null ? taxDeducted : 0) +
                (processingFee != null ? processingFee : 0) +
                (gatewayFee != null ? gatewayFee : 0);

        double calculatedNet = totalAmount - deductions;
        return Math.abs(calculatedNet - netAmount) < 0.01; // Allow small floating point differences
    }

    /**
     * Validate that period start is before period end
     */
    @AssertTrue(message = "Period start must be before period end")
    public boolean isValidPeriod() {
        if (periodStart == null || periodEnd == null) return true;
        return periodStart.isBefore(periodEnd) || periodStart.equals(periodEnd);
    }

    /**
     * Validate that at least one payment method is provided
     */
    @AssertTrue(message = "Either bank account or UPI details must be provided")
    public boolean hasPaymentMethod() {
        return bankAccount != null || upiDetails != null;
    }

    // ==================== Builder Extensions ====================

    /**
     * Builder with convenience methods for common scenarios
     */
    public static class PayoutRequestBuilder {

        public PayoutRequestBuilder forProvider(String providerId, String providerName) {
            this.providerId = providerId;
            this.providerName = providerName;
            return this;
        }

        public PayoutRequestBuilder forPeriod(LocalDate start, LocalDate end) {
            this.periodStart = start;
            this.periodEnd = end;
            return this;
        }

        public PayoutRequestBuilder withAmounts(Double total, Double commission, Double tax, Double net) {
            this.totalAmount = total;
            this.commissionDeducted = commission;
            this.taxDeducted = tax;
            this.netAmount = net;
            return this;
        }

        public PayoutRequestBuilder withBankAccount(String holderName, String bankName,
                                                    String accountNumber, String ifscCode) {
            BankAccountDetails bankDetails = BankAccountDetails.builder()
                    .accountHolderName(holderName)
                    .bankName(bankName)
                    .accountNumber(accountNumber)
                    .ifscCode(ifscCode)
                    .build();
            this.bankAccount = bankDetails;
            return this;
        }

        public PayoutRequestBuilder withUpi(String upiId) {
            UpiDetails upi = UpiDetails.builder()
                    .upiId(upiId)
                    .build();
            this.upiDetails = upi;
            return this;
        }

        public PayoutRequestBuilder withBookings(List<String> bookingIds) {
            this.bookingIds = bookingIds;
            return this;
        }

        public PayoutRequestBuilder autoProcess() {
            this.autoProcess = true;
            return this;
        }

        public PayoutRequestBuilder highPriority() {
            this.priority = 1;
            return this;
        }
    }
}
