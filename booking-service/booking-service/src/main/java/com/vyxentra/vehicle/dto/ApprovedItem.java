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
public  class ApprovedItem {
    private String itemId;
    private Boolean approved;
    private BigDecimal approvedCost; // If customer wants to negotiate
    private String rejectionReason;
}
