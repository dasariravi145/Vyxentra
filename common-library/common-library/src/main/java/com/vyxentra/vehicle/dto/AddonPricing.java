package com.vyxentra.vehicle.dto;

import com.vyxentra.vehicle.enums.VehicleType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public  class AddonPricing {
    private String addonId;
    private String addonName;
    private String description;
    private String priceType;
    private BigDecimal price;
    private Map<VehicleType, BigDecimal> vehicleSpecificPrice;
    private boolean isMandatory;
    private boolean isAvailable;
}
