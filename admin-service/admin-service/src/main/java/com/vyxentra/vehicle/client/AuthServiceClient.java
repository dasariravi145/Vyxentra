package com.vyxentra.vehicle.client;


import com.vyxentra.vehicle.dto.response.ApiResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient(name = "auth-service", fallback = AuthServiceClientFallback.class)
public interface AuthServiceClient {

    @PostMapping("/api/v1/auth/admin/block-user")
    ApiResponse<Void> blockUser(@RequestParam("userId") String userId,
                                @RequestParam("reason") String reason);

    @PostMapping("/api/v1/auth/admin/unblock-user")
    ApiResponse<Void> unblockUser(@RequestParam("userId") String userId);

    @PostMapping("/api/v1/auth/admin/reset-password")
    ApiResponse<Void> resetPassword(@RequestParam("userId") String userId);
}
