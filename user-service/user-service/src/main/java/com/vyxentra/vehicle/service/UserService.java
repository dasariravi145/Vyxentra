package com.vyxentra.vehicle.service;


import com.vyxentra.vehicle.dto.request.UpdateProfileRequest;
import com.vyxentra.vehicle.dto.response.UserProfileResponse;

public interface UserService {

    UserProfileResponse getUserProfile(String userId);

    UserProfileResponse updateProfile(String userId, UpdateProfileRequest request);

    void deactivateAccount(String userId);

    void incrementBookingCount(String userId);

    void updateTotalSpent(String userId, Integer amount);
}