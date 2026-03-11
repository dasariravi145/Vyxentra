package com.vyxentra.vehicle.dto.response;


import com.vyxentra.vehicle.enums.Role;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserDetailResponse {

    private String userId;
    private String phoneNumber;
    private String email;
    private String fullName;
    private Role role;
    private Boolean active;
    private Boolean emailVerified;
    private Boolean phoneVerified;
    private LocalDateTime createdAt;
    private LocalDateTime lastLoginAt;
    private Integer failedAttempts;
    private Boolean locked;
    private LocalDateTime lockTime;

    // Statistics
    private Integer totalBookings;
    private Integer totalSpent;
    private Double averageRating;

    // Provider specific (if applicable)
    private String businessName;
    private String providerStatus;
    private String providerId;

    // Admin metadata
    private String createdBy;
    private String updatedBy;
    private LocalDateTime updatedAt;
    private Map<String, Object> metadata;
}
