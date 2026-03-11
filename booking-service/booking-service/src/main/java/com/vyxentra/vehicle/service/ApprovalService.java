package com.vyxentra.vehicle.service;


import com.vyxentra.vehicle.dto.ApprovalHistory;
import com.vyxentra.vehicle.dto.ApprovalStatistics;
import com.vyxentra.vehicle.dto.request.DamageApprovalRequest;
import com.vyxentra.vehicle.dto.response.DamageReportResponse;

import java.util.List;

public interface ApprovalService {

    DamageReportResponse approveDamage(String reportId, String customerId, DamageApprovalRequest request);

    /**
     * Reject a damage report entirely
     *
     * @param reportId The ID of the damage report
     * @param customerId The ID of the customer rejecting
     * @param reason The reason for rejection
     * @return The updated damage report response
     */
    DamageReportResponse rejectDamage(String reportId, String customerId, String reason);


    void processPendingApprovals();


    List<DamageReportResponse> getPendingApprovalsForCustomer(String customerId);

    void escalateApproval(String reportId, String customerId, String reason);


    List<ApprovalHistory> getApprovalHistory(String bookingId);

    ApprovalStatistics getApprovalStatistics();
}
