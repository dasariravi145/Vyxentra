package com.vyxentra.vehicle.events;


import com.vyxentra.vehicle.enums.ProviderStatus;
import com.vyxentra.vehicle.enums.ProviderType;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@Data
@SuperBuilder
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class ProviderEvent extends BaseEvent {
    private String providerId;
    private String serviceId;
    private String serviceType;
    private String businessName;
    private ProviderType providerType;
    private ProviderStatus status;
    private boolean supportsBike;
    private boolean supportsCar;
    private Double latitude;
    private Double longitude;
    private String address;
    private String phone;
    private String email;
    private String approvedBy;
    private String rejectionReason;
    private String suspendedReason;
    private Integer rating;
}
