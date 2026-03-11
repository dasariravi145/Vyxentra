package com.vyxentra.vehicle.client;

import com.vyxentra.vehicle.dto.response.ApiResponse;
import com.vyxentra.vehicle.dto.response.ProviderResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class ProviderServiceClientFallback implements ProviderServiceClient {

    @Override
    public ApiResponse<ProviderResponse> getProvider(String providerId) {
        log.error("Fallback: Unable to fetch provider: {}", providerId);
        return ApiResponse.error(null, "Provider service is currently unavailable");
    }

    @Override
    public ApiResponse<ProviderResponse> getProviderByUserId(String userId) {
        log.error("Fallback: Unable to fetch provider for user: {}", userId);
        return ApiResponse.error(null, "Provider service is currently unavailable");
    }
}
