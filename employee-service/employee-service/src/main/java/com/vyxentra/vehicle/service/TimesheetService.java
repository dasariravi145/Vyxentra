package com.vyxentra.vehicle.service;


import com.vyxentra.vehicle.dto.request.TimesheetEntryRequest;
import com.vyxentra.vehicle.dto.response.PageResponse;
import com.vyxentra.vehicle.dto.response.TimesheetResponse;
import org.springframework.data.domain.Pageable;

import java.time.LocalDate;
import java.util.List;

public interface TimesheetService {

    TimesheetResponse addTimesheetEntry(String employeeId, TimesheetEntryRequest request);

    List<TimesheetResponse> getEmployeeTimesheets(String employeeId, LocalDate fromDate, LocalDate toDate);

    TimesheetResponse getTimesheetByDate(String employeeId, LocalDate date);

    void submitTimesheet(String timesheetId, String employeeId);

    void approveTimesheet(String timesheetId, String providerId);

    void rejectTimesheet(String timesheetId, String providerId, String reason);

    PageResponse<TimesheetResponse> getPendingTimesheets(String providerId, LocalDate fromDate,
                                                         LocalDate toDate, Pageable pageable);

    TimesheetResponse getTimesheetSummary(String employeeId, LocalDate fromDate, LocalDate toDate);
}
