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
public  class FuelInfo {
    private String fuelType;
    private Integer quantity;
    private Double costPerLiter;
    private BigDecimal totalFuelCost;
}
