package com.vyxentra.vehicle.controller;


import com.vyxentra.vehicle.dto.request.AssignmentRequest;
import com.vyxentra.vehicle.dto.response.ApiResponse;
import com.vyxentra.vehicle.dto.response.AssignmentResponse;
import com.vyxentra.vehicle.service.AssignmentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/v1/employees/assignments")
@RequiredArgsConstructor
public class AssignmentController {

    private final AssignmentService assignmentService;

    @PostMapping
    public ResponseEntity<ApiResponse<AssignmentResponse>> assignEmployee(
            @RequestHeader("X-User-ID") String providerId,
            @Valid @RequestBody AssignmentRequest request) {
        log.info("Assigning employee {} to booking {}", request.getEmployeeId(), request.getBookingId());
        AssignmentResponse response = assignmentService.assignEmployee(providerId, request);
        return ResponseEntity.ok(ApiResponse.success(response, "Employee assigned successfully"));
    }

    @GetMapping("/{assignmentId}")
    public ResponseEntity<ApiResponse<AssignmentResponse>> getAssignment(
            @PathVariable String assignmentId) {
        log.info("Getting assignment: {}", assignmentId);
        AssignmentResponse response = assignmentService.getAssignment(assignmentId);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/booking/{bookingId}")
    public ResponseEntity<ApiResponse<AssignmentResponse>> getAssignmentByBooking(
            @PathVariable String bookingId) {
        log.info("Getting assignment for booking: {}", bookingId);
        AssignmentResponse response = assignmentService.getAssignmentByBookingId(bookingId);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/employee/{employeeId}")
    public ResponseEntity<ApiResponse<List<AssignmentResponse>>> getEmployeeAssignments(
            @PathVariable String employeeId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fromDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate toDate) {
        log.info("Getting assignments for employee: {}", employeeId);
        List<AssignmentResponse> responses = assignmentService.getEmployeeAssignments(employeeId, fromDate, toDate);
        return ResponseEntity.ok(ApiResponse.success(responses));
    }

    @GetMapping("/employee/{employeeId}/current")
    public ResponseEntity<ApiResponse<AssignmentResponse>> getCurrentAssignment(
            @PathVariable String employeeId) {
        log.info("Getting current assignment for employee: {}", employeeId);
        AssignmentResponse response = assignmentService.getCurrentAssignment(employeeId);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @PatchMapping("/{assignmentId}/start")
    public ResponseEntity<ApiResponse<Void>> startAssignment(
            @PathVariable String assignmentId,
            @RequestHeader("X-User-ID") String employeeId) {
        log.info("Starting assignment: {} for employee: {}", assignmentId, employeeId);
        assignmentService.startAssignment(assignmentId, employeeId);
        return ResponseEntity.ok(ApiResponse.success(null, "Service started"));
    }

    @PatchMapping("/{assignmentId}/complete")
    public ResponseEntity<ApiResponse<Void>> completeAssignment(
            @PathVariable String assignmentId,
            @RequestHeader("X-User-ID") String employeeId,
            @RequestParam(required = false) String notes) {
        log.info("Completing assignment: {} for employee: {}", assignmentId, employeeId);
        assignmentService.completeAssignment(assignmentId, employeeId, notes);
        return ResponseEntity.ok(ApiResponse.success(null, "Service completed"));
    }

    @PatchMapping("/{assignmentId}/cancel")
    public ResponseEntity<ApiResponse<Void>> cancelAssignment(
            @PathVariable String assignmentId,
            @RequestHeader("X-User-ID") String providerId,
            @RequestParam String reason) {
        log.info("Cancelling assignment: {} by provider: {}", assignmentId, providerId);
        assignmentService.cancelAssignment(assignmentId, providerId, reason);
        return ResponseEntity.ok(ApiResponse.success(null, "Assignment cancelled"));
    }

    @GetMapping("/provider/{providerId}/today")
    public ResponseEntity<ApiResponse<List<AssignmentResponse>>> getTodayAssignments(
            @PathVariable String providerId) {
        log.info("Getting today's assignments for provider: {}", providerId);
        List<AssignmentResponse> responses = assignmentService.getTodayAssignments(providerId);
        return ResponseEntity.ok(ApiResponse.success(responses));
    }
}
