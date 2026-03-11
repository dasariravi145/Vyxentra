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
public  class DamageItemResponse {
    private String itemId;
    private String itemName;
    private String description;
    private Double estimatedCost;
    private Double approvedCost;
    private Boolean isApproved;
    private LocalDateTime approvedAt;
    private String rejectionReason;
    private List<String> images;
}