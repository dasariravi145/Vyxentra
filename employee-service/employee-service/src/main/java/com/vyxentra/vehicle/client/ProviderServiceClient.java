package com.vyxentra.vehicle.client;


import com.vyxentra.vehicle.dto.response.ApiResponse;
import com.vyxentra.vehicle.dto.response.ProviderResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "provider-service", fallback = ProviderServiceClientFallback.class)
public interface ProviderServiceClient {

    @GetMapping("/api/v1/providers/{providerId}")
    ApiResponse<ProviderResponse> getProvider(@PathVariable("providerId") String providerId);

    @GetMapping("/api/v1/providers/user/{userId}")
    ApiResponse<ProviderResponse> getProviderByUserId(@PathVariable("userId") String userId);
}
