package com.vyxentra.vehicle.dto.response;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AssignmentResponse {

    private String assignmentId;
    private String employeeId;
    private String employeeName;
    private String bookingId;
    private String providerId;
    private String assignedBy;
    private LocalDateTime assignedAt;
    private LocalDateTime startedAt;
    private LocalDateTime completedAt;
    private String status; // ASSIGNED, STARTED, COMPLETED, CANCELLED
    private Integer estimatedDuration;
    private Integer actualDuration;
    private String notes;
}
