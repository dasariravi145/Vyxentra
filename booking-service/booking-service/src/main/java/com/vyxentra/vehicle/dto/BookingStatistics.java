package com.vyxentra.vehicle.dto;

import java.time.LocalDate;
import java.util.Map;

@lombok.Data
@lombok.Builder
@lombok.NoArgsConstructor
@lombok.AllArgsConstructor
public  class BookingStatistics {
    private long totalBookings;
    private long completedBookings;
    private long cancelledBookings;
    private double totalRevenue;
    private double averageBookingValue;
    private Map<String, Long> statusDistribution;
    private Map<LocalDate, Long> dailyBookings;
}
