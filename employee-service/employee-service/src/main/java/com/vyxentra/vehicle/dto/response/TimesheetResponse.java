package com.vyxentra.vehicle.dto.response;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TimesheetResponse {

    private String timesheetId;
    private String employeeId;
    private String employeeName;
    private LocalDate timesheetDate;
    private BigDecimal totalHours;
    private BigDecimal regularHours;
    private BigDecimal overtimeHours;
    private String status; // DRAFT, SUBMITTED, APPROVED, REJECTED
    private LocalDateTime submittedAt;
    private String approvedBy;
    private LocalDateTime approvedAt;
    private String rejectionReason;

    private List<TimesheetEntryResponse> entries;
}

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
class TimesheetEntryResponse {
    private String entryId;
    private String assignmentId;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private Integer durationMinutes;
    private boolean overtime;
    private String description;
}
