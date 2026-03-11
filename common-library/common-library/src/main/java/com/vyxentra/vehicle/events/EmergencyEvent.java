package com.vyxentra.vehicle.events;

import com.vyxentra.vehicle.enums.EmergencyType;
import com.vyxentra.vehicle.enums.VehicleType;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.math.BigDecimal;

@Data
@SuperBuilder
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class EmergencyEvent extends BaseEvent {
    private String emergencyId;
    private String bookingId;
    private String customerId;
    private String providerId;
    private EmergencyType emergencyType;
    private VehicleType vehicleType;
    private Double latitude;
    private Double longitude;
    private String location;
    private BigDecimal amount;
    private BigDecimal multiplier;
    private String status;

    // Petrol emergency specific
    private String fuelType;
    private Integer quantity;
    private BigDecimal fuelCost;
}
