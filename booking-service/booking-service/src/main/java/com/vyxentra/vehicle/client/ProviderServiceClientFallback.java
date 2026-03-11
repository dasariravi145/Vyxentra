package com.vyxentra.vehicle.client;


import com.vyxentra.vehicle.dto.response.ApiResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Map;

@Slf4j
@Component
public class ProviderServiceClientFallback implements ProviderServiceClient {

    @Override
    public ApiResponse<Map<String, Object>> getProvider(String providerId) {
        log.error("Fallback: Unable to fetch provider: {}", providerId);
        return ApiResponse.error(null, "Provider service is currently unavailable");
    }

    @Override
    public ApiResponse<Boolean> validateProvider(String providerId, String vehicleType) {
        log.error("Fallback: Unable to validate provider: {} for vehicle: {}", providerId, vehicleType);
        return ApiResponse.success(true); // Optimistically allow
    }

    @Override
    public ApiResponse<Void> incrementBookingCount(String providerId) {
        log.error("Fallback: Unable to increment booking count for provider: {}", providerId);
        return ApiResponse.error(null, "Provider service is currently unavailable");
    }

    @Override
    public ApiResponse<Void> updateProviderRating(String providerId, Integer rating) {
        log.error("Fallback: Unable to update rating for provider: {}", providerId);
        return ApiResponse.error(null, "Provider service is currently unavailable");
    }
}