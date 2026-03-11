package com.vyxentra.vehicle.dto.response;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ServiceSearchResponse {

    private String serviceId;
    private String name;
    private String shortDescription;
    private String categoryName;
    private String icon;
    private Double price;
    private String vehicleType;
    private Boolean isPopular;
    private Boolean isRecommended;
    private Integer estimatedDurationMin;
    private Double relevanceScore;
}
