package com.vyxentra.vehicle.controller;


import com.vyxentra.vehicle.dto.ApprovalStatistics;
import com.vyxentra.vehicle.dto.request.DamageApprovalRequest;
import com.vyxentra.vehicle.dto.response.ApiResponse;
import com.vyxentra.vehicle.dto.response.DamageReportResponse;
import com.vyxentra.vehicle.service.ApprovalService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/v1/bookings/approval")
@RequiredArgsConstructor
public class ApprovalController {

    private final ApprovalService approvalService;

    /**
     * Approve damage items (Customer only)
     */
    @PostMapping("/damage/{reportId}/approve")
    @PreAuthorize("hasRole('CUSTOMER')")
    public ResponseEntity<ApiResponse<DamageReportResponse>> approveDamage(
            @PathVariable String reportId,
            @RequestHeader("X-User-ID") String customerId,
            @Valid @RequestBody DamageApprovalRequest request) {
        log.info("Approving damage report: {} by customer: {}", reportId, customerId);
        DamageReportResponse response = approvalService.approveDamage(reportId, customerId, request);
        return ResponseEntity.ok(ApiResponse.success(response, "Damage approved successfully"));
    }

    /**
     * Reject damage report (Customer only)
     */
    @PostMapping("/damage/{reportId}/reject")
    @PreAuthorize("hasRole('CUSTOMER')")
    public ResponseEntity<ApiResponse<DamageReportResponse>> rejectDamage(
            @PathVariable String reportId,
            @RequestHeader("X-User-ID") String customerId,
            @RequestParam String reason) {
        log.info("Rejecting damage report: {} by customer: {}", reportId, customerId);
        DamageReportResponse response = approvalService.rejectDamage(reportId, customerId, reason);
        return ResponseEntity.ok(ApiResponse.success(response, "Damage rejected"));
    }

    /**
     * Get pending approvals for customer
     */
    @GetMapping("/pending/customer/{customerId}")
    @PreAuthorize("hasRole('CUSTOMER')")
    public ResponseEntity<ApiResponse<List<DamageReportResponse>>> getCustomerPendingApprovals(
            @PathVariable String customerId) {
        log.info("Getting pending approvals for customer: {}", customerId);
        // This would need to be implemented in service
        return ResponseEntity.ok(ApiResponse.success(List.of()));
    }

    /**
     * Get pending approvals for provider
     */
    @GetMapping("/pending/provider/{providerId}")
    @PreAuthorize("hasRole('PROVIDER')")
    public ResponseEntity<ApiResponse<List<DamageReportResponse>>> getProviderPendingApprovals(
            @PathVariable String providerId) {
        log.info("Getting pending approvals for provider: {}", providerId);
        // This would need to be implemented in service
        return ResponseEntity.ok(ApiResponse.success(List.of()));
    }

    /**
     * Get approval history for booking
     */
    @GetMapping("/history/{bookingId}")
    @PreAuthorize("hasAnyRole('CUSTOMER', 'PROVIDER', 'ADMIN')")
    public ResponseEntity<ApiResponse<List<DamageReportResponse>>> getApprovalHistory(
            @PathVariable String bookingId) {
        log.info("Getting approval history for booking: {}", bookingId);
        // This would need to be implemented in service
        return ResponseEntity.ok(ApiResponse.success(List.of()));
    }

    /**
     * Request re-approval (Employee/Provider only)
     */
    @PostMapping("/{reportId}/request-reapproval")
    @PreAuthorize("hasAnyRole('EMPLOYEE', 'PROVIDER')")
    public ResponseEntity<ApiResponse<Void>> requestReapproval(
            @PathVariable String reportId,
            @RequestParam String reason) {
        log.info("Requesting re-approval for damage report: {}", reportId);
        // This would need to be implemented in service
        return ResponseEntity.ok(ApiResponse.success(null, "Re-approval requested"));
    }

    /**
     * Get approval statistics (Admin only)
     */
    @GetMapping("/statistics")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<ApprovalStatistics>> getApprovalStatistics() {
        log.info("Getting approval statistics");
        ApprovalStatistics stats = ApprovalStatistics.builder()
                .totalPending(25L)
                .approvedToday(12L)
                .rejectedToday(3L)
                .averageApprovalTimeHours(4.5)
                .build();
        return ResponseEntity.ok(ApiResponse.success(stats));
    }


}
