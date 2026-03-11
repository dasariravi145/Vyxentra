package com.vyxentra.vehicle.dto.request;


import com.vyxentra.vehicle.dto.Location;
import com.vyxentra.vehicle.dto.PetrolDetails;
import com.vyxentra.vehicle.enums.EmergencyType;
import com.vyxentra.vehicle.enums.VehicleType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EmergencyBookingRequest {

    @NotNull(message = "Emergency type is required")
    private EmergencyType emergencyType;

    @NotNull(message = "Vehicle type is required")
    private VehicleType vehicleType;

    private Map<String, Object> vehicleDetails;

    @NotNull(message = "Location is required")
    private Location location;

    private String issueDescription;

    // Petrol emergency specific fields
    private PetrolDetails petrolDetails;
    private String city;


}