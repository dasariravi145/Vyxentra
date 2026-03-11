package com.vyxentra.vehicle.repository;


import com.vyxentra.vehicle.entity.Assignment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface AssignmentRepository extends JpaRepository<Assignment, String> {

    Optional<Assignment> findByBookingId(String bookingId);

    List<Assignment> findByEmployeeId(String employeeId);

    List<Assignment> findByProviderId(String providerId);

    @Query("SELECT a FROM Assignment a WHERE a.employee.id = :employeeId " +
            "AND a.assignedAt BETWEEN :startDate AND :endDate")
    List<Assignment> findByEmployeeIdAndDateRange(@Param("employeeId") String employeeId,
                                                  @Param("startDate") LocalDateTime startDate,
                                                  @Param("endDate") LocalDateTime endDate);

    @Query("SELECT a FROM Assignment a WHERE a.employee.id = :employeeId " +
            "AND a.status IN ('ASSIGNED', 'STARTED') ORDER BY a.assignedAt DESC")
    List<Assignment> findCurrentAssignments(@Param("employeeId") String employeeId);

    @Query("SELECT a FROM Assignment a WHERE a.employee.id = :employeeId " +
            "AND a.status = 'STARTED'")
    Optional<Assignment> findStartedAssignment(@Param("employeeId") String employeeId);

    @Query("SELECT COUNT(a) > 0 FROM Assignment a WHERE a.employee.id = :employeeId " +
            "AND a.status IN ('ASSIGNED', 'STARTED')")
    boolean hasActiveAssignment(@Param("employeeId") String employeeId);

    @Query("SELECT COUNT(a) FROM Assignment a WHERE a.employee.id = :employeeId " +
            "AND DATE(a.assignedAt) = CURRENT_DATE")
    int countTodayAssignments(@Param("employeeId") String employeeId);

    @Modifying
    @Query("UPDATE Assignment a SET a.status = :status, a.notes = :notes WHERE a.id = :assignmentId")
    void updateStatus(@Param("assignmentId") String assignmentId,
                      @Param("status") String status,
                      @Param("notes") String notes);

    @Modifying
    @Query("UPDATE Assignment a SET a.startedAt = :startedAt WHERE a.id = :assignmentId")
    void markStarted(@Param("assignmentId") String assignmentId,
                     @Param("startedAt") LocalDateTime startedAt);

    @Modifying
    @Query("UPDATE Assignment a SET a.completedAt = :completedAt, a.actualDuration = :duration " +
            "WHERE a.id = :assignmentId")
    void markCompleted(@Param("assignmentId") String assignmentId,
                       @Param("completedAt") LocalDateTime completedAt,
                       @Param("duration") Integer duration);
}
