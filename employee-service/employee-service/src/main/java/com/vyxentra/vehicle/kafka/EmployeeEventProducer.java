package com.vyxentra.vehicle.kafka;


import com.vyxentra.vehicle.constants.ServiceConstants;
import com.vyxentra.vehicle.events.BaseEvent;
import com.vyxentra.vehicle.events.ServiceEvent;
import com.vyxentra.vehicle.utils.CorrelationIdUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Component;

import java.util.concurrent.CompletableFuture;

@Slf4j
@Component
@RequiredArgsConstructor
public class EmployeeEventProducer {

    private final KafkaTemplate<String, BaseEvent> kafkaTemplate;

    public void publishEmployeeRegistered(String employeeId, String userId, String providerId) {
        ServiceEvent event = ServiceEvent.builder()
                .employeeId(employeeId)
                .providerId(providerId)
                .userId(userId)
                .eventType("EMPLOYEE_REGISTERED")
                .correlationId(CorrelationIdUtil.getCurrentCorrelationId())
                .source("employee-service")
                .build();

        publishEvent("employee.registered", event);
    }

    public void publishEmployeeUpdated(String employeeId, String userId) {
        ServiceEvent event = ServiceEvent.builder()
                .employeeId(employeeId)
                .userId(userId)
                .eventType("EMPLOYEE_UPDATED")
                .correlationId(CorrelationIdUtil.getCurrentCorrelationId())
                .source("employee-service")
                .build();

        publishEvent("employee.updated", event);
    }

    public void publishEmployeeStatusChanged(String employeeId, String status, String providerId) {
        ServiceEvent event = ServiceEvent.builder()
                .employeeId(employeeId)
                .providerId(providerId)
                .status(status)
                .eventType("EMPLOYEE_STATUS_CHANGED")
                .correlationId(CorrelationIdUtil.getCurrentCorrelationId())
                .source("employee-service")
                .build();

        publishEvent("employee.status", event);
    }

    public void publishEmployeeAssigned(String assignmentId, String employeeId, String bookingId) {
        ServiceEvent event = ServiceEvent.builder()
                .employeeId(employeeId)
                .bookingId(bookingId)
                .status("ASSIGNED")
                .eventType("EMPLOYEE_ASSIGNED")
                .correlationId(CorrelationIdUtil.getCurrentCorrelationId())
                .source("employee-service")
                .build();

        publishEvent("employee.assigned", event);
    }

    public void publishServiceStarted(String bookingId, String employeeId) {
        ServiceEvent event = ServiceEvent.builder()
                .bookingId(bookingId)
                .employeeId(employeeId)
                .status("STARTED")
                .startTime(java.time.Instant.now())
                .eventType(ServiceConstants.SERVICE_STARTED_TOPIC)
                .correlationId(CorrelationIdUtil.getCurrentCorrelationId())
                .source("employee-service")
                .build();

        publishEvent(ServiceConstants.SERVICE_STARTED_TOPIC, event);
    }

    public void publishServiceCompleted(String bookingId, String employeeId, Integer duration) {
        ServiceEvent event = ServiceEvent.builder()
                .bookingId(bookingId)
                .employeeId(employeeId)
                .status("COMPLETED")
                .actualDuration(duration)
                .eventType(ServiceConstants.SERVICE_COMPLETED_TOPIC)
                .correlationId(CorrelationIdUtil.getCurrentCorrelationId())
                .source("employee-service")
                .build();

        publishEvent(ServiceConstants.SERVICE_COMPLETED_TOPIC, event);
    }

    public void publishAssignmentCancelled(String assignmentId, String bookingId, String reason) {
        ServiceEvent event = ServiceEvent.builder()
                .bookingId(bookingId)
                .status("CANCELLED")
                .delayReason(reason)
                .eventType("ASSIGNMENT_CANCELLED")
                .correlationId(CorrelationIdUtil.getCurrentCorrelationId())
                .source("employee-service")
                .build();

        publishEvent("assignment.cancelled", event);
    }

    public void publishEmployeeSkillAdded(String employeeId, Long skillId, Integer proficiencyLevel) {
        ServiceEvent event = ServiceEvent.builder()
                .employeeId(employeeId)
                .status("SKILL_ADDED")
                .notes("Skill ID: " + skillId + ", Proficiency: " + proficiencyLevel)
                .eventType("EMPLOYEE_SKILL_ADDED")
                .correlationId(CorrelationIdUtil.getCurrentCorrelationId())
                .source("employee-service")
                .build();

        publishEvent("employee.skill.added", event);
    }

    public void publishTimesheetSubmitted(String timesheetId, String employeeId) {
        ServiceEvent event = ServiceEvent.builder()
                .employeeId(employeeId)
                .status("SUBMITTED")
                .eventType("TIMESHEET_SUBMITTED")
                .correlationId(CorrelationIdUtil.getCurrentCorrelationId())
                .source("employee-service")
                .build();

        publishEvent("timesheet.submitted", event);
    }

    public void publishTimesheetApproved(String timesheetId, String providerId) {
        ServiceEvent event = ServiceEvent.builder()
                .providerId(providerId)
                .status("APPROVED")
                .eventType("TIMESHEET_APPROVED")
                .correlationId(CorrelationIdUtil.getCurrentCorrelationId())
                .source("employee-service")
                .build();

        publishEvent("timesheet.approved", event);
    }

    public void publishTimesheetRejected(String timesheetId, String providerId, String reason) {
        ServiceEvent event = ServiceEvent.builder()
                .providerId(providerId)
                .status("REJECTED")
                .delayReason(reason)
                .eventType("TIMESHEET_REJECTED")
                .correlationId(CorrelationIdUtil.getCurrentCorrelationId())
                .source("employee-service")
                .build();

        publishEvent("timesheet.rejected", event);
    }

    private void publishEvent(String topic, BaseEvent event) {
        CompletableFuture<SendResult<String, BaseEvent>> future = kafkaTemplate.send(topic, event);

        future.whenComplete((result, ex) -> {
            if (ex == null) {
                log.debug("Event published successfully to {}: {}", topic, event.getEventId());
            } else {
                log.error("Failed to publish event to {}: {}", topic, ex.getMessage());
            }
        });
    }
}
