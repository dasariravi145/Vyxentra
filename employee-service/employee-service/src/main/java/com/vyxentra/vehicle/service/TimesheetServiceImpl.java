package com.vyxentra.vehicle.service;


import com.vyxentra.vehicle.dto.request.TimesheetEntryRequest;
import com.vyxentra.vehicle.dto.response.PageResponse;
import com.vyxentra.vehicle.dto.response.TimesheetResponse;
import com.vyxentra.vehicle.entity.Assignment;
import com.vyxentra.vehicle.entity.Employee;
import com.vyxentra.vehicle.entity.Timesheet;
import com.vyxentra.vehicle.entity.TimesheetEntry;
import com.vyxentra.vehicle.exceptions.BusinessException;
import com.vyxentra.vehicle.exceptions.ErrorCode;
import com.vyxentra.vehicle.exceptions.ResourceNotFoundException;
import com.vyxentra.vehicle.kafka.EmployeeEventProducer;
import com.vyxentra.vehicle.mapper.TimesheetMapper;
import com.vyxentra.vehicle.repository.AssignmentRepository;
import com.vyxentra.vehicle.repository.EmployeeRepository;
import com.vyxentra.vehicle.repository.TimesheetEntryRepository;
import com.vyxentra.vehicle.repository.TimesheetRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class TimesheetServiceImpl implements TimesheetService {

    private final TimesheetRepository timesheetRepository;
    private final TimesheetEntryRepository timesheetEntryRepository;
    private final AssignmentRepository assignmentRepository;
    private final EmployeeRepository employeeRepository;
    private final TimesheetMapper timesheetMapper;
    private final EmployeeEventProducer eventProducer;

    @Value("${employee.timesheet.submission-deadline-hours:24}")
    private int submissionDeadlineHours;

    @Override
    @Transactional
    public TimesheetResponse addTimesheetEntry(String employeeId, TimesheetEntryRequest request) {
        log.info("Adding timesheet entry for employee: {}", employeeId);

        // Validate times are within same day and not in future
        LocalDate entryDate = request.getStartTime().toLocalDate();
        if (!entryDate.equals(request.getEndTime().toLocalDate())) {
            throw new BusinessException(ErrorCode.VALIDATION_ERROR, "Timesheet entry must be within same day");
        }

        if (request.getStartTime().isAfter(request.getEndTime())) {
            throw new BusinessException(ErrorCode.VALIDATION_ERROR, "Start time must be before end time");
        }

        if (request.getEndTime().isAfter(LocalDateTime.now())) {
            throw new BusinessException(ErrorCode.VALIDATION_ERROR, "Cannot add future timesheet entries");
        }

        // Get or create timesheet for the day
        Timesheet timesheet = timesheetRepository.findByEmployeeIdAndTimesheetDate(employeeId, entryDate)
                .orElseGet(() -> createTimesheet(employeeId, entryDate));

        // Verify timesheet is in DRAFT state
        if (!"DRAFT".equals(timesheet.getStatus())) {
            throw new BusinessException(ErrorCode.VALIDATION_ERROR,
                    "Cannot modify timesheet in " + timesheet.getStatus() + " status");
        }

        // Get assignment and verify it belongs to employee
        Assignment assignment = assignmentRepository.findById(request.getAssignmentId())
                .orElseThrow(() -> new ResourceNotFoundException("Assignment", request.getAssignmentId()));

        if (!assignment.getEmployee().getId().equals(employeeId)) {
            throw new BusinessException(ErrorCode.UNAUTHORIZED, "Assignment does not belong to this employee");
        }

        // Check for overlapping entries
        boolean hasOverlap = timesheet.getEntries().stream()
                .anyMatch(existing -> isOverlapping(existing, request));
        if (hasOverlap) {
            throw new BusinessException(ErrorCode.VALIDATION_ERROR, "Timesheet entry overlaps with existing entry");
        }

        // Calculate duration
        int durationMinutes = (int) java.time.Duration.between(request.getStartTime(), request.getEndTime()).toMinutes();

        // Determine if overtime (after 8 hours for the day)
        boolean isOvertime = false;
        int totalMinutesForDay = timesheet.getEntries().stream()
                .mapToInt(TimesheetEntry::getDurationMinutes)
                .sum() + durationMinutes;

        if (totalMinutesForDay > 8 * 60) {
            isOvertime = true;
        }

        // Create entry
        TimesheetEntry entry = TimesheetEntry.builder()
                .timesheet(timesheet)
                .assignment(assignment)
                .startTime(request.getStartTime())
                .endTime(request.getEndTime())
                .durationMinutes(durationMinutes)
                .isOvertime(isOvertime)
                .description(request.getDescription())
                .build();

        timesheet.getEntries().add(entry);
        timesheetEntryRepository.save(entry);

        // Update timesheet totals
        updateTimesheetTotals(timesheet);

        log.info("Timesheet entry added successfully");

        return timesheetMapper.toResponse(timesheet);
    }

    @Override
    @Transactional(readOnly = true)
    public List<TimesheetResponse> getEmployeeTimesheets(String employeeId, LocalDate fromDate, LocalDate toDate) {
        log.debug("Getting timesheets for employee: {} from {} to {}", employeeId, fromDate, toDate);

        List<Timesheet> timesheets = timesheetRepository.findByEmployeeIdAndTimesheetDateBetween(
                employeeId, fromDate, toDate);

        return timesheetMapper.toResponseList(timesheets);
    }

    @Override
    @Transactional(readOnly = true)
    public TimesheetResponse getTimesheetByDate(String employeeId, LocalDate date) {
        log.debug("Getting timesheet for employee: {} on date: {}", employeeId, date);

        Timesheet timesheet = timesheetRepository.findByEmployeeIdAndTimesheetDate(employeeId, date)
                .orElseThrow(() -> new ResourceNotFoundException("Timesheet", "date", date.toString()));

        return timesheetMapper.toResponse(timesheet);
    }

    @Override
    @Transactional
    public void submitTimesheet(String timesheetId, String employeeId) {
        log.info("Submitting timesheet: {} for employee: {}", timesheetId, employeeId);

        Timesheet timesheet = findTimesheetById(timesheetId);

        // Verify timesheet belongs to employee
        if (!timesheet.getEmployee().getId().equals(employeeId)) {
            throw new BusinessException(ErrorCode.UNAUTHORIZED, "Not authorized to submit this timesheet");
        }

        // Verify timesheet is in DRAFT state
        if (!"DRAFT".equals(timesheet.getStatus())) {
            throw new BusinessException(ErrorCode.VALIDATION_ERROR,
                    "Timesheet already " + timesheet.getStatus());
        }

        // Verify submission deadline
        LocalDate yesterday = LocalDate.now().minusDays(1);
        if (timesheet.getTimesheetDate().isBefore(yesterday)) {
            throw new BusinessException(ErrorCode.VALIDATION_ERROR,
                    "Cannot submit timesheet after deadline");
        }

        timesheet.setStatus("SUBMITTED");
        timesheet.setSubmittedAt(LocalDateTime.now());
        timesheetRepository.save(timesheet);

        // Publish event
        eventProducer.publishTimesheetSubmitted(timesheetId, employeeId);

        log.info("Timesheet submitted: {}", timesheetId);
    }

    @Override
    @Transactional
    public void approveTimesheet(String timesheetId, String providerId) {
        log.info("Approving timesheet: {} by provider: {}", timesheetId, providerId);

        Timesheet timesheet = findTimesheetById(timesheetId);

        // Verify timesheet is in SUBMITTED state
        if (!"SUBMITTED".equals(timesheet.getStatus())) {
            throw new BusinessException(ErrorCode.VALIDATION_ERROR,
                    "Timesheet must be in SUBMITTED state to approve");
        }

        timesheet.setStatus("APPROVED");
        timesheet.setApprovedBy(providerId);
        timesheet.setApprovedAt(LocalDateTime.now());
        timesheetRepository.save(timesheet);

        // Publish event
        eventProducer.publishTimesheetApproved(timesheetId, providerId);

        log.info("Timesheet approved: {}", timesheetId);
    }

    @Override
    @Transactional
    public void rejectTimesheet(String timesheetId, String providerId, String reason) {
        log.info("Rejecting timesheet: {} by provider: {}", timesheetId, providerId);

        Timesheet timesheet = findTimesheetById(timesheetId);

        // Verify timesheet is in SUBMITTED state
        if (!"SUBMITTED".equals(timesheet.getStatus())) {
            throw new BusinessException(ErrorCode.VALIDATION_ERROR,
                    "Timesheet must be in SUBMITTED state to reject");
        }

        timesheet.setStatus("REJECTED");
        timesheet.setRejectionReason(reason);
        timesheetRepository.save(timesheet);

        // Publish event
        eventProducer.publishTimesheetRejected(timesheetId, providerId, reason);

        log.info("Timesheet rejected: {}", timesheetId);
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<TimesheetResponse> getPendingTimesheets(String providerId, LocalDate fromDate,
                                                                LocalDate toDate, Pageable pageable) {
        log.debug("Getting pending timesheets for provider: {}", providerId);

        Page<Timesheet> page = timesheetRepository.findPendingByProviderId(providerId, fromDate, toDate, pageable);

        return PageResponse.<TimesheetResponse>builder()
                .content(timesheetMapper.toResponseList(page.getContent()))
                .pageNumber(page.getNumber())
                .pageSize(page.getSize())
                .totalElements(page.getTotalElements())
                .totalPages(page.getTotalPages())
                .first(page.isFirst())
                .last(page.isLast())
                .hasNext(page.hasNext())
                .hasPrevious(page.hasPrevious())
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public TimesheetResponse getTimesheetSummary(String employeeId, LocalDate fromDate, LocalDate toDate) {
        log.debug("Getting timesheet summary for employee: {} from {} to {}", employeeId, fromDate, toDate);

        Double totalHours = timesheetRepository.getTotalHoursInRange(employeeId, fromDate, toDate);

        // Create a summary response
        return TimesheetResponse.builder()
                .employeeId(employeeId)
                .totalHours(totalHours != null ? BigDecimal.valueOf(totalHours) : BigDecimal.ZERO)
                .build();
    }

    private Timesheet createTimesheet(String employeeId, LocalDate date) {
        Employee employee = employeeRepository.findById(employeeId)
                .orElseThrow(() -> new ResourceNotFoundException("Employee", employeeId));

        Timesheet timesheet = Timesheet.builder()
                .employee(employee)
                .timesheetDate(date)
                .totalHours(BigDecimal.ZERO)
                .regularHours(BigDecimal.ZERO)
                .overtimeHours(BigDecimal.ZERO)
                .status("DRAFT")
                .build();

        return timesheetRepository.save(timesheet);
    }

    private void updateTimesheetTotals(Timesheet timesheet) {
        int totalMinutes = timesheet.getEntries().stream()
                .mapToInt(TimesheetEntry::getDurationMinutes)
                .sum();

        int regularMinutes = timesheet.getEntries().stream()
                .filter(e -> !e.getIsOvertime())
                .mapToInt(TimesheetEntry::getDurationMinutes)
                .sum();

        int overtimeMinutes = timesheet.getEntries().stream()
                .filter(TimesheetEntry::getIsOvertime)
                .mapToInt(TimesheetEntry::getDurationMinutes)
                .sum();

        timesheet.setTotalHours(BigDecimal.valueOf(totalMinutes / 60.0));
        timesheet.setRegularHours(BigDecimal.valueOf(regularMinutes / 60.0));
        timesheet.setOvertimeHours(BigDecimal.valueOf(overtimeMinutes / 60.0));

        timesheetRepository.save(timesheet);
    }

    private boolean isOverlapping(TimesheetEntry existing, TimesheetEntryRequest newEntry) {
        return !(newEntry.getEndTime().isBefore(existing.getStartTime()) ||
                newEntry.getStartTime().isAfter(existing.getEndTime()));
    }

    private Timesheet findTimesheetById(String timesheetId) {
        return timesheetRepository.findById(timesheetId)
                .orElseThrow(() -> new ResourceNotFoundException("Timesheet", timesheetId));
    }
}
