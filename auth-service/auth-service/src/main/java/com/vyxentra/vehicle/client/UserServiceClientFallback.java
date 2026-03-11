package com.vyxentra.vehicle.client;

import com.vyxentra.vehicle.dto.response.ApiResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class UserServiceClientFallback implements UserServiceClient {

    @Override
    public ApiResponse<UserDetails> loadUserByUserId(String userId) {
        log.error("Fallback: Unable to load user details for user: {}", userId);
        return ApiResponse.error(null, "User service is currently unavailable");
    }
}