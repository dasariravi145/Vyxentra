package com.vyxentra.vehicle.controller;


import com.vyxentra.vehicle.dto.request.ProviderRegistrationRequest;
import com.vyxentra.vehicle.dto.request.ProviderUpdateRequest;
import com.vyxentra.vehicle.dto.response.ApiResponse;
import com.vyxentra.vehicle.dto.response.ProviderDetailResponse;
import com.vyxentra.vehicle.dto.response.ProviderResponse;
import com.vyxentra.vehicle.service.ProviderService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/api/v1/providers")
@RequiredArgsConstructor
public class ProviderController {

    private final ProviderService providerService;

    @PostMapping("/register")
    public ResponseEntity<ApiResponse<ProviderResponse>> registerProvider(
            @RequestHeader("X-User-ID") String userId,
            @Valid @RequestBody ProviderRegistrationRequest request) {
        log.info("Registering provider for user: {}", userId);
        ProviderResponse response = providerService.registerProvider(userId, request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(response, "Provider registered successfully"));
    }

    @GetMapping("/profile")
    public ResponseEntity<ApiResponse<ProviderDetailResponse>> getMyProfile(
            @RequestHeader("X-User-ID") String userId) {
        log.info("Getting provider profile for user: {}", userId);
        ProviderDetailResponse response = providerService.getProviderProfileByUserId(userId);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/{providerId}")
    public ResponseEntity<ApiResponse<ProviderDetailResponse>> getProviderProfile(
            @PathVariable String providerId) {
        log.info("Getting provider profile: {}", providerId);
        ProviderDetailResponse response = providerService.getProviderProfile(providerId);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @PutMapping("/profile")
    public ResponseEntity<ApiResponse<ProviderResponse>> updateProviderProfile(
            @RequestHeader("X-User-ID") String userId,
            @Valid @RequestBody ProviderUpdateRequest request) {
        log.info("Updating provider profile for user: {}", userId);
        ProviderResponse response = providerService.updateProviderProfile(userId, request);
        return ResponseEntity.ok(ApiResponse.success(response, "Profile updated successfully"));
    }

    @GetMapping("/{providerId}/availability")
    public ResponseEntity<ApiResponse<Boolean>> checkProviderAvailability(
            @PathVariable String providerId) {
        log.info("Checking availability for provider: {}", providerId);
        boolean isAvailable = providerService.isProviderAvailable(providerId);
        return ResponseEntity.ok(ApiResponse.success(isAvailable));
    }
}
