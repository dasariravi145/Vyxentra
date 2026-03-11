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
public  class VerificationCheckpoint {
    private String checkpointId;
    private String name;
    private String description;
    private Boolean isCompleted;
    private String completedBy;
    private Instant completedAt;
    private String remarks;
}