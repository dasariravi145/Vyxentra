package com.vyxentra.vehicle.dto.response;


import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.time.LocalTime;
import java.util.List;
import java.util.Map;

/**
 * DTO for provider information received from provider-service
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ProviderResponse {

    private String providerId;
    private String userId;
    private String businessName;
    private String providerType; // SERVICE_CENTER, WASHING_CENTER, FUEL_DELIVERY, etc.
    private String ownerName;
    private String email;
    private String phone;
    private String alternatePhone;
    private String status; // ACTIVE, SUSPENDED, PENDING_APPROVAL, etc.

    // Location information
    private String addressLine1;
    private String addressLine2;
    private String city;
    private String state;
    private String postalCode;
    private String country;
    private Double latitude;
    private Double longitude;

    // Service capabilities
    private Boolean supportsBike;
    private Boolean supportsCar;
    private Boolean supportsEmergency;
    private List<String> emergencyTypes; // REPAIR, FUEL, TOWING

    // Operating hours
    private LocalTime openingTime;
    private LocalTime closingTime;
    private String workingDays;
    private Boolean twentyFourSeven;

    // Ratings and stats
    private Double averageRating;
    private Integer totalReviews;
    private Integer totalEmergenciesHandled;
    private Double acceptanceRate;
    private Double completionRate;

    // Vehicle information
    private String vehicleType;
    private String vehicleNumber;
    private String vehicleModel;

    // Current availability
    private Boolean isAvailable;
    private Boolean isOnline;
    private Instant lastLocationUpdate;
    private Double currentLatitude;
    private Double currentLongitude;

    // Fuel delivery specific
    private Boolean hasFuel;
    private Map<String, Double> fuelPrices; // fuelType -> price

    // Towing specific
    private Boolean hasTowTruck;
    private Integer maxTowingCapacity; // in kg

    // Additional metadata
    private Map<String, Object> metadata;
    private Instant createdAt;
    private Instant updatedAt;

    /**
     * Check if provider is available for emergency
     */
    public boolean isAvailableForEmergency() {
        return "ACTIVE".equals(status) &&
                Boolean.TRUE.equals(isAvailable) &&
                Boolean.TRUE.equals(isOnline) &&
                supportsEmergency;
    }

    /**
     * Check if provider supports specific emergency type
     */
    public boolean supportsEmergencyType(String emergencyType) {
        return emergencyTypes != null && emergencyTypes.contains(emergencyType);
    }

    /**
     * Check if provider supports vehicle type
     */
    public boolean supportsVehicleType(String vehicleType) {
        if ("BIKE".equals(vehicleType)) {
            return Boolean.TRUE.equals(supportsBike);
        } else if ("CAR".equals(vehicleType)) {
            return Boolean.TRUE.equals(supportsCar);
        }
        return false;
    }

    /**
     * Get fuel price for specific fuel type
     */
    public Double getFuelPrice(String fuelType) {
        if (fuelPrices != null && fuelPrices.containsKey(fuelType)) {
            return fuelPrices.get(fuelType);
        }
        return null;
    }

    /**
     * Check if provider is currently within operating hours
     */
    public boolean isWithinOperatingHours() {
        if (Boolean.TRUE.equals(twentyFourSeven)) {
            return true;
        }

        if (openingTime == null || closingTime == null) {
            return true; // Assume always open if not specified
        }

        LocalTime now = LocalTime.now();
        return !now.isBefore(openingTime) && !now.isAfter(closingTime);
    }
}