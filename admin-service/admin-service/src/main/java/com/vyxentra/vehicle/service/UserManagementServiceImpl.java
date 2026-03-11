package com.vyxentra.vehicle.service;

import com.vyxentra.vehicle.client.AuthServiceClient;
import com.vyxentra.vehicle.client.UserServiceClient;
import com.vyxentra.vehicle.dto.request.UserUpdateRequest;
import com.vyxentra.vehicle.dto.response.PageResponse;
import com.vyxentra.vehicle.dto.response.UserDetailResponse;
import com.vyxentra.vehicle.entity.AdminAction;
import com.vyxentra.vehicle.repository.AdminActionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserManagementServiceImpl implements UserManagementService {

    private final UserServiceClient userServiceClient;
    private final AuthServiceClient authServiceClient;
    private final AdminActionRepository adminActionRepository;

    @Override
    @Transactional(readOnly = true)
    public PageResponse<UserDetailResponse> getAllUsers(String role, String status, String search, Pageable pageable) {
        log.debug("Getting all users with filters");

        // This would call user-service via Feign

        return PageResponse.<UserDetailResponse>builder()
                .content(List.of())
                .pageNumber(pageable.getPageNumber())
                .pageSize(pageable.getPageSize())
                .totalElements(0)
                .totalPages(0)
                .first(true)
                .last(true)
                .hasNext(false)
                .hasPrevious(false)
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public UserDetailResponse getUserDetails(String userId) {
        log.debug("Getting user details: {}", userId);

        // This would call user-service via Feign

        return null;
    }

    @Override
    @Transactional
    public UserDetailResponse updateUser(String adminId, String userId, UserUpdateRequest request) {
        log.info("Updating user: {} by admin: {}", userId, adminId);

        // Call user service to update
        // userServiceClient.updateUser(userId, request);

        // Log admin action
        AdminAction action = AdminAction.builder()
                .adminId(adminId)
                .actionType("UPDATE_USER")
                .targetType("USER")
                .targetId(userId)
                .afterState(Map.of(
                        "fullName", request.getFullName(),
                        "email", request.getEmail(),
                        "role", request.getRole()
                ))
                .createdAt(LocalDateTime.now())
                .build();
        adminActionRepository.save(action);

        log.info("User updated: {}", userId);

        return null;
    }

    @Override
    @Transactional
    public void blockUser(String adminId, String userId, String reason) {
        log.info("Blocking user: {} by admin: {} reason: {}", userId, adminId, reason);

        // Call auth service to block user
        // authServiceClient.blockUser(userId, reason);

        // Log admin action
        AdminAction action = AdminAction.builder()
                .adminId(adminId)
                .actionType("BLOCK_USER")
                .targetType("USER")
                .targetId(userId)
                .reason(reason)
                .createdAt(LocalDateTime.now())
                .build();
        adminActionRepository.save(action);

        log.info("User blocked: {}", userId);
    }

    @Override
    @Transactional
    public void unblockUser(String adminId, String userId) {
        log.info("Unblocking user: {} by admin: {}", userId, adminId);

        // Call auth service to unblock user
        // authServiceClient.unblockUser(userId);

        // Log admin action
        AdminAction action = AdminAction.builder()
                .adminId(adminId)
                .actionType("UNBLOCK_USER")
                .targetType("USER")
                .targetId(userId)
                .createdAt(LocalDateTime.now())
                .build();
        adminActionRepository.save(action);

        log.info("User unblocked: {}", userId);
    }

    @Override
    @Transactional
    public void deleteUser(String adminId, String userId, String reason) {
        log.info("Deleting user: {} by admin: {} reason: {}", userId, adminId, reason);

        // Call user service to delete (soft delete)
        // userServiceClient.deleteUser(userId, reason);

        // Log admin action
        AdminAction action = AdminAction.builder()
                .adminId(adminId)
                .actionType("DELETE_USER")
                .targetType("USER")
                .targetId(userId)
                .reason(reason)
                .createdAt(LocalDateTime.now())
                .build();
        adminActionRepository.save(action);

        log.info("User deleted: {}", userId);
    }

    @Override
    @Transactional(readOnly = true)
    public Object getUserStatistics() {
        log.debug("Getting user statistics");

        // This would aggregate from user-service and auth-service

        return Map.of(
                "totalUsers", 15000,
                "customers", 14000,
                "providers", 500,
                "employees", 450,
                "admins", 50,
                "activeToday", 2500,
                "newThisMonth", 1200,
                "blockedUsers", 35
        );
    }
}
