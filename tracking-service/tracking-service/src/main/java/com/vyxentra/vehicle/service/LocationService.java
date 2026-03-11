package com.vyxentra.vehicle.service;


import com.vyxentra.vehicle.dto.request.LocationUpdateRequest;
import com.vyxentra.vehicle.dto.response.LocationResponse;
import com.vyxentra.vehicle.dto.response.PathResponse;

import java.time.LocalDateTime;
import java.util.List;

public interface LocationService {

    void updateLocation(String userId, LocationUpdateRequest request);

    LocationResponse getCurrentLocation(String entityId, String entityType);

    List<LocationResponse> getLocationHistory(String entityId, String entityType,
                                              LocalDateTime from, LocalDateTime to);

    PathResponse getTrackingPath(String bookingId);

    List<String> getActiveTrackings(String providerId);

    void cleanupOldLocations();
}
