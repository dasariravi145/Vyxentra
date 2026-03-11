package com.vyxentra.vehicle.repository;


import com.vyxentra.vehicle.entity.Timesheet;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface TimesheetRepository extends JpaRepository<Timesheet, String> {

    Optional<Timesheet> findByEmployeeIdAndTimesheetDate(String employeeId, LocalDate date);

    List<Timesheet> findByEmployeeIdAndTimesheetDateBetween(String employeeId, LocalDate fromDate, LocalDate toDate);

    @Query("SELECT t FROM Timesheet t WHERE t.employee.providerId = :providerId " +
            "AND t.status = 'SUBMITTED' " +
            "AND (:fromDate IS NULL OR t.timesheetDate >= :fromDate) " +
            "AND (:toDate IS NULL OR t.timesheetDate <= :toDate)")
    Page<Timesheet> findPendingByProviderId(@Param("providerId") String providerId,
                                            @Param("fromDate") LocalDate fromDate,
                                            @Param("toDate") LocalDate toDate,
                                            Pageable pageable);

    @Query("SELECT SUM(t.totalHours) FROM Timesheet t WHERE t.employee.id = :employeeId " +
            "AND t.timesheetDate BETWEEN :fromDate AND :toDate")
    Double getTotalHoursInRange(@Param("employeeId") String employeeId,
                                @Param("fromDate") LocalDate fromDate,
                                @Param("toDate") LocalDate toDate);

    @Modifying
    @Query("UPDATE Timesheet t SET t.status = 'SUBMITTED', t.submittedAt = CURRENT_TIMESTAMP " +
            "WHERE t.id = :timesheetId")
    void submitTimesheet(@Param("timesheetId") String timesheetId);

    @Modifying
    @Query("UPDATE Timesheet t SET t.status = 'APPROVED', t.approvedBy = :approvedBy, " +
            "t.approvedAt = CURRENT_TIMESTAMP WHERE t.id = :timesheetId")
    void approveTimesheet(@Param("timesheetId") String timesheetId,
                          @Param("approvedBy") String approvedBy);

    @Modifying
    @Query("UPDATE Timesheet t SET t.status = 'REJECTED', t.rejectionReason = :reason " +
            "WHERE t.id = :timesheetId")
    void rejectTimesheet(@Param("timesheetId") String timesheetId,
                         @Param("reason") String reason);
}
