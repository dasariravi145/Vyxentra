package com.vyxentra.vehicle.dto.response;


import com.vyxentra.vehicle.enums.ProviderStatus;
import com.vyxentra.vehicle.enums.ProviderType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.List;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProviderApprovalResponse {

    private String approvalId;
    private String providerId;
    private String businessName;
    private ProviderType providerType;
    private String ownerName;
    private String email;
    private String phone;
    private ProviderStatus status;
    private String gstNumber;
    private String panNumber;
    private String city;
    private String state;

    private List<DocumentVerification> documents;
    private boolean allDocumentsUploaded;
    private boolean allDocumentsVerified;
    private List<String> pendingDocuments;
    private List<String> verifiedDocuments;

    private String assignedAdmin;
    private String assignedAdminName;
    private Instant assignedAt;
    private String reviewedBy;
    private String reviewedByName;
    private Instant reviewedAt;
    private String approvalComments;
    private String rejectionReason;
    private Double customCommission;
    private Integer priority;

    private Map<String, Boolean> verificationChecklist;
    private List<String> verificationNotes;

    private Instant submittedAt;
    private Instant lastUpdatedAt;
    private Instant expectedReviewBy;

    private String riskLevel;
    private Double riskScore;
    private List<String> riskFactors;

    private List<ApprovalHistory> history;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class DocumentVerification {
        private String documentId;
        private String documentType;
        private String documentName;
        private String documentUrl;
        private String documentNumber;
        private boolean verified;
        private Instant verifiedAt;
        private String verifiedBy;
        private String remarks;
        private List<String> issues;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ApprovalHistory {
        private String id;
        private String action;
        private String performedBy;
        private String performedByName;
        private Instant performedAt;
        private String comments;
    }
}
