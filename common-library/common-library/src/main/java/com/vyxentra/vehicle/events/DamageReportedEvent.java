package com.vyxentra.vehicle.events;

import com.vyxentra.vehicle.dto.DamageReportDTO;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@Data
@EqualsAndHashCode(callSuper = true)
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class DamageReportedEvent extends BaseEvent {

    private String damageReportId;
    private String bookingId;
    private String providerId;
    private String employeeId;
    private String employeeName;
    private DamageReportDTO damageReport;
    private boolean requiresApproval;
    private String notificationType; // SMS, PUSH, EMAIL
}
