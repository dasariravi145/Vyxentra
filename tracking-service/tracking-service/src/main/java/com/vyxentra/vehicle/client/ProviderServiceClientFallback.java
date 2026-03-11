package com.vyxentra.vehicle.client;

import com.vyxentra.vehicle.dto.response.ApiResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class ProviderServiceClientFallback implements ProviderServiceClient {

    @Override
    public ApiResponse<Object> getProviderDetails(String providerId) {
        log.error("Fallback: Unable to fetch provider details: {}", providerId);
        return ApiResponse.error(null, "Provider service is currently unavailable");
    }
}
