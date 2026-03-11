package com.vyxentra.vehicle.dto.response;

import com.vyxentra.vehicle.dto.DocumentInfo;
import com.vyxentra.vehicle.enums.ProviderType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProviderApprovalResponse {

    private String providerId;
    private String userId;
    private String businessName;
    private ProviderType businessType;
    private String gstNumber;
    private String panNumber;
    private String registrationNumber;
    private String email;
    private String phone;
    private String address;
    private String city;
    private String state;
    private String status;
    private Double latitude;
    private Double longitude;
    private Boolean supportsBike;
    private Boolean supportsCar;
    private LocalDateTime registeredAt;

    // Documents
    private List<DocumentInfo> documents;
    private Boolean allDocumentsUploaded;
    private Boolean allDocumentsVerified;

    // Approval Info
    private String approvedBy;
    private LocalDateTime approvedAt;
    private String rejectionReason;

    // Additional Info
    private Integer totalBookings;
    private Double averageRating;


}
