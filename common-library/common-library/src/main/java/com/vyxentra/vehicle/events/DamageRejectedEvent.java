package com.vyxentra.vehicle.events;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.util.List;

@Data
@EqualsAndHashCode(callSuper = true)
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class DamageRejectedEvent extends BaseEvent {

    private String damageReportId;
    private String bookingId;
    private String customerId;
    private String rejectionReason;
    private List<String> rejectedPartIds;
    private List<String> rejectedServiceIds;
    private boolean requiresNewEstimate;
}
