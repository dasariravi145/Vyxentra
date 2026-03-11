package com.vyxentra.vehicle.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public  class VolumeDiscount {
    private Integer volumeId;
    private String volumeName;
    private Integer minBookings;
    private Integer maxBookings;
    private Double discountPercentage;
    private String period; // MONTHLY, QUARTERLY, YEARLY
}
