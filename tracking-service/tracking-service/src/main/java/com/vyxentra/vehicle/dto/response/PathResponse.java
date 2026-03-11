package com.vyxentra.vehicle.dto.response;


import com.vyxentra.vehicle.dto.PathPoint;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PathResponse {

    private String bookingId;
    private List<PathPoint> path;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private Double totalDistance;


}
