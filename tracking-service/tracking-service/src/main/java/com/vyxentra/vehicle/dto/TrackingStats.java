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
public  class TrackingStats {
    private Double totalDistanceKm;
    private Integer totalDurationMinutes;
    private Integer averageSpeed;
    private Integer maxSpeed;
    private Integer etaChanges;
    private LocalDateTime startedAt;
    private LocalDateTime completedAt;
}
