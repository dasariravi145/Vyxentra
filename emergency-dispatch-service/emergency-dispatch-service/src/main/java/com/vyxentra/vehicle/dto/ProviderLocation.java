package com.vyxentra.vehicle.dto;

import java.util.Set;

@lombok.Data
@lombok.Builder
@lombok.NoArgsConstructor
@lombok.AllArgsConstructor
public class ProviderLocation {
    private String providerId;
    private Double latitude;
    private Double longitude;
    private Long timestamp;
    private String vehicleType;
    private Set<String> emergencyTypes;
    private Boolean available;
    private Double speed;
    private Double heading;
}
