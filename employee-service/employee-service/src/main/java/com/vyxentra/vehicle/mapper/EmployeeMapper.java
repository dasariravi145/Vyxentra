package com.vyxentra.vehicle.mapper;


import com.vyxentra.vehicle.dto.response.EmployeeDetailResponse;
import com.vyxentra.vehicle.dto.response.EmployeeResponse;
import com.vyxentra.vehicle.entity.Employee;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

import java.util.List;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface EmployeeMapper {

    @Mapping(target = "employeeId", source = "id")
    @Mapping(target = "fullName", expression = "java(employee.getFirstName() + \" \" + employee.getLastName())")
    EmployeeResponse toResponse(Employee employee);

    List<EmployeeResponse> toResponseList(List<Employee> employees);

    @Mapping(target = "employeeId", source = "id")
    @Mapping(target = "fullName", expression = "java(employee.getFirstName() + \" \" + employee.getLastName())")
    EmployeeDetailResponse toDetailResponse(Employee employee);
}
