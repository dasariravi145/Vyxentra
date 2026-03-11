package com.vyxentra.vehicle.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EmployeeDetailResponse {

    private String employeeId;
    private String userId;
    private String providerId;
    private String employeeCode;
    private String firstName;
    private String lastName;
    private String fullName;
    private String email;
    private String phone;
    private String alternatePhone;
    private LocalDate dateOfBirth;
    private String gender;
    private String addressLine1;
    private String addressLine2;
    private String city;
    private String state;
    private String postalCode;
    private String country;
    private String profilePicture;
    private String designation;
    private String department;
    private String employmentType;
    private LocalDate joiningDate;
    private LocalDate exitDate;
    private String status;
    private Double averageRating;
    private Integer totalServicesCompleted;

    private List<SkillResponse> skills;
    private AssignmentResponse currentAssignment;
    private List<AssignmentResponse> recentAssignments;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
