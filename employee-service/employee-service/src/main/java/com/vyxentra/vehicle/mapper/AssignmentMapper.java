package com.vyxentra.vehicle.mapper;


import com.vyxentra.vehicle.dto.response.AssignmentResponse;
import com.vyxentra.vehicle.entity.Assignment;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

import java.util.List;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface AssignmentMapper {

    @Mapping(target = "assignmentId", source = "id")
    @Mapping(target = "employeeId", source = "employee.id")
    @Mapping(target = "employeeName", expression = "java(assignment.getEmployee().getFirstName() + " +
            "\" \" + assignment.getEmployee().getLastName())")
    AssignmentResponse toResponse(Assignment assignment);

    List<AssignmentResponse> toResponseList(List<Assignment> assignments);
}