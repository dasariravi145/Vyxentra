package com.vyxentra.vehicle.service;

import com.vyxentra.vehicle.dto.TrackingStats;
import com.vyxentra.vehicle.dto.request.TrackingSubscribeRequest;
import com.vyxentra.vehicle.dto.response.ETAUpdateResponse;
import com.vyxentra.vehicle.dto.response.TrackingInfoResponse;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.List;

public interface TrackingService {

    TrackingInfoResponse subscribeToTracking(String userId, TrackingSubscribeRequest request);

    void unsubscribeFromTracking(String userId, String bookingId);

    TrackingInfoResponse getTrackingSession(String bookingId);

    String getTrackingStatus(String bookingId);

    void pauseTracking(String userId, String bookingId);

    void resumeTracking(String userId, String bookingId);

    Integer getCurrentETA(String bookingId);

    List<TrackingInfoResponse> getUserTrackingHistory(String userId, LocalDateTime from, LocalDateTime to);

    List<ETAUpdateResponse> getETAHistory(String bookingId);

    TrackingStats getTrackingStats(String bookingId);

    void createTrackingSession(String bookingId, String customerId, String providerId,
                               Double destLat, Double destLng, String destAddress);

    void endTrackingSession(String bookingId);

    void processStaleSessions();
}
