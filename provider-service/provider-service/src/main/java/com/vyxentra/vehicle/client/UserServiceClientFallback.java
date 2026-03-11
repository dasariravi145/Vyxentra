package com.vyxentra.vehicle.client;


import com.vyxentra.vehicle.dto.response.ApiResponse;
import com.vyxentra.vehicle.dto.response.UserProfileResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class UserServiceClientFallback implements UserServiceClient {

    @Override
    public ApiResponse<UserProfileResponse> getUserProfile(String userId) {
        log.error("Fallback: Unable to fetch user profile for user: {}", userId);
        return ApiResponse.error(null);
    }

    @Override
    public ApiResponse<UserProfileResponse> getMyProfile(String userId) {
        log.error("Fallback: Unable to fetch my profile for user: {}", userId);
        return ApiResponse.error(null);
    }

    @Override
    public ApiResponse<Boolean> checkUserExists(String userId) {
        log.error("Fallback: Unable to check if user exists: {}", userId);
        return ApiResponse.success(false);
    }

    @Override
    public ApiResponse<Boolean> validateUser(String userId) {
        log.error("Fallback: Unable to validate user: {}", userId);
        return ApiResponse.success(false);
    }
}
