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
    public ApiResponse<Boolean> validateProviderForEmergency(String providerId, String emergencyType,
                                                             String vehicleType) {
        log.error("Fallback: Unable to validate provider for emergency: {}", providerId);
        return ApiResponse.success(true); // Optimistically allow
    }

    @Override
    public ApiResponse<Void> incrementEmergencyCount(String providerId) {
        log.error("Fallback: Unable to increment emergency count for provider: {}", providerId);
        return ApiResponse.error(null, "Provider service is currently unavailable");
    }
}
