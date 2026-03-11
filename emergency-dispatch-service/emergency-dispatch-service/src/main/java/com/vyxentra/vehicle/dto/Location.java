package com.vyxentra.vehicle.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public  class Location {
    private Double latitude;
    private Double longitude;
    private String address;
    private String city;
    private LocalDateTime timestamp;
    private String providerId;
}