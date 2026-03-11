package com.vyxentra.vehicle.dto;

import java.util.Map;

@lombok.Data
@lombok.Builder
@lombok.NoArgsConstructor
@lombok.AllArgsConstructor
public  class WebSocketStats {
    private long totalConnections;
    private long activeConnections;
    private long uniqueUsers;
    private long activeTrackingSessions;
    private Map<String, Long> connectionsByUserType;
    private double averageConnectionsPerSession;
}
