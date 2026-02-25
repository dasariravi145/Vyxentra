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
public class RepairDelayedEvent extends BaseEvent {

    private String bookingId;
    private String providerId;
    private String reason;
    private Instant originalCompletionTime;
    private Instant newEstimatedCompletionTime;
    private int delayMinutes;
    private boolean partsAvailabilityIssue;
    private boolean customerNotified;
}
