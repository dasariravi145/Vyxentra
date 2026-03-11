package com.vyxentra.vehicle.service;

import com.vyxentra.vehicle.client.BookingServiceClient;
import com.vyxentra.vehicle.dto.TrackingStats;
import com.vyxentra.vehicle.dto.request.TrackingSubscribeRequest;
import com.vyxentra.vehicle.dto.response.ETAUpdateResponse;
import com.vyxentra.vehicle.dto.response.LocationResponse;
import com.vyxentra.vehicle.dto.response.TrackingInfoResponse;
import com.vyxentra.vehicle.entity.ETAHistory;
import com.vyxentra.vehicle.entity.TrackingSession;
import com.vyxentra.vehicle.exceptions.BusinessException;
import com.vyxentra.vehicle.exceptions.ErrorCode;
import com.vyxentra.vehicle.exceptions.ResourceNotFoundException;
import com.vyxentra.vehicle.repository.ETAHistoryRepository;
import com.vyxentra.vehicle.repository.LocationUpdateRepository;
import com.vyxentra.vehicle.repository.TrackingSessionRepository;
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
public class TrackingServiceImpl implements TrackingService {

    private final TrackingSessionRepository sessionRepository;
    private final ETAHistoryRepository etaHistoryRepository;
    private final LocationUpdateRepository locationRepository;
    private final LocationService locationService;
    private final ETAService etaService;
    private final LocationBroadcastService broadcastService;
    private final RedisTemplate<String, String> redisTemplate;

    private final BookingServiceClient bookingServiceClient;

    @Value("${tracking.session.expiry-minutes:120}")
    private int sessionExpiryMinutes;

    @Override
    @Transactional
    public TrackingInfoResponse subscribeToTracking(String userId, TrackingSubscribeRequest request) {
        log.info("User {} subscribing to tracking for booking: {}", userId, request.getBookingId());

        TrackingSession session = sessionRepository.findByBookingId(request.getBookingId())
                .orElseThrow(() -> new ResourceNotFoundException("TrackingSession", "booking", request.getBookingId()));

        // Verify user has permission to track this booking
        if (!userId.equals(session.getCustomerId()) &&
                !userId.equals(session.getProviderId()) &&
                !userId.equals(session.getEmployeeId())) {
            throw new BusinessException(ErrorCode.UNAUTHORIZED, "Not authorized to track this booking");
        }

        // Store subscription in Redis for quick access
        String subscriptionKey = "tracking:sub:" + request.getBookingId() + ":" + userId;
        redisTemplate.opsForValue().set(subscriptionKey, "ACTIVE", sessionExpiryMinutes, TimeUnit.MINUTES);

        // Add to active subscribers set
        redisTemplate.opsForSet().add("tracking:subscribers:" + request.getBookingId(), userId);

        log.info("User {} subscribed to tracking for booking: {}", userId, request.getBookingId());

        return mapToResponse(session);
    }

    @Override
    @Transactional
    public void unsubscribeFromTracking(String userId, String bookingId) {
        log.info("User {} unsubscribing from tracking for booking: {}", userId, bookingId);

        String subscriptionKey = "tracking:sub:" + bookingId + ":" + userId;
        redisTemplate.delete(subscriptionKey);
        redisTemplate.opsForSet().remove("tracking:subscribers:" + bookingId, userId);

        log.info("User {} unsubscribed from tracking for booking: {}", userId, bookingId);
    }

    @Override
    @Transactional(readOnly = true)
    public TrackingInfoResponse getTrackingSession(String bookingId) {
        TrackingSession session = sessionRepository.findByBookingId(bookingId)
                .orElseThrow(() -> new ResourceNotFoundException("TrackingSession", "booking", bookingId));
        return mapToResponse(session);
    }

    @Override
    @Transactional(readOnly = true)
    public String getTrackingStatus(String bookingId) {
        return sessionRepository.findByBookingId(bookingId)
                .map(TrackingSession::getStatus)
                .orElse("NOT_FOUND");
    }

    @Override
    @Transactional
    public void pauseTracking(String userId, String bookingId) {
        log.info("Pausing tracking for booking: {} by user: {}", bookingId, userId);

        TrackingSession session = sessionRepository.findByBookingId(bookingId)
                .orElseThrow(() -> new ResourceNotFoundException("TrackingSession", "booking", bookingId));

        // Only provider or employee can pause tracking
        if (!userId.equals(session.getProviderId()) && !userId.equals(session.getEmployeeId())) {
            throw new BusinessException(ErrorCode.UNAUTHORIZED, "Only provider can pause tracking");
        }

        session.setStatus("PAUSED");
        sessionRepository.save(session);

        // Notify subscribers
        broadcastService.broadcastTrackingPaused(bookingId);

        log.info("Tracking paused for booking: {}", bookingId);
    }

    @Override
    @Transactional
    public void resumeTracking(String userId, String bookingId) {
        log.info("Resuming tracking for booking: {} by user: {}", bookingId, userId);

        TrackingSession session = sessionRepository.findByBookingId(bookingId)
                .orElseThrow(() -> new ResourceNotFoundException("TrackingSession", "booking", bookingId));

        // Only provider or employee can resume tracking
        if (!userId.equals(session.getProviderId()) && !userId.equals(session.getEmployeeId())) {
            throw new BusinessException(ErrorCode.UNAUTHORIZED, "Only provider can resume tracking");
        }

        session.setStatus("ACTIVE");
        sessionRepository.save(session);

        // Notify subscribers
        broadcastService.broadcastTrackingResumed(bookingId);

        log.info("Tracking resumed for booking: {}", bookingId);
    }

    @Override
    @Transactional(readOnly = true)
    public Integer getCurrentETA(String bookingId) {
        return sessionRepository.findByBookingId(bookingId)
                .map(TrackingSession::getCurrentEtaMinutes)
                .orElse(null);
    }

    @Override
    @Transactional(readOnly = true)
    public List<TrackingInfoResponse> getUserTrackingHistory(String userId, LocalDateTime from, LocalDateTime to) {
        List<TrackingSession> sessions = sessionRepository.findByUserAndDateRange(userId, from, to);
        return sessions.stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<ETAUpdateResponse> getETAHistory(String bookingId) {
        TrackingSession session = sessionRepository.findByBookingId(bookingId)
                .orElseThrow(() -> new ResourceNotFoundException("TrackingSession", "booking", bookingId));

        return etaHistoryRepository.findTop10ByTrackingSessionIdOrderByCalculatedAtDesc(session.getId())
                .stream()
                .map(this::mapToETAResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public TrackingStats getTrackingStats(String bookingId) {
        TrackingSession session = sessionRepository.findByBookingId(bookingId)
                .orElseThrow(() -> new ResourceNotFoundException("TrackingSession", "booking", bookingId));

        // Calculate stats from location history
        List<LocationResponse> locations = locationService.getLocationHistory(
                session.getProviderId(), "PROVIDER", session.getStartedAt(),
                session.getCompletedAt() != null ? session.getCompletedAt() : LocalDateTime.now());

        double maxSpeed = 0;
        int etaChanges = etaHistoryRepository.findByTrackingSessionIdOrderByCalculatedAtDesc(session.getId()).size();

        for (LocationResponse loc : locations) {
            if (loc.getSpeed() != null && loc.getSpeed() > maxSpeed) {
                maxSpeed = loc.getSpeed();
            }
        }

        return TrackingStats.builder()
                .totalDistanceKm(session.getTotalDistanceKm())
                .totalDurationMinutes(session.getCompletedAt() != null ?
                        (int) java.time.Duration.between(session.getStartedAt(), session.getCompletedAt()).toMinutes() : null)
                .averageSpeed(session.getTotalDistanceKm() != null && session.getCompletedAt() != null ?
                        (int) (session.getTotalDistanceKm() /
                                (java.time.Duration.between(session.getStartedAt(), session.getCompletedAt()).toHours())) : null)
                .maxSpeed((int) maxSpeed)
                .etaChanges(etaChanges)
                .startedAt(session.getStartedAt())
                .completedAt(session.getCompletedAt())
                .build();
    }

    @Override
    @Transactional
    public void createTrackingSession(String bookingId, String customerId, String providerId,
                                      Double destLat, Double destLng, String destAddress) {
        log.info("Creating tracking session for booking: {}", bookingId);

        // Check if session already exists
        if (sessionRepository.findByBookingId(bookingId).isPresent()) {
            log.warn("Tracking session already exists for booking: {}", bookingId);
            return;
        }

        LocalDateTime now = LocalDateTime.now();

        TrackingSession session = TrackingSession.builder()
                .bookingId(bookingId)
                .customerId(customerId)
                .providerId(providerId)
                .status("ACTIVE")
                .destinationLat(destLat)
                .destinationLng(destLng)
                .destinationAddress(destAddress)
                .startedAt(now)
                .expiresAt(now.plusMinutes(sessionExpiryMinutes))
                .totalDistanceKm(0.0)
                .build();

        session = sessionRepository.save(session);

        log.info("Tracking session created with ID: {}", session.getId());
    }

    @Override
    @Transactional
    public void endTrackingSession(String bookingId) {
        log.info("Ending tracking session for booking: {}", bookingId);

        sessionRepository.findByBookingId(bookingId).ifPresent(session -> {
            session.setStatus("COMPLETED");
            session.setCompletedAt(LocalDateTime.now());
            sessionRepository.save(session);

            // Notify subscribers
            broadcastService.broadcastTrackingEnded(bookingId);

            log.info("Tracking session ended for booking: {}", bookingId);
        });
    }

    @Override
    @Transactional
    public void processStaleSessions() {
        log.info("Processing stale tracking sessions");

        LocalDateTime timeout = LocalDateTime.now().minusMinutes(30);
        List<TrackingSession> staleSessions = sessionRepository.findStaleSessions(timeout);

        for (TrackingSession session : staleSessions) {
            session.setStatus("PAUSED");
            sessionRepository.save(session);
            log.info("Marked session {} as paused due to inactivity", session.getId());
        }

        LocalDateTime now = LocalDateTime.now();
        List<TrackingSession> expiredSessions = sessionRepository.findExpiredSessions(now);

        for (TrackingSession session : expiredSessions) {
            session.setStatus("EXPIRED");
            sessionRepository.save(session);
            log.info("Marked session {} as expired", session.getId());
        }
    }

    private TrackingInfoResponse mapToResponse(TrackingSession session) {
        LocationResponse currentLocation = null;
        if (session.getCurrentLocationLat() != null && session.getCurrentLocationLng() != null) {
            currentLocation = LocationResponse.builder()
                    .latitude(session.getCurrentLocationLat())
                    .longitude(session.getCurrentLocationLng())
                    .timestamp(session.getLastUpdateAt())
                    .build();
        }

        LocationResponse destination = LocationResponse.builder()
                .latitude(session.getDestinationLat())
                .longitude(session.getDestinationLng())
                .build();

        LocalDateTime estimatedArrival = session.getCurrentEtaMinutes() != null ?
                session.getLastUpdateAt().plusMinutes(session.getCurrentEtaMinutes()) : null;

        // Check if provider is online (has recent location updates)
        boolean isProviderOnline = locationRepository
                .findFirstByEntityIdAndEntityTypeOrderByCreatedAtDesc(session.getProviderId(), "PROVIDER")
                .map(update -> update.getCreatedAt().isAfter(LocalDateTime.now().minusMinutes(5)))
                .orElse(false);

        return TrackingInfoResponse.builder()
                .sessionId(session.getId())
                .bookingId(session.getBookingId())
                .customerId(session.getCustomerId())
                .providerId(session.getProviderId())
                .employeeId(session.getEmployeeId())
                .status(session.getStatus())
                .startLocation(currentLocation)
                .currentLocation(currentLocation)
                .destination(destination)
                .totalDistanceKm(session.getTotalDistanceKm())
                .currentETA(session.getCurrentEtaMinutes())
                .startedAt(session.getStartedAt())
                .lastUpdateAt(session.getLastUpdateAt())
                .estimatedArrival(estimatedArrival)
                .isProviderOnline(isProviderOnline)
                .isTrackingActive("ACTIVE".equals(session.getStatus()))
                .build();
    }

    private ETAUpdateResponse mapToETAResponse(ETAHistory eta) {
        return ETAUpdateResponse.builder()
                .bookingId(eta.getTrackingSession().getBookingId())
                .etaMinutes(eta.getEtaMinutes())
                .distanceKm(eta.getDistanceKm())
                .reason(eta.getReason())
                .calculatedAt(eta.getCalculatedAt())
                .build();
    }
}
