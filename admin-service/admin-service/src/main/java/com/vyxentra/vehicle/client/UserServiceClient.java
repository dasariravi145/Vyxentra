package com.vyxentra.vehicle.client;


import com.vyxentra.vehicle.dto.request.UserUpdateRequest;
import com.vyxentra.vehicle.dto.response.ApiResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

@FeignClient(name = "user-service", fallback = UserServiceClientFallback.class)
public interface UserServiceClient {

    @GetMapping("/api/v1/users")
    ApiResponse<Object> getAllUsers(@RequestParam(value = "role", required = false) String role,
                                    @RequestParam(value = "status", required = false) String status,
                                    @RequestParam(value = "search", required = false) String search,
                                    @RequestParam("page") int page,
                                    @RequestParam("size") int size);

    @GetMapping("/api/v1/users/{userId}")
    ApiResponse<Object> getUserDetails(@PathVariable("userId") String userId);

    @PutMapping("/api/v1/users/{userId}")
    ApiResponse<Object> updateUser(@PathVariable("userId") String userId,
                                   @RequestBody UserUpdateRequest request);

    @DeleteMapping("/api/v1/users/{userId}")
    ApiResponse<Void> deleteUser(@PathVariable("userId") String userId,
                                 @RequestParam("reason") String reason);
}
