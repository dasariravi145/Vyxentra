package com.vyxentra.vehicle.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public  class CommissionTier {
    private Integer tierId;
    private String tierName;
    private Double minAmount;
    private Double maxAmount;
    private Double commissionPercentage;
    private Double fixedCommission;
    private String description;
}
