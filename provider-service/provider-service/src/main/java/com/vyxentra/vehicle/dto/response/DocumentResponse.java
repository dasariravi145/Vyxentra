package com.vyxentra.vehicle.dto.response;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DocumentResponse {

    private String documentId;
    private String providerId;
    private String documentType;
    private String documentName;
    private String documentUrl;
    private String documentNumber;
    private boolean verified;
    private Instant verifiedAt;
    private String verifiedBy;
    private String remarks;
    private Instant expiryDate;
    private Instant uploadedAt;
    private int version;
    private boolean isExpired;
}
