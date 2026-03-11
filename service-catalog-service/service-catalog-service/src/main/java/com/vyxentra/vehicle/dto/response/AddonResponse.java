package com.vyxentra.vehicle.dto.response;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AddonResponse {

    private String addonId;
    private String serviceId;
    private String name;
    private String description;
    private String priceType; // FIXED, PER_VEHICLE, PER_HOUR
    private Double basePrice;
    private Map<String, Double> vehiclePricing;
    private Boolean isMandatory;
    private Boolean isActive;
    private Integer displayOrder;
    private Instant createdAt;
    private Instant updatedAt;
}
