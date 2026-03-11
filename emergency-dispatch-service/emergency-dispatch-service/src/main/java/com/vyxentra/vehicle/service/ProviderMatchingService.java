package com.vyxentra.vehicle.service;



import com.vyxentra.vehicle.dto.ProviderLocation;
import com.vyxentra.vehicle.dto.ProviderLocationUpdate;
import com.vyxentra.vehicle.dto.response.ProviderMatchResponse;

import java.util.List;
import java.util.Set;

public interface ProviderMatchingService {

    /**
     * Find nearby providers using Redis Geo indexing
     */
    List<ProviderMatchResponse> findNearbyProviders(String geoIndex, Double latitude,
                                                    Double longitude, Integer radiusKm);

    /**
     * Find nearby providers with bounding box for better performance
     */
    List<ProviderMatchResponse> findNearbyProvidersInBoundingBox(String geoIndex,
                                                                 Double minLat, Double maxLat,
                                                                 Double minLon, Double maxLon);

    /**
     * Update provider location in Redis Geo indices
     */
    void updateProviderLocation(String providerId, Double latitude, Double longitude);

    /**
     * Add provider to specific geo index
     */
    void addProviderToIndex(String providerId, Double latitude, Double longitude, String index);

    /**
     * Remove provider from specific geo index
     */
    void removeProviderFromIndex(String providerId, String index);

    /**
     * Mark provider as unavailable (remove from all search indices)
     */
    void markProviderUnavailable(String providerId);

    /**
     * Mark provider as unavailable with reason
     */
    void markProviderUnavailable(String providerId, String reason);

    /**
     * Mark provider as available (add to appropriate indices based on provider type)
     */
    void markProviderAvailable(String providerId);

    /**
     * Mark provider as available with specific vehicle support
     */
    void markProviderAvailable(String providerId, String vehicleType, Set<String> emergencyTypes);

    /**
     * Check if provider is available in geo indices
     */
    boolean isProviderAvailable(String providerId);

    /**
     * Get provider's current location from Redis
     */
    ProviderLocation getProviderLocation(String providerId);

    /**
     * Get all available providers for emergency type
     */
    List<ProviderMatchResponse> getAvailableProviders(String emergencyType, String vehicleType);

    /**
     * Calculate ETA based on distance
     */
    int calculateETA(double distanceKm);

    /**
     * Batch update provider locations
     */
    void batchUpdateProviderLocations(List<ProviderLocationUpdate> updates);

    /**
     * Clear all provider locations (maintenance)
     */
    void clearAllProviderLocations();

    /**
     * Get count of available providers
     */
    long getAvailableProvidersCount(String emergencyType, String vehicleType);
}
