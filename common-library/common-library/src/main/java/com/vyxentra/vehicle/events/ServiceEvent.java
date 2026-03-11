package com.vyxentra.vehicle.events;


import com.vyxentra.vehicle.enums.ServiceType;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.time.Instant;

@Data
@SuperBuilder
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class ServiceEvent extends BaseEvent {
    private String bookingId;
    private String providerId;
    private String employeeId;
    private ServiceType serviceType;
    private String status; // STARTED, COMPLETED, DELAYED
    private Instant startTime;
    private Instant endTime;
    private Integer estimatedDuration;
    private Integer actualDuration;
    private String delayReason;
    private String notes;
}
