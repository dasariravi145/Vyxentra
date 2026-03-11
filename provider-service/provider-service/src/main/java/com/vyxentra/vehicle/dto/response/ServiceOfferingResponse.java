package com.vyxentra.vehicle.dto.response;



import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ServiceOfferingResponse {

    private String serviceId;
    private String serviceType;
    private String name;
    private String description;
    private Map<String, BigDecimal> pricing;
    private Integer estimatedDuration;
    private boolean isActive;
}
