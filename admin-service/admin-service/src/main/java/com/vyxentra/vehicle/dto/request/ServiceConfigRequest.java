package com.vyxentra.vehicle.dto.request;


import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ServiceConfigRequest {

    private Double basePrice;

    private Double priceMultiplier;

    private Boolean isActive;

    private Boolean requiresApproval;

    @Positive(message = "Max daily bookings must be positive")
    private Integer maxDailyBookings;

    @Positive(message = "Min advance hours must be positive")
    private Integer minAdvanceHours;

    private Map<String, Object> vehiclePricing;
}
