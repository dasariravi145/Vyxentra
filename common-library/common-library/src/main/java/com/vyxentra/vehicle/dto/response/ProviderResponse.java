package com.vyxentra.vehicle.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.vyxentra.vehicle.dto.AddressDTO;
import com.vyxentra.vehicle.dto.GeoLocationDTO;
import com.vyxentra.vehicle.enums.ProviderStatus;
import com.vyxentra.vehicle.enums.ProviderType;
import com.vyxentra.vehicle.enums.VehicleType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Map;
import java.util.Set;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ProviderResponse {

    private String id;
    private String businessName;
    private String ownerName;
    private String mobileNumber;
    private String email;
    private String gstNumber;
    private ProviderType providerType;
    private ProviderStatus status;
    private AddressDTO address;
    private GeoLocationDTO location;
    private Double distance; // from search location

    // Service capabilities
    private boolean supportsBike;
    private boolean supportsCar;

    // Pricing
    private Map<VehicleType, BigDecimal> baseServiceCharge;
    private Map<VehicleType, BigDecimal> hourlyLaborRate;
    private Map<VehicleType, BigDecimal> emergencyMultiplier;

    // Business info
    private String openingTime;
    private String closingTime;
    private Set<Integer> workingDays;
    private boolean currentlyOpen;

    // Metrics
    private Double rating;
    private Integer totalReviews;
    private Integer completedBookings;

    // Approval
    private boolean adminApproved;
    private Instant approvedAt;
    private String approvedBy;

    // Timestamps
    private Instant createdAt;
    private Instant updatedAt;
}
