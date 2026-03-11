package com.vyxentra.vehicle.dto.response;

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
public class DamageReportResponse {

    private String reportId;
    private String bookingId;
    private String reportedBy;
    private String reportedByName;
    private LocalDateTime reportedAt;
    private String status; // REPORTED, APPROVED, REJECTED, PARTIALLY_APPROVED
    private Double totalAmount;
    private Double approvedAmount;
    private String notes;
    private List<String> images;

    private List<DamageItemResponse> items;

    private String approvedBy;
    private LocalDateTime approvedAt;
    private String rejectionReason;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
