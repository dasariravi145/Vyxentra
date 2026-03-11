package com.vyxentra.vehicle.service;


import com.vyxentra.vehicle.client.BookingServiceClient;
import com.vyxentra.vehicle.dto.request.AssignmentRequest;
import com.vyxentra.vehicle.dto.response.AssignmentResponse;
import com.vyxentra.vehicle.entity.Assignment;
import com.vyxentra.vehicle.entity.Employee;
import com.vyxentra.vehicle.exceptions.BusinessException;
import com.vyxentra.vehicle.exceptions.ErrorCode;
import com.vyxentra.vehicle.exceptions.ResourceNotFoundException;
import com.vyxentra.vehicle.kafka.EmployeeEventProducer;
import com.vyxentra.vehicle.mapper.AssignmentMapper;
import com.vyxentra.vehicle.repository.AssignmentRepository;
import com.vyxentra.vehicle.repository.EmployeeRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class AssignmentServiceImpl implements AssignmentService {

    private final AssignmentRepository assignmentRepository;
    private final EmployeeRepository employeeRepository;
    private final AssignmentMapper assignmentMapper;
    private final EmployeeEventProducer eventProducer;
    private final BookingServiceClient bookingServiceClient;

    @Value("${employee.max-assignments-per-day:8}")
    private int maxAssignmentsPerDay;

    @Override
    @Transactional
    public AssignmentResponse assignEmployee(String providerId, AssignmentRequest request) {
        log.info("Assigning employee {} to booking {}", request.getEmployeeId(), request.getBookingId());

        // Check if booking already has an assignment
        if (assignmentRepository.findByBookingId(request.getBookingId()).isPresent()) {
            throw new BusinessException(ErrorCode.VALIDATION_ERROR, "Booking already has an assigned employee");
        }

        // Get employee
        Employee employee = employeeRepository.findById(request.getEmployeeId())
                .orElseThrow(() -> new ResourceNotFoundException("Employee", request.getEmployeeId()));

        // Verify employee belongs to provider
        if (!employee.getProviderId().equals(providerId)) {
            throw new BusinessException(ErrorCode.UNAUTHORIZED, "Employee does not belong to your provider");
        }

        // Verify employee is active
        if (!"ACTIVE".equals(employee.getStatus())) {
            throw new BusinessException(ErrorCode.VALIDATION_ERROR, "Employee is not active");
        }

        // Check daily assignment limit
        int todayAssignments = assignmentRepository.countTodayAssignments(employee.getId());
        if (todayAssignments >= maxAssignmentsPerDay) {
            throw new BusinessException(ErrorCode.VALIDATION_ERROR,
                    "Employee has reached maximum assignments for today");
        }

        // Verify booking exists and is in correct state (call to booking service)
        // This would be done via Feign client

        // Create assignment
        Assignment assignment = Assignment.builder()
                .employee(employee)
                .bookingId(request.getBookingId())
                .providerId(providerId)
                .assignedBy(providerId)
                .assignedAt(LocalDateTime.now())
                .status("ASSIGNED")
                .estimatedDuration(request.getEstimatedDuration())
                .build();

        assignment = assignmentRepository.save(assignment);

        // Update booking with employee assignment (call to booking service)
        // bookingServiceClient.assignEmployee(request.getBookingId(), employee.getId());

        // Publish event
        eventProducer.publishEmployeeAssigned(assignment.getId(), employee.getId(), request.getBookingId());

        log.info("Employee assigned successfully: {}", assignment.getId());

        return assignmentMapper.toResponse(assignment);
    }

    @Override
    @Transactional(readOnly = true)
    public AssignmentResponse getAssignment(String assignmentId) {
        Assignment assignment = findAssignmentById(assignmentId);
        return assignmentMapper.toResponse(assignment);
    }

    @Override
    @Transactional(readOnly = true)
    public AssignmentResponse getAssignmentByBookingId(String bookingId) {
        Assignment assignment = assignmentRepository.findByBookingId(bookingId)
                .orElseThrow(() -> new ResourceNotFoundException("Assignment", "booking", bookingId));
        return assignmentMapper.toResponse(assignment);
    }

    @Override
    @Transactional(readOnly = true)
    public List<AssignmentResponse> getEmployeeAssignments(String employeeId, LocalDate fromDate, LocalDate toDate) {
        LocalDateTime start = fromDate != null ? fromDate.atStartOfDay() : LocalDateTime.MIN;
        LocalDateTime end = toDate != null ? toDate.atTime(LocalTime.MAX) : LocalDateTime.MAX;

        List<Assignment> assignments = assignmentRepository.findByEmployeeIdAndDateRange(employeeId, start, end);
        return assignmentMapper.toResponseList(assignments);
    }

    @Override
    @Transactional(readOnly = true)
    public AssignmentResponse getCurrentAssignment(String employeeId) {
        List<Assignment> assignments = assignmentRepository.findCurrentAssignments(employeeId);
        if (assignments.isEmpty()) {
            return null;
        }
        return assignmentMapper.toResponse(assignments.get(0));
    }

    @Override
    @Transactional
    public void startAssignment(String assignmentId, String employeeId) {
        log.info("Starting assignment: {} for employee: {}", assignmentId, employeeId);

        Assignment assignment = findAssignmentById(assignmentId);

        // Verify employee owns this assignment
        if (!assignment.getEmployee().getId().equals(employeeId)) {
            throw new BusinessException(ErrorCode.UNAUTHORIZED, "Not authorized to start this assignment");
        }

        // Verify assignment is in ASSIGNED state
        if (!"ASSIGNED".equals(assignment.getStatus())) {
            throw new BusinessException(ErrorCode.VALIDATION_ERROR,
                    "Assignment cannot be started. Current status: " + assignment.getStatus());
        }

        // Update assignment
        assignment.setStatus("STARTED");
        assignment.setStartedAt(LocalDateTime.now());
        assignmentRepository.save(assignment);

        // Update booking status (call to booking service)
        // bookingServiceClient.updateBookingStatus(assignment.getBookingId(), "IN_PROGRESS");

        // Publish event
        eventProducer.publishServiceStarted(assignment.getBookingId(), employeeId);

        log.info("Assignment started: {}", assignmentId);
    }

    @Override
    @Transactional
    public void completeAssignment(String assignmentId, String employeeId, String notes) {
        log.info("Completing assignment: {} for employee: {}", assignmentId, employeeId);

        Assignment assignment = findAssignmentById(assignmentId);

        // Verify employee owns this assignment
        if (!assignment.getEmployee().getId().equals(employeeId)) {
            throw new BusinessException(ErrorCode.UNAUTHORIZED, "Not authorized to complete this assignment");
        }

        // Verify assignment is in STARTED state
        if (!"STARTED".equals(assignment.getStatus())) {
            throw new BusinessException(ErrorCode.VALIDATION_ERROR,
                    "Assignment cannot be completed. Current status: " + assignment.getStatus());
        }

        // Calculate actual duration
        LocalDateTime now = LocalDateTime.now();
        int actualDuration = 0;
        if (assignment.getStartedAt() != null) {
            actualDuration = (int) java.time.Duration.between(assignment.getStartedAt(), now).toMinutes();
        }

        // Update assignment
        assignment.setStatus("COMPLETED");
        assignment.setCompletedAt(now);
        assignment.setActualDuration(actualDuration);
        assignment.setNotes(notes);
        assignmentRepository.save(assignment);

        // Increment employee's service count
        employeeRepository.incrementServicesCompleted(employeeId);

        // Update booking status (call to booking service)
        // bookingServiceClient.updateBookingStatus(assignment.getBookingId(), "COMPLETED");

        // Publish event
        eventProducer.publishServiceCompleted(assignment.getBookingId(), employeeId, actualDuration);

        log.info("Assignment completed: {}", assignmentId);
    }

    @Override
    @Transactional
    public void cancelAssignment(String assignmentId, String providerId, String reason) {
        log.info("Cancelling assignment: {} by provider: {}", assignmentId, providerId);

        Assignment assignment = findAssignmentById(assignmentId);

        // Verify provider owns this assignment
        if (!assignment.getProviderId().equals(providerId)) {
            throw new BusinessException(ErrorCode.UNAUTHORIZED, "Not authorized to cancel this assignment");
        }

        // Update assignment
        assignment.setStatus("CANCELLED");
        assignment.setNotes(reason);
        assignmentRepository.save(assignment);

        // Update booking status (call to booking service)
        // bookingServiceClient.updateBookingStatus(assignment.getBookingId(), "CANCELLED");

        // Publish event
        eventProducer.publishAssignmentCancelled(assignmentId, assignment.getBookingId(), reason);

        log.info("Assignment cancelled: {}", assignmentId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<AssignmentResponse> getTodayAssignments(String providerId) {
        log.debug("Getting today's assignments for provider: {}", providerId);

        LocalDateTime startOfDay = LocalDate.now().atStartOfDay();
        LocalDateTime endOfDay = LocalDate.now().atTime(LocalTime.MAX);

        List<Assignment> assignments = assignmentRepository.findByProviderId(providerId).stream()
                .filter(a -> a.getAssignedAt().isAfter(startOfDay) && a.getAssignedAt().isBefore(endOfDay))
                .toList();

        return assignmentMapper.toResponseList(assignments);
    }

    private Assignment findAssignmentById(String assignmentId) {
        return assignmentRepository.findById(assignmentId)
                .orElseThrow(() -> new ResourceNotFoundException("Assignment", assignmentId));
    }
}
