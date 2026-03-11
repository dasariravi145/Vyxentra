package com.vyxentra.vehicle.dto;

import java.util.Map;

@lombok.Data
@lombok.Builder
@lombok.NoArgsConstructor
@lombok.AllArgsConstructor
public  class ProviderBookingStatistics {
    private long totalBookings;
    private long completedBookings;
    private long cancelledBookings;
    private double averageRating;
    private double completionRate;
    private Map<String, Long> statusDistribution;
}
