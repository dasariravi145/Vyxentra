package com.vyxentra.vehicle.dto.response;


import com.vyxentra.vehicle.enums.Role;
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
public class UserProfileResponse {

    private String userId;
    private String phoneNumber;
    private String email;
    private String fullName;
    private String alternatePhone;
    private Role role;
    private String profilePicture;

    // Preferences
    private Boolean smsNotifications;
    private Boolean emailNotifications;
    private Boolean pushNotifications;
    private String preferredLanguage;

    // Stats
    private Integer totalBookings;
    private Integer totalSpent;
    private Double averageRating;

    // Timestamps
    private Instant createdAt;
    private Instant lastLoginAt;

    // Related entities
    private List<AddressResponse> addresses;
    private List<VehicleResponse> vehicles;
    private AddressResponse defaultAddress;
    private VehicleResponse defaultVehicle;

    // Provider specific (if applicable)
    private String businessName;
    private String providerStatus;
    private Boolean supportsBike;
    private Boolean supportsCar;
}