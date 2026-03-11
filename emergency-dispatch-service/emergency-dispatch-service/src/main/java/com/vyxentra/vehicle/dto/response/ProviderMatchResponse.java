package com.vyxentra.vehicle.dto.response;


import com.vyxentra.vehicle.dto.Location;
import com.vyxentra.vehicle.enums.VehicleType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProviderMatchResponse {

    private String providerId;
    private String businessName;
    private Double distance; // in km
    private Integer etaMinutes;
    private Double rating;
    private Boolean isAvailable;
    private String phone;
    private Location location;
    private String vehicleType;
    private Double latitude;
    private Double longitude;

}
