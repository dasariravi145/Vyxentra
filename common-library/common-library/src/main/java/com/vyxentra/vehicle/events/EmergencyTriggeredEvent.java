package com.vyxentra.vehicle.events;

import com.vyxentra.vehicle.enums.EmergencyType;
import com.vyxentra.vehicle.enums.VehicleType;
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
public class EmergencyTriggeredEvent extends BaseEvent {

    private String emergencyId;
    private String bookingId;
    private String customerId;
    private String customerName;
    private String customerMobile;
    private VehicleType vehicleType;
    private EmergencyType emergencyType;
    private String description;
    private Double latitude;
    private Double longitude;
    private String address;
    private Integer searchRadius;
    private Double emergencyMultiplier;
    private boolean autoExpandRadius;
}