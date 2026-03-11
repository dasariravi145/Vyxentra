package com.vyxentra.vehicle.dto;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public  class PayoutSummary {
    private Double totalPayouts;
    private Long totalTransactions;
    private Double averagePayout;
    private Double minPayout;
    private Double maxPayout;
    private Map<String, Double> monthlyBreakdown;
    private Map<String, Long> statusBreakdown;
    private Double totalEarned;
    private Double totalPaid;
    private Double pendingAmount;
    private long successfulPayouts;
    private long failedPayouts;
    private long pendingPayouts;
    private LocalDate lastPayoutDate;
    private double lastPayoutAmount;
    private Double averagePayoutAmount;
}
