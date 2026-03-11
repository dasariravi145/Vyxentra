package com.vyxentra.vehicle.dto.request;


import jakarta.validation.constraints.NotBlank;
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
public class AddonRequest {

    @NotBlank(message = "Addon name is required")
    private String name;

    private String description;

    @NotBlank(message = "Price type is required")
    private String priceType; // FIXED, PER_VEHICLE, PER_HOUR

    @Positive(message = "Base price must be positive")
    private Double basePrice;

    private Map<String, Double> vehiclePricing; // vehicleType -> price

    private Boolean isMandatory;
    private Boolean isActive;
    private Integer displayOrder;
}
