package com.vyxentra.vehicle.service;


import com.vyxentra.vehicle.client.ProviderServiceClient;
import com.vyxentra.vehicle.dto.Location;
import com.vyxentra.vehicle.dto.ProviderLocation;
import com.vyxentra.vehicle.dto.ProviderLocationUpdate;
import com.vyxentra.vehicle.dto.response.ProviderMatchResponse;
import com.vyxentra.vehicle.dto.response.ProviderResponse;
import com.vyxentra.vehicle.utils.EmergencyConstants;
import com.vyxentra.vehicle.utils.GeoUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.geo.Circle;
import org.springframework.data.geo.Distance;
import org.springframework.data.geo.GeoResults;
import org.springframework.data.geo.Point;
import org.springframework.data.redis.connection.RedisGeoCommands;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class ProviderMatchingServiceImpl implements ProviderMatchingService {

    private final RedisTemplate<String, String> redisTemplate;
    private final ProviderServiceClient providerServiceClient;

    // Redis Geo indices
    private static final String PROVIDER_GEO_INDEX = EmergencyConstants.PROVIDER_GEO_INDEX;
    private static final String BIKE_REPAIR_INDEX = EmergencyConstants.BIKE_REPAIR_INDEX;
    private static final String CAR_REPAIR_INDEX = EmergencyConstants.CAR_REPAIR_INDEX;
    private static final String BIKE_FUEL_INDEX = EmergencyConstants.BIKE_FUEL_INDEX;
    private static final String CAR_FUEL_INDEX = EmergencyConstants.CAR_FUEL_INDEX;

    // Provider availability key prefix
    private static final String PROVIDER_AVAILABILITY_PREFIX = EmergencyConstants.PROVIDER_AVAILABILITY_PREFIX;
    private static final String PROVIDER_LOCATION_PREFIX = EmergencyConstants.PROVIDER_LOCATION_PREFIX;
    private static final String PROVIDER_DETAILS_PREFIX = "provider:details:";

    @Override
    public List<ProviderMatchResponse> findNearbyProviders(String geoIndex, Double latitude,
                                                           Double longitude, Integer radiusKm) {
        log.debug("Finding providers in {} within {} km of ({}, {})", geoIndex, radiusKm, latitude, longitude);

        Point point = new Point(longitude, latitude);
        Distance distance = new Distance(radiusKm, RedisGeoCommands.DistanceUnit.KILOMETERS);
        Circle circle = new Circle(point, distance);

        RedisGeoCommands.GeoRadiusCommandArgs args = RedisGeoCommands.GeoRadiusCommandArgs
                .newGeoRadiusArgs()
                .includeCoordinates()
                .includeDistance()
                .sortAscending()
                .limit(50);

        GeoResults<RedisGeoCommands.GeoLocation<String>> results = redisTemplate.opsForGeo()
                .radius(geoIndex, circle, args);

        List<ProviderMatchResponse> providers = new ArrayList<>();

        if (results != null) {
            results.getContent().forEach(result -> {
                RedisGeoCommands.GeoLocation<String> location = result.getContent();
                double distanceKm = result.getDistance().getValue();

                // Check if provider is marked as available
                String availabilityKey = PROVIDER_AVAILABILITY_PREFIX + location.getName();
                Boolean isAvailable = redisTemplate.hasKey(availabilityKey);

                if (Boolean.TRUE.equals(isAvailable)) {
                    ProviderMatchResponse provider = ProviderMatchResponse.builder()
                            .providerId(location.getName())
                            .distance(distanceKm)
                            .etaMinutes(calculateETA(distanceKm))
                            .latitude(location.getPoint().getY())
                            .longitude(location.getPoint().getX())
                            .isAvailable(true)
                            .build();

                    // Try to enrich with provider details from cache
                    enrichProviderDetails(provider);
                    providers.add(provider);
                }
            });
        }

        log.debug("Found {} available providers within {} km", providers.size(), radiusKm);
        return providers;
    }

    @Override
    public List<ProviderMatchResponse> findNearbyProvidersInBoundingBox(String geoIndex,
                                                                        Double minLat, Double maxLat,
                                                                        Double minLon, Double maxLon) {
        // This would use Redis GEOSEARCH with BYBOX in Redis 6.2+
        // For now, fall back to radius search
        double centerLat = (minLat + maxLat) / 2;
        double centerLon = (minLon + maxLon) / 2;
        double radiusKm = GeoUtils.calculateDistance(minLat, minLon, maxLat, maxLon) / 2;

        return findNearbyProviders(geoIndex, centerLat, centerLon, (int) Math.ceil(radiusKm));
    }

    @Override
    public void updateProviderLocation(String providerId, Double latitude, Double longitude) {
        log.debug("Updating location for provider: {} to ({}, {})", providerId, latitude, longitude);

        Point point = new Point(longitude, latitude);

        // Update in all geo indices
        redisTemplate.opsForGeo().add(PROVIDER_GEO_INDEX, point, providerId);

        // Also update in specialized indices based on provider type
        updateProviderSpecializedIndices(providerId, latitude, longitude);

        // Store current location separately for quick access
        String locationKey = PROVIDER_LOCATION_PREFIX + providerId;
        redisTemplate.opsForValue().set(locationKey,
                latitude + "," + longitude + "," + System.currentTimeMillis(),
                EmergencyConstants.PROVIDER_LOCATION_TTL_MINUTES, TimeUnit.MINUTES);
    }

    @Override
    public void addProviderToIndex(String providerId, Double latitude, Double longitude, String index) {
        Point point = new Point(longitude, latitude);
        redisTemplate.opsForGeo().add(index, point, providerId);
        log.debug("Added provider {} to index {}", providerId, index);
    }

    @Override
    public void removeProviderFromIndex(String providerId, String index) {
        redisTemplate.opsForGeo().remove(index, providerId);
        log.debug("Removed provider {} from index {}", providerId, index);
    }

    @Override
    public void markProviderUnavailable(String providerId) {
        markProviderUnavailable(providerId, "Manual override");
    }

    @Override
    public void markProviderUnavailable(String providerId, String reason) {
        log.info("Marking provider {} as unavailable. Reason: {}", providerId, reason);

        // Remove from availability flag
        String availabilityKey = PROVIDER_AVAILABILITY_PREFIX + providerId;
        redisTemplate.delete(availabilityKey);

        // Remove from all geo indices
        removeProviderFromIndex(providerId, PROVIDER_GEO_INDEX);
        removeProviderFromIndex(providerId, BIKE_REPAIR_INDEX);
        removeProviderFromIndex(providerId, CAR_REPAIR_INDEX);
        removeProviderFromIndex(providerId, BIKE_FUEL_INDEX);
        removeProviderFromIndex(providerId, CAR_FUEL_INDEX);

        // Publish unavailable event (optional)
        log.info("Provider {} marked as unavailable", providerId);
    }

    @Override
    public void markProviderAvailable(String providerId) {
        log.info("Marking provider {} as available", providerId);

        // Fetch provider details to determine which indices to add to
        try {
            var response = providerServiceClient.getProvider(providerId);
            if (response.isSuccess() && response.getData() != null) {
                ProviderResponse provider = response.getData();

                Set<String> emergencyTypes = provider.getEmergencyTypes() != null ?
                        new HashSet<>(provider.getEmergencyTypes()) : new HashSet<>();

                markProviderAvailable(providerId,
                        determineVehicleType(provider),
                        emergencyTypes);
            } else {
                // If can't fetch details, add to general index only
                markProviderAvailableWithDefaults(providerId);
            }
        } catch (Exception e) {
            log.error("Failed to fetch provider details for {}, using defaults", providerId, e);
            markProviderAvailableWithDefaults(providerId);
        }
    }

    @Override
    public void markProviderAvailable(String providerId, String vehicleType, Set<String> emergencyTypes) {
        log.info("Marking provider {} as available with vehicle: {}, emergency types: {}",
                providerId, vehicleType, emergencyTypes);

        // Get current location
        ProviderLocation location = getProviderLocation(providerId);
        if (location == null) {
            log.warn("Cannot mark provider {} as available: no location found", providerId);
            return;
        }

        Point point = new Point(location.getLongitude(), location.getLatitude());

        // Add to general index
        redisTemplate.opsForGeo().add(PROVIDER_GEO_INDEX, point, providerId);

        // Add to specialized indices based on emergency types
        if (emergencyTypes != null && !emergencyTypes.isEmpty()) {
            for (String emergencyType : emergencyTypes) {
                String index = getGeoIndexForEmergencyType(emergencyType, vehicleType);
                if (index != null) {
                    redisTemplate.opsForGeo().add(index, point, providerId);
                }
            }
        }

        // Set availability flag with TTL
        String availabilityKey = PROVIDER_AVAILABILITY_PREFIX + providerId;
        redisTemplate.opsForValue().set(availabilityKey, "true",
                EmergencyConstants.PROVIDER_AVAILABILITY_TTL_HOURS, TimeUnit.HOURS);

        // Cache provider details
        String detailsKey = PROVIDER_DETAILS_PREFIX + providerId;
        String detailsValue = vehicleType +
                (emergencyTypes != null ? "," + String.join(",", emergencyTypes) : "");
        redisTemplate.opsForValue().set(detailsKey, detailsValue,
                EmergencyConstants.PROVIDER_AVAILABILITY_TTL_HOURS, TimeUnit.HOURS);

        log.info("Provider {} marked as available and added to relevant indices", providerId);
    }

    @Override
    public boolean isProviderAvailable(String providerId) {
        String availabilityKey = PROVIDER_AVAILABILITY_PREFIX + providerId;
        return Boolean.TRUE.equals(redisTemplate.hasKey(availabilityKey));
    }

    @Override
    public ProviderLocation getProviderLocation(String providerId) {
        String locationKey = PROVIDER_LOCATION_PREFIX + providerId;
        String locationStr = redisTemplate.opsForValue().get(locationKey);

        if (locationStr != null) {
            String[] parts = locationStr.split(",");
            if (parts.length >= 3) {
                return ProviderLocation.builder()
                        .providerId(providerId)
                        .latitude(Double.parseDouble(parts[0]))
                        .longitude(Double.parseDouble(parts[1]))
                        .timestamp(Long.parseLong(parts[2]))
                        .available(isProviderAvailable(providerId))
                        .build();
            }
        }
        return null;
    }

    @Override
    public List<ProviderMatchResponse> getAvailableProviders(String emergencyType, String vehicleType) {
        String geoIndex = getGeoIndexForEmergencyType(emergencyType, vehicleType);
        if (geoIndex == null) {
            return List.of();
        }

        // Get all members from the geo index
        Set<Object> providerIds = redisTemplate.opsForHash().keys(geoIndex);

        if (providerIds == null || providerIds.isEmpty()) {
            return List.of();
        }

        return providerIds.stream()
                .map(Object::toString)
                .filter(this::isProviderAvailable)
                .map(providerId -> {
                    ProviderLocation loc = getProviderLocation(providerId);

                    if (loc != null) {
                        return ProviderMatchResponse.builder()
                                .providerId(providerId)
                                .latitude(loc.getLatitude())
                                .longitude(loc.getLongitude())
                                .isAvailable(true)
                                .build();
                    }
                    return null;
                })
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }

    @Override
    public int calculateETA(double distanceKm) {
        // Average speed of 30 km/h in city
        return (int) Math.ceil(distanceKm / 30 * 60);
    }

    @Override
    public void batchUpdateProviderLocations(List<ProviderLocationUpdate> updates) {
        log.info("Batch updating {} provider locations", updates.size());

        for (ProviderLocationUpdate update : updates) {
            try {
                updateProviderLocation(update.getProviderId(),
                        update.getLatitude(),
                        update.getLongitude());

                if (Boolean.FALSE.equals(update.getAvailable())) {
                    markProviderUnavailable(update.getProviderId(), "Batch update - unavailable");
                } else if (Boolean.TRUE.equals(update.getAvailable())) {
                    markProviderAvailable(update.getProviderId(),
                            update.getVehicleType(),
                            update.getEmergencyTypes());
                }
            } catch (Exception e) {
                log.error("Failed to update location for provider {}", update.getProviderId(), e);
            }
        }
    }

    @Override
    public void clearAllProviderLocations() {
        log.warn("Clearing all provider locations from Redis");

        // Delete all geo indices
        redisTemplate.delete(PROVIDER_GEO_INDEX);
        redisTemplate.delete(BIKE_REPAIR_INDEX);
        redisTemplate.delete(CAR_REPAIR_INDEX);
        redisTemplate.delete(BIKE_FUEL_INDEX);
        redisTemplate.delete(CAR_FUEL_INDEX);

        // Delete all availability keys
        Set<String> availabilityKeys = redisTemplate.keys(PROVIDER_AVAILABILITY_PREFIX + "*");
        if (availabilityKeys != null && !availabilityKeys.isEmpty()) {
            redisTemplate.delete(availabilityKeys);
        }

        // Delete all location keys
        Set<String> locationKeys = redisTemplate.keys(PROVIDER_LOCATION_PREFIX + "*");
        if (locationKeys != null && !locationKeys.isEmpty()) {
            redisTemplate.delete(locationKeys);
        }

        // Delete all details keys
        Set<String> detailsKeys = redisTemplate.keys(PROVIDER_DETAILS_PREFIX + "*");
        if (detailsKeys != null && !detailsKeys.isEmpty()) {
            redisTemplate.delete(detailsKeys);
        }
    }

    @Override
    public long getAvailableProvidersCount(String emergencyType, String vehicleType) {

        String geoIndex = getGeoIndexForEmergencyType(emergencyType, vehicleType);

        if (geoIndex == null) {
            return 0;
        }

        Set<Object> providerIds = redisTemplate.opsForHash().keys(geoIndex);

        if (providerIds == null || providerIds.isEmpty()) {
            return 0;
        }

        return providerIds.stream()
                .map(Object::toString)
                .filter(this::isProviderAvailable)
                .count();
    }

    /**
     * Private helper methods
     */
    private void updateProviderSpecializedIndices(String providerId, Double latitude, Double longitude) {
        // Get provider details from cache or service
        String detailsKey = PROVIDER_DETAILS_PREFIX + providerId;
        String details = redisTemplate.opsForValue().get(detailsKey);

        if (details != null) {
            String[] parts = details.split(",");
            String vehicleType = parts[0];
            String[] emergencyTypes = parts.length > 1 ? parts[1].split(",") : new String[0];

            Point point = new Point(longitude, latitude);

            for (String emergencyType : emergencyTypes) {
                String index = getGeoIndexForEmergencyType(emergencyType, vehicleType);
                if (index != null) {
                    redisTemplate.opsForGeo().add(index, point, providerId);
                }
            }
        }
    }

    private String getGeoIndexForEmergencyType(String emergencyType, String vehicleType) {
        if (emergencyType == null || vehicleType == null) {
            return null;
        }

        switch (emergencyType.toUpperCase()) {
            case "REPAIR_EMERGENCY":
                return "CAR".equalsIgnoreCase(vehicleType) ? CAR_REPAIR_INDEX : BIKE_REPAIR_INDEX;
            case "PETROL_EMERGENCY":
                return "CAR".equalsIgnoreCase(vehicleType) ? CAR_FUEL_INDEX : BIKE_FUEL_INDEX;
            default:
                return PROVIDER_GEO_INDEX;
        }
    }

    private void markProviderAvailableWithDefaults(String providerId) {
        ProviderLocation location = getProviderLocation(providerId);
        if (location != null) {
            Point point = new Point(location.getLongitude(), location.getLatitude());
            redisTemplate.opsForGeo().add(PROVIDER_GEO_INDEX, point, providerId);

            String availabilityKey = PROVIDER_AVAILABILITY_PREFIX + providerId;
            redisTemplate.opsForValue().set(availabilityKey, "true",
                    EmergencyConstants.PROVIDER_AVAILABILITY_TTL_HOURS, TimeUnit.HOURS);

            log.info("Provider {} marked as available with default settings", providerId);
        } else {
            log.warn("Cannot mark provider {} as available: no location found", providerId);
        }
    }

    private void enrichProviderDetails(ProviderMatchResponse provider) {
        String detailsKey = PROVIDER_DETAILS_PREFIX + provider.getProviderId();
        String details = redisTemplate.opsForValue().get(detailsKey);

        if (details != null) {
            String[] parts = details.split(",");
            if (parts.length > 0) {
                provider.setVehicleType(parts[0]);
            }
        }
    }

    private String determineVehicleType(ProviderResponse provider) {
        if (provider.getSupportsBike() && !provider.getSupportsCar()) {
            return "BIKE";
        } else if (provider.getSupportsCar() && !provider.getSupportsBike()) {
            return "CAR";
        } else {
            return "BOTH";
        }
    }


}