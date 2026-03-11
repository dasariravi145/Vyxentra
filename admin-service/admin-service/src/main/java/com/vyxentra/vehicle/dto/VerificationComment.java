package com.vyxentra.vehicle.dto;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public  class VerificationComment {
    private String commentId;
    private String comment;
    private String createdBy;
    private String createdByName;
    private Instant createdAt;
    private Boolean isAdminComment;
    private Boolean isInternal;
}