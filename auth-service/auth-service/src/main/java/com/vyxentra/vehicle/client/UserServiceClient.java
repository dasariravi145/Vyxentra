package com.vyxentra.vehicle.client;

import com.vyxentra.vehicle.dto.request.UserRegistrationRequest;
import com.vyxentra.vehicle.dto.response.UserResponse;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

@FeignClient(name = "user-service", path = "/api/v1/users", fallback = UserServiceClientFallback.class)
public interface UserServiceClient {

    @GetMapping("/mobile/{countryCode}/{mobileNumber}")
    @CircuitBreaker(name = "userService")
    @Retry(name = "userService")
    UserResponse getUserByMobile(@PathVariable String countryCode, @PathVariable String mobileNumber);

    @GetMapping("/{userId}")
    @CircuitBreaker(name = "userService")
    @Retry(name = "userService")
    UserResponse getUserById(@PathVariable String userId);

    @PostMapping
    @CircuitBreaker(name = "userService")
    @Retry(name = "userService")
    UserResponse createUser(@RequestBody UserRegistrationRequest request);

    @PutMapping("/{userId}/device")
    @CircuitBreaker(name = "userService")
    @Retry(name = "userService")
    void updateDeviceInfo(@PathVariable String userId,
                          @RequestParam String deviceId,
                          @RequestParam(required = false) String fcmToken);
}
