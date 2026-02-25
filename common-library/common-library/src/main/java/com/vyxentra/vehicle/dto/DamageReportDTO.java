package com.vyxentra.vehicle.dto;

import com.vyxentra.vehicle.enums.DamageStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DamageReportDTO {

    private String id;
    private String bookingId;
    private String reportedBy;
    private String reportedByEmployeeId;
    private Instant reportedAt;
    private String description;
    private List<String> imageUrls = new ArrayList<>();
    private DamageStatus status;

    @Builder.Default
    private List<PartItemDTO> requiredParts = new ArrayList<>();

    @Builder.Default
    private List<ServiceItemDTO> requiredServices = new ArrayList<>();

    private String mechanicRemarks;
    private PriceBreakdownDTO priceBreakdown;
    private Instant estimatedCompletionTime;
}

