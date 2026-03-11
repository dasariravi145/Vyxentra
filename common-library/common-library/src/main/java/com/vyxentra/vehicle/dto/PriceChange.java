package com.vyxentra.vehicle.dto;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.Instant;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public  class PriceChange {
    private BigDecimal oldPrice;
    private BigDecimal newPrice;
    private BigDecimal changePercentage;
    private Instant changedAt;
    private String reason;
}
