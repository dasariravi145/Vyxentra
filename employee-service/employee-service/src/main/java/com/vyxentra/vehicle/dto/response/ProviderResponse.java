package com.vyxentra.vehicle.dto.response;


import com.vyxentra.vehicle.enums.ProviderStatus;
import com.vyxentra.vehicle.enums.ProviderType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.time.LocalTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProviderResponse {

    private String providerId;
    private String userId;
    private String businessName;
    private ProviderType providerType;
    private String ownerName;
    private String email;
    private String phone;
    private ProviderStatus status;
    private String address;
    private String city;
    private String state;
    private Double latitude;
    private Double longitude;
    private Boolean supportsBike;
    private Boolean supportsCar;
    private LocalTime openingTime;
    private LocalTime closingTime;
    private String workingDays;
    private Double averageRating;
    private Integer totalReviews;
    private Integer totalBookings;
    private Double completionRate;
    private Boolean isVerified;
    private Instant createdAt;
}
