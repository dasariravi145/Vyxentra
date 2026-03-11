package com.vyxentra.vehicle.dto;

import java.util.Map;

@lombok.Data
@lombok.Builder
@lombok.NoArgsConstructor
@lombok.AllArgsConstructor
public  class CustomerBookingStatistics {
    private long totalBookings;
    private long completedBookings;
    private long cancelledBookings;
    private double totalSpent;
    private double averageRating;
    private Map<String, Long> serviceTypeDistribution;
}
