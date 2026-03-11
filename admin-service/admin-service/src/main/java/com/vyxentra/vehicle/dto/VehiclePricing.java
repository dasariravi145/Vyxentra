package com.vyxentra.vehicle.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public  class VehiclePricing {
    private Double basePrice;
    private Double priceMultiplier;
    private Integer estimatedDurationMinutes;
    private Boolean isActive;
    private String priceType; // FIXED, PER_HOUR, PER_KM
    private Map<String, Double> addonPricing;
}
