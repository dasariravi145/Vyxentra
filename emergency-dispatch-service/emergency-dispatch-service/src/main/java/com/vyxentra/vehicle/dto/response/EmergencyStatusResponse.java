package com.vyxentra.vehicle.dto.response;

import com.vyxentra.vehicle.dto.AssignmentInfo;
import com.vyxentra.vehicle.enums.EmergencyStatus;
import com.vyxentra.vehicle.enums.EmergencyType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EmergencyStatusResponse {

    private String requestId;
    private String requestNumber;
    private EmergencyStatus status;
    private EmergencyType emergencyType;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private LocalDateTime expiryTime;
    private AssignmentInfo assignment;
    private String statusDescription;
    private Integer timeElapsedMinutes;
    private Integer timeRemainingMinutes;
}
