package com.vyxentra.vehicle.client;


import com.vyxentra.vehicle.dto.request.UserUpdateRequest;
import com.vyxentra.vehicle.dto.response.ApiResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class UserServiceClientFallback implements UserServiceClient {

    @Override
    public ApiResponse<Object> getAllUsers(String role, String status, String search, int page, int size) {
        log.error("Fallback: Unable to fetch users");
        return ApiResponse.error(null, "User service is currently unavailable");
    }

    @Override
    public ApiResponse<Object> getUserDetails(String userId) {
        log.error("Fallback: Unable to fetch user details: {}", userId);
        return ApiResponse.error(null, "User service is currently unavailable");
    }

    @Override
    public ApiResponse<Object> updateUser(String userId, UserUpdateRequest request) {
        log.error("Fallback: Unable to update user: {}", userId);
        return ApiResponse.error(null, "User service is currently unavailable");
    }

    @Override
    public ApiResponse<Void> deleteUser(String userId, String reason) {
        log.error("Fallback: Unable to delete user: {}", userId);
        return ApiResponse.error(null, "User service is currently unavailable");
    }
}