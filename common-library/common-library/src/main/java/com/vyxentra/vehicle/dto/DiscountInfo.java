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
public  class DiscountInfo {
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
