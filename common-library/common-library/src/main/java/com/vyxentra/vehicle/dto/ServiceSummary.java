package com.vyxentra.vehicle.dto;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public  class ServiceSummary {
    private String serviceType;
    private String name;
    private Double price;
    private Integer estimatedDuration;
    private String currency;
    private String vehicleType;
}