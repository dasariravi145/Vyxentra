package com.vyxentra.vehicle.events;

import com.vyxentra.vehicle.dto.PriceBreakdownDTO;
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
public class ServiceCompletedEvent extends BaseEvent {

    private String bookingId;
    private String providerId;
    private String employeeId;
    private Instant completionTime;
    private PriceBreakdownDTO finalPrice;
    private boolean requiresCustomerApproval;
    private String completionNotes;
    private String[] imageUrls;
}
