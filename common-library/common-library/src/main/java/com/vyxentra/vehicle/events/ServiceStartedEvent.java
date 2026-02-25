package com.vyxentra.vehicle.events;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.time.Instant;

@Data
@EqualsAndHashCode(callSuper = true)
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class ServiceStartedEvent extends BaseEvent {

    private String bookingId;
    private String providerId;
    private String employeeId;
    private String employeeName;
    private Instant startTime;
    private String serviceType;
    private String notes;
}
