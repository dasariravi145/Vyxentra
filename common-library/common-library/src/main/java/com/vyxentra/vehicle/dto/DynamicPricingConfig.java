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
public  class DynamicPricingConfig {
    private boolean enabled;
    private Double surgeMultiplier;
    private Integer demandThreshold;
    private Double timeBasedMultiplier;
    private Map<String, Double> timeSlots;
}

