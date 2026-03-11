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
public  class MarketComparison {
    private BigDecimal marketAverage;
    private BigDecimal marketMin;
    private BigDecimal marketMax;
    private String position;
    private int percentileRank;
    private String recommendation;
}
