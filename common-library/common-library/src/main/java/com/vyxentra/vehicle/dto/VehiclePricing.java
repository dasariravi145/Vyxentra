package com.vyxentra.vehicle.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public  class VehiclePricing {
    private BigDecimal price;
    private BigDecimal priceMultiplier;
    private Integer estimatedDurationMinutes;
    private boolean isAvailable;
}
