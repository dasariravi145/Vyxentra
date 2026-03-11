package com.vyxentra.vehicle.service;


import com.vyxentra.vehicle.client.ProviderServiceClient;
import com.vyxentra.vehicle.dto.request.EmployeeRegistrationRequest;
import com.vyxentra.vehicle.dto.request.EmployeeUpdateRequest;
import com.vyxentra.vehicle.dto.request.SkillUpdateRequest;
import com.vyxentra.vehicle.dto.response.EmployeeDetailResponse;
import com.vyxentra.vehicle.dto.response.EmployeeResponse;
import com.vyxentra.vehicle.dto.response.SkillResponse;
import com.vyxentra.vehicle.entity.Employee;
import com.vyxentra.vehicle.entity.Skill;
import com.vyxentra.vehicle.exceptions.BusinessException;
import com.vyxentra.vehicle.exceptions.ErrorCode;
import com.vyxentra.vehicle.exceptions.ResourceNotFoundException;
import com.vyxentra.vehicle.kafka.EmployeeEventProducer;
import com.vyxentra.vehicle.mapper.EmployeeMapper;
import com.vyxentra.vehicle.repository.AssignmentRepository;
import com.vyxentra.vehicle.repository.EmployeeRepository;
import com.vyxentra.vehicle.repository.SkillRepository;
import com.vyxentra.vehicle.validator.EmployeeValidator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class EmployeeServiceImpl implements EmployeeService {

    private final EmployeeRepository employeeRepository;
    private final SkillRepository skillRepository;
    private final AssignmentRepository assignmentRepository;
    private final EmployeeMapper employeeMapper;
    private final EmployeeValidator employeeValidator;
    private final EmployeeEventProducer eventProducer;
    private final ProviderServiceClient providerServiceClient;

    @Override
    @Transactional
    public EmployeeResponse registerEmployee(String userId, EmployeeRegistrationRequest request) {
        log.info("Registering employee for user: {}", userId);

        // Check if user already registered as employee
        if (employeeRepository.existsByUserId(userId)) {
            throw new BusinessException(ErrorCode.VALIDATION_ERROR, "User already registered as employee");
        }

        // Verify provider exists and is active (call to provider service)
        // This would be done via Feign client

        // Generate employee code if not provided
        String employeeCode = request.getEmployeeCode();
        if (employeeCode == null || employeeCode.isEmpty()) {
            employeeCode = generateEmployeeCode(request.getJoiningDate(), request.getFirstName());
        }

        // Check unique employee code
        if (employeeRepository.existsByEmployeeCode(employeeCode)) {
            throw new BusinessException(ErrorCode.VALIDATION_ERROR, "Employee code already exists");
        }

        // Create employee
        Employee employee = Employee.builder()
                .userId(userId)
                .providerId(getProviderIdFromUserId(userId)) // This would come from auth context
                .employeeCode(employeeCode)
                .firstName(request.getFirstName())
                .lastName(request.getLastName())
                .email(request.getEmail())
                .phone(request.getPhone())
                .alternatePhone(request.getAlternatePhone())
                .dateOfBirth(request.getDateOfBirth())
                .gender(request.getGender())
                .addressLine1(request.getAddressLine1())
                .addressLine2(request.getAddressLine2())
                .city(request.getCity())
                .state(request.getState())
                .postalCode(request.getPostalCode())
                .country(request.getCountry())
                .designation(request.getDesignation())
                .department(request.getDepartment())
                .employmentType(request.getEmploymentType())
                .joiningDate(request.getJoiningDate())
                .status("ACTIVE")
                .averageRating(0.0)
                .totalServicesCompleted(0)
                .createdBy(userId)
                .build();

        employee = employeeRepository.save(employee);

        // Publish event
        eventProducer.publishEmployeeRegistered(employee.getId(), userId, employee.getProviderId());

        log.info("Employee registered successfully with ID: {}", employee.getId());

        return employeeMapper.toResponse(employee);
    }

    @Override
    @Transactional(readOnly = true)
    public EmployeeDetailResponse getEmployeeProfile(String employeeId) {
        log.debug("Fetching employee profile: {}", employeeId);

        Employee employee = findEmployeeById(employeeId);
        EmployeeDetailResponse response = employeeMapper.toDetailResponse(employee);

        // Add skills
        List<Skill> skills = skillRepository.findByEmployeeId(employeeId);
        response.setSkills(skills.stream()
                .map(skill -> SkillResponse.builder()
                        .skillId(skill.getId())
                        .name(skill.getName())
                        .category(skill.getCategory())
                        .description(skill.getDescription())
                        .build())
                .collect(Collectors.toList()));

        // Add current assignment
        assignmentRepository.findCurrentAssignments(employeeId).stream()
                .findFirst()
                .ifPresent(assignment -> {
                    // Would map to AssignmentResponse
                });

        return response;
    }

    @Override
    @Transactional(readOnly = true)
    public EmployeeDetailResponse getEmployeeProfileByUserId(String userId) {
        log.debug("Fetching employee profile for user: {}", userId);

        Employee employee = employeeRepository.findByUserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Employee", "user", userId));

        return getEmployeeProfile(employee.getId());
    }

    @Override
    @Transactional
    public EmployeeDetailResponse updateEmployeeProfile(String userId, EmployeeUpdateRequest request) {
        log.info("Updating employee profile for user: {}", userId);

        Employee employee = employeeRepository.findByUserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Employee", "user", userId));

        // Update fields
        if (request.getFirstName() != null) {
            employee.setFirstName(request.getFirstName());
        }
        if (request.getLastName() != null) {
            employee.setLastName(request.getLastName());
        }
        if (request.getEmail() != null) {
            employee.setEmail(request.getEmail());
        }
        if (request.getPhone() != null) {
            employee.setPhone(request.getPhone());
        }
        if (request.getAlternatePhone() != null) {
            employee.setAlternatePhone(request.getAlternatePhone());
        }
        if (request.getAddressLine1() != null) {
            employee.setAddressLine1(request.getAddressLine1());
        }
        if (request.getAddressLine2() != null) {
            employee.setAddressLine2(request.getAddressLine2());
        }
        if (request.getCity() != null) {
            employee.setCity(request.getCity());
        }
        if (request.getState() != null) {
            employee.setState(request.getState());
        }
        if (request.getPostalCode() != null) {
            employee.setPostalCode(request.getPostalCode());
        }
        if (request.getCountry() != null) {
            employee.setCountry(request.getCountry());
        }
        if (request.getProfilePicture() != null) {
            employee.setProfilePicture(request.getProfilePicture());
        }
        if (request.getDesignation() != null) {
            employee.setDesignation(request.getDesignation());
        }
        if (request.getDepartment() != null) {
            employee.setDepartment(request.getDepartment());
        }

        employee.setUpdatedBy(userId);
        employee = employeeRepository.save(employee);

        // Publish event
        eventProducer.publishEmployeeUpdated(employee.getId(), userId);

        log.info("Employee profile updated successfully: {}", employee.getId());

        return getEmployeeProfile(employee.getId());
    }

    @Override
    @Transactional
    public void updateEmployeeStatus(String employeeId, String status, String providerId) {
        log.info("Updating employee {} status to: {} by provider: {}", employeeId, status, providerId);

        Employee employee = findEmployeeById(employeeId);

        // Verify provider owns this employee
        if (!employee.getProviderId().equals(providerId)) {
            throw new BusinessException(ErrorCode.UNAUTHORIZED, "Not authorized to update this employee");
        }

        employee.setStatus(status);
        employee.setUpdatedBy(providerId);
        employeeRepository.save(employee);

        // Publish event
        eventProducer.publishEmployeeStatusChanged(employeeId, status, providerId);

        log.info("Employee status updated to: {}", status);
    }

    @Override
    @Transactional(readOnly = true)
    public List<EmployeeResponse> getProviderEmployees(String providerId) {
        log.debug("Getting employees for provider: {}", providerId);

        List<Employee> employees = employeeRepository.findByProviderId(providerId);
        return employeeMapper.toResponseList(employees);
    }

    @Override
    @Transactional(readOnly = true)
    public List<EmployeeResponse> getAvailableEmployees(String providerId, String date, String skill) {
        log.debug("Getting available employees for provider: {} on date: {}", providerId, date);

        // Get all active employees for provider
        List<Employee> employees = employeeRepository.findActiveByProviderId(providerId);

        // Filter by skill if provided
        if (skill != null && !skill.isEmpty()) {
            employees = employees.stream()
                    .filter(e -> e.getSkills().stream()
                            .anyMatch(s -> s.getName().equalsIgnoreCase(skill)))
                    .collect(Collectors.toList());
        }

        // Filter by availability on given date
        if (date != null && !date.isEmpty()) {
            LocalDate targetDate = LocalDate.parse(date, DateTimeFormatter.ISO_DATE);
            // Check if employee has less than max assignments for the day
            employees = employees.stream()
                    .filter(e -> {
                        int todayAssignments = assignmentRepository.countTodayAssignments(e.getId());
                        return todayAssignments < 8; // Max 8 assignments per day
                    })
                    .collect(Collectors.toList());
        }

        return employeeMapper.toResponseList(employees);
    }

    @Override
    @Transactional(readOnly = true)
    public List<SkillResponse> getAllSkills() {
        log.debug("Getting all skills");

        return skillRepository.findAll().stream()
                .map(skill -> SkillResponse.builder()
                        .skillId(skill.getId())
                        .name(skill.getName())
                        .category(skill.getCategory())
                        .description(skill.getDescription())
                        .build())
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public SkillResponse addSkill(String providerId, SkillUpdateRequest request) {
        log.info("Adding new skill: {} by provider: {}", request.getName(), providerId);

        // Check if skill already exists
        if (skillRepository.findByName(request.getName()).isPresent()) {
            throw new BusinessException(ErrorCode.VALIDATION_ERROR, "Skill already exists");
        }

        Skill skill = Skill.builder()
                .name(request.getName())
                .category(request.getCategory())
                .description(request.getDescription())
                .build();

        skill = skillRepository.save(skill);

        log.info("Skill added successfully with ID: {}", skill.getId());

        return SkillResponse.builder()
                .skillId(skill.getId())
                .name(skill.getName())
                .category(skill.getCategory())
                .description(skill.getDescription())
                .build();
    }

    @Override
    @Transactional
    public void addEmployeeSkill(String employeeId, Long skillId, Integer proficiencyLevel, String providerId) {
        log.info("Adding skill {} to employee {} with proficiency {}", skillId, employeeId, proficiencyLevel);

        Employee employee = findEmployeeById(employeeId);

        // Verify provider owns this employee
        if (!employee.getProviderId().equals(providerId)) {
            throw new BusinessException(ErrorCode.UNAUTHORIZED, "Not authorized to modify this employee");
        }

        Skill skill = skillRepository.findById(skillId)
                .orElseThrow(() -> new ResourceNotFoundException("Skill", skillId.toString()));

        employee.getSkills().add(skill);
        employeeRepository.save(employee);

        // Publish event
        eventProducer.publishEmployeeSkillAdded(employeeId, skillId, proficiencyLevel);

        log.info("Skill added to employee successfully");
    }

    @Override
    @Transactional
    public void removeEmployeeSkill(String employeeId, Long skillId, String providerId) {
        log.info("Removing skill {} from employee {}", skillId, employeeId);

        Employee employee = findEmployeeById(employeeId);

        // Verify provider owns this employee
        if (!employee.getProviderId().equals(providerId)) {
            throw new BusinessException(ErrorCode.UNAUTHORIZED, "Not authorized to modify this employee");
        }

        employee.getSkills().removeIf(skill -> skill.getId().equals(skillId));
        employeeRepository.save(employee);

        log.info("Skill removed from employee successfully");
    }

    @Override
    @Transactional(readOnly = true)
    public List<SkillResponse> getEmployeeSkills(String employeeId) {
        log.debug("Getting skills for employee: {}", employeeId);

        List<Skill> skills = skillRepository.findByEmployeeId(employeeId);

        return skills.stream()
                .map(skill -> SkillResponse.builder()
                        .skillId(skill.getId())
                        .name(skill.getName())
                        .category(skill.getCategory())
                        .description(skill.getDescription())
                        .build())
                .collect(Collectors.toList());
    }

    private Employee findEmployeeById(String employeeId) {
        return employeeRepository.findById(employeeId)
                .orElseThrow(() -> new ResourceNotFoundException("Employee", employeeId));
    }

    private String generateEmployeeCode(LocalDate joiningDate, String firstName) {
        String year = String.valueOf(joiningDate.getYear()).substring(2);
        String month = String.format("%02d", joiningDate.getMonthValue());
        String random = UUID.randomUUID().toString().substring(0, 4).toUpperCase();
        String firstThree = firstName.length() >= 3 ? firstName.substring(0, 3).toUpperCase() : firstName.toUpperCase();

        return String.format("EMP%s%s%s%s", year, month, firstThree, random);
    }

    private String getProviderIdFromUserId(String userId) {
        // This would come from auth context or user service
        // For now, return a dummy value
        return "prov_789012";
    }
}
