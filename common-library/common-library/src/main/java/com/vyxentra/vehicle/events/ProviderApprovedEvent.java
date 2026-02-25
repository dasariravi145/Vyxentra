package com.vyxentra.vehicle.events;

import com.vyxentra.vehicle.enums.ProviderType;
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
public class ProviderApprovedEvent extends BaseEvent {

    private String providerId;
    private String businessName;
    private String ownerName;
    private String mobileNumber;
    private String email;
    private ProviderType providerType;
    private String approvedBy;
    private Instant approvedAt;
    private boolean supportsBike;
    private boolean supportsCar;
}
