package com.vyxentra.vehicle.client;


import com.vyxentra.vehicle.dto.response.ApiResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class AuthServiceClientFallback implements AuthServiceClient {

    @Override
    public ApiResponse<Void> blockUser(String userId, String reason) {
        log.error("Fallback: Unable to block user: {}", userId);
        return ApiResponse.error(null, "Auth service is currently unavailable");
    }

    @Override
    public ApiResponse<Void> unblockUser(String userId) {
        log.error("Fallback: Unable to unblock user: {}", userId);
        return ApiResponse.error(null, "Auth service is currently unavailable");
    }

    @Override
    public ApiResponse<Void> resetPassword(String userId) {
        log.error("Fallback: Unable to reset password for user: {}", userId);
        return ApiResponse.error(null, "Auth service is currently unavailable");
    }
}
