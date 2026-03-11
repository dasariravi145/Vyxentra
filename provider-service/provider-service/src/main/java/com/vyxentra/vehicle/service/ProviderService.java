package com.vyxentra.vehicle.service;


import com.vyxentra.vehicle.dto.request.ProviderRegistrationRequest;
import com.vyxentra.vehicle.dto.request.ProviderUpdateRequest;
import com.vyxentra.vehicle.dto.response.ProviderDetailResponse;
import com.vyxentra.vehicle.dto.response.ProviderResponse;
import com.vyxentra.vehicle.enums.ProviderStatus;

import java.util.List;

public interface ProviderService {

    ProviderResponse registerProvider(String userId, ProviderRegistrationRequest request);

    ProviderDetailResponse getProviderProfile(String providerId);

    ProviderDetailResponse getProviderProfileByUserId(String userId);

    ProviderResponse updateProviderProfile(String userId, ProviderUpdateRequest request);

    List<ProviderResponse> getProvidersByStatus(ProviderStatus status);

    boolean isProviderAvailable(String providerId);

    void incrementBookingCount(String providerId);

    void updateProviderRating(String providerId, Double rating);

    void validateProviderForBooking(String providerId);
}
