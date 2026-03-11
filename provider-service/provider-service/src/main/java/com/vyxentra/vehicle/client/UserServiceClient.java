package com.vyxentra.vehicle.client;


import com.vyxentra.vehicle.dto.response.ApiResponse;
import com.vyxentra.vehicle.dto.response.UserProfileResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;

@FeignClient(name = "user-service", url = "${services.user-service.url:http://localhost:7149}", fallback = UserServiceClientFallback.class)
public interface UserServiceClient {

    @GetMapping("/api/v1/users/{userId}")
    ApiResponse<UserProfileResponse> getUserProfile(@PathVariable("userId") String userId);

    @GetMapping("/api/v1/users/profile")
    ApiResponse<UserProfileResponse> getMyProfile(@RequestHeader("X-User-ID") String userId);

    @GetMapping("/api/v1/users/{userId}/exists")
    ApiResponse<Boolean> checkUserExists(@PathVariable("userId") String userId);

    @GetMapping("/api/v1/users/{userId}/validate")
    ApiResponse<Boolean> validateUser(@PathVariable("userId") String userId);
}
