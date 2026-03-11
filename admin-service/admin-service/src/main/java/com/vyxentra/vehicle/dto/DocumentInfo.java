package com.vyxentra.vehicle.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public  class DocumentInfo {
    private String documentId;
    private String documentType;
    private String documentUrl;
    private String documentNumber;
    private Boolean verified;
    private LocalDateTime verifiedAt;
    private String verifiedBy;
    private String remarks;
}
