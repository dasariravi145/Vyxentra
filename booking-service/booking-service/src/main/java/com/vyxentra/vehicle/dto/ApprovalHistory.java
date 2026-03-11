package com.vyxentra.vehicle.dto;

@lombok.Data
@lombok.Builder
@lombok.NoArgsConstructor
@lombok.AllArgsConstructor
public class ApprovalHistory {
    private String reportId;
    private String action; // APPROVED, REJECTED, ESCALATED, AUTO_APPROVED
    private String performedBy;
    private String performedByName;
    private java.time.LocalDateTime performedAt;
    private String comments;
    private Double amount;
}