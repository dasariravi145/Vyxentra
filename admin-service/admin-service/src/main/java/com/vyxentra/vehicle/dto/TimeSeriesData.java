package com.vyxentra.vehicle.dto;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public  class TimeSeriesData {
    private LocalDate date;
    private Double revenue;
    private Double commission;
    private Long bookings;
}
