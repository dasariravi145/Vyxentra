package com.vyxentra.vehicle.service;


import com.vyxentra.vehicle.dto.request.EmergencyBookingRequest;
import com.vyxentra.vehicle.dto.request.ProviderAcceptRequest;
import com.vyxentra.vehicle.dto.response.EmergencyBookingResponse;
import com.vyxentra.vehicle.dto.response.ProviderMatchResponse;

import java.util.List;

public interface EmergencyDispatchService {

    // Core emergency operations
    EmergencyBookingResponse requestEmergency(String customerId, EmergencyBookingRequest request);

    EmergencyBookingResponse getEmergencyRequest(String requestId);

    EmergencyBookingResponse getEmergencyRequestByNumber(String requestNumber);

    void cancelEmergencyRequest(String requestId, String customerId, String reason);

    // Provider matching
    List<ProviderMatchResponse> findNearbyProviders(Double latitude, Double longitude,
                                                    String emergencyType, String vehicleType,
                                                    Integer radiusKm);

    EmergencyBookingResponse acceptEmergency(String providerId, ProviderAcceptRequest request);

    void updateProviderLocation(String providerId, Double latitude, Double longitude);

    // Active emergency queries
    EmergencyBookingResponse getActiveEmergencyForCustomer(String customerId);

    EmergencyBookingResponse getActiveAssignmentForProvider(String providerId);

    // Assignment lifecycle
    void providerArrived(String assignmentId, String providerId);

    void completeEmergency(String assignmentId, String providerId);

    // Scheduled jobs
    void processExpiredRequests();

    void expandSearchRadius();

    /**
     * Expand search radius for a specific emergency request
     * Used when all notified providers have timed out
     *
     * @param requestId The ID of the emergency request
     */
    void expandSearchRadiusForRequest(String requestId);

    // Provider status handlers
    void handleProviderOffline(String providerId);

    void handleProviderOnline(String providerId);

    void handleEmergencyCompleted(String emergencyId, String providerId);

    void handleProviderAccepted(String providerId, String emergencyId);
}
