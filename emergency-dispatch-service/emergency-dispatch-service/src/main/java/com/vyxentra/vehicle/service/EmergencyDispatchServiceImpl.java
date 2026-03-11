package com.vyxentra.vehicle.service;
import com.vyxentra.vehicle.client.BookingServiceClient;
import com.vyxentra.vehicle.client.PaymentServiceClient;
import com.vyxentra.vehicle.client.ProviderServiceClient;
import com.vyxentra.vehicle.dto.AssignmentInfo;
import com.vyxentra.vehicle.dto.FuelInfo;
import com.vyxentra.vehicle.dto.Location;
import com.vyxentra.vehicle.dto.request.EmergencyBookingRequest;
import com.vyxentra.vehicle.dto.request.ProviderAcceptRequest;
import com.vyxentra.vehicle.dto.response.EmergencyBookingResponse;
import com.vyxentra.vehicle.dto.response.FuelPriceResponse;
import com.vyxentra.vehicle.dto.response.ProviderMatchResponse;
import com.vyxentra.vehicle.dto.response.ProviderResponse;
import com.vyxentra.vehicle.entity.EmergencyAssignment;
import com.vyxentra.vehicle.entity.EmergencyRequest;
import com.vyxentra.vehicle.entity.ProviderNotification;
import com.vyxentra.vehicle.enums.EmergencyStatus;
import com.vyxentra.vehicle.enums.EmergencyType;
import com.vyxentra.vehicle.enums.ProviderResponseStatus; // Note: singular 'exception'
import com.vyxentra.vehicle.exception.EmergencyException;
import com.vyxentra.vehicle.exceptions.BusinessException;
import com.vyxentra.vehicle.exceptions.ErrorCode;
import com.vyxentra.vehicle.exceptions.ResourceNotFoundException;
import com.vyxentra.vehicle.kafka.EmergencyEventProducer;
import com.vyxentra.vehicle.repository.EmergencyAssignmentRepository;
import com.vyxentra.vehicle.repository.EmergencyRequestRepository;
import com.vyxentra.vehicle.repository.ProviderNotificationRepository;
import com.vyxentra.vehicle.utils.EmergencyConstants;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class EmergencyDispatchServiceImpl implements EmergencyDispatchService {

    private final EmergencyRequestRepository requestRepository;
    private final EmergencyAssignmentRepository assignmentRepository;
    private final ProviderNotificationRepository notificationRepository;
    private final ProviderMatchingService providerMatchingService;
    private final FuelPriceService fuelPriceService;
    private final DistanceCalculationService distanceCalculationService;
    private final DistributedLockService lockService;
    private final EmergencyEventProducer eventProducer;
    private final RedisTemplate<String, String> redisTemplate;

    private final ProviderServiceClient providerServiceClient;
    private final BookingServiceClient bookingServiceClient;
    private final PaymentServiceClient paymentServiceClient;

    @Value("${emergency.search.initial-radius-km:5}")
    private int initialRadiusKm;

    @Value("${emergency.search.max-radius-km:50}")
    private int maxRadiusKm;

    @Value("${emergency.search.radius-increment-km:5}")
    private int radiusIncrementKm;

    @Value("${emergency.search.provider-timeout-seconds:30}")
    private int providerTimeoutSeconds;

    @Value("${emergency.search.request-expiry-minutes:15}")
    private int requestExpiryMinutes;

    @Value("${emergency.multipliers.repair:1.5}")
    private double repairMultiplier;

    @Value("${emergency.multipliers.petrol:1.2}")
    private double petrolMultiplier;

    // ==================== Core Emergency Operations ====================

    @Override
    @Transactional
    public EmergencyBookingResponse requestEmergency(String customerId, EmergencyBookingRequest request) {
        log.info("Processing emergency request for customer: {}, type: {}", customerId, request.getEmergencyType());

        // Check if customer already has active emergency
        if (requestRepository.findActiveByCustomerId(customerId).isPresent()) {
            throw new BusinessException(ErrorCode.CUSTOMER_ALREADY_HAS_EMERGENCY,
                    "You already have an active emergency request");
        }

        // Validate location
        validateCoordinates(request.getLocation().getLatitude(), request.getLocation().getLongitude());

        // Generate request number
        String requestNumber = generateRequestNumber();

        // Calculate base amount and multiplier
        double multiplier = request.getEmergencyType() == EmergencyType.REPAIR_EMERGENCY ?
                repairMultiplier : petrolMultiplier;

        double baseAmount = 0.0;
        Double fuelCost = null;
        Double totalFuelCost = null;

        // Handle petrol emergency specific calculations
        if (request.getEmergencyType() == EmergencyType.PETROL_EMERGENCY) {
            if (request.getPetrolDetails() == null) {
                throw new BusinessException(ErrorCode.VALIDATION_ERROR, "Petrol details required for fuel emergency");
            }

            // Validate fuel quantity
            if (request.getPetrolDetails().getQuantity() <= 0 ||
                    request.getPetrolDetails().getQuantity() > 50) {
                throw new BusinessException(ErrorCode.QUANTITY_EXCEEDS_LIMIT,
                        "Fuel quantity must be between 1 and 50 liters");
            }

            // Get fuel price - using synchronous method
            try {

                FuelPriceResponse fuelPriceResponse = fuelPriceService.getFuelPriceDetails(
                        request.getPetrolDetails().getFuelType(),
                        request.getLocation().getCity()
                ).block();

                fuelCost = fuelPriceResponse.getPrice();

                if (fuelPriceResponse.isFallback()) {
                    log.warn("Using fallback fuel price data from source: {}", fuelPriceResponse.getSource());
                }

                log.info("Fuel price from {}: ₹{} per liter for {} in {}",
                        fuelPriceResponse.getSource(),
                        fuelCost,
                        request.getPetrolDetails().getFuelType(),
                        request.getLocation().getCity());

            } catch (Exception e) {

                log.error("Error fetching fuel price, using default: {}", e.getMessage());

                fuelCost = getDefaultFuelPrice(request.getPetrolDetails().getFuelType());
            }

            if (fuelCost == null) {
                fuelCost = getDefaultFuelPrice(request.getPetrolDetails().getFuelType());
            }

            totalFuelCost = fuelCost * request.getPetrolDetails().getQuantity();
            baseAmount = totalFuelCost;
        }

        double totalAmount = baseAmount * multiplier;

        // Create emergency request
        EmergencyRequest emergencyRequest = EmergencyRequest.builder()
                .requestNumber(requestNumber)
                .customerId(customerId)
                .emergencyType(request.getEmergencyType())
                .vehicleType(request.getVehicleType())
                .vehicleDetails(request.getVehicleDetails())
                .locationLat(request.getLocation().getLatitude())
                .locationLng(request.getLocation().getLongitude())
                .locationAddress(request.getLocation().getAddress())
                .status(String.valueOf(EmergencyStatus.SEARCHING))
                .searchRadiusKm(initialRadiusKm)
                .currentRadiusKm(initialRadiusKm)
                .maxRadiusKm(maxRadiusKm)
                .expiryTime(LocalDateTime.now().plusMinutes(requestExpiryMinutes))
                .issueDescription(request.getIssueDescription())
                .baseAmount(baseAmount)
                .multiplier(BigDecimal.valueOf(multiplier))
                .totalAmount(BigDecimal.valueOf(totalAmount))
                .build();

        // Set petrol specific fields
        if (request.getEmergencyType() == EmergencyType.PETROL_EMERGENCY) {
            emergencyRequest.setFuelType(request.getPetrolDetails().getFuelType());
            emergencyRequest.setQuantityLiters(request.getPetrolDetails().getQuantity());
            emergencyRequest.setFuelCostPerLiter(fuelCost);
            emergencyRequest.setTotalFuelCost(BigDecimal.valueOf(totalFuelCost));
        }

        emergencyRequest = requestRepository.save(emergencyRequest);

        // Cache request in Redis for fast access
        cacheEmergencyRequest(emergencyRequest);

        // Find nearby providers
        List<ProviderMatchResponse> nearbyProviders = findNearbyProviders(
                request.getLocation().getLatitude(),
                request.getLocation().getLongitude(),
                request.getEmergencyType().name(),
                request.getVehicleType().name(),
                initialRadiusKm
        );

        // Notify providers
        notifyProviders(emergencyRequest, nearbyProviders);

        // Publish event
        eventProducer.publishEmergencyTriggered(emergencyRequest);

        log.info("Emergency request created with ID: {}, Number: {}", emergencyRequest.getId(), requestNumber);

        return mapToResponse(emergencyRequest, null);
    }

    @Override
    @Transactional(readOnly = true)
    public EmergencyBookingResponse getEmergencyRequest(String requestId) {
        EmergencyRequest request = findRequestById(requestId);
        EmergencyAssignment assignment = assignmentRepository.findByRequestId(requestId).orElse(null);
        return mapToResponse(request, assignment);
    }

    @Override
    @Transactional(readOnly = true)
    public EmergencyBookingResponse getEmergencyRequestByNumber(String requestNumber) {
        EmergencyRequest request = requestRepository.findByRequestNumber(requestNumber)
                .orElseThrow(() -> new ResourceNotFoundException("EmergencyRequest", "number", requestNumber));
        EmergencyAssignment assignment = assignmentRepository.findByRequestId(request.getId()).orElse(null);
        return mapToResponse(request, assignment);
    }

    @Override
    @Transactional
    public void cancelEmergencyRequest(String requestId, String customerId, String reason) {
        log.info("Cancelling emergency request: {} by customer: {}", requestId, customerId);

        EmergencyRequest request = findRequestById(requestId);

        if (!request.getCustomerId().equals(customerId)) {
            throw new BusinessException(ErrorCode.UNAUTHORIZED, "Not authorized to cancel this request");
        }

        // Convert string to enum for comparison
        String statusStr = request.getStatus();
        EmergencyStatus currentStatus = EmergencyStatus.valueOf(statusStr);

        if (currentStatus != EmergencyStatus.SEARCHING) {
            throw new EmergencyException(requestId, ErrorCode.EMERGENCY_INVALID_STATUS,
                    "Cannot cancel request in " + statusStr + " state");
        }

        request.setStatus(String.valueOf(EmergencyStatus.CANCELLED));
        requestRepository.save(request);

        // Remove from cache
        redisTemplate.delete(EmergencyConstants.EMERGENCY_REQUEST_PREFIX + requestId);

        // Mark all pending notifications as cancelled
        markPendingNotificationsAsCancelled(requestId);

        // Publish cancellation event
        eventProducer.publishEmergencyCancelled(request, reason);

        log.info("Emergency request cancelled: {}", requestId);
    }

    // ==================== Provider Matching Operations ====================

    @Override
    @Transactional(readOnly = true)
    public List<ProviderMatchResponse> findNearbyProviders(Double latitude, Double longitude,
                                                           String emergencyType, String vehicleType,
                                                           Integer radiusKm) {
        log.debug("Finding nearby providers for emergency: {} at ({}, {})", emergencyType, latitude, longitude);

        // Validate coordinates
        validateCoordinates(latitude, longitude);

        // Determine which Redis Geo index to use
        String geoIndex = getGeoIndex(emergencyType, vehicleType);

        // Search in Redis Geo
        List<ProviderMatchResponse> providers = providerMatchingService.findNearbyProviders(
                geoIndex, latitude, longitude, radiusKm);

        // Enrich with provider details from cache/service
        providers.forEach(this::enrichProviderDetails);

        log.debug("Found {} providers within {} km", providers.size(), radiusKm);
        return providers;
    }

    @Override
    @Transactional
    public EmergencyBookingResponse acceptEmergency(String providerId, ProviderAcceptRequest request) {
        log.info("Provider {} accepting emergency request: {}", providerId, request.getRequestId());

        // Acquire distributed lock to prevent race conditions
        String lockKey = EmergencyConstants.EMERGENCY_LOCK_PREFIX + request.getRequestId();
        boolean locked = lockService.tryLock(lockKey, 10, TimeUnit.SECONDS);

        if (!locked) {
            throw new BusinessException(ErrorCode.LOCK_ACQUISITION_FAILED,
                    "Unable to process acceptance at this time. Please try again.");
        }

        try {
            EmergencyRequest emergencyRequest = findRequestById(request.getRequestId());

            // Validate request state
            if (!emergencyRequest.getStatus().equals(EmergencyStatus.SEARCHING.name())) {
                throw new EmergencyException(
                        emergencyRequest.getId(),
                        ErrorCode.EMERGENCY_INVALID_STATUS,
                        "Emergency request already " + emergencyRequest.getStatus()
                );
            }

            // Check if already assigned
            if (assignmentRepository.existsByRequestId(request.getRequestId())) {
                throw new BusinessException(ErrorCode.EMERGENCY_ALREADY_ASSIGNED,
                        "Emergency already accepted by another provider");
            }

            // Check if request has expired
            if (emergencyRequest.getExpiryTime().isBefore(LocalDateTime.now())) {
                emergencyRequest.setStatus(String.valueOf(EmergencyStatus.EXPIRED));
                requestRepository.save(emergencyRequest);
                throw new BusinessException(ErrorCode.EMERGENCY_EXPIRED,
                        "Emergency request has expired");
            }

            // Validate provider is active and eligible
            validateProviderForEmergency(providerId, emergencyRequest);

            // Calculate distance and ETA
            double distance = distanceCalculationService.calculateDistance(
                    emergencyRequest.getLocationLat(), emergencyRequest.getLocationLng(),
                    request.getCurrentLocation().getLatitude(), request.getCurrentLocation().getLongitude()
            );

            int eta = distanceCalculationService.calculateETA(distance);

            // Get provider details
            ProviderResponse provider = getProviderDetails(providerId);

            // Create assignment
            EmergencyAssignment assignment = EmergencyAssignment.builder()
                    .request(emergencyRequest)
                    .providerId(providerId)
                    .providerName(provider.getBusinessName())
                    .providerPhone(provider.getPhone())
                    .providerLat(request.getCurrentLocation().getLatitude())
                    .providerLng(request.getCurrentLocation().getLongitude())
                    .distanceKm(distance)
                    .etaMinutes(eta)
                    .acceptedAt(LocalDateTime.now())
                    .status("ACCEPTED")
                    .build();

            assignment = assignmentRepository.save(assignment);

            // Update request status
            emergencyRequest.setStatus(String.valueOf(EmergencyStatus.ASSIGNED));
            requestRepository.save(emergencyRequest);

            // Create booking in booking service
            String bookingId = createBookingFromEmergency(emergencyRequest, assignment);
            assignment.setBookingId(bookingId);
            assignmentRepository.save(assignment);

            // Mark provider as unavailable
            providerMatchingService.markProviderUnavailable(providerId, "Assigned to emergency: " + emergencyRequest.getId());

            // Update notification status
            updateProviderNotificationStatus(emergencyRequest.getId(), providerId, ProviderResponseStatus.ACCEPTED);

            // Mark other pending notifications as rejected
            markOtherNotificationsAsRejected(emergencyRequest.getId(), providerId);

            // Update cache
            redisTemplate.delete(EmergencyConstants.EMERGENCY_REQUEST_PREFIX + emergencyRequest.getId());
            cacheEmergencyAssignment(assignment);

            // Publish event
            eventProducer.publishEmergencyAssigned(emergencyRequest, assignment);

            log.info("Emergency request {} accepted by provider {}", request.getRequestId(), providerId);

            return mapToResponse(emergencyRequest, assignment);

        } finally {
            lockService.unlock(lockKey);
        }
    }

    @Override
    public void updateProviderLocation(String providerId, Double latitude, Double longitude) {
        log.debug("Updating location for provider: {} to ({}, {})", providerId, latitude, longitude);

        // Validate coordinates
        validateCoordinates(latitude, longitude);

        // Update in Redis Geo indices
        providerMatchingService.updateProviderLocation(providerId, latitude, longitude);

        // Check if provider has active assignment and update ETA
        updateETAForActiveAssignment(providerId, latitude, longitude);
    }

    // ==================== Active Emergency Queries ====================

    @Override
    @Transactional(readOnly = true)
    public EmergencyBookingResponse getActiveEmergencyForCustomer(String customerId) {
        EmergencyRequest request = requestRepository.findActiveByCustomerId(customerId)
                .orElse(null);

        if (request == null) {
            return null;
        }

        EmergencyAssignment assignment = assignmentRepository.findByRequestId(request.getId()).orElse(null);
        return mapToResponse(request, assignment);
    }

    @Override
    @Transactional(readOnly = true)
    public EmergencyBookingResponse getActiveAssignmentForProvider(String providerId) {
        EmergencyAssignment assignment = assignmentRepository.findActiveByProviderId(providerId)
                .orElse(null);

        if (assignment == null) {
            return null;
        }

        return mapToResponse(assignment.getRequest(), assignment);
    }

    // ==================== Assignment Lifecycle ====================

    @Override
    @Transactional
    public void providerArrived(String assignmentId, String providerId) {
        log.info("Provider {} arrived at location for assignment: {}", providerId, assignmentId);

        EmergencyAssignment assignment = findAssignmentById(assignmentId);

        if (!assignment.getProviderId().equals(providerId)) {
            throw new BusinessException(ErrorCode.UNAUTHORIZED, "Not authorized to update this assignment");
        }

        if (!"ACCEPTED".equals(assignment.getStatus())) {
            throw new EmergencyException(assignment.getRequest().getId(), ErrorCode.EMERGENCY_INVALID_STATUS,
                    "Assignment must be in ACCEPTED state");
        }

        assignment.setStatus("ARRIVED");
        assignmentRepository.save(assignment);

        EmergencyRequest request = assignment.getRequest();
        request.setStatus(String.valueOf(EmergencyStatus.PROVIDER_ARRIVED));
        requestRepository.save(request);

        // Update booking status via booking service
        try {
            bookingServiceClient.updateBookingStatus(assignment.getBookingId(), "PROVIDER_ARRIVED");
        } catch (Exception e) {
            log.error("Failed to update booking status: {}", e.getMessage());
        }

        // Publish event
        eventProducer.publishProviderArrived(request, assignment);
    }


    @Transactional
    public void startService(String assignmentId, String providerId) {
        log.info("Provider {} starting service for assignment: {}", providerId, assignmentId);

        EmergencyAssignment assignment = findAssignmentById(assignmentId);

        if (!assignment.getProviderId().equals(providerId)) {
            throw new BusinessException(ErrorCode.UNAUTHORIZED, "Not authorized to update this assignment");
        }

        if (!"ARRIVED".equals(assignment.getStatus())) {
            throw new EmergencyException(assignment.getRequest().getId(), ErrorCode.EMERGENCY_INVALID_STATUS,
                    "Assignment must be in ARRIVED state");
        }

        assignment.setStatus("IN_PROGRESS");
        assignment.setStartedAt(LocalDateTime.now());
        assignmentRepository.save(assignment);

        EmergencyRequest request = assignment.getRequest();
        request.setStatus(String.valueOf(EmergencyStatus.IN_PROGRESS));
        requestRepository.save(request);

        // Update booking status
        try {
            bookingServiceClient.updateBookingStatus(assignment.getBookingId(), "IN_PROGRESS");
        } catch (Exception e) {
            log.error("Failed to update booking status: {}", e.getMessage());
        }

        // Publish event
        eventProducer.publishServiceStarted(request, assignment);
    }

    @Override
    @Transactional
    public void completeEmergency(String assignmentId, String providerId) {
        log.info("Provider {} completing emergency assignment: {}", providerId, assignmentId);

        EmergencyAssignment assignment = findAssignmentById(assignmentId);

        if (!assignment.getProviderId().equals(providerId)) {
            throw new BusinessException(ErrorCode.UNAUTHORIZED, "Not authorized to complete this assignment");
        }

        assignment.setStatus("COMPLETED");
        assignment.setCompletedAt(LocalDateTime.now());
        assignmentRepository.save(assignment);

        EmergencyRequest request = assignment.getRequest();
        request.setStatus(String.valueOf(EmergencyStatus.COMPLETED));
        requestRepository.save(request);

        // Mark provider as available again
        providerMatchingService.markProviderAvailable(providerId);

        // Update booking status
        try {
            bookingServiceClient.updateBookingStatus(assignment.getBookingId(), "COMPLETED");
        } catch (Exception e) {
            log.error("Failed to update booking status: {}", e.getMessage());
        }

        // Initiate payment
        initiatePayment(request, assignment);

        // Publish event
        eventProducer.publishEmergencyCompleted(request, assignment);

        log.info("Emergency completed: {}", assignmentId);
    }

    // ==================== Scheduled Jobs ====================

    @Override
    @Transactional
    public void processExpiredRequests() {
        log.info("Processing expired emergency requests");

        LocalDateTime now = LocalDateTime.now();
        List<EmergencyRequest> expiredRequests = requestRepository.findExpiredRequests(now);

        for (EmergencyRequest request : expiredRequests) {
            try {
                request.setStatus(String.valueOf(EmergencyStatus.EXPIRED));
                requestRepository.save(request);

                // Mark all pending notifications as expired
                markPendingNotificationsAsExpired(request.getId());

                // Remove from cache
                redisTemplate.delete(EmergencyConstants.EMERGENCY_REQUEST_PREFIX + request.getId());

                // Notify customer
                eventProducer.publishEmergencyExpired(request);

                log.info("Emergency request expired: {}", request.getId());
            } catch (Exception e) {
                log.error("Error processing expired request {}: {}", request.getId(), e.getMessage());
            }
        }
    }

    @Override
    @Transactional
    public void expandSearchRadius() {
        log.info("Expanding search radius for active emergency requests");

        List<EmergencyRequest> requests = requestRepository.findRequestsForRadiusExpansion();

        for (EmergencyRequest request : requests) {
            try {
                expandSearchRadiusForRequest(request.getId());
            } catch (Exception e) {
                log.error("Error expanding radius for request {}: {}", request.getId(), e.getMessage());
            }
        }
    }

    @Override
    @Transactional
    public void expandSearchRadiusForRequest(String requestId) {
        EmergencyRequest request = findRequestById(requestId);

        // Convert string to enum for comparison
        String statusStr = request.getStatus();
        EmergencyStatus currentStatus = EmergencyStatus.valueOf(statusStr);

        if (currentStatus != EmergencyStatus.SEARCHING) {
            throw new EmergencyException(requestId, ErrorCode.EMERGENCY_INVALID_STATUS,
                    "Cannot cancel request in " + statusStr + " state");
        }

        int newRadius = request.getCurrentRadiusKm() + radiusIncrementKm;

        if (newRadius > request.getMaxRadiusKm()) {
            request.setStatus(String.valueOf(EmergencyStatus.EXPIRED));
            requestRepository.save(request);
            log.info("Request {} expired after reaching max radius", requestId);

            // Notify customer that no providers were found
            eventProducer.publishEmergencyExpired(request);
            return;
        }

        request.setCurrentRadiusKm(newRadius);
        request.setExpiryTime(LocalDateTime.now().plusMinutes(requestExpiryMinutes));
        requestRepository.save(request);

        // Search for providers with new radius
        List<ProviderMatchResponse> providers = findNearbyProviders(
                request.getLocationLat(),
                request.getLocationLng(),
                request.getEmergencyType().name(),
                request.getVehicleType().name(),
                newRadius
        );

        // Filter out already notified providers
        List<ProviderMatchResponse> newProviders = filterNewProviders(request, providers);

        // Notify new providers
        if (!newProviders.isEmpty()) {
            notifyProviders(request, newProviders);
        }

        log.info("Expanded search radius for request {} to {} km, found {} new providers",
                requestId, newRadius, newProviders.size());
    }

    // ==================== Provider Status Handlers ====================

    @Override
    public void handleProviderOffline(String providerId) {
        log.info("Handling provider offline: {}", providerId);
        providerMatchingService.markProviderUnavailable(providerId, "Provider went offline");
    }

    @Override
    public void handleProviderOnline(String providerId) {
        log.info("Handling provider online: {}", providerId);
        providerMatchingService.markProviderAvailable(providerId);
    }

    @Override
    public void handleEmergencyCompleted(String emergencyId, String providerId) {
        log.info("Handling emergency completed: {} by provider: {}", emergencyId, providerId);
        // This would be called from Kafka consumer
        providerMatchingService.markProviderAvailable(providerId);
    }

    @Override
    public void handleProviderAccepted(String providerId, String emergencyId) {
        log.info("Handling provider accepted: {} for emergency: {}", providerId, emergencyId);
        // This would be called when provider accepts
        providerMatchingService.markProviderUnavailable(providerId, "On emergency: " + emergencyId);
    }

    // ==================== Private Helper Methods ====================

    private void validateCoordinates(Double latitude, Double longitude) {
        if (latitude == null || longitude == null) {
            throw new BusinessException(ErrorCode.INVALID_LOCATION, "Location coordinates are required");
        }
        if (latitude < -90 || latitude > 90) {
            throw new BusinessException(ErrorCode.INVALID_LOCATION, "Invalid latitude: " + latitude);
        }
        if (longitude < -180 || longitude > 180) {
            throw new BusinessException(ErrorCode.INVALID_LOCATION, "Invalid longitude: " + longitude);
        }
    }

    private double getDefaultFuelPrice(String fuelType) {
        return "PETROL".equalsIgnoreCase(fuelType) ? 102.50 : 94.50;
    }

    private void notifyProviders(EmergencyRequest request, List<ProviderMatchResponse> providers) {
        for (ProviderMatchResponse provider : providers) {
            // Check if already notified
            if (notificationRepository.existsByRequestIdAndProviderId(request.getId(), provider.getProviderId())) {
                continue;
            }

            // Create notification record
            ProviderNotification notification = ProviderNotification.builder()
                    .request(request)
                    .providerId(provider.getProviderId())
                    .notifiedAt(LocalDateTime.now())
                    .responseStatus(ProviderResponseStatus.PENDING)
                    .build();
            notificationRepository.save(notification);

            // Send push notification to provider (implement actual push)
            log.info("Notified provider: {} for request: {}", provider.getProviderId(), request.getId());

            // Set timeout for provider response
            String timeoutKey = EmergencyConstants.PROVIDER_TIMEOUT_PREFIX + request.getId() + ":" + provider.getProviderId();
            redisTemplate.opsForValue().set(timeoutKey, "PENDING", providerTimeoutSeconds, TimeUnit.SECONDS);
        }
    }

    private List<ProviderMatchResponse> filterNewProviders(EmergencyRequest request,
                                                           List<ProviderMatchResponse> providers) {
        return providers.stream()
                .filter(p -> !notificationRepository.existsByRequestIdAndProviderId(
                        request.getId(), p.getProviderId()))
                .collect(Collectors.toList());
    }

    private void markPendingNotificationsAsCancelled(String requestId) {
        List<ProviderNotification> pending = notificationRepository.findPendingByRequestId(requestId);
        for (ProviderNotification notification : pending) {
            notification.setResponseStatus(ProviderResponseStatus.REJECTED);
            notification.setRespondedAt(LocalDateTime.now());
            notificationRepository.save(notification);
        }
    }

    private void markPendingNotificationsAsExpired(String requestId) {
        List<ProviderNotification> pending = notificationRepository.findPendingByRequestId(requestId);
        for (ProviderNotification notification : pending) {
            notification.setResponseStatus(ProviderResponseStatus.TIMEOUT);
            notification.setRespondedAt(LocalDateTime.now());
            notificationRepository.save(notification);
        }
    }

    private void updateProviderNotificationStatus(String requestId, String providerId,
                                                  ProviderResponseStatus status) {
        notificationRepository.findByRequestIdAndProviderId(requestId, providerId)
                .ifPresent(notification -> {
                    notification.setResponseStatus(status);
                    notification.setRespondedAt(LocalDateTime.now());
                    notificationRepository.save(notification);
                });
    }

    private void markOtherNotificationsAsRejected(String requestId, String acceptedProviderId) {
        List<ProviderNotification> pending = notificationRepository.findPendingByRequestId(requestId);
        for (ProviderNotification notification : pending) {
            if (!notification.getProviderId().equals(acceptedProviderId)) {
                notification.setResponseStatus(ProviderResponseStatus.REJECTED);
                notification.setRespondedAt(LocalDateTime.now());
                notificationRepository.save(notification);
            }
        }
    }

    private void validateProviderForEmergency(String providerId, EmergencyRequest request) {
        try {
            var response = providerServiceClient.validateProviderForEmergency(
                    providerId,
                    request.getEmergencyType().name(),
                    request.getVehicleType().name()
            );

            if (!response.isSuccess() || Boolean.FALSE.equals(response.getData())) {
                throw new BusinessException(ErrorCode.PROVIDER_NOT_ELIGIBLE,
                        "Provider not eligible for this emergency");
            }

            // Check if provider is available in matching service
            if (!providerMatchingService.isProviderAvailable(providerId)) {
                throw new BusinessException(ErrorCode.PROVIDER_NOT_AVAILABLE,
                        "Provider is not available");
            }

        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            log.error("Error validating provider for emergency: {}", e.getMessage());
            throw new BusinessException(ErrorCode.PROVIDER_NOT_ELIGIBLE,
                    "Failed to validate provider eligibility");
        }
    }

    private ProviderResponse getProviderDetails(String providerId) {
        try {
            var response = providerServiceClient.getProvider(providerId);
            if (response.isSuccess() && response.getData() != null) {
                return response.getData();
            }
        } catch (Exception e) {
            log.error("Failed to fetch provider details: {}", e.getMessage());
        }
        // Return minimal provider info if service is unavailable
        return ProviderResponse.builder()
                .providerId(providerId)
                .businessName("Provider")
                .phone("N/A")
                .build();
    }

    private void updateETAForActiveAssignment(String providerId, Double latitude, Double longitude) {
        assignmentRepository.findActiveByProviderId(providerId).ifPresent(assignment -> {
            EmergencyRequest request = assignment.getRequest();
            double newDistance = distanceCalculationService.calculateDistance(
                    latitude, longitude,
                    request.getLocationLat(), request.getLocationLng()
            );
            int newEta = distanceCalculationService.calculateETA(newDistance);

            assignment.setDistanceKm(newDistance);
            assignment.setEtaMinutes(newEta);
            assignmentRepository.save(assignment);

            log.debug("Updated ETA for assignment {} to {} minutes", assignment.getId(), newEta);
        });
    }

    private String createBookingFromEmergency(EmergencyRequest request, EmergencyAssignment assignment) {
        try {

            Map<String, Object> bookingData = new HashMap<>();
            bookingData.put("emergencyRequest", request);
            bookingData.put("providerId", assignment.getProviderId());
            bookingData.put("providerName", assignment.getProviderName());

            var response = bookingServiceClient.createEmergencyBooking(bookingData);

            if (response.isSuccess() && response.getData() != null) {
                return response.getData();
            }

        } catch (Exception e) {
            log.error("Failed to create booking from emergency: {}", e.getMessage());
        }

        // fallback booking id
        return "EMG_BOK_" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
    }

    private void initiatePayment(EmergencyRequest request, EmergencyAssignment assignment) {
        try {
            paymentServiceClient.createEmergencyPayment(
                    assignment.getBookingId(),
                    request.getTotalAmount().doubleValue(),
                    request.getCustomerId()
            );

            log.info("Payment initiated for emergency: {}", request.getId());

        } catch (Exception e) {
            log.error("Failed to initiate payment for emergency {}: {}", request.getId(), e.getMessage());
        }
    }

    private String generateRequestNumber() {
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmm"));
        String random = UUID.randomUUID().toString().substring(0, 4).toUpperCase();
        return "EMG" + timestamp + random;
    }

    private String getGeoIndex(String emergencyType, String vehicleType) {
        if ("REPAIR_EMERGENCY".equals(emergencyType)) {
            return "CAR".equals(vehicleType) ?
                    EmergencyConstants.CAR_REPAIR_INDEX : EmergencyConstants.BIKE_REPAIR_INDEX;
        } else if ("PETROL_EMERGENCY".equals(emergencyType)) {
            return "CAR".equals(vehicleType) ?
                    EmergencyConstants.CAR_FUEL_INDEX : EmergencyConstants.BIKE_FUEL_INDEX;
        }
        return EmergencyConstants.PROVIDER_GEO_INDEX;
    }

    private void cacheEmergencyRequest(EmergencyRequest request) {
        String key = EmergencyConstants.EMERGENCY_REQUEST_PREFIX + request.getId();
        redisTemplate.opsForValue().set(key, request.getId(), requestExpiryMinutes, TimeUnit.MINUTES);
    }

    private void cacheEmergencyAssignment(EmergencyAssignment assignment) {
        String key = "emergency:assignment:" + assignment.getProviderId();
        redisTemplate.opsForValue().set(key, assignment.getId(), 1, TimeUnit.HOURS);
    }

    private EmergencyRequest findRequestById(String requestId) {
        return requestRepository.findById(requestId)
                .orElseThrow(() -> new ResourceNotFoundException("EmergencyRequest", requestId));
    }

    private EmergencyAssignment findAssignmentById(String assignmentId) {
        return assignmentRepository.findById(assignmentId)
                .orElseThrow(() -> new ResourceNotFoundException("EmergencyAssignment", assignmentId));
    }

    private void enrichProviderDetails(ProviderMatchResponse provider) {
        // Try to get from cache first, then from service
        String cacheKey = "provider:details:" + provider.getProviderId();
        String cached = redisTemplate.opsForValue().get(cacheKey);

        if (cached != null) {
            String[] parts = cached.split(",");
            if (parts.length > 0) {
                provider.setBusinessName(parts[0]);
                if (parts.length > 1) {
                    provider.setPhone(parts[1]);
                }
                if (parts.length > 2) {
                    try {
                        provider.setRating(Double.parseDouble(parts[2]));
                    } catch (NumberFormatException e) {
                        // Ignore
                    }
                }
            }
        } else {
            // Fetch from provider service asynchronously or on-demand
            try {
                var response = providerServiceClient.getProvider(provider.getProviderId());
                if (response.isSuccess() && response.getData() != null) {
                    ProviderResponse pr = response.getData();
                    provider.setBusinessName(pr.getBusinessName());
                    provider.setPhone(pr.getPhone());
                    provider.setRating(pr.getAverageRating());

                    // Cache for future
                    redisTemplate.opsForValue().set(cacheKey,
                            pr.getBusinessName() + "," + pr.getPhone() + "," +
                                    (pr.getAverageRating() != null ? pr.getAverageRating() : "0"),
                            30, TimeUnit.MINUTES);
                }
            } catch (Exception e) {
                log.debug("Could not fetch provider details for {}", provider.getProviderId());
            }
        }
    }

    private EmergencyBookingResponse mapToResponse(EmergencyRequest request, EmergencyAssignment assignment) {

        EmergencyBookingResponse response = EmergencyBookingResponse.builder()
                .requestId(request.getId())
                .requestNumber(request.getRequestNumber())
                .customerId(request.getCustomerId())
                .emergencyType(request.getEmergencyType())
                .vehicleType(request.getVehicleType())
                .vehicleDetails(request.getVehicleDetails())
                .location(Location.builder()
                        .latitude(request.getLocationLat())
                        .longitude(request.getLocationLng())
                        .address(request.getLocationAddress())
                        .build())
                .status(request.getStatus())
                .searchRadiusKm(request.getCurrentRadiusKm())
                .expiryTime(request.getExpiryTime())
                .baseAmount(request.getBaseAmount())
                .multiplier(request.getMultiplier())
                .totalAmount(request.getTotalAmount())
                .createdAt(request.getCreatedAt())
                .updatedAt(request.getUpdatedAt())
                .build();

        // Add fuel info if petrol emergency
        if (request.getEmergencyType() == EmergencyType.PETROL_EMERGENCY) {

            response.setFuelInfo(
                    FuelInfo.builder()
                            .fuelType(request.getFuelType())
                            .quantity(request.getQuantityLiters())
                            .costPerLiter(request.getFuelCostPerLiter())
                            .totalFuelCost(request.getTotalFuelCost())
                            .build()
            );
        }

        // Add assignment if present
        if (assignment != null) {

            AssignmentInfo.AssignmentInfoBuilder builder =
                    AssignmentInfo.builder()
                            .assignmentId(assignment.getId())
                            .providerId(assignment.getProviderId())
                            .providerName(assignment.getProviderName())
                            .providerPhone(assignment.getProviderPhone())
                            .distanceKm(assignment.getDistanceKm())
                            .etaMinutes(assignment.getEtaMinutes())
                            .status(assignment.getStatus())
                            .acceptedAt(assignment.getAcceptedAt())
                            .bookingId(assignment.getBookingId());

            if (assignment.getProviderLat() != null && assignment.getProviderLng() != null) {

                Location location = Location.builder()
                        .latitude(assignment.getProviderLat())
                        .longitude(assignment.getProviderLng())
                        .timestamp(assignment.getAcceptedAt())
                        .build();

                builder.currentLocation(location);
            }

            response.setAssignment(builder.build());
        }

        return response;
    }
}