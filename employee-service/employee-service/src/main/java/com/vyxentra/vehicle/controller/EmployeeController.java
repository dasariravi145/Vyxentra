package com.vyxentra.vehicle.controller;


import com.vyxentra.vehicle.dto.request.EmployeeRegistrationRequest;
import com.vyxentra.vehicle.dto.request.EmployeeUpdateRequest;
import com.vyxentra.vehicle.dto.request.SkillUpdateRequest;
import com.vyxentra.vehicle.dto.response.ApiResponse;
import com.vyxentra.vehicle.dto.response.EmployeeDetailResponse;
import com.vyxentra.vehicle.dto.response.EmployeeResponse;
import com.vyxentra.vehicle.dto.response.SkillResponse;
import com.vyxentra.vehicle.service.EmployeeService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/v1/employees")
@RequiredArgsConstructor
public class EmployeeController {

    private final EmployeeService employeeService;

    @PostMapping("/register")
    public ResponseEntity<ApiResponse<EmployeeResponse>> registerEmployee(
            @RequestHeader("X-User-ID") String userId,
            @Valid @RequestBody EmployeeRegistrationRequest request) {
        log.info("Registering employee for user: {}", userId);
        EmployeeResponse response = employeeService.registerEmployee(userId, request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(response, "Employee registered successfully"));
    }

    @GetMapping("/profile")
    public ResponseEntity<ApiResponse<EmployeeDetailResponse>> getMyProfile(
            @RequestHeader("X-User-ID") String userId) {
        log.info("Getting employee profile for user: {}", userId);
        EmployeeDetailResponse response = employeeService.getEmployeeProfileByUserId(userId);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/{employeeId}")
    public ResponseEntity<ApiResponse<EmployeeDetailResponse>> getEmployeeProfile(
            @PathVariable String employeeId) {
        log.info("Getting employee profile: {}", employeeId);
        EmployeeDetailResponse response = employeeService.getEmployeeProfile(employeeId);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @PutMapping("/profile")
    public ResponseEntity<ApiResponse<EmployeeDetailResponse>> updateProfile(
            @RequestHeader("X-User-ID") String userId,
            @Valid @RequestBody EmployeeUpdateRequest request) {
        log.info("Updating employee profile for user: {}", userId);
        EmployeeDetailResponse response = employeeService.updateEmployeeProfile(userId, request);
        return ResponseEntity.ok(ApiResponse.success(response, "Profile updated successfully"));
    }

    @PatchMapping("/{employeeId}/status")
    public ResponseEntity<ApiResponse<Void>> updateEmployeeStatus(
            @PathVariable String employeeId,
            @RequestParam String status,
            @RequestHeader("X-User-ID") String providerId) {
        log.info("Updating employee {} status to: {}", employeeId, status);
        employeeService.updateEmployeeStatus(employeeId, status, providerId);
        return ResponseEntity.ok(ApiResponse.success(null, "Status updated successfully"));
    }

    @GetMapping("/provider/{providerId}")
    public ResponseEntity<ApiResponse<List<EmployeeResponse>>> getProviderEmployees(
            @PathVariable String providerId) {
        log.info("Getting employees for provider: {}", providerId);
        List<EmployeeResponse> responses = employeeService.getProviderEmployees(providerId);
        return ResponseEntity.ok(ApiResponse.success(responses));
    }

    @GetMapping("/available")
    public ResponseEntity<ApiResponse<List<EmployeeResponse>>> getAvailableEmployees(
            @RequestParam String providerId,
            @RequestParam(required = false) String date,
            @RequestParam(required = false) String skill) {
        log.info("Getting available employees for provider: {}", providerId);
        List<EmployeeResponse> responses = employeeService.getAvailableEmployees(providerId, date, skill);
        return ResponseEntity.ok(ApiResponse.success(responses));
    }

    // ========== Skills Management ==========

    @GetMapping("/skills")
    public ResponseEntity<ApiResponse<List<SkillResponse>>> getAllSkills() {
        log.info("Getting all skills");
        List<SkillResponse> responses = employeeService.getAllSkills();
        return ResponseEntity.ok(ApiResponse.success(responses));
    }

    @PostMapping("/skills")
    public ResponseEntity<ApiResponse<SkillResponse>> addSkill(
            @RequestHeader("X-User-ID") String providerId,
            @Valid @RequestBody SkillUpdateRequest request) {
        log.info("Adding new skill: {}", request.getName());
        SkillResponse response = employeeService.addSkill(providerId, request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(response, "Skill added successfully"));
    }

    @PostMapping("/{employeeId}/skills")
    public ResponseEntity<ApiResponse<Void>> addEmployeeSkill(
            @PathVariable String employeeId,
            @RequestParam Long skillId,
            @RequestParam Integer proficiencyLevel,
            @RequestHeader("X-User-ID") String providerId) {
        log.info("Adding skill {} to employee {} with proficiency {}", skillId, employeeId, proficiencyLevel);
        employeeService.addEmployeeSkill(employeeId, skillId, proficiencyLevel, providerId);
        return ResponseEntity.ok(ApiResponse.success(null, "Skill added to employee"));
    }

    @DeleteMapping("/{employeeId}/skills/{skillId}")
    public ResponseEntity<ApiResponse<Void>> removeEmployeeSkill(
            @PathVariable String employeeId,
            @PathVariable Long skillId,
            @RequestHeader("X-User-ID") String providerId) {
        log.info("Removing skill {} from employee {}", skillId, employeeId);
        employeeService.removeEmployeeSkill(employeeId, skillId, providerId);
        return ResponseEntity.ok(ApiResponse.success(null, "Skill removed from employee"));
    }

    @GetMapping("/{employeeId}/skills")
    public ResponseEntity<ApiResponse<List<SkillResponse>>> getEmployeeSkills(
            @PathVariable String employeeId) {
        log.info("Getting skills for employee: {}", employeeId);
        List<SkillResponse> responses = employeeService.getEmployeeSkills(employeeId);
        return ResponseEntity.ok(ApiResponse.success(responses));
    }
}