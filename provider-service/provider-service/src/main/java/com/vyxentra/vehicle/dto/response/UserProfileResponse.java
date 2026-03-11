package com.vyxentra.vehicle.dto.response;


import com.vyxentra.vehicle.enums.UserRole;
import com.vyxentra.vehicle.enums.UserStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.List;
import java.util.Set;

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
    private Set<UserRole> roles;
    private UserStatus status;
    private String profilePicture;
    private Boolean smsNotifications;
    private Boolean emailNotifications;
    private Boolean pushNotifications;
    private String preferredLanguage;
    private Integer totalBookings;
    private Integer totalSpent;
    private Double averageRating;
    private Instant createdAt;
    private Instant lastLoginAt;
    private Instant updatedAt;
    private String city;
    private String state;
    private String country;
    private Boolean emailVerified;
    private Boolean phoneVerified;
    private Boolean isActive;
    private String providerId;
    private String businessName;
    private String providerStatus;
    private String referredBy;
    private List<String> tags;
    private Instant dateOfBirth;
    private String gender;
}
