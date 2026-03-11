package com.vyxentra.vehicle.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.vyxentra.vehicle.dto.BankAccountDetails;
import com.vyxentra.vehicle.dto.BookingSummary;
import com.vyxentra.vehicle.dto.PayoutSummary;
import com.vyxentra.vehicle.dto.TaxDetails;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

/**
 * Response DTO for provider payouts
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class PayoutResponse {

    private String payoutId;
    private String payoutNumber;
    private String providerId;
    private String providerName;
    private String providerEmail;
    private String providerPhone;

    private String failedAt;

    // Amount details
    private Double totalAmount;
    private Double commissionDeducted;
    private Double taxDeducted;
    private Double netAmount;
    private String currency;

    // Period details
    private LocalDate periodStart;
    private LocalDate periodEnd;
    private String periodDisplay;

    // Booking details
    private Integer bookingCount;
    private List<String> bookingIds;
    private List<BookingSummary> bookings;

    // Status
    private String status; // PENDING, PROCESSING, SUCCESS, FAILED
    private String statusDescription;

    // Payment details
    private String paymentMethod;
    private String paymentMethodDisplay;
    private BankAccountDetails bankAccount;

    // Gateway details
    private String gatewayName;
    private String gatewayPayoutId;
    private String gatewayReference;
    private String gatewayResponse;

    // Timestamps
    private Instant requestedAt;
    private Instant processedAt;
    private Instant completedAt;
    private Instant createdAt;
    private Instant updatedAt;

    // Settlement details
    private Instant estimatedSettlementDate;
    private Instant actualSettlementDate;
    private String settlementReference;
    private String settlementStatus;

    // Fee details
    private Double gatewayFee;
    private Double processingFee;
    private Double totalDeductions;

    // Failure details
    private Boolean isFailed;
    private String failureReason;
    private String failureCode;
    private String failureDetails;

    // Retry details
    private Integer retryCount;
    private Boolean canRetry;
    private Instant nextRetryAt;

    // Tax details
    private TaxDetails taxDetails;

    // Metadata
    private Map<String, Object> metadata;
    private List<String> tags;

    // Summary
    private PayoutSummary summary;

    // ==================== Helper Methods ====================

    /**
     * Check if payout was successful
     */
    public boolean isSuccessful() {
        return "SUCCESS".equalsIgnoreCase(status);
    }

    /**
     * Check if payout is pending
     */
    public boolean isPending() {
        return "PENDING".equalsIgnoreCase(status) || "PROCESSING".equalsIgnoreCase(status);
    }

    /**
     * Check if payout failed
     */
    public boolean isFailed() {
        return "FAILED".equalsIgnoreCase(status) || Boolean.TRUE.equals(isFailed);
    }

    /**
     * Get display amount
     */
    public String getFormattedAmount() {
        String currencySymbol = "INR".equals(currency) ? "₹" : currency;
        return currencySymbol + " " + String.format("%.2f", netAmount != null ? netAmount : totalAmount);
    }

    /**
     * Get period display string
     */
    public String getFormattedPeriod() {
        if (periodStart != null && periodEnd != null) {
            return periodStart.toString() + " to " + periodEnd.toString();
        }
        return periodDisplay;
    }

    /**
     * Builder with convenience methods
     */
    public static class PayoutResponseBuilder {

        public PayoutResponseBuilder fromSuccessfulPayout(String payoutId, String providerId, Double netAmount) {
            this.payoutId = payoutId;
            this.providerId = providerId;
            this.netAmount = netAmount;
            this.status = "SUCCESS";
            this.processedAt = Instant.now();
            return this;
        }

        public PayoutResponseBuilder fromFailedPayout(String payoutId, String providerId, String reason) {
            this.payoutId = payoutId;
            this.providerId = providerId;
            this.status = "FAILED";
            this.isFailed = true;
            this.failureReason = reason;
            this.failedAt = String.valueOf(Instant.now());
            return this;
        }

        public PayoutResponseBuilder withBookingSummary(List<BookingSummary> bookings) {
            this.bookings = bookings;
            this.bookingCount = bookings != null ? bookings.size() : 0;
            this.totalAmount = bookings != null ?
                    bookings.stream().mapToDouble(BookingSummary::getAmount).sum() : 0.0;
            return this;
        }
    }
}
