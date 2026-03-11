package com.vyxentra.vehicle.dto.response;


import com.fasterxml.jackson.annotation.JsonInclude;
import com.vyxentra.vehicle.dto.AddonConfig;
import com.vyxentra.vehicle.dto.DiscountConfig;
import com.vyxentra.vehicle.dto.TimeSlot;
import com.vyxentra.vehicle.dto.VehiclePricing;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.List;
import java.util.Map;

/**
 * Response DTO for service configuration in admin panel
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ServiceConfigResponse {

    private String configId;
    private String serviceType;
    private String serviceName;
    private String serviceCategory;
    private String description;

    // Basic configuration
    private Double basePrice;
    private Double minPrice;
    private Double maxPrice;
    private String currency;
    private Double priceMultiplier;
    private Integer estimatedDurationMinutes;

    // Vehicle-specific pricing
    private Map<String, VehiclePricing> vehiclePricing; // BIKE, CAR, SUV, etc.

    // Availability
    private Boolean isActive;
    private Boolean isPopular;
    private Boolean isRecommended;
    private Boolean isEmergency;
    private Boolean requiresApproval;
    private Boolean requiresPrePayment;

    // Limits
    private Integer maxDailyBookings;
    private Integer maxWeeklyBookings;
    private Integer maxMonthlyBookings;
    private Integer minAdvanceHours;
    private Integer maxAdvanceDays;
    private Integer cancellationHours;

    // Timing
    private List<TimeSlot> availableTimeSlots;
    private List<String> holidays;
    private Map<String, String> seasonalPricing; // season -> multiplier

    // Commission
    private Double commissionPercentage;
    private Double minCommission;
    private Double maxCommission;

    // Addons
    private List<AddonConfig> availableAddons;
    private List<String> mandatoryAddons;

    // Discounts
    private List<DiscountConfig> activeDiscounts;
    private Boolean hasFirstTimeDiscount;
    private Double firstTimeDiscountPercentage;

    // Provider requirements
    private List<String> requiredProviderSkills;
    private List<String> requiredCertifications;
    private Integer minProviderRating;
    private Integer minProviderExperience;

    // Location restrictions
    private List<String> serviceableCities;
    private List<String> serviceablePincodes;
    private Double maxServiceRadiusKm;
    private Boolean isPanIndia;

    // Metadata
    private Map<String, Object> metadata;
    private List<String> tags;
    private String version;

    // Audit
    private String createdBy;
    private String updatedBy;
    private Instant createdAt;
    private Instant updatedAt;
    private Boolean isDefault;

    // ==================== Helper Methods ====================

    /**
     * Get price for specific vehicle type
     */
    public Double getPriceForVehicle(String vehicleType) {
        if (vehiclePricing != null && vehiclePricing.containsKey(vehicleType)) {
            return vehiclePricing.get(vehicleType).getBasePrice();
        }
        return basePrice;
    }

    /**
     * Check if service is available for vehicle
     */
    public boolean isAvailableForVehicle(String vehicleType) {
        if (vehiclePricing == null) return true;
        VehiclePricing pricing = vehiclePricing.get(vehicleType);
        return pricing != null && Boolean.TRUE.equals(pricing.getIsActive());
    }

    /**
     * Get estimated duration for vehicle
     */
    public Integer getDurationForVehicle(String vehicleType) {
        if (vehiclePricing != null && vehiclePricing.containsKey(vehicleType)) {
            Integer duration = vehiclePricing.get(vehicleType).getEstimatedDurationMinutes();
            if (duration != null) return duration;
        }
        return estimatedDurationMinutes;
    }

    /**
     * Check if time slot is available
     */
    public boolean isTimeSlotAvailable(String dayOfWeek, String time) {
        if (availableTimeSlots == null) return true;

        return availableTimeSlots.stream()
                .filter(slot -> slot.getDayOfWeek().equalsIgnoreCase(dayOfWeek))
                .anyMatch(slot -> {
                    boolean afterStart = slot.getStartTime().compareTo(time) <= 0;
                    boolean beforeEnd = slot.getEndTime().compareTo(time) >= 0;
                    return afterStart && beforeEnd && Boolean.TRUE.equals(slot.getIsAvailable());
                });
    }

    /**
     * Get surge multiplier for time slot
     */
    public Double getSurgeMultiplier(String dayOfWeek, String time) {
        if (availableTimeSlots == null) return 1.0;

        return availableTimeSlots.stream()
                .filter(slot -> slot.getDayOfWeek().equalsIgnoreCase(dayOfWeek))
                .filter(slot -> {
                    boolean afterStart = slot.getStartTime().compareTo(time) <= 0;
                    boolean beforeEnd = slot.getEndTime().compareTo(time) >= 0;
                    return afterStart && beforeEnd;
                })
                .findFirst()
                .map(TimeSlot::getSurgeMultiplier)
                .orElse(1.0);
    }

    /**
     * Calculate final price with all multipliers
     */
    public Double calculateFinalPrice(String vehicleType, String dayOfWeek, String time, Double quantity) {
        Double base = getPriceForVehicle(vehicleType);
        Double surgeMultiplier = getSurgeMultiplier(dayOfWeek, time);
        Double vehicleMultiplier = 1.0;

        if (vehiclePricing != null && vehiclePricing.containsKey(vehicleType)) {
            vehicleMultiplier = vehiclePricing.get(vehicleType).getPriceMultiplier();
            if (vehicleMultiplier == null) vehicleMultiplier = 1.0;
        }

        Double price = base * vehicleMultiplier * surgeMultiplier;

        if (quantity != null && quantity > 1) {
            price = price * quantity;
        }

        return price;
    }

    /**
     * Get applicable discounts
     */
    public List<DiscountConfig> getApplicableDiscounts(Double amount, Boolean isFirstTime, String vehicleType) {
        if (activeDiscounts == null) return List.of();

        Instant now = Instant.now();
        return activeDiscounts.stream()
                .filter(DiscountConfig::getIsActive)
                .filter(d -> d.getValidFrom() == null || d.getValidFrom().isBefore(now))
                .filter(d -> d.getValidUntil() == null || d.getValidUntil().isAfter(now))
                .filter(d -> d.getMinBookingValue() == null || amount >= d.getMinBookingValue())
                .filter(d -> d.getApplicableVehicles() == null ||
                        d.getApplicableVehicles().contains(vehicleType))
                .filter(d -> !Boolean.TRUE.equals(d.getIsFirstTimeOnly()) ||
                        Boolean.TRUE.equals(isFirstTime))
                .filter(d -> d.getUsageLimit() == null || d.getUsedCount() < d.getUsageLimit())
                .toList();
    }

    /**
     * Get status badge color for UI
     */
    public String getStatusBadgeColor() {
        if (Boolean.TRUE.equals(isActive)) return "green";
        return "red";
    }

    /**
     * Get display name with status
     */
    public String getDisplayName() {
        return serviceName + (isActive ? "" : " (Inactive)");
    }
}
