package com.vyxentra.vehicle.dto;

import com.vyxentra.vehicle.entity.DamageReport;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@SuperBuilder
@NoArgsConstructor
public  class DamageItem {
    private String itemId;
    private String description;
    private BigDecimal cost;
    private boolean approved;
    private String rejectionReason;
    private BigDecimal approvedCost;
    private LocalDateTime approvedAt;
    private BigDecimal estimatedCost;
    private DamageReport damageReport;
    private String itemName;
    private List<String> images;

}
