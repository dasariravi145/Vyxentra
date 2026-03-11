package com.vyxentra.vehicle.client;


import com.vyxentra.vehicle.dto.request.ProviderApprovalRequest;
import com.vyxentra.vehicle.dto.response.ApiResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class ProviderServiceClientFallback implements ProviderServiceClient {

    @Override
    public ApiResponse<Object> getPendingProviders(int page, int size) {
        log.error("Fallback: Unable to fetch pending providers");
        return ApiResponse.error(null, "Provider service is currently unavailable");
    }

    @Override
    public ApiResponse<Object> getProviderDetails(String providerId) {
        log.error("Fallback: Unable to fetch provider details: {}", providerId);
        return ApiResponse.error(null, "Provider service is currently unavailable");
    }

    @Override
    public ApiResponse<Void> approveProvider(String providerId, String adminId, ProviderApprovalRequest request) {
        log.error("Fallback: Unable to approve provider: {}", providerId);
        return ApiResponse.error(null, "Provider service is currently unavailable");
    }

    @Override
    public ApiResponse<Void> rejectProvider(String providerId, String adminId, String reason) {
        log.error("Fallback: Unable to reject provider: {}", providerId);
        return ApiResponse.error(null, "Provider service is currently unavailable");
    }

    @Override
    public ApiResponse<Void> suspendProvider(String providerId, String adminId, String reason) {
        log.error("Fallback: Unable to suspend provider: {}", providerId);
        return ApiResponse.error(null, "Provider service is currently unavailable");
    }

    @Override
    public ApiResponse<Void> activateProvider(String providerId, String adminId) {
        log.error("Fallback: Unable to activate provider: {}", providerId);
        return ApiResponse.error(null, "Provider service is currently unavailable");
    }
}
