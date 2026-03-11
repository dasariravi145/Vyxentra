package com.vyxentra.vehicle.mapper;



import com.vyxentra.vehicle.dto.response.TimesheetResponse;
import com.vyxentra.vehicle.entity.Timesheet;
import com.vyxentra.vehicle.entity.TimesheetEntry;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

import java.util.List;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface TimesheetMapper {

    @Mapping(target = "timesheetId", source = "id")
    @Mapping(target = "employeeId", source = "employee.id")
    @Mapping(target = "employeeName", expression = "java(timesheet.getEmployee().getFirstName() + " +
            "\" \" + timesheet.getEmployee().getLastName())")
    TimesheetResponse toResponse(Timesheet timesheet);

    List<TimesheetResponse> toResponseList(List<Timesheet> timesheets);

    @Mapping(target = "entryId", source = "id")
    TimesheetResponse toEntryResponse(TimesheetEntry entry);
}