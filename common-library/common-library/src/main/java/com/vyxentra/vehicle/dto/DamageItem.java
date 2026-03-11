package com.vyxentra.vehicle.dto;


import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Data Transfer Object for damage item information
 * Used across services (booking, notification, admin) for damage reporting and approval
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class DamageItem implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * Unique identifier for the damage item
     */
    private String itemId;

    /**
     * Reference to the damage report
     */
    private String damageReportId;

    /**
     * Name/type of the damaged item (e.g., "Cylinder Head", "Valve Seal")
     */
    private String itemName;

    /**
     * Detailed description of the damage
     */
    private String description;

    /**
     * Estimated cost for repair/replacement
     */
    private BigDecimal estimatedCost;

    /**
     * Approved cost after customer approval/negotiation
     */
    private BigDecimal approvedCost;

    /**
     * Whether this damage item is approved by customer
     */
    private Boolean isApproved;

    /**
     * Whether this damage item is rejected by customer
     */
    private Boolean isRejected;

    /**
     * Status of the damage item (PENDING, APPROVED, REJECTED, PARTIALLY_APPROVED)
     */
    private String status;

    /**
     * Timestamp when the item was approved
     */
    private LocalDateTime approvedAt;

    /**
     * ID of the person who approved this item
     */
    private String approvedBy;

    /**
     * Name of the person who approved this item
     */
    private String approvedByName;

    /**
     * Reason for rejection (if rejected)
     */
    private String rejectionReason;

    /**
     * Additional notes or comments
     */
    private String notes;

    /**
     * List of image URLs showing the damage
     */
    private List<String> imageUrls;

    /**
     * Sequence/order of this item in the report
     */
    private Integer sequence;

    /**
     * Category of damage (MECHANICAL, ELECTRICAL, COSMETIC, etc.)
     */
    private String category;

    /**
     * Severity level (LOW, MEDIUM, HIGH, CRITICAL)
     */
    private String severity;

    /**
     * Whether this is a mandatory repair
     */
    private Boolean isMandatory;

    /**
     * Estimated repair time in minutes
     */
    private Integer estimatedRepairTimeMinutes;

    /**
     * Warranty information for the repair
     */
    private String warrantyInfo;

    /**
     * Parts required for repair
     */
    private List<RequiredPart> requiredParts;

    /**
     * Metadata for additional information
     */
    private java.util.Map<String, Object> metadata;

    /**
     * Timestamp when the item was created
     */
    private LocalDateTime createdAt;

    /**
     * Timestamp when the item was last updated
     */
    private LocalDateTime updatedAt;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class RequiredPart {
        private String partId;
        private String partName;
        private String partNumber;
        private Integer quantity;
        private BigDecimal unitPrice;
        private BigDecimal totalPrice;
        private Boolean isAvailable;
        private String supplier;
        private Integer estimatedDeliveryDays;
    }

    // ==================== Helper Methods ====================

    /**
     * Check if the damage item is approved
     */
    public boolean isApproved() {
        return Boolean.TRUE.equals(isApproved) || "APPROVED".equalsIgnoreCase(status);
    }

    /**
     * Check if the damage item is rejected
     */
    public boolean isRejected() {
        return Boolean.TRUE.equals(isRejected) || "REJECTED".equalsIgnoreCase(status);
    }

    /**
     * Check if the damage item is pending approval
     */
    public boolean isPending() {
        return !isApproved() && !isRejected() &&
                ("PENDING".equalsIgnoreCase(status) || status == null);
    }

    /**
     * Get the final cost (approved cost if available, otherwise estimated cost)
     */
    public BigDecimal getFinalCost() {
        if (approvedCost != null) {
            return approvedCost;
        }
        return estimatedCost;
    }

    /**
     * Calculate the discount amount (difference between estimated and approved)
     */
    public BigDecimal getDiscountAmount() {
        if (estimatedCost != null && approvedCost != null) {
            return estimatedCost.subtract(approvedCost);
        }
        return BigDecimal.ZERO;
    }

    /**
     * Calculate discount percentage
     */
    public Double getDiscountPercentage() {
        if (estimatedCost != null && approvedCost != null &&
                estimatedCost.compareTo(BigDecimal.ZERO) > 0) {
            return estimatedCost.subtract(approvedCost)
                    .divide(estimatedCost, 2, BigDecimal.ROUND_HALF_UP)
                    .doubleValue() * 100;
        }
        return 0.0;
    }

    /**
     * Builder with convenience methods
     */
    public static class DamageItemBuilder {

        public DamageItemBuilder withItem(String name, String description, BigDecimal estimatedCost) {
            this.itemName = name;
            this.description = description;
            this.estimatedCost = estimatedCost;
            return this;
        }

        public DamageItemBuilder withApproval(BigDecimal approvedCost, String approvedBy) {
            this.approvedCost = approvedCost;
            this.approvedBy = approvedBy;
            this.isApproved = true;
            this.status = "APPROVED";
            this.approvedAt = LocalDateTime.now();
            return this;
        }

        public DamageItemBuilder withRejection(String reason) {
            this.isRejected = true;
            this.status = "REJECTED";
            this.rejectionReason = reason;
            return this;
        }

        public DamageItemBuilder withImages(List<String> imageUrls) {
            this.imageUrls = imageUrls;
            return this;
        }

        public DamageItemBuilder withParts(List<RequiredPart> parts) {
            this.requiredParts = parts;
            return this;
        }
    }

    // ==================== Constants ====================

    public static final String STATUS_PENDING = "PENDING";
    public static final String STATUS_APPROVED = "APPROVED";
    public static final String STATUS_REJECTED = "REJECTED";
    public static final String STATUS_PARTIALLY_APPROVED = "PARTIALLY_APPROVED";

    public static final String SEVERITY_LOW = "LOW";
    public static final String SEVERITY_MEDIUM = "MEDIUM";
    public static final String SEVERITY_HIGH = "HIGH";
    public static final String SEVERITY_CRITICAL = "CRITICAL";

    public static final String CATEGORY_MECHANICAL = "MECHANICAL";
    public static final String CATEGORY_ELECTRICAL = "ELECTRICAL";
    public static final String CATEGORY_COSMETIC = "COSMETIC";
    public static final String CATEGORY_STRUCTURAL = "STRUCTURAL";
}