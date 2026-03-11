package com.vyxentra.vehicle.service;



import com.vyxentra.vehicle.dto.request.EmployeeRegistrationRequest;
import com.vyxentra.vehicle.dto.request.EmployeeUpdateRequest;
import com.vyxentra.vehicle.dto.request.SkillUpdateRequest;
import com.vyxentra.vehicle.dto.response.EmployeeDetailResponse;
import com.vyxentra.vehicle.dto.response.EmployeeResponse;
import com.vyxentra.vehicle.dto.response.SkillResponse;

import java.util.List;

public interface EmployeeService {

    EmployeeResponse registerEmployee(String userId, EmployeeRegistrationRequest request);

    EmployeeDetailResponse getEmployeeProfile(String employeeId);

    EmployeeDetailResponse getEmployeeProfileByUserId(String userId);

    EmployeeDetailResponse updateEmployeeProfile(String userId, EmployeeUpdateRequest request);

    void updateEmployeeStatus(String employeeId, String status, String providerId);

    List<EmployeeResponse> getProviderEmployees(String providerId);

    List<EmployeeResponse> getAvailableEmployees(String providerId, String date, String skill);

    // Skills
    List<SkillResponse> getAllSkills();

    SkillResponse addSkill(String providerId, SkillUpdateRequest request);

    void addEmployeeSkill(String employeeId, Long skillId, Integer proficiencyLevel, String providerId);

    void removeEmployeeSkill(String employeeId, Long skillId, String providerId);

    List<SkillResponse> getEmployeeSkills(String employeeId);
}