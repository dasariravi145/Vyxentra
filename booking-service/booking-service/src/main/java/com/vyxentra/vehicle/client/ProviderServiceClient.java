package com.vyxentra.vehicle.client;

import com.vyxentra.vehicle.dto.response.ApiResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.Map;

@FeignClient(name = "provider-service", fallback = ProviderServiceClientFallback.class)
public interface ProviderServiceClient {

    @GetMapping("/api/v1/providers/{providerId}")
    ApiResponse<Map<String, Object>> getProvider(@PathVariable("providerId") String providerId);

    @GetMapping("/api/v1/providers/validate/{providerId}")
    ApiResponse<Boolean> validateProvider(@PathVariable("providerId") String providerId,
                                          @RequestParam("vehicleType") String vehicleType);

    @PostMapping("/api/v1/providers/{providerId}/increment-booking")
    ApiResponse<Void> incrementBookingCount(@PathVariable("providerId") String providerId);

    @PostMapping("/api/v1/providers/{providerId}/update-rating")
    ApiResponse<Void> updateProviderRating(@PathVariable("providerId") String providerId,
                                           @RequestParam("rating") Integer rating);
}
