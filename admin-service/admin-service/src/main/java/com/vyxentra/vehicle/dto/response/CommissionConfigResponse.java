package com.vyxentra.vehicle.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.vyxentra.vehicle.dto.CommissionTier;
import com.vyxentra.vehicle.dto.DurationDiscount;
import com.vyxentra.vehicle.dto.PromotionalRate;
import com.vyxentra.vehicle.dto.VolumeDiscount;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

/**
 * Response DTO for commission configuration in admin panel
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class CommissionConfigResponse {

    private String configId;
    private String providerType;
    private String providerTypeDisplay;

    // Commission rates
    private Double commissionPercentage;
    private Double minCommission;
    private Double maxCommission;
    private Double fixedCommission;
    private String commissionType; // PERCENTAGE, FIXED, TIERED

    // Tiered commission structure
    private List<CommissionTier> commissionTiers;

    // Validity period
    private LocalDate effectiveFrom;
    private LocalDate effectiveTo;
    private Boolean isActive;
    private Boolean isDefault;

    // Service-specific commissions
    private Map<String, Double> serviceSpecificCommissions; // serviceType -> percentage

    // Vehicle-specific commissions
    private Map<String, Double> vehicleSpecificCommissions; // vehicleType -> percentage

    // Promotional rates
    private List<PromotionalRate> promotionalRates;

    // Special rates for new providers
    private Boolean hasNewProviderRate;
    private Double newProviderRate;
    private Integer newProviderPeriodDays;

    // Volume-based discounts
    private List<VolumeDiscount> volumeDiscounts;

    // Caps and limits
    private Double absoluteMinCommission;
    private Double absoluteMaxCommission;
    private Boolean hasCapping;
    private Double cappingAmount;
    private String cappingPeriod; // DAILY, WEEKLY, MONTHLY

    // Weekend/Holiday rates
    private Double weekendRate;
    private Double holidayRate;
    private List<LocalDate> holidays;

    // Emergency service rates
    private Double emergencyRate;
    private Double emergencyMultiplier;

    // Long duration discounts
    private List<DurationDiscount> durationDiscounts;

    // Geographic variations
    private Map<String, Double> citySpecificRates;
    private Map<String, Double> stateSpecificRates;

    // Metadata
    private String description;
    private String notes;
    private Map<String, Object> metadata;
    private List<String> tags;

    // Audit
    private String createdBy;
    private String updatedBy;
    private Instant createdAt;
    private Instant updatedAt;
    private String version;

    // Statistics
    private Long applicableProviders;
    private Double averageCommissionCollected;
    private Double totalCommissionCollected;
    private LocalDate lastAppliedDate;

    // ==================== Helper Methods ====================

    /**
     * Get display name for provider type
     */
    public String getProviderTypeDisplay() {
        if (providerTypeDisplay != null) return providerTypeDisplay;

        switch (providerType) {
            case "SERVICE_CENTER": return "Service Center";
            case "WASHING_CENTER": return "Washing Center";
            case "GARAGE": return "Garage";
            case "TOWING_SERVICE": return "Towing Service";
            case "FUEL_DELIVERY": return "Fuel Delivery";
            case "EMERGENCY_SERVICE": return "Emergency Service";
            case "MECHANIC": return "Independent Mechanic";
            case "SPA": return "Car Spa";
            case "DETAILING": return "Detailing Center";
            default: return providerType;
        }
    }

    /**
     * Calculate commission for given amount
     */
    public Double calculateCommission(Double amount) {
        if (amount == null || amount <= 0) return 0.0;

        Double commission = 0.0;

        if ("PERCENTAGE".equals(commissionType)) {
            commission = amount * (commissionPercentage / 100);
        } else if ("FIXED".equals(commissionType)) {
            commission = fixedCommission != null ? fixedCommission : 0.0;
        } else if ("TIERED".equals(commissionType) && commissionTiers != null) {
            for (CommissionTier tier : commissionTiers) {
                if (amount >= tier.getMinAmount() &&
                        (tier.getMaxAmount() == null || amount <= tier.getMaxAmount())) {
                    if (tier.getCommissionPercentage() != null) {
                        commission = amount * (tier.getCommissionPercentage() / 100);
                    } else {
                        commission = tier.getFixedCommission() != null ? tier.getFixedCommission() : 0.0;
                    }
                    break;
                }
            }
        }

        // Apply min/max caps
        if (minCommission != null && commission < minCommission) {
            commission = minCommission;
        }
        if (maxCommission != null && commission > maxCommission) {
            commission = maxCommission;
        }

        return commission;
    }

    /**
     * Calculate commission for specific service type
     */
    public Double calculateServiceSpecificCommission(String serviceType, Double amount) {
        if (serviceSpecificCommissions != null && serviceSpecificCommissions.containsKey(serviceType)) {
            Double serviceRate = serviceSpecificCommissions.get(serviceType);
            Double commission = amount * (serviceRate / 100);

            if (minCommission != null && commission < minCommission) {
                commission = minCommission;
            }
            if (maxCommission != null && commission > maxCommission) {
                commission = maxCommission;
            }

            return commission;
        }

        return calculateCommission(amount);
    }

    /**
     * Calculate commission with volume discount
     */
    public Double calculateWithVolumeDiscount(Double amount, Integer monthlyBookings) {
        Double commission = calculateCommission(amount);

        if (volumeDiscounts != null && monthlyBookings != null) {
            for (VolumeDiscount discount : volumeDiscounts) {
                if (monthlyBookings >= discount.getMinBookings() &&
                        (discount.getMaxBookings() == null || monthlyBookings <= discount.getMaxBookings())) {
                    commission = commission * (1 - discount.getDiscountPercentage() / 100);
                    break;
                }
            }
        }

        return commission;
    }

    /**
     * Check if commission config is currently active
     */
    public boolean isCurrentlyActive() {
        LocalDate today = LocalDate.now();
        boolean dateValid = true;

        if (effectiveFrom != null && effectiveFrom.isAfter(today)) {
            dateValid = false;
        }
        if (effectiveTo != null && effectiveTo.isBefore(today)) {
            dateValid = false;
        }

        return isActive && dateValid;
    }

    /**
     * Get applicable commission rate for new provider
     */
    public Double getApplicableNewProviderRate(LocalDate joiningDate) {
        if (!hasNewProviderRate || newProviderRate == null || newProviderPeriodDays == null) {
            return commissionPercentage;
        }

        LocalDate periodEnd = joiningDate.plusDays(newProviderPeriodDays);
        if (LocalDate.now().isBefore(periodEnd)) {
            return newProviderRate;
        }

        return commissionPercentage;
    }

    /**
     * Get status badge color for UI
     */
    public String getStatusBadgeColor() {
        if (!isActive) return "red";
        if (!isCurrentlyActive()) return "orange";
        return "green";
    }

    /**
     * Get effective period display
     */
    public String getEffectivePeriodDisplay() {
        if (effectiveFrom == null && effectiveTo == null) return "Always";
        if (effectiveFrom != null && effectiveTo == null) return "From " + effectiveFrom;
        if (effectiveFrom == null && effectiveTo != null) return "Until " + effectiveTo;
        return effectiveFrom + " to " + effectiveTo;
    }
}
