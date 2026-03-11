package com.vyxentra.vehicle.dto.request;


import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProviderApprovalRequest {

    @NotBlank(message = "Provider ID is required")
    private String providerId;

    private String comments;

    private Boolean approveServices;

    private Double customCommission;

    private String rejectionReason;

    private Boolean sendNotification;

    private List<String> approvedDocuments;

    private List<String> rejectedDocuments;

    private Map<String, String> documentRemarks;

    private Integer priority;

    private String assignedTo;
}
