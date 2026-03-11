package com.vyxentra.vehicle.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public  class DiscountConfig {
    private String discountId;
    private String name;
    private String description;
    private String discountType; // PERCENTAGE, FIXED
    private Double discountValue;
    private String couponCode;
    private Boolean isActive;
    private Instant validFrom;
    private Instant validUntil;
    private Integer minBookingValue;
    private Integer maxDiscountAmount;
    private List<String> applicableVehicles;
    private Boolean isFirstTimeOnly;
    private Integer usageLimit;
    private Integer usedCount;
}
