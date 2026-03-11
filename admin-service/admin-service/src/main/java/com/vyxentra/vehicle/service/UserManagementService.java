package com.vyxentra.vehicle.service;

import com.vyxentra.vehicle.dto.request.UserUpdateRequest;
import com.vyxentra.vehicle.dto.response.PageResponse;
import com.vyxentra.vehicle.dto.response.UserDetailResponse;
import org.springframework.data.domain.Pageable;

public interface UserManagementService {

    PageResponse<UserDetailResponse> getAllUsers(String role, String status, String search, Pageable pageable);

    UserDetailResponse getUserDetails(String userId);

    UserDetailResponse updateUser(String adminId, String userId, UserUpdateRequest request);

    void blockUser(String adminId, String userId, String reason);

    void unblockUser(String adminId, String userId);

    void deleteUser(String adminId, String userId, String reason);

    Object getUserStatistics();
}