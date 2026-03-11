package com.vyxentra.vehicle.dto.response;



import com.vyxentra.vehicle.enums.ProviderStatus;
import com.vyxentra.vehicle.enums.ProviderType;

import com.vyxentra.vehicle.enums.UserRole;
import com.vyxentra.vehicle.enums.UserStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.time.LocalTime;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProviderDetailResponse {

    private String providerId;
    private String userId;
    private String businessName;
    private ProviderType providerType;
    private String ownerName;
    private String gstNumber;
    private String panNumber;
    private String registrationNumber;
    private String email;
    private String phone;
    private String alternatePhone;
    private String website;
    private String businessDescription;
    private String profilePicture;

    private UserStatus userStatus;
    private Set<UserRole> userRoles;
    private Boolean emailVerified;
    private Boolean phoneVerified;

    private String addressLine1;
    private String addressLine2;
    private String city;
    private String state;
    private String postalCode;
    private String country;
    private Double latitude;
    private Double longitude;

    private ProviderStatus status;
    private String suspensionReason;

    private Boolean supportsBike;
    private Boolean supportsCar;
    private LocalTime openingTime;
    private LocalTime closingTime;
    private String workingDays;
    private Boolean twentyFourSeven;

    private Double averageRating;
    private Integer totalReviews;
    private Integer totalBookings;
    private Double completionRate;
    private Integer yearOfEstablishment;
    private Integer employeeCount;
    private Boolean isVerified;

    private List<ServiceOfferingResponse> services;
    private List<DocumentResponse> documents;
    private List<ReviewResponse> recentReviews;
    private BankDetailsResponse bankDetails;
    private InsuranceDetailsResponse insuranceDetails;

    private String approvedBy;
    private Instant approvedAt;
    private String rejectionReason;

    private Instant createdAt;
    private Instant updatedAt;
    private Instant lastLoginAt;

    private Map<String, Object> metadata;
}
