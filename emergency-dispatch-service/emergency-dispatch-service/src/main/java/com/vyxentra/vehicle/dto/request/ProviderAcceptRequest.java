package com.vyxentra.vehicle.dto.request;


import com.vyxentra.vehicle.dto.Location;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProviderAcceptRequest {

    @NotBlank(message = "Request ID is required")
    private String requestId;

    @NotNull(message = "Current location is required")
    private Location currentLocation;

    private Integer etaMinutes;

}