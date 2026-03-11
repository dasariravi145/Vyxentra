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
public  class AddonConfig {
    private String addonId;
    private String name;
    private String description;
    private String priceType; // FIXED, PER_HOUR, PER_VEHICLE
    private Double basePrice;
    private Map<String, Double> vehiclePricing;
    private Boolean isMandatory;
    private Boolean isActive;
    private Integer maxQuantity;
    private Integer displayOrder;
}
