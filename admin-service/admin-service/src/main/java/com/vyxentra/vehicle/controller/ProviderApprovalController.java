package com.vyxentra.vehicle.controller;

import com.vyxentra.vehicle.dto.request.ProviderApprovalRequest;
import com.vyxentra.vehicle.dto.response.ApiResponse;
import com.vyxentra.vehicle.dto.response.DocumentVerificationResponse;
import com.vyxentra.vehicle.dto.response.PageResponse;
import com.vyxentra.vehicle.dto.response.ProviderApprovalResponse;
import com.vyxentra.vehicle.service.ProviderApprovalService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/v1/admin/providers")
@RequiredArgsConstructor
public class ProviderApprovalController {

    private final ProviderApprovalService providerApprovalService;

    @GetMapping("/pending")
    public ResponseEntity<ApiResponse<PageResponse<ProviderApprovalResponse>>> getPendingProviders(
            @RequestHeader("X-User-ID") String adminId,
            @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.ASC) Pageable pageable) {
        log.info("Getting pending providers for admin: {}", adminId);
        PageResponse<ProviderApprovalResponse> response = providerApprovalService.getPendingProviders(pageable);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/{providerId}")
    public ResponseEntity<ApiResponse<ProviderApprovalResponse>> getProviderDetails(
            @RequestHeader("X-User-ID") String adminId,
            @PathVariable String providerId) {
        log.info("Getting provider details for admin: {} provider: {}", adminId, providerId);
        ProviderApprovalResponse response = providerApprovalService.getProviderDetails(providerId);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @PostMapping("/{providerId}/approve")
    public ResponseEntity<ApiResponse<ProviderApprovalResponse>> approveProvider(
            @RequestHeader("X-User-ID") String adminId,
            @PathVariable String providerId,
            @Valid @RequestBody ProviderApprovalRequest request) {
        log.info("Approving provider: {} by admin: {}", providerId, adminId);
        ProviderApprovalResponse response = providerApprovalService.approveProvider(adminId, providerId, request);
        return ResponseEntity.ok(ApiResponse.success(response, "Provider approved successfully"));
    }

    @PostMapping("/{providerId}/reject")
    public ResponseEntity<ApiResponse<ProviderApprovalResponse>> rejectProvider(
            @RequestHeader("X-User-ID") String adminId,
            @PathVariable String providerId,
            @RequestParam String reason) {
        log.info("Rejecting provider: {} by admin: {} reason: {}", providerId, adminId, reason);
        ProviderApprovalResponse response = providerApprovalService.rejectProvider(adminId, providerId, reason);
        return ResponseEntity.ok(ApiResponse.success(response, "Provider rejected"));
    }

    @PostMapping("/{providerId}/suspend")
    public ResponseEntity<ApiResponse<ProviderApprovalResponse>> suspendProvider(
            @RequestHeader("X-User-ID") String adminId,
            @PathVariable String providerId,
            @RequestParam String reason) {
        log.info("Suspending provider: {} by admin: {} reason: {}", providerId, adminId, reason);
        ProviderApprovalResponse response = providerApprovalService.suspendProvider(adminId, providerId, reason);
        return ResponseEntity.ok(ApiResponse.success(response, "Provider suspended"));
    }

    @PostMapping("/{providerId}/activate")
    public ResponseEntity<ApiResponse<ProviderApprovalResponse>> activateProvider(
            @RequestHeader("X-User-ID") String adminId,
            @PathVariable String providerId) {
        log.info("Activating provider: {} by admin: {}", providerId, adminId);
        ProviderApprovalResponse response = providerApprovalService.activateProvider(adminId, providerId);
        return ResponseEntity.ok(ApiResponse.success(response, "Provider activated"));
    }

    @GetMapping("/documents/pending")
    public ResponseEntity<ApiResponse<List<DocumentVerificationResponse>>> getPendingDocuments(
            @RequestHeader("X-User-ID") String adminId) {
        log.info("Getting pending document verifications for admin: {}", adminId);
        List<DocumentVerificationResponse> responses = providerApprovalService.getPendingDocuments();
        return ResponseEntity.ok(ApiResponse.success(responses));
    }

    @PostMapping("/documents/{documentId}/verify")
    public ResponseEntity<ApiResponse<Void>> verifyDocument(
            @RequestHeader("X-User-ID") String adminId,
            @PathVariable String documentId,
            @RequestParam boolean verified,
            @RequestParam(required = false) String remarks) {
        log.info("Verifying document: {} by admin: {} verified: {}", documentId, adminId, verified);
        providerApprovalService.verifyDocument(adminId, documentId, verified, remarks);
        return ResponseEntity.ok(ApiResponse.success(null, "Document verification updated"));
    }

    @GetMapping("/statistics")
    public ResponseEntity<ApiResponse<Object>> getProviderStatistics(
            @RequestHeader("X-User-ID") String adminId) {
        log.info("Getting provider statistics for admin: {}", adminId);
        Object statistics = providerApprovalService.getProviderStatistics();
        return ResponseEntity.ok(ApiResponse.success(statistics));
    }
}
