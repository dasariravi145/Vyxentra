package com.vyxentra.vehicle.dto.request;

import com.vyxentra.vehicle.enums.PricingAlgorithm;
import com.vyxentra.vehicle.enums.ServiceType;
import com.vyxentra.vehicle.enums.VehicleType;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ServicePricingRequest {

    @NotNull(message = "Service type is required")
    private ServiceType serviceType;

    @NotNull(message = "Base price is required")
    @Positive(message = "Price must be positive")
    private BigDecimal basePrice;

    private String currency;

    private Map<VehicleType, BigDecimal> vehicleSpecificPricing;

    private Integer estimatedDurationMinutes;

    private String description;

    private Boolean isActive;

    private BigDecimal taxPercentage;

    private PricingAlgorithm algorithm;

    private DynamicPricingConfig dynamicConfig;

    private List<AddonPricing> addons;

    private Map<String, Object> metadata;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class DynamicPricingConfig {
        private boolean enabled;
        private Double surgeMultiplier;
        private Integer demandThreshold;
        private Double timeBasedMultiplier;
        private Map<String, Double> timeSlots;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class AddonPricing {
        private String addonId;
        private String name;
        private String description;
        private String priceType;
        private BigDecimal price;
        private Map<VehicleType, BigDecimal> vehicleSpecificPrice;
        private boolean isMandatory;
        private Integer maxQuantity;
    }
}
