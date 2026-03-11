package com.vyxentra.vehicle.client;


import com.vyxentra.vehicle.dto.response.ApiResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Map;

@Slf4j
@Component
public class UserServiceClientFallback implements UserServiceClient {

    @Override
    public ApiResponse<Map<String, Object>> getUser(String userId) {
        log.error("Fallback: Unable to fetch user: {}", userId);
        return ApiResponse.error(null);
    }
}
