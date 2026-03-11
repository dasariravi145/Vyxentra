package com.vyxentra.vehicle.dto.request;

import com.vyxentra.vehicle.dto.AddonDefinition;
import com.vyxentra.vehicle.dto.ServiceVehiclePricing;
import com.vyxentra.vehicle.enums.ProviderType;
import com.vyxentra.vehicle.enums.ServiceType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ServiceDefinitionRequest {

    @NotBlank(message = "Category ID is required")
    private String categoryId;

    @NotNull(message = "Service type is required")
    private ServiceType serviceType;

    @NotBlank(message = "Service name is required")
    private String name;

    private String description;
    private String shortDescription;
    private String icon;
    private String imageUrl;

    @Positive(message = "Estimated duration must be positive")
    private Integer estimatedDurationMin;

    @NotNull(message = "Provider type is required")
    private ProviderType providerType;

    private Boolean isActive;
    private Boolean isPopular;
    private Boolean isRecommended;

    private Integer displayOrder;
    private List<String> tags;

    @NotNull(message = "Vehicle type pricing is required")
    private Map<String, ServiceVehiclePricing> vehiclePricing;

    private List<AddonDefinition> addons;




}
