package com.vyxentra.vehicle.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public  class AssignmentInfo {
    private String assignmentId;
    private String providerId;
    private String providerName;
    private String providerPhone;
    private Double distanceKm;
    private Integer etaMinutes;
    private String status;
    private LocalDateTime acceptedAt;
    private String bookingId;
    private Location currentLocation;
}
