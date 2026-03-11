package com.vyxentra.vehicle.dto.response;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TrackingInfoResponse {

    private String sessionId;
    private String bookingId;
    private String customerId;
    private String providerId;
    private String employeeId;
    private String status; // ACTIVE, PAUSED, COMPLETED, EXPIRED

    private LocationResponse startLocation;
    private LocationResponse currentLocation;
    private LocationResponse destination;

    private Double totalDistanceKm;
    private Integer currentETA;
    private LocalDateTime startedAt;
    private LocalDateTime lastUpdateAt;
    private LocalDateTime estimatedArrival;

    private Boolean isProviderOnline;
    private Boolean isTrackingActive;
}
