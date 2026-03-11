package com.vyxentra.vehicle.validator;


import com.vyxentra.vehicle.exceptions.BusinessException;
import com.vyxentra.vehicle.exceptions.ErrorCode;
import com.vyxentra.vehicle.repository.EmployeeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class EmployeeValidator {

    private final EmployeeRepository employeeRepository;

    public void validateEmployeeCode(String employeeCode, String employeeId) {
        if (employeeRepository.existsByEmployeeCode(employeeCode)) {
            employeeRepository.findByEmployeeCode(employeeCode).ifPresent(existing -> {
                if (!existing.getId().equals(employeeId)) {
                    throw new BusinessException(ErrorCode.VALIDATION_ERROR, "Employee code already exists");
                }
            });
        }
    }

    public void validateEmployeeForAssignment(String employeeId, String providerId) {
        employeeRepository.findById(employeeId)
                .filter(e -> e.getProviderId().equals(providerId))
                .filter(e -> "ACTIVE".equals(e.getStatus()))
                .orElseThrow(() -> new BusinessException(ErrorCode.VALIDATION_ERROR,
                        "Employee not available for assignment"));
    }
}
