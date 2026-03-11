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
public class ETAUpdateResponse {

    private String bookingId;
    private Integer etaMinutes;
    private Double distanceKm;
    private String reason; // TRAFFIC, ROUTE_CHANGE, etc.
    private LocalDateTime calculatedAt;
}
