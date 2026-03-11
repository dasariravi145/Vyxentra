package com.vyxentra.vehicle.controller;


import com.vyxentra.vehicle.dto.request.*;
import com.vyxentra.vehicle.dto.response.*;
import com.vyxentra.vehicle.service.AddressService;
import com.vyxentra.vehicle.service.UserService;
import com.vyxentra.vehicle.service.VehicleService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;
    private final AddressService addressService;
    private final VehicleService vehicleService;

    // ==================== Profile Management ====================

    @GetMapping("/profile")
    public ResponseEntity<ApiResponse<UserProfileResponse>> getMyProfile(
            @RequestHeader("X-User-ID") String userId) {
        log.info("Getting profile for user: {}", userId);
        UserProfileResponse response = userService.getUserProfile(userId);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/{userId}")
    public ResponseEntity<ApiResponse<UserProfileResponse>> getUserProfile(
            @PathVariable String userId) {
        log.info("Getting profile for user: {}", userId);
        UserProfileResponse response = userService.getUserProfile(userId);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @PutMapping("/profile")
    public ResponseEntity<ApiResponse<UserProfileResponse>> updateProfile(
            @RequestHeader("X-User-ID") String userId,
            @Valid @RequestBody UpdateProfileRequest request) {
        log.info("Updating profile for user: {}", userId);
        UserProfileResponse response = userService.updateProfile(userId, request);
        return ResponseEntity.ok(ApiResponse.success(response, "Profile updated successfully"));
    }

    @DeleteMapping("/profile")
    public ResponseEntity<ApiResponse<Void>> deactivateAccount(
            @RequestHeader("X-User-ID") String userId) {
        log.info("Deactivating account for user: {}", userId);
        userService.deactivateAccount(userId);
        return ResponseEntity.ok(ApiResponse.success(null, "Account deactivated successfully"));
    }

    // ==================== Address Management ====================

    @PostMapping("/addresses")
    public ResponseEntity<ApiResponse<AddressResponse>> addAddress(
            @RequestHeader("X-User-ID") String userId,
            @Valid @RequestBody AddAddressRequest request) {
        log.info("Adding address for user: {}", userId);
        AddressResponse response = addressService.addAddress(userId, request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(response, "Address added successfully"));
    }

    @GetMapping("/addresses")
    public ResponseEntity<ApiResponse<PageResponse<AddressResponse>>> getUserAddresses(
            @RequestHeader("X-User-ID") String userId,
            @PageableDefault(size = 20, sort = "isDefault", direction = Sort.Direction.DESC) Pageable pageable) {
        log.info("Getting addresses for user: {}", userId);
        PageResponse<AddressResponse> response = addressService.getUserAddresses(userId, pageable);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/addresses/{addressId}")
    public ResponseEntity<ApiResponse<AddressResponse>> getAddress(
            @RequestHeader("X-User-ID") String userId,
            @PathVariable String addressId) {
        log.info("Getting address {} for user: {}", addressId, userId);
        AddressResponse response = addressService.getAddress(userId, addressId);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @PutMapping("/addresses/{addressId}")
    public ResponseEntity<ApiResponse<AddressResponse>> updateAddress(
            @RequestHeader("X-User-ID") String userId,
            @PathVariable String addressId,
            @Valid @RequestBody UpdateAddressRequest request) {
        log.info("Updating address {} for user: {}", addressId, userId);
        AddressResponse response = addressService.updateAddress(userId, addressId, request);
        return ResponseEntity.ok(ApiResponse.success(response, "Address updated successfully"));
    }

    @DeleteMapping("/addresses/{addressId}")
    public ResponseEntity<ApiResponse<Void>> deleteAddress(
            @RequestHeader("X-User-ID") String userId,
            @PathVariable String addressId) {
        log.info("Deleting address {} for user: {}", addressId, userId);
        addressService.deleteAddress(userId, addressId);
        return ResponseEntity.ok(ApiResponse.success(null, "Address deleted successfully"));
    }

    @PatchMapping("/addresses/{addressId}/default")
    public ResponseEntity<ApiResponse<Void>> setDefaultAddress(
            @RequestHeader("X-User-ID") String userId,
            @PathVariable String addressId) {
        log.info("Setting default address {} for user: {}", addressId, userId);
        addressService.setDefaultAddress(userId, addressId);
        return ResponseEntity.ok(ApiResponse.success(null, "Default address updated successfully"));
    }

    // ==================== Vehicle Management ====================

    @PostMapping("/vehicles")
    public ResponseEntity<ApiResponse<VehicleResponse>> addVehicle(
            @RequestHeader("X-User-ID") String userId,
            @Valid @RequestBody AddVehicleRequest request) {
        log.info("Adding vehicle for user: {}", userId);
        VehicleResponse response = vehicleService.addVehicle(userId, request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(response, "Vehicle added successfully"));
    }

    @GetMapping("/vehicles")
    public ResponseEntity<ApiResponse<PageResponse<VehicleResponse>>> getUserVehicles(
            @RequestHeader("X-User-ID") String userId,
            @PageableDefault(size = 20) Pageable pageable) {
        log.info("Getting vehicles for user: {}", userId);
        PageResponse<VehicleResponse> response = vehicleService.getUserVehicles(userId, pageable);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/vehicles/{vehicleId}")
    public ResponseEntity<ApiResponse<VehicleResponse>> getVehicle(
            @RequestHeader("X-User-ID") String userId,
            @PathVariable String vehicleId) {
        log.info("Getting vehicle {} for user: {}", vehicleId, userId);
        VehicleResponse response = vehicleService.getVehicle(userId, vehicleId);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @PutMapping("/vehicles/{vehicleId}")
    public ResponseEntity<ApiResponse<VehicleResponse>> updateVehicle(
            @RequestHeader("X-User-ID") String userId,
            @PathVariable String vehicleId,
            @Valid @RequestBody UpdateVehicleRequest request) {
        log.info("Updating vehicle {} for user: {}", vehicleId, userId);
        VehicleResponse response = vehicleService.updateVehicle(userId, vehicleId, request);
        return ResponseEntity.ok(ApiResponse.success(response, "Vehicle updated successfully"));
    }

    @DeleteMapping("/vehicles/{vehicleId}")
    public ResponseEntity<ApiResponse<Void>> deleteVehicle(
            @RequestHeader("X-User-ID") String userId,
            @PathVariable String vehicleId) {
        log.info("Deleting vehicle {} for user: {}", vehicleId, userId);
        vehicleService.deleteVehicle(userId, vehicleId);
        return ResponseEntity.ok(ApiResponse.success(null, "Vehicle deleted successfully"));
    }

    @PatchMapping("/vehicles/{vehicleId}/default")
    public ResponseEntity<ApiResponse<Void>> setDefaultVehicle(
            @RequestHeader("X-User-ID") String userId,
            @PathVariable String vehicleId) {
        log.info("Setting default vehicle {} for user: {}", vehicleId, userId);
        vehicleService.setDefaultVehicle(userId, vehicleId);
        return ResponseEntity.ok(ApiResponse.success(null, "Default vehicle updated successfully"));
    }
}