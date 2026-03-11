package com.vyxentra.vehicle.service;


import com.vyxentra.vehicle.dto.ApprovalStatistics;
import com.vyxentra.vehicle.dto.request.ProviderApprovalRequest;
import com.vyxentra.vehicle.dto.response.ProviderApprovalResponse;
import com.vyxentra.vehicle.dto.response.ProviderResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface ProviderApprovalService {

    Page<ProviderApprovalResponse> getPendingApprovals(Pageable pageable);

    ProviderApprovalResponse getApprovalDetails(String providerId);

    ProviderResponse approveProvider(String adminId, ProviderApprovalRequest request);

    ProviderResponse rejectProvider(String adminId, String providerId, String reason);

    ProviderResponse suspendProvider(String adminId, String providerId, String reason);

    ProviderResponse activateProvider(String adminId, String providerId);

    ApprovalStatistics getApprovalStatistics();

    void verifyDocuments(String adminId, String providerId, List<String> documentIds, boolean verified);

    boolean isProviderApproved(String providerId);

    List<ProviderApprovalResponse> getApprovalHistory(String providerId);

    void assignApproval(String providerId, String adminId);
}