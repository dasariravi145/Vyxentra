package com.vyxentra.vehicle.dto.response;


import com.vyxentra.vehicle.enums.VehicleType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class VehicleResponse {

    private String vehicleId;
    private VehicleType vehicleType;
    private String make;
    private String model;
    private String year;
    private String registrationNumber;
    private String color;
    private boolean isDefault;
    private String fuelType;
    private String transmissionType;
    private Integer engineCapacity;
    private Instant createdAt;
    private Instant updatedAt;
}
