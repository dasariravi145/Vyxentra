package com.vyxentra.vehicle.events;

import com.vyxentra.vehicle.dto.DamageApprovalDTO;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.math.BigDecimal;

@Data
@EqualsAndHashCode(callSuper = true)
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class DamageApprovedEvent extends BaseEvent {

    private String approvalId;
    private String damageReportId;
    private String bookingId;
    private String customerId;
    private DamageApprovalDTO approval;
    private BigDecimal approvedAmount;
    private int approvedPartsCount;
    private int approvedServicesCount;
    private boolean fullyApproved;
}