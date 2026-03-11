package com.vyxentra.vehicle.dto.response;

@lombok.Data
@lombok.Builder
@lombok.NoArgsConstructor
@lombok.AllArgsConstructor
public  class NearbyProviderResponse {
    private String providerId;
    private String businessName;
    private Double distance;
    private Integer etaMinutes;
    private Double rating;
    private Boolean available;
}
