package com.vyxentra.vehicle.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public  class PendingApproval {
    private String providerId;
    private String businessName;
    private String ownerName;
    private LocalDateTime registeredAt;
    private Long pendingDocuments;
}
