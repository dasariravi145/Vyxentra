package com.vyxentra.vehicle.repository;

import com.vyxentra.vehicle.entity.TimesheetEntry;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TimesheetEntryRepository extends JpaRepository<TimesheetEntry, String> {

    List<TimesheetEntry> findByTimesheetId(String timesheetId);

    List<TimesheetEntry> findByAssignmentId(String assignmentId);

    @Query("SELECT SUM(te.durationMinutes) FROM TimesheetEntry te " +
            "WHERE te.assignment.id = :assignmentId")
    Integer getTotalMinutesForAssignment(@Param("assignmentId") String assignmentId);

    boolean existsByAssignmentId(String assignmentId);
}
