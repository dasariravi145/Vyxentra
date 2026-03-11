package com.vyxentra.vehicle.controller;

import com.vyxentra.vehicle.dto.request.UserUpdateRequest;
import com.vyxentra.vehicle.dto.response.ApiResponse;
import com.vyxentra.vehicle.dto.response.PageResponse;
import com.vyxentra.vehicle.dto.response.UserDetailResponse;
import com.vyxentra.vehicle.service.UserManagementService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/api/v1/admin/users")
@RequiredArgsConstructor
public class UserManagementController {

    private final UserManagementService userManagementService;

    @GetMapping
    public ResponseEntity<ApiResponse<PageResponse<UserDetailResponse>>> getAllUsers(
            @RequestHeader("X-User-ID") String adminId,
            @RequestParam(required = false) String role,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String search,
            @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
        log.info("Getting all users for admin: {} with filters", adminId);
        PageResponse<UserDetailResponse> response = userManagementService.getAllUsers(role, status, search, pageable);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/{userId}")
    public ResponseEntity<ApiResponse<UserDetailResponse>> getUserDetails(
            @RequestHeader("X-User-ID") String adminId,
            @PathVariable String userId) {
        log.info("Getting user details for admin: {} user: {}", adminId, userId);
        UserDetailResponse response = userManagementService.getUserDetails(userId);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @PutMapping("/{userId}")
    public ResponseEntity<ApiResponse<UserDetailResponse>> updateUser(
            @RequestHeader("X-User-ID") String adminId,
            @PathVariable String userId,
            @Valid @RequestBody UserUpdateRequest request) {
        log.info("Updating user: {} by admin: {}", userId, adminId);
        UserDetailResponse response = userManagementService.updateUser(adminId, userId, request);
        return ResponseEntity.ok(ApiResponse.success(response, "User updated successfully"));
    }

    @PostMapping("/{userId}/block")
    public ResponseEntity<ApiResponse<Void>> blockUser(
            @RequestHeader("X-User-ID") String adminId,
            @PathVariable String userId,
            @RequestParam String reason) {
        log.info("Blocking user: {} by admin: {} reason: {}", userId, adminId, reason);
        userManagementService.blockUser(adminId, userId, reason);
        return ResponseEntity.ok(ApiResponse.success(null, "User blocked"));
    }

    @PostMapping("/{userId}/unblock")
    public ResponseEntity<ApiResponse<Void>> unblockUser(
            @RequestHeader("X-User-ID") String adminId,
            @PathVariable String userId) {
        log.info("Unblocking user: {} by admin: {}", userId, adminId);
        userManagementService.unblockUser(adminId, userId);
        return ResponseEntity.ok(ApiResponse.success(null, "User unblocked"));
    }

    @DeleteMapping("/{userId}")
    public ResponseEntity<ApiResponse<Void>> deleteUser(
            @RequestHeader("X-User-ID") String adminId,
            @PathVariable String userId,
            @RequestParam String reason) {
        log.info("Deleting user: {} by admin: {} reason: {}", userId, adminId, reason);
        userManagementService.deleteUser(adminId, userId, reason);
        return ResponseEntity.ok(ApiResponse.success(null, "User deleted"));
    }

    @GetMapping("/statistics")
    public ResponseEntity<ApiResponse<Object>> getUserStatistics(
            @RequestHeader("X-User-ID") String adminId) {
        log.info("Getting user statistics for admin: {}", adminId);
        Object statistics = userManagementService.getUserStatistics();
        return ResponseEntity.ok(ApiResponse.success(statistics));
    }
}
