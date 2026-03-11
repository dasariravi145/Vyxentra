package com.vyxentra.vehicle.service;


import com.vyxentra.vehicle.dto.request.AssignmentRequest;
import com.vyxentra.vehicle.dto.response.AssignmentResponse;

import java.time.LocalDate;
import java.util.List;

public interface AssignmentService {

    AssignmentResponse assignEmployee(String providerId, AssignmentRequest request);

    AssignmentResponse getAssignment(String assignmentId);

    AssignmentResponse getAssignmentByBookingId(String bookingId);

    List<AssignmentResponse> getEmployeeAssignments(String employeeId, LocalDate fromDate, LocalDate toDate);

    AssignmentResponse getCurrentAssignment(String employeeId);

    void startAssignment(String assignmentId, String employeeId);

    void completeAssignment(String assignmentId, String employeeId, String notes);

    void cancelAssignment(String assignmentId, String providerId, String reason);

    List<AssignmentResponse> getTodayAssignments(String providerId);
}
