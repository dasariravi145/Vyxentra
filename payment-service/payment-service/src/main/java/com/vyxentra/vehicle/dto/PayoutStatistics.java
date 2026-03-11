package com.vyxentra.vehicle.dto;

import java.time.LocalDate;
import java.util.Map;

@lombok.Data
@lombok.Builder
@lombok.NoArgsConstructor
@lombok.AllArgsConstructor
public class PayoutStatistics {
    private Long totalPayouts;
    private Double totalAmount;
    private Double totalCommission;
    private Double totalTax;
    private Long uniqueProviders;
    private Map<String, Long> statusDistribution;
    private Map<LocalDate, Double> dailyBreakdown;
    private Map<String, Double> providerBreakdown;
    private Double averagePayoutPerProvider;
    private Double minPayout;
    private Double maxPayout;
}
