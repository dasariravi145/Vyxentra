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
public  class AddonDefinition {
    private String name;
    private String description;
    private String priceType; // FIXED, PER_VEHICLE, PER_HOUR
    private Double basePrice;
    private Map<String, Double> vehiclePricing;
    private Boolean isMandatory;
    private Boolean isActive;
    private Integer displayOrder;
}
