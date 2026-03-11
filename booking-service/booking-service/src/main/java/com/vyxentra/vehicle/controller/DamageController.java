package com.vyxentra.vehicle.controller;


import com.vyxentra.vehicle.dto.request.DamageApprovalRequest;
import com.vyxentra.vehicle.dto.request.DamageReportRequest;
import com.vyxentra.vehicle.dto.response.ApiResponse;
import com.vyxentra.vehicle.dto.response.DamageReportResponse;
import com.vyxentra.vehicle.service.DamageService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/v1/bookings/damage")
@RequiredArgsConstructor
public class DamageController {

    private final DamageService damageService;

    /**
     * Report damage (Employee only)
     */
    @PostMapping("/report")
    @PreAuthorize("hasRole('EMPLOYEE')")
    public ResponseEntity<ApiResponse<DamageReportResponse>> reportDamage(
            @RequestHeader("X-User-ID") String employeeId,
            @Valid @RequestBody DamageReportRequest request) {
        log.info("Reporting damage for booking: {} by employee: {}", request.getBookingId(), employeeId);
        DamageReportResponse response = damageService.reportDamage(employeeId, request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(response, "Damage reported successfully"));
    }

    /**
     * Get damage report by ID
     */
    @GetMapping("/report/{reportId}")
    @PreAuthorize("hasAnyRole('CUSTOMER', 'PROVIDER', 'EMPLOYEE', 'ADMIN')")
    public ResponseEntity<ApiResponse<DamageReportResponse>> getDamageReport(
            @PathVariable String reportId) {
        log.info("Getting damage report: {}", reportId);
        DamageReportResponse response = damageService.getDamageReport(reportId);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    /**
     * Get all damage reports for a booking
     */
    @GetMapping("/booking/{bookingId}")
    @PreAuthorize("hasAnyRole('CUSTOMER', 'PROVIDER', 'EMPLOYEE', 'ADMIN')")
    public ResponseEntity<ApiResponse<List<DamageReportResponse>>> getBookingDamageReports(
            @PathVariable String bookingId) {
        log.info("Getting damage reports for booking: {}", bookingId);
        List<DamageReportResponse> responses = damageService.getBookingDamageReports(bookingId);
        return ResponseEntity.ok(ApiResponse.success(responses));
    }

    /**
     * Approve damage report (Customer only)
     */
    @PostMapping("/report/{reportId}/approve")
    @PreAuthorize("hasRole('CUSTOMER')")
    public ResponseEntity<ApiResponse<Void>> approveDamage(
            @PathVariable String reportId,
            @RequestHeader("X-User-ID") String customerId,
            @Valid @RequestBody DamageApprovalRequest request) {
        log.info("Approving damage report: {} by customer: {}", reportId, customerId);
        damageService.approveDamage(reportId, customerId, request);
        return ResponseEntity.ok(ApiResponse.success(null, "Damage approved successfully"));
    }

    /**
     * Reject damage report (Customer only)
     */
    @PostMapping("/report/{reportId}/reject")
    @PreAuthorize("hasRole('CUSTOMER')")
    public ResponseEntity<ApiResponse<Void>> rejectDamage(
            @PathVariable String reportId,
            @RequestHeader("X-User-ID") String customerId,
            @RequestParam String reason) {
        log.info("Rejecting damage report: {} by customer: {}", reportId, customerId);
        damageService.rejectDamage(reportId, customerId, reason);
        return ResponseEntity.ok(ApiResponse.success(null, "Damage rejected"));
    }

    /**
     * Get pending damage reports for customer
     */
    @GetMapping("/pending/{customerId}")
    @PreAuthorize("hasRole('CUSTOMER') or #customerId == authentication.principal.username")
    public ResponseEntity<ApiResponse<List<DamageReportResponse>>> getPendingDamageReports(
            @PathVariable String customerId) {
        log.info("Getting pending damage reports for customer: {}", customerId);
        List<DamageReportResponse> responses = damageService.getPendingDamageReports(customerId);
        return ResponseEntity.ok(ApiResponse.success(responses));
    }

    /**
     * Get all pending damage reports (Admin only)
     */
    @GetMapping("/pending")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<List<DamageReportResponse>>> getAllPendingDamageReports() {
        log.info("Getting all pending damage reports");
        // This would need to be implemented in service
        return ResponseEntity.ok(ApiResponse.success(List.of()));
    }

    /**
     * Add comment to damage report
     */
    @PostMapping("/report/{reportId}/comment")
    @PreAuthorize("hasAnyRole('CUSTOMER', 'PROVIDER', 'EMPLOYEE')")
    public ResponseEntity<ApiResponse<Void>> addComment(
            @PathVariable String reportId,
            @RequestHeader("X-User-ID") String userId,
            @RequestParam String comment) {
        log.info("Adding comment to damage report: {} by user: {}", reportId, userId);
        // This would need to be implemented in service
        return ResponseEntity.ok(ApiResponse.success(null, "Comment added successfully"));
    }

    /**
     * Upload additional images to damage report
     */
    @PostMapping("/report/{reportId}/images")
    @PreAuthorize("hasAnyRole('EMPLOYEE', 'PROVIDER')")
    public ResponseEntity<ApiResponse<Void>> uploadImages(
            @PathVariable String reportId,
            @RequestBody List<String> imageUrls) {
        log.info("Uploading images to damage report: {}", reportId);
        // This would need to be implemented in service
        return ResponseEntity.ok(ApiResponse.success(null, "Images uploaded successfully"));
    }
}
