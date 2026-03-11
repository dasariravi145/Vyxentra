package com.vyxentra.vehicle.client;


import com.vyxentra.vehicle.dto.response.ApiResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient(name = "employee-service", fallback = EmployeeServiceClientFallback.class)
public interface EmployeeServiceClient {

    @GetMapping("/api/v1/employees/validate/{employeeId}")
    ApiResponse<Boolean> validateEmployee(@PathVariable("employeeId") String employeeId,
                                          @RequestParam("providerId") String providerId);

    @PostMapping("/api/v1/employees/{employeeId}/increment-service")
    ApiResponse<Void> incrementServiceCount(@PathVariable("employeeId") String employeeId);
}
