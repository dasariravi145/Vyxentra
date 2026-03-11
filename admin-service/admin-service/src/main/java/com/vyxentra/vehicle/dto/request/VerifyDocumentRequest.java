package com.vyxentra.vehicle.dto.request;

import com.vyxentra.vehicle.dto.VerificationCheckpoint;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public  class VerifyDocumentRequest {
    private String remarks;
    private Boolean markAsVerified;
    private List<VerificationCheckpoint> checkpoints;
    private Boolean notifyProvider;
}
