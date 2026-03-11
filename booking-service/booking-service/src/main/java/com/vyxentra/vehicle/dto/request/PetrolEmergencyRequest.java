package com.vyxentra.vehicle.dto.request;

import com.vyxentra.vehicle.dto.Location;

import java.util.Map;

@lombok.Data
@lombok.Builder
@lombok.NoArgsConstructor
@lombok.AllArgsConstructor
public  class PetrolEmergencyRequest {
    private String providerId;
    private String vehicleType;
    private Map<String, Object> vehicleDetails;
    private Location location;
    private String fuelType;
    private Integer quantity;
    private String customerNotes;
}
