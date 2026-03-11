package com.vyxentra.vehicle.dto.response;


import com.vyxentra.vehicle.enums.ProviderType;
import com.vyxentra.vehicle.enums.ServiceType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ServiceDefinitionResponse {

    private String serviceId;
    private String categoryId;
    private String categoryName;
    private ServiceType serviceType;
    private String name;
    private String description;
    private String shortDescription;
    private String icon;
    private String imageUrl;
    private Integer estimatedDurationMin;
    private ProviderType providerType;
    private Boolean isActive;
    private Boolean isPopular;
    private Boolean isRecommended;
    private Double minPrice;
    private Double maxPrice;
    private Integer displayOrder;
    private List<String> tags;
    private Instant createdAt;
    private Instant updatedAt;
}
