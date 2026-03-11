package com.vyxentra.vehicle.service;

import com.vyxentra.vehicle.dto.PathPoint;
import com.vyxentra.vehicle.dto.request.LocationUpdateRequest;
import com.vyxentra.vehicle.dto.response.LocationResponse;
import com.vyxentra.vehicle.dto.response.PathResponse;
import com.vyxentra.vehicle.entity.LocationUpdate;
import com.vyxentra.vehicle.entity.TrackingSession;
import com.vyxentra.vehicle.exceptions.BusinessException;
import com.vyxentra.vehicle.exceptions.ErrorCode;
import com.vyxentra.vehicle.exceptions.ResourceNotFoundException;
import com.vyxentra.vehicle.repository.LocationUpdateRepository;
import com.vyxentra.vehicle.repository.TrackingSessionRepository;
import com.vyxentra.vehicle.utils.GeoUtil;
import com.vyxentra.vehicle.websocket.LocationBroadcastService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class LocationServiceImpl implements LocationService {

    private final LocationUpdateRepository locationRepository;
    private final TrackingSessionRepository sessionRepository;
    private final ETAService etaService;
    private final LocationBroadcastService broadcastService;
    private final RedisTemplate<String, String> redisTemplate;

    @Value("${tracking.location.history-retention-days:7}")
    private int historyRetentionDays;

    @Value("${tracking.location.update-threshold-meters:10}")
    private int updateThresholdMeters;

    @Value("${tracking.location.max-updates-per-minute:60}")
    private int maxUpdatesPerMinute;

    @Override
    @Transactional
    public void updateLocation(String userId, LocationUpdateRequest request) {
        log.debug("Location update from user: {} at ({}, {})", userId, request.getLatitude(), request.getLongitude());

        // Rate limiting
        String rateLimitKey = "location:rate:" + userId + ":" + LocalDateTime.now().getMinute();
        Long count = redisTemplate.opsForValue().increment(rateLimitKey);
        if (count == 1) {
            redisTemplate.expire(rateLimitKey, 1, TimeUnit.MINUTES);
        }
        if (count > maxUpdatesPerMinute) {
            throw new BusinessException(ErrorCode.RATE_LIMIT_EXCEEDED, "Too many location updates");
        }

        // Save location update
        LocationUpdate update = LocationUpdate.builder()
                .entityType(request.getEntityType() != null ? request.getEntityType() : "PROVIDER")
                .entityId(userId)
                .bookingId(request.getBookingId())
                .latitude(request.getLatitude())
                .longitude(request.getLongitude())
                .speed(request.getSpeed())
                .heading(request.getHeading())
                .accuracy(request.getAccuracy())
                .altitude(request.getAltitude())
                .source(request.getSource() != null ? request.getSource() : "GPS")
                .build();

        locationRepository.save(update);

        // Update tracking session if this is for a booking
        if (request.getBookingId() != null) {
            updateTrackingSession(request.getBookingId(), request);
        }

        // Cache current location in Redis for quick access
        String cacheKey = "location:current:" + userId;
        redisTemplate.opsForValue().set(cacheKey,
                request.getLatitude() + "," + request.getLongitude() + "," + LocalDateTime.now().toString(),
                5, TimeUnit.MINUTES);

        // Broadcast to subscribers
        if (request.getBookingId() != null) {
            broadcastService.broadcastLocation(request.getBookingId(), mapToResponse(update));
        }
    }

    @Override
    @Transactional(readOnly = true)
    public LocationResponse getCurrentLocation(String entityId, String entityType) {
        // Try Redis cache first
        String cacheKey = "location:current:" + entityId;
        String cached = redisTemplate.opsForValue().get(cacheKey);

        if (cached != null) {
            String[] parts = cached.split(",");
            return LocationResponse.builder()
                    .entityId(entityId)
                    .entityType(entityType)
                    .latitude(Double.parseDouble(parts[0]))
                    .longitude(Double.parseDouble(parts[1]))
                    .timestamp(LocalDateTime.parse(parts[2]))
                    .build();
        }

        // Fallback to database
        LocationUpdate update = locationRepository
                .findFirstByEntityIdAndEntityTypeOrderByCreatedAtDesc(entityId, entityType)
                .orElseThrow(() -> new ResourceNotFoundException("Location", "entity", entityId));

        return mapToResponse(update);
    }

    @Override
    @Transactional(readOnly = true)
    public List<LocationResponse> getLocationHistory(String entityId, String entityType,
                                                     LocalDateTime from, LocalDateTime to) {
        List<LocationUpdate> updates = locationRepository
                .findByEntityIdAndEntityTypeAndCreatedAtBetweenOrderByCreatedAtAsc(
                        entityId, entityType, from, to);

        return updates.stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public PathResponse getTrackingPath(String bookingId) {
        TrackingSession session = sessionRepository.findByBookingId(bookingId)
                .orElseThrow(() -> new ResourceNotFoundException("TrackingSession", "booking", bookingId));

        LocalDateTime start = session.getStartedAt();
        LocalDateTime end = session.getCompletedAt() != null ? session.getCompletedAt() : LocalDateTime.now();

        List<LocationUpdate> updates = locationRepository.findPathForBooking(bookingId, start, end);

        List<PathPoint> path = updates.stream()
                .map(update -> PathPoint.builder()
                        .latitude(update.getLatitude())
                        .longitude(update.getLongitude())
                        .timestamp(update.getCreatedAt())
                        .speed(update.getSpeed())
                        .build())
                .collect(Collectors.toList());

        return PathResponse.builder()
                .bookingId(bookingId)
                .path(path)
                .startTime(start)
                .endTime(end)
                .totalDistance(session.getTotalDistanceKm())
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public List<String> getActiveTrackings(String providerId) {
        if (providerId != null) {
            return sessionRepository.findByProviderIdOrderByCreatedAtDesc(providerId).stream()
                    .filter(s -> "ACTIVE".equals(s.getStatus()))
                    .map(TrackingSession::getBookingId)
                    .collect(Collectors.toList());
        }

        return sessionRepository.findAll().stream()
                .filter(s -> "ACTIVE".equals(s.getStatus()))
                .map(TrackingSession::getBookingId)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public void cleanupOldLocations() {
        log.info("Cleaning up old location history");

        LocalDateTime cutoffDate = LocalDateTime.now().minusDays(historyRetentionDays);
        int deleted = locationRepository.deleteOlderThan(cutoffDate);

        log.info("Deleted {} old location records", deleted);
    }

    private void updateTrackingSession(String bookingId, LocationUpdateRequest request) {
        sessionRepository.findByBookingId(bookingId).ifPresent(session -> {
            // Calculate distance from last known location
            if (session.getCurrentLocationLat() != null && session.getCurrentLocationLng() != null) {
                double distance = GeoUtil.calculateDistance(
                        session.getCurrentLocationLat(), session.getCurrentLocationLng(),
                        request.getLatitude(), request.getLongitude()
                ) * 1000; // Convert to meters

                // Only update if moved more than threshold
                if (distance >= updateThresholdMeters) {
                    double distanceKm = distance / 1000;
                    session.setTotalDistanceKm(session.getTotalDistanceKm() + distanceKm);
                }
            }

            // Calculate new ETA
            Integer eta = etaService.calculateETA(
                    request.getLatitude(), request.getLongitude(),
                    session.getDestinationLat(), session.getDestinationLng()
            );

            session.setCurrentLocationLat(request.getLatitude());
            session.setCurrentLocationLng(request.getLongitude());
            session.setLastUpdateAt(LocalDateTime.now());
            session.setCurrentEtaMinutes(eta);

            sessionRepository.save(session);
        });
    }

    private LocationResponse mapToResponse(LocationUpdate update) {
        return LocationResponse.builder()
                .entityId(update.getEntityId())
                .entityType(update.getEntityType())
                .latitude(update.getLatitude())
                .longitude(update.getLongitude())
                .speed(update.getSpeed())
                .heading(update.getHeading())
                .accuracy(update.getAccuracy())
                .altitude(update.getAltitude())
                .source(update.getSource())
                .timestamp(update.getCreatedAt())
                .build();
    }
}
