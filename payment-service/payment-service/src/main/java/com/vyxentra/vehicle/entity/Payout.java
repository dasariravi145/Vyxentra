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
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@Entity
@Table(name = "payouts", indexes = {
        @Index(name = "idx_payout_provider", columnList = "provider_id"),
        @Index(name = "idx_payout_status", columnList = "status"),
        @Index(name = "idx_payout_number", columnList = "payout_number"),
        @Index(name = "idx_payout_period", columnList = "period_start, period_end"),
        @Index(name = "idx_payout_gateway", columnList = "gateway_payout_id")
})
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EntityListeners(AuditingEntityListener.class)
public class Payout {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @Column(name = "payout_number", nullable = false, unique = true)
    private String payoutNumber;

    @Column(name = "provider_id", nullable = false)
    private String providerId;

    @Column(name = "provider_name")
    private String providerName;

    // ==================== Amount Fields ====================

    @Column(name = "total_amount", nullable = false)
    private Double totalAmount;

    @Column(name = "commission_deducted")
    private Double commissionDeducted;

    @Column(name = "tax_deducted")
    private Double taxDeducted;

    @Column(name = "processing_fee")
    private Double processingFee;

    @Column(name = "gateway_fee")
    private Double gatewayFee;

    @Column(name = "net_amount", nullable = false)
    private Double netAmount;

    @Column(name = "currency", length = 3)
    private String currency;

    // ==================== Period Fields ====================

    @Column(name = "period_start", nullable = false)
    private LocalDate periodStart;

    @Column(name = "period_end", nullable = false)
    private LocalDate periodEnd;

    // ==================== Booking Fields ====================

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "booking_ids", columnDefinition = "jsonb")
    private List<String> bookingIds;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "booking_details", columnDefinition = "jsonb")
    private Map<String, Object> bookingDetails;

    // ==================== Status Fields ====================

    @Column(nullable = false)
    private String status; // PENDING, PROCESSING, SUCCESS, FAILED, CANCELLED

    @Column(name = "payment_method")
    private String paymentMethod; // BANK_TRANSFER, UPI, CHEQUE, CASH

    // ==================== Bank/UPI Details ====================

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "account_details", columnDefinition = "jsonb")
    private Map<String, Object> accountDetails;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "upi_details", columnDefinition = "jsonb")
    private Map<String, Object> upiDetails;

    // ==================== Gateway Fields ====================

    @Column(name = "gateway_payout_id")
    private String gatewayPayoutId;

    @Column(name = "gateway_reference")
    private String gatewayReference;

    @Column(name = "gateway_response", length = 4000)
    private String gatewayResponse;

    // ==================== Timestamp Fields ====================

    @Column(name = "requested_at")
    private Instant requestedAt;

    @Column(name = "processed_at")
    private Instant processedAt;

    @Column(name = "completed_at")
    private Instant completedAt;

    // ==================== Settlement Fields ====================

    @Column(name = "settlement_status")
    private String settlementStatus; // PENDING, SETTLED, FAILED

    @Column(name = "settlement_reference")
    private String settlementReference;

    @Column(name = "estimated_settlement_date")
    private LocalDate estimatedSettlementDate;

    @Column(name = "actual_settlement_date")
    private LocalDate actualSettlementDate;

    // ==================== Failure Fields ====================

    @Column(name = "failure_reason", length = 1000)
    private String failureReason;

    @Column(name = "failure_code")
    private String failureCode;

    // ==================== Retry Fields ====================

    @Column(name = "retry_count")
    private Integer retryCount;

    @Column(name = "max_retry_count")
    private Integer maxRetryCount;

    // ==================== Priority Fields ====================

    private Integer priority; // 1-Highest, 5-Lowest

    // ==================== Notes Fields ====================

    @Column(length = 1000)
    private String notes;

    @Column(name = "admin_notes", length = 1000)
    private String adminNotes;

    // ==================== Metadata Fields ====================

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(columnDefinition = "jsonb")
    private Map<String, Object> metadata;

    // ==================== Audit Fields ====================

    @Column(name = "created_by")
    private String createdBy;

    @Column(name = "updated_by")
    private String updatedBy;

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @LastModifiedDate
    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;
    private Double amount;
    // ==================== Helper Methods ====================

    @PrePersist
    protected void onCreate() {
        if (retryCount == null) retryCount = 0;
        if (maxRetryCount == null) maxRetryCount = 3;
        if (priority == null) priority = 3;
        if (currency == null) currency = "INR";
        if (status == null) status = "PENDING";
    }

    /**
     * Check if payout is successful
     */
    public boolean isSuccessful() {
        return "SUCCESS".equals(status);
    }

    /**
     * Check if payout is pending
     */
    public boolean isPending() {
        return "PENDING".equals(status) || "PROCESSING".equals(status);
    }

    /**
     * Check if payout is failed
     */
    public boolean isFailed() {
        return "FAILED".equals(status);
    }

    /**
     * Check if payout can be retried
     */
    public boolean canRetry() {
        return "FAILED".equals(status) && retryCount < maxRetryCount;
    }

    /**
     * Increment retry count
     */
    public void incrementRetryCount() {
        this.retryCount = (this.retryCount == null ? 1 : this.retryCount + 1);
    }

    /**
     * Add metadata
     */
    public void addMetadata(String key, Object value) {
        if (this.metadata == null) {
            this.metadata = new java.util.HashMap<>();
        }
        this.metadata.put(key, value);
    }

    /**
     * Get metadata value
     */
    public Object getMetadata(String key) {
        if (this.metadata == null) return null;
        return this.metadata.get(key);
    }
}