package com.vyxentra.vehicle.dto;

@lombok.Data
@lombok.Builder
@lombok.NoArgsConstructor
@lombok.AllArgsConstructor
public class ApprovalStatistics {
    private long pendingApprovals;
    private long approvedToday;
    private long rejectedToday;
    private long suspendedCount;
    private double averageApprovalTimeHours;
    private long pendingDocumentVerification;
}