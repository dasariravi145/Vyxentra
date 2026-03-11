package com.vyxentra.vehicle.dto.request;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LocationUpdateRequest {

    @NotNull(message = "Latitude is required")
    @Min(value = -90, message = "Invalid latitude")
    @Max(value = 90, message = "Invalid latitude")
    private Double latitude;

    @NotNull(message = "Longitude is required")
    @Min(value = -180, message = "Invalid longitude")
    @Max(value = 180, message = "Invalid longitude")
    private Double longitude;

    private String entityType; // PROVIDER, EMPLOYEE, CUSTOMER

    private String bookingId;

    private Double speed; // km/h

    @Min(value = 0, message = "Heading must be 0-360")
    @Max(value = 360, message = "Heading must be 0-360")
    private Double heading; // degrees

    private Double accuracy; // meters

    private Double altitude; // meters

    private String source; // GPS, NETWORK, MANUAL
}
