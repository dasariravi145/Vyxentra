package com.vyxentra.vehicle.client;

import com.vyxentra.vehicle.dto.request.UserRegistrationRequest;
import com.vyxentra.vehicle.dto.response.UserResponse;
import org.springframework.stereotype.Component;

@Component
class UserServiceClientFallback implements UserServiceClient {

    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(UserServiceClientFallback.class);

    @Override
    public UserResponse getUserByMobile(String countryCode, String mobileNumber) {
        log.error("Fallback: getUserByMobile failed for {}{}", countryCode, mobileNumber);
        throw new RuntimeException("User service is temporarily unavailable");
    }

    @Override
    public UserResponse getUserById(String userId) {
        log.error("Fallback: getUserById failed for {}", userId);
        throw new RuntimeException("User service is temporarily unavailable");
    }

    @Override
    public UserResponse createUser(UserRegistrationRequest request) {
        log.error("Fallback: createUser failed for {}", request.getMobileNumber());
        throw new RuntimeException("User service is temporarily unavailable");
    }

    @Override
    public void updateDeviceInfo(String userId, String deviceId, String fcmToken) {
        log.error("Fallback: updateDeviceInfo failed for {}", userId);
        throw new RuntimeException("User service is temporarily unavailable");
    }
}
