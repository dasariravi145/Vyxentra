package com.vyxentra.vehicle.client;


import com.vyxentra.vehicle.dto.response.ApiResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class EmployeeServiceClientFallback implements EmployeeServiceClient {

    @Override
    public ApiResponse<Boolean> validateEmployee(String employeeId, String providerId) {
        log.error("Fallback: Unable to validate employee: {} for provider: {}", employeeId, providerId);
        return ApiResponse.success(true);
    }

    @Override
    public ApiResponse<Void> incrementServiceCount(String employeeId) {
        log.error("Fallback: Unable to increment service count for employee: {}", employeeId);
        return ApiResponse.error(null, "Employee service is currently unavailable");
    }
}