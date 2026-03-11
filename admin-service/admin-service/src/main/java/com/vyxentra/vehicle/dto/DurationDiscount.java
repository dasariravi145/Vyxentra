package com.vyxentra.vehicle.dto;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public  class DurationDiscount {
    private Integer durationId;
    private String durationName;
    private Integer minHours;
    private Integer maxHours;
    private Double discountPercentage;
}
