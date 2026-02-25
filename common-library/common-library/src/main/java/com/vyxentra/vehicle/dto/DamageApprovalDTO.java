package com.vyxentra.vehicle.dto;

import com.vyxentra.vehicle.enums.ApprovalStatus;
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
public class DamageApprovalDTO {

    private String damageReportId;
    private String customerId;
    private ApprovalStatus status;
    private List<String> approvedPartIds;
    private List<String> approvedServiceIds;
    private List<String> rejectedPartIds;
    private List<String> rejectedServiceIds;
    private String customerRemarks;
    private Instant approvedAt;
    private PriceBreakdownDTO approvedPriceBreakdown;
}
