package com.vyxentra.vehicle.dto.response;


import com.vyxentra.vehicle.enums.ProviderType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProviderSearchResponse {

    private String providerId;
    private String businessName;
    private ProviderType providerType;
    private String address;
    private Double latitude;
    private Double longitude;
    private Double distance;
    private String phone;
    private Boolean supportsBike;
    private Boolean supportsCar;
    private Boolean isOpenNow;
    private Double averageRating;
    private Integer totalReviews;
    private List<ServiceSummary> services;
    private String openingTime;
    private String closingTime;
    private Boolean isVerified;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ServiceSummary {
        private String serviceType;
        private String name;
        private Double price;
        private Integer estimatedDuration;
        private String currency;
    }
}
