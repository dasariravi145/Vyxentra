package com.vyxentra.vehicle.controller;


import com.vyxentra.vehicle.dto.ApprovalStatistics;
import com.vyxentra.vehicle.dto.request.ProviderApprovalRequest;
import com.vyxentra.vehicle.dto.response.ApiResponse;
import com.vyxentra.vehicle.dto.response.PageResponse;
import com.vyxentra.vehicle.dto.response.ProviderApprovalResponse;
import com.vyxentra.vehicle.dto.response.ProviderResponse;
import com.vyxentra.vehicle.service.ProviderApprovalService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/v1/providers/approval")
@RequiredArgsConstructor
public class ProviderApprovalController {

    private final ProviderApprovalService approvalService;

    @GetMapping("/pending")
    public ResponseEntity<ApiResponse<PageResponse<ProviderApprovalResponse>>> getPendingApprovals(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir) {

        Sort sort = sortDir.equalsIgnoreCase("desc")
                ? Sort.by(sortBy).descending()
                : Sort.by(sortBy).ascending();
        Pageable pageable = PageRequest.of(page, size, sort);

        Page<ProviderApprovalResponse> approvals = approvalService.getPendingApprovals(pageable);

        PageResponse<ProviderApprovalResponse> response = PageResponse.<ProviderApprovalResponse>builder()
                .content(approvals.getContent())
                .pageNumber(approvals.getNumber())
                .pageSize(approvals.getSize())
                .totalElements(approvals.getTotalElements())
                .totalPages(approvals.getTotalPages())
                .first(approvals.isFirst())
                .last(approvals.isLast())
                .hasNext(approvals.hasNext())
                .hasPrevious(approvals.hasPrevious())
                .build();

        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/{providerId}")
    public ResponseEntity<ApiResponse<ProviderApprovalResponse>> getApprovalDetails(
            @PathVariable String providerId) {
        ProviderApprovalResponse response = approvalService.getApprovalDetails(providerId);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @PostMapping("/approve")
    public ResponseEntity<ApiResponse<ProviderResponse>> approveProvider(
            @RequestHeader("X-User-ID") String adminId,
            @Valid @RequestBody ProviderApprovalRequest request) {
        ProviderResponse response = approvalService.approveProvider(adminId, request);
        return ResponseEntity.ok(ApiResponse.success(response, "Provider approved successfully"));
    }

    @PostMapping("/{providerId}/reject")
    public ResponseEntity<ApiResponse<ProviderResponse>> rejectProvider(
            @RequestHeader("X-User-ID") String adminId,
            @PathVariable String providerId,
            @RequestParam String reason) {
        ProviderResponse response = approvalService.rejectProvider(adminId, providerId, reason);
        return ResponseEntity.ok(ApiResponse.success(response, "Provider rejected"));
    }

    @PostMapping("/{providerId}/suspend")
    public ResponseEntity<ApiResponse<ProviderResponse>> suspendProvider(
            @RequestHeader("X-User-ID") String adminId,
            @PathVariable String providerId,
            @RequestParam String reason) {
        ProviderResponse response = approvalService.suspendProvider(adminId, providerId, reason);
        return ResponseEntity.ok(ApiResponse.success(response, "Provider suspended"));
    }

    @PostMapping("/{providerId}/activate")
    public ResponseEntity<ApiResponse<ProviderResponse>> activateProvider(
            @RequestHeader("X-User-ID") String adminId,
            @PathVariable String providerId) {
        ProviderResponse response = approvalService.activateProvider(adminId, providerId);
        return ResponseEntity.ok(ApiResponse.success(response, "Provider activated"));
    }

    @GetMapping("/statistics")
    public ResponseEntity<ApiResponse<ApprovalStatistics>> getApprovalStatistics() {
        ApprovalStatistics stats = approvalService.getApprovalStatistics();
        return ResponseEntity.ok(ApiResponse.success(stats));
    }

    @PostMapping("/{providerId}/documents/verify")
    public ResponseEntity<ApiResponse<Void>> verifyDocuments(
            @RequestHeader("X-User-ID") String adminId,
            @PathVariable String providerId,
            @RequestBody List<String> documentIds,
            @RequestParam boolean verified) {
        approvalService.verifyDocuments(adminId, providerId, documentIds, verified);
        return ResponseEntity.ok(ApiResponse.success(null, "Documents verified"));
    }

    @PostMapping("/{providerId}/assign/{adminId}")
    public ResponseEntity<ApiResponse<Void>> assignApproval(
            @PathVariable String providerId,
            @PathVariable String adminId) {
        approvalService.assignApproval(providerId, adminId);
        return ResponseEntity.ok(ApiResponse.success(null, "Approval assigned"));
    }
}
