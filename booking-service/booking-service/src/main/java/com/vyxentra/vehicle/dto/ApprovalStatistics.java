package com.vyxentra.vehicle.dto;

@lombok.Data
@lombok.Builder
@lombok.NoArgsConstructor
@lombok.AllArgsConstructor
public  class ApprovalStatistics {
    private Long pendingApprovals;      // Total pending approvals
    private Long totalPending;          // Alias for pendingApprovals (if needed)
    private Long approvedToday;          // Number approved today
    private Long rejectedToday;          // Number rejected today
    private Long autoApprovedToday;      // Number auto-approved today
    private Double averageApprovalTimeHours; // Average time to approve in hours
    private Double averageApprovalAmount;    // Average approved amount
    private Long expiredToday;            // Number expired today
    private Long escalatedToday;          // Number escalated today

    public Long getPendingApprovals() {
        return pendingApprovals != null ? pendingApprovals : totalPending;
    }
}
