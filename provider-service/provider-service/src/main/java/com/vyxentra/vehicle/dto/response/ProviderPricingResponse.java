package com.vyxentra.vehicle.dto.response;

import com.vyxentra.vehicle.enums.PricingAlgorithm;
import com.vyxentra.vehicle.enums.ServiceType;
import com.vyxentra.vehicle.enums.VehicleType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProviderPricingResponse {

    private String pricingId;
    private String providerId;
    private String providerName;
    private ServiceType serviceType;
    private String serviceName;

    private BigDecimal basePrice;
    private String currency;

    private Map<VehicleType, VehiclePricing> vehiclePricing;
    private List<AddonPricing> addons;

    private List<DiscountInfo> activeDiscounts;
    private boolean hasDiscount;
    private BigDecimal discountedPrice;

    private PricingAlgorithm algorithm;
    private DynamicPricingConfig dynamicConfig;

    private PriceRange priceRange;

    private Instant lastUpdated;
    private Instant priceValidUntil;
    private List<PriceChange> recentChanges;

    private MarketComparison marketComparison;

    private Map<String, BigDecimal> additionalFees;
    private boolean includesTax;
    private BigDecimal taxPercentage;

    private boolean isAvailable;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class VehiclePricing {
        private BigDecimal price;
        private BigDecimal priceMultiplier;
        private Integer estimatedDurationMinutes;
        private boolean isAvailable;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class AddonPricing {
        private String addonId;
        private String addonName;
        private String description;
        private String priceType;
        private BigDecimal price;
        private Map<VehicleType, BigDecimal> vehicleSpecificPrice;
        private boolean isMandatory;
        private boolean isAvailable;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class DiscountInfo {
        private String discountId;
        private String name;
        private String description;
        private String discountType;
        private BigDecimal discountValue;
        private BigDecimal discountedAmount;
        private String validFrom;
        private String validUntil;
        private String couponCode;
    }

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
    public static class PriceRange {
        private BigDecimal minPrice;
        private BigDecimal maxPrice;
        private BigDecimal averagePrice;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PriceChange {
        private BigDecimal oldPrice;
        private BigDecimal newPrice;
        private BigDecimal changePercentage;
        private Instant changedAt;
        private String reason;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class MarketComparison {
        private BigDecimal marketAverage;
        private BigDecimal marketMin;
        private BigDecimal marketMax;
        private String position;
        private int percentileRank;
        private String recommendation;
    }
}
