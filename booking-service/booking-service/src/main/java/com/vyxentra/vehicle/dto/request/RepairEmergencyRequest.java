package com.vyxentra.vehicle.dto.request;

import com.vyxentra.vehicle.dto.Location;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@lombok.Data
@lombok.Builder
@lombok.NoArgsConstructor
@lombok.AllArgsConstructor
public  class RepairEmergencyRequest {
    private String providerId;
    private String vehicleType;
    private Map<String, Object> vehicleDetails;
    private Location location;
    private String issueDescription;
    private String customerNotes;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Location {
        private Double latitude;
        private Double longitude;
        private String address;
    }
}
