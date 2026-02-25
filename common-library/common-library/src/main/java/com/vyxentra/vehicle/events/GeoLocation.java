package com.vyxentra.vehicle.events;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public  class GeoLocation {
    private Double latitude;
    private Double longitude;
    private String address;
}
