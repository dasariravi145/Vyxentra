package com.vyxentra.vehicle.controller;


import com.vyxentra.vehicle.dto.request.TimesheetEntryRequest;
import com.vyxentra.vehicle.dto.response.ApiResponse;
import com.vyxentra.vehicle.dto.response.PageResponse;
import com.vyxentra.vehicle.dto.response.TimesheetResponse;
import com.vyxentra.vehicle.service.TimesheetService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/v1/employees/timesheets")
@RequiredArgsConstructor
public class TimesheetController {

    private final TimesheetService timesheetService;

    @PostMapping("/entries")
    public ResponseEntity<ApiResponse<TimesheetResponse>> addTimesheetEntry(
            @RequestHeader("X-User-ID") String employeeId,
            @Valid @RequestBody TimesheetEntryRequest request) {
        log.info("Adding timesheet entry for employee: {}", employeeId);
        TimesheetResponse response = timesheetService.addTimesheetEntry(employeeId, request);
        return ResponseEntity.ok(ApiResponse.success(response, "Timesheet entry added"));
    }

    @GetMapping("/my")
    public ResponseEntity<ApiResponse<List<TimesheetResponse>>> getMyTimesheets(
            @RequestHeader("X-User-ID") String employeeId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fromDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate toDate) {
        log.info("Getting timesheets for employee: {} from {} to {}", employeeId, fromDate, toDate);
        List<TimesheetResponse> responses = timesheetService.getEmployeeTimesheets(employeeId, fromDate, toDate);
        return ResponseEntity.ok(ApiResponse.success(responses));
    }

    @GetMapping("/employee/{employeeId}")
    public ResponseEntity<ApiResponse<List<TimesheetResponse>>> getEmployeeTimesheets(
            @PathVariable String employeeId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fromDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate toDate,
            @RequestHeader("X-User-ID") String providerId) {
        log.info("Provider {} getting timesheets for employee: {}", providerId, employeeId);
        List<TimesheetResponse> responses = timesheetService.getEmployeeTimesheets(employeeId, fromDate, toDate);
        return ResponseEntity.ok(ApiResponse.success(responses));
    }

    @GetMapping("/date/{date}")
    public ResponseEntity<ApiResponse<TimesheetResponse>> getTimesheetByDate(
            @RequestHeader("X-User-ID") String employeeId,
            @PathVariable @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        log.info("Getting timesheet for employee: {} on date: {}", employeeId, date);
        TimesheetResponse response = timesheetService.getTimesheetByDate(employeeId, date);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @PostMapping("/{timesheetId}/submit")
    public ResponseEntity<ApiResponse<Void>> submitTimesheet(
            @PathVariable String timesheetId,
            @RequestHeader("X-User-ID") String employeeId) {
        log.info("Submitting timesheet: {} for employee: {}", timesheetId, employeeId);
        timesheetService.submitTimesheet(timesheetId, employeeId);
        return ResponseEntity.ok(ApiResponse.success(null, "Timesheet submitted"));
    }

    @PostMapping("/{timesheetId}/approve")
    public ResponseEntity<ApiResponse<Void>> approveTimesheet(
            @PathVariable String timesheetId,
            @RequestHeader("X-User-ID") String providerId) {
        log.info("Approving timesheet: {} by provider: {}", timesheetId, providerId);
        timesheetService.approveTimesheet(timesheetId, providerId);
        return ResponseEntity.ok(ApiResponse.success(null, "Timesheet approved"));
    }

    @PostMapping("/{timesheetId}/reject")
    public ResponseEntity<ApiResponse<Void>> rejectTimesheet(
            @PathVariable String timesheetId,
            @RequestParam String reason,
            @RequestHeader("X-User-ID") String providerId) {
        log.info("Rejecting timesheet: {} by provider: {}", timesheetId, providerId);
        timesheetService.rejectTimesheet(timesheetId, providerId, reason);
        return ResponseEntity.ok(ApiResponse.success(null, "Timesheet rejected"));
    }

    @GetMapping("/pending")
    public ResponseEntity<ApiResponse<PageResponse<TimesheetResponse>>> getPendingTimesheets(
            @RequestHeader("X-User-ID") String providerId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fromDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate toDate,
            Pageable pageable) {
        log.info("Getting pending timesheets for provider: {}", providerId);
        PageResponse<TimesheetResponse> response = timesheetService.getPendingTimesheets(
                providerId, fromDate, toDate, pageable);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/summary")
    public ResponseEntity<ApiResponse<TimesheetResponse>> getTimesheetSummary(
            @RequestHeader("X-User-ID") String employeeId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fromDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate toDate) {
        log.info("Getting timesheet summary for employee: {} from {} to {}", employeeId, fromDate, toDate);
        TimesheetResponse response = timesheetService.getTimesheetSummary(employeeId, fromDate, toDate);
        return ResponseEntity.ok(ApiResponse.success(response));
    }
}
