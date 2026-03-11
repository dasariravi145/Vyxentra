package com.vyxentra.vehicle.dto.response;


import com.vyxentra.vehicle.enums.ProviderType;
import com.vyxentra.vehicle.enums.ServiceType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.List;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ServiceDetailResponse {

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
    private Integer displayOrder;
    private List<String> tags;

    // Vehicle-specific details
    private Map<String, ServiceVehicleDetail> vehicleDetails;

    // Addons
    private List<AddonResponse> addons;

    // FAQs
    private List<ServiceFAQ> faqs;

    private Instant createdAt;
    private Instant updatedAt;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ServiceVehicleDetail {
        private Double basePrice;
        private Double priceMultiplier;
        private Integer estimatedDurationMin;
        private Boolean isActive;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ServiceFAQ {
        private String question;
        private String answer;
    }
}
