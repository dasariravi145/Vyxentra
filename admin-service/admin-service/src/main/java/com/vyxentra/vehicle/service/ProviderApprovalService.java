package com.vyxentra.vehicle.service;

import com.vyxentra.vehicle.dto.request.ProviderApprovalRequest;
import com.vyxentra.vehicle.dto.response.DocumentVerificationResponse;
import com.vyxentra.vehicle.dto.response.PageResponse;
import com.vyxentra.vehicle.dto.response.ProviderApprovalResponse;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface ProviderApprovalService {

    PageResponse<ProviderApprovalResponse> getPendingProviders(Pageable pageable);

    ProviderApprovalResponse getProviderDetails(String providerId);

    ProviderApprovalResponse approveProvider(String adminId, String providerId, ProviderApprovalRequest request);

    ProviderApprovalResponse rejectProvider(String adminId, String providerId, String reason);

    ProviderApprovalResponse suspendProvider(String adminId, String providerId, String reason);

    ProviderApprovalResponse activateProvider(String adminId, String providerId);

    List<DocumentVerificationResponse> getPendingDocuments();

    void verifyDocument(String adminId, String documentId, boolean verified, String remarks);

    Object getProviderStatistics();
}