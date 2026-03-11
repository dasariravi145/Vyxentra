package com.vyxentra.vehicle.dto;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public  class TimeSlot {
    private String dayOfWeek; // MONDAY, TUESDAY, etc.
    private String startTime;
    private String endTime;
    private Integer maxBookings;
    private Boolean isAvailable;
    private Double surgeMultiplier;
}
