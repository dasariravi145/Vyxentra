package com.vyxentra.vehicle.client;


import com.vyxentra.vehicle.dto.response.ApiResponse;
import com.vyxentra.vehicle.dto.response.ProviderResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient(name = "provider-service", fallback = ProviderServiceClientFallback.class)
public interface ProviderServiceClient {

    @GetMapping("/api/v1/providers/{providerId}")
    ApiResponse<ProviderResponse> getProvider(@PathVariable("providerId") String providerId);

    @GetMapping("/api/v1/providers/validate/{providerId}")
    ApiResponse<Boolean> validateProviderForEmergency(@PathVariable("providerId") String providerId,
                                                      @RequestParam("emergencyType") String emergencyType,
                                                      @RequestParam("vehicleType") String vehicleType);

    @PostMapping("/api/v1/providers/{providerId}/increment-emergency")
    ApiResponse<Void> incrementEmergencyCount(@PathVariable("providerId") String providerId);
}
