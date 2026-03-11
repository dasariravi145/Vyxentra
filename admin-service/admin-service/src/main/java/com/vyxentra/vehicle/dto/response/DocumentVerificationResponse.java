package com.vyxentra.vehicle.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.vyxentra.vehicle.dto.VerificationCheckpoint;
import com.vyxentra.vehicle.dto.VerificationComment;
import com.vyxentra.vehicle.dto.VerificationHistory;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

/**
 * Response DTO for document verification in admin panel
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class DocumentVerificationResponse {

    private String documentId;
    private String providerId;
    private String providerName;
    private String businessName;
    private String providerEmail;
    private String providerPhone;

    // Document details
    private String documentType;
    private String documentTypeDisplay;
    private String documentNumber;
    private String documentUrl;
    private String thumbnailUrl;
    private String fileName;
    private String fileSize;
    private String mimeType;

    // Document status
    private Boolean isVerified;
    private Boolean isRejected;
    private Boolean isPending;
    private String verificationStatus; // PENDING, VERIFIED, REJECTED, EXPIRED

    // Verification details
    private String verifiedBy;
    private String verifiedByName;
    private Instant verifiedAt;
    private String rejectionReason;
    private String rejectionRemarks;
    private List<String> rejectionIssues;

    // Document metadata
    private String issuedBy;
    private String issuedCountry;
    private LocalDate issueDate;
    private LocalDate expiryDate;
    private Boolean isExpired;
    private Boolean isExpiringSoon;
    private Integer daysUntilExpiry;

    // Upload details
    private String uploadedBy;
    private String uploadedByName;
    private Instant uploadedAt;
    private Integer version;
    private Boolean isLatestVersion;

    // OCR/Extracted data
    private Map<String, Object> extractedData;
    private Double extractionConfidence;
    private Boolean isManualReviewRequired;

    // Verification checklist
    private List<VerificationCheckpoint> checkpoints;
    private Integer completedCheckpoints;
    private Integer totalCheckpoints;

    // Risk assessment
    private String riskLevel; // LOW, MEDIUM, HIGH
    private List<String> riskFlags;
    private String fraudProbability;

    // History
    private List<VerificationHistory> verificationHistory;

    // Comments
    private List<VerificationComment> comments;
    private Integer commentCount;

    // Timestamps
    private Instant createdAt;
    private Instant updatedAt;


    // ==================== Helper Methods ====================

    /**
     * Check if document is pending verification
     */
    public boolean isPending() {
        return "PENDING".equals(verificationStatus) || Boolean.TRUE.equals(isPending);
    }

    /**
     * Check if document is verified
     */
    public boolean isVerified() {
        return "VERIFIED".equals(verificationStatus) || Boolean.TRUE.equals(isVerified);
    }

    /**
     * Check if document is rejected
     */
    public boolean isRejected() {
        return "REJECTED".equals(verificationStatus) || Boolean.TRUE.equals(isRejected);
    }

    /**
     * Check if document is expired
     */
    public boolean isExpired() {
        return expiryDate != null && expiryDate.isBefore(LocalDate.now());
    }

    /**
     * Get document type display name
     */
    public String getDocumentTypeDisplay() {
        if (documentTypeDisplay != null) return documentTypeDisplay;

        switch (documentType) {
            case "GST_CERTIFICATE": return "GST Certificate";
            case "PAN_CARD": return "PAN Card";
            case "AADHAAR_CARD": return "Aadhaar Card";
            case "BUSINESS_REGISTRATION": return "Business Registration";
            case "BANK_STATEMENT": return "Bank Statement";
            case "INSURANCE_CERTIFICATE": return "Insurance Certificate";
            case "TRADE_LICENSE": return "Trade License";
            case "IDENTITY_PROOF": return "Identity Proof";
            case "ADDRESS_PROOF": return "Address Proof";
            default: return documentType;
        }
    }

    /**
     * Get verification progress percentage
     */
    public Integer getVerificationProgress() {
        if (totalCheckpoints == null || totalCheckpoints == 0) return 0;
        if (completedCheckpoints == null) return 0;
        return (completedCheckpoints * 100) / totalCheckpoints;
    }

    /**
     * Get status badge color for UI
     */
    public String getStatusBadgeColor() {
        if (isVerified()) return "green";
        if (isRejected()) return "red";
        if (isExpired()) return "orange";
        if (isPending()) return "yellow";
        return "gray";
    }

    /**
     * Get risk badge color
     */
    public String getRiskBadgeColor() {
        if ("HIGH".equals(riskLevel)) return "red";
        if ("MEDIUM".equals(riskLevel)) return "orange";
        return "green";
    }
}