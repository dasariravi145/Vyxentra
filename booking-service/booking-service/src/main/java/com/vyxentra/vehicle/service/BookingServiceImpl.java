package com.vyxentra.vehicle.service;


import com.vyxentra.vehicle.client.EmployeeServiceClient;
import com.vyxentra.vehicle.client.PaymentServiceClient;
import com.vyxentra.vehicle.client.ProviderServiceClient;
import com.vyxentra.vehicle.client.UserServiceClient;
import com.vyxentra.vehicle.dto.request.BookingSearchRequest;
import com.vyxentra.vehicle.dto.request.CreateBookingRequest;
import com.vyxentra.vehicle.dto.response.*;
import com.vyxentra.vehicle.entity.Booking;
import com.vyxentra.vehicle.entity.BookingSnapshot;
import com.vyxentra.vehicle.entity.BookingTimeline;
import com.vyxentra.vehicle.enums.BookingStatus;
import com.vyxentra.vehicle.exceptions.BusinessException;
import com.vyxentra.vehicle.exceptions.ErrorCode;
import com.vyxentra.vehicle.exceptions.ResourceNotFoundException;
import com.vyxentra.vehicle.kafka.BookingEventProducer;
import com.vyxentra.vehicle.mapper.BookingMapper;
import com.vyxentra.vehicle.mapper.TimelineMapper;
import com.vyxentra.vehicle.repository.*;
import com.vyxentra.vehicle.spec.BookingSpecification;
import com.vyxentra.vehicle.validator.BookingValidator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class BookingServiceImpl implements BookingService {

    private final BookingRepository bookingRepository;
    private final BookingServiceRepository bookingServiceRepository;
    private final BookingAddonRepository addonRepository;
    private final BookingTimelineRepository timelineRepository;
    private final BookingSnapshotRepository snapshotRepository;
    private final BookingMapper bookingMapper;
    private final TimelineMapper timelineMapper;
    private final BookingValidator bookingValidator;
    private final SnapshotService snapshotService;
    private final BookingEventProducer eventProducer;

    private final ProviderServiceClient providerServiceClient;
    private final UserServiceClient userServiceClient;
    private final PaymentServiceClient paymentServiceClient;
    private final EmployeeServiceClient employeeServiceClient;

    @Value("${booking.expiry-minutes:30}")
    private int bookingExpiryMinutes;

    @Value("${booking.multiplier.emergency:1.5}")
    private double emergencyMultiplier;

    @Value("${booking.multiplier.petrol:1.2}")
    private double petrolMultiplier;

    @Value("${booking.commission.percentage:15}")
    private int commissionPercentage;

    @Override
    @Transactional
    public BookingResponse createBooking(String customerId, CreateBookingRequest request) {
        log.info("Creating booking for customer: {}", customerId);

        // Validate provider
        bookingValidator.validateProvider(request.getProviderId(), request.getVehicleType());

        // Check if provider supports the vehicle type and service
        // This would call provider service

        // Generate booking number
        String bookingNumber = generateBookingNumber();

        // Calculate amount based on service type
        double totalAmount = calculateTotalAmount(request);
        double commissionAmount = totalAmount * commissionPercentage / 100;
        double providerAmount = totalAmount - commissionAmount;

        // Determine if upfront payment is required
        boolean upfrontPaymentRequired = isUpfrontPaymentRequired(request);

        // Create booking
        Booking booking = Booking.builder()
                .bookingNumber(bookingNumber)
                .customerId(customerId)
                .providerId(request.getProviderId())
                .vehicleType(request.getVehicleType())
                .vehicleDetails(request.getVehicleDetails())
                .serviceType(request.getServiceType())
                .status(upfrontPaymentRequired ? BookingStatus.PENDING_PAYMENT : BookingStatus.PENDING_CONFIRMATION)
                .scheduledTime(request.getScheduledTime())
                .locationLat(request.getLocation().getLatitude())
                .locationLng(request.getLocation().getLongitude())
                .locationAddress(request.getLocation().getAddress())
                .totalAmount(totalAmount)
                .commissionAmount(commissionAmount)
                .providerAmount(providerAmount)
                .isEmergency(request.getIsEmergency() != null ? request.getIsEmergency() : false)
                .emergencyType(request.getEmergencyType())
                .upfrontPaymentRequired(upfrontPaymentRequired)
                .upfrontPaid(false)
                .paymentStatus("PENDING")
                .customerNotes(request.getCustomerNotes())
                .createdBy(customerId)
                .build();

        booking = bookingRepository.save(booking);

        // Add timeline entry
        addTimelineEntry(booking, booking.getStatus().name(), "Booking created", customerId);

        // Create snapshot
        snapshotService.createSnapshot(booking, "INITIAL", customerId);

        // If emergency, apply multiplier and publish event
        if (Boolean.TRUE.equals(request.getIsEmergency())) {
            eventProducer.publishEmergencyTriggered(booking);
        }

        // Publish booking created event
        eventProducer.publishBookingCreated(booking);

        log.info("Booking created successfully with ID: {}, Number: {}", booking.getId(), bookingNumber);

        return bookingMapper.toResponse(booking);
    }

    @Override
    @Transactional(readOnly = true)
    public BookingDetailResponse getBooking(String bookingId) {
        log.debug("Getting booking: {}", bookingId);

        Booking booking = findBookingById(bookingId);
        BookingDetailResponse response = bookingMapper.toDetailResponse(booking);

        // Enrich with customer details
        // userServiceClient.getUserProfile(booking.getCustomerId());

        // Enrich with provider details
        // providerServiceClient.getProvider(booking.getProviderId());

        return response;
    }

    @Override
    @Transactional(readOnly = true)
    public BookingDetailResponse getBookingByNumber(String bookingNumber) {
        log.debug("Getting booking by number: {}", bookingNumber);

        Booking booking = bookingRepository.findByBookingNumber(bookingNumber)
                .orElseThrow(() -> new ResourceNotFoundException("Booking", "number", bookingNumber));

        return getBooking(booking.getId());
    }

    @Override
    @Transactional(readOnly = true)
    public List<BookingResponse> getCustomerBookings(String customerId, String status,
                                                     LocalDateTime fromDate, LocalDateTime toDate) {
        log.debug("Getting bookings for customer: {}", customerId);

        BookingStatus bookingStatus = status != null ? BookingStatus.valueOf(status) : null;

        List<Booking> bookings = bookingRepository.findCustomerBookings(
                customerId, bookingStatus, fromDate, toDate);

        return bookingMapper.toResponseList(bookings);
    }

    @Override
    @Transactional(readOnly = true)
    public List<BookingResponse> getProviderBookings(String providerId, String status,
                                                     LocalDateTime fromDate, LocalDateTime toDate) {
        log.debug("Getting bookings for provider: {}", providerId);

        BookingStatus bookingStatus = status != null ? BookingStatus.valueOf(status) : null;

        List<Booking> bookings = bookingRepository.findProviderBookings(
                providerId, bookingStatus, fromDate, toDate);

        return bookingMapper.toResponseList(bookings);
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<BookingResponse> searchBookings(BookingSearchRequest request, Pageable pageable) {
        log.debug("Searching bookings with filters");

        Specification<Booking> spec = BookingSpecification.withFilters(request);
        Page<Booking> page = bookingRepository.findAll(spec, pageable);

        return PageResponse.<BookingResponse>builder()
                .content(bookingMapper.toResponseList(page.getContent()))
                .pageNumber(page.getNumber())
                .pageSize(page.getSize())
                .totalElements(page.getTotalElements())
                .totalPages(page.getTotalPages())
                .first(page.isFirst())
                .last(page.isLast())
                .hasNext(page.hasNext())
                .hasPrevious(page.hasPrevious())
                .build();
    }

    @Override
    @Transactional
    public void cancelBooking(String bookingId, String reason, String userId) {
        log.info("Cancelling booking: {} by user: {}", bookingId, userId);

        Booking booking = findBookingById(bookingId);

        // Validate cancellation based on current status
        bookingValidator.validateCancellation(booking, userId);

        // Update booking
        booking.setStatus(BookingStatus.CANCELLED);
        booking.setCancellationReason(reason);
        booking.setUpdatedBy(userId);
        bookingRepository.save(booking);

        // Add timeline entry
        addTimelineEntry(booking, "CANCELLED", "Booking cancelled: " + reason, userId);

        // Create snapshot
        snapshotService.createSnapshot(booking, "CANCELLATION", userId);

        // If payment was made, initiate refund
        if (booking.getUpfrontPaid() || booking.getPaidAmount() > 0) {
            // paymentServiceClient.initiateRefund(bookingId);
        }

        log.info("Booking cancelled successfully: {}", bookingId);
    }

    @Override
    @Transactional
    public void confirmBooking(String bookingId, String userId) {
        log.info("Confirming booking: {} by user: {}", bookingId, userId);

        Booking booking = findBookingById(bookingId);

        // Validate confirmation
        if (booking.getStatus() != BookingStatus.PENDING_CONFIRMATION) {
            throw new BusinessException(ErrorCode.BOOKING_INVALID_STATUS,
                    "Booking cannot be confirmed. Current status: " + booking.getStatus());
        }

        // For washing centers, ensure payment is received
        // This would check provider type via provider service
        boolean isWashingCenter = false; // Placeholder
        if (isWashingCenter && !booking.getUpfrontPaid()) {
            throw new BusinessException(ErrorCode.BOOKING_PAYMENT_REQUIRED,
                    "Upfront payment required for washing center bookings");
        }

        booking.setStatus(BookingStatus.CONFIRMED);
        booking.setUpdatedBy(userId);
        bookingRepository.save(booking);

        // Add timeline entry
        addTimelineEntry(booking, "CONFIRMED", "Booking confirmed", userId);

        log.info("Booking confirmed successfully: {}", bookingId);
    }

    @Override
    @Transactional
    public void assignEmployee(String bookingId, String employeeId, String providerId) {
        log.info("Assigning employee {} to booking: {} by provider: {}", employeeId, bookingId, providerId);

        Booking booking = findBookingById(bookingId);

        // Verify provider owns this booking
        if (!booking.getProviderId().equals(providerId)) {
            throw new BusinessException(ErrorCode.UNAUTHORIZED, "Not authorized to assign to this booking");
        }

        // Validate assignment
        if (booking.getStatus() != BookingStatus.CONFIRMED) {
            throw new BusinessException(ErrorCode.BOOKING_INVALID_STATUS,
                    "Booking must be CONFIRMED to assign employee");
        }

        // Verify employee belongs to provider and is available
        // employeeServiceClient.validateEmployee(employeeId, providerId);

        booking.setEmployeeId(employeeId);
        booking.setStatus(BookingStatus.ASSIGNED);
        booking.setUpdatedBy(providerId);
        bookingRepository.save(booking);

        // Add timeline entry
        addTimelineEntry(booking, "ASSIGNED", "Employee assigned: " + employeeId, providerId);

        log.info("Employee assigned successfully to booking: {}", bookingId);
    }

    @Override
    @Transactional
    public void startService(String bookingId, String employeeId) {
        log.info("Starting service for booking: {} by employee: {}", bookingId, employeeId);

        Booking booking = findBookingById(bookingId);

        // Verify employee is assigned to this booking
        if (!employeeId.equals(booking.getEmployeeId())) {
            throw new BusinessException(ErrorCode.UNAUTHORIZED, "Not authorized to start this service");
        }

        // Validate status
        if (booking.getStatus() != BookingStatus.ASSIGNED) {
            throw new BusinessException(ErrorCode.BOOKING_INVALID_STATUS,
                    "Booking must be ASSIGNED to start service");
        }

        booking.setStatus(BookingStatus.IN_PROGRESS);
        booking.setActualStartTime(LocalDateTime.now());
        booking.setUpdatedBy(employeeId);
        bookingRepository.save(booking);

        // Add timeline entry
        addTimelineEntry(booking, "IN_PROGRESS", "Service started", employeeId);

        // Publish event
        eventProducer.publishServiceStarted(booking);

        log.info("Service started for booking: {}", bookingId);
    }

    @Override
    @Transactional
    public void completeService(String bookingId, String employeeId) {
        log.info("Completing service for booking: {} by employee: {}", bookingId, employeeId);

        Booking booking = findBookingById(bookingId);

        // Verify employee is assigned to this booking
        if (!employeeId.equals(booking.getEmployeeId())) {
            throw new BusinessException(ErrorCode.UNAUTHORIZED, "Not authorized to complete this service");
        }

        // Validate status
        if (booking.getStatus() != BookingStatus.IN_PROGRESS) {
            throw new BusinessException(ErrorCode.BOOKING_INVALID_STATUS,
                    "Booking must be IN_PROGRESS to complete");
        }

        booking.setStatus(BookingStatus.COMPLETED);
        booking.setActualEndTime(LocalDateTime.now());
        booking.setUpdatedBy(employeeId);
        bookingRepository.save(booking);

        // Add timeline entry
        addTimelineEntry(booking, "COMPLETED", "Service completed", employeeId);

        // Create post-service snapshot
        snapshotService.createSnapshot(booking, "POST_SERVICE", employeeId);

        // Publish event
        eventProducer.publishServiceCompleted(booking);

        // Update provider and employee stats
        // providerServiceClient.incrementBookingCount(booking.getProviderId());
        // employeeServiceClient.incrementServiceCount(employeeId);

        log.info("Service completed for booking: {}", bookingId);
    }

    @Override
    @Transactional
    public void rateBooking(String bookingId, Integer rating, String review, String customerId) {
        log.info("Rating booking: {} with rating: {} by customer: {}", bookingId, rating, customerId);

        Booking booking = findBookingById(bookingId);

        // Verify customer owns this booking
        if (!customerId.equals(booking.getCustomerId())) {
            throw new BusinessException(ErrorCode.UNAUTHORIZED, "Not authorized to rate this booking");
        }

        // Validate status
        if (booking.getStatus() != BookingStatus.COMPLETED) {
            throw new BusinessException(ErrorCode.BOOKING_INVALID_STATUS,
                    "Only completed bookings can be rated");
        }

        booking.setRating(rating);
        booking.setReview(review);
        booking.setUpdatedBy(customerId);
        bookingRepository.save(booking);

        // Add timeline entry
        addTimelineEntry(booking, "RATED", "Booking rated: " + rating + "/5", customerId);

        // Update provider rating
        // providerServiceClient.updateProviderRating(booking.getProviderId(), rating);

        log.info("Booking rated successfully: {}", bookingId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<BookingResponse> getUpcomingBookings(String userId, Integer hours) {
        log.debug("Getting upcoming bookings for user: {}", userId);

        LocalDateTime now = LocalDateTime.now();
        LocalDateTime timeLimit = hours != null ? now.plusHours(hours) : now.plusDays(1);

        List<Booking> bookings;
        if (userId.startsWith("cust")) {
            bookings = bookingRepository.findUpcomingCustomerBookings(userId, now);
        } else if (userId.startsWith("prov")) {
            bookings = bookingRepository.findUpcomingProviderBookings(userId, now);
        } else {
            return List.of();
        }

        // Filter by time limit
        bookings = bookings.stream()
                .filter(b -> b.getScheduledTime().isBefore(timeLimit))
                .toList();

        return bookingMapper.toResponseList(bookings);
    }

    @Override
    @Transactional(readOnly = true)
    public List<BookingTimelineResponse> getBookingTimeline(String bookingId) {
        log.debug("Getting timeline for booking: {}", bookingId);

        List<BookingTimeline> timeline = timelineRepository.findByBookingIdOrderByChangedAtDesc(bookingId);
        return timelineMapper.toResponseList(timeline);
    }

    @Override
    @Transactional(readOnly = true)
    public BookingSnapshotResponse getBookingSnapshot(String bookingId, String snapshotId) {
        log.debug("Getting snapshot for booking: {}", bookingId);

        BookingSnapshot snapshot;
        if (snapshotId != null) {
            snapshot = snapshotRepository.findById(snapshotId)
                    .orElseThrow(() -> new ResourceNotFoundException("Snapshot", snapshotId));
        } else {
            snapshot = snapshotRepository.findFirstByBookingIdOrderByCreatedAtDesc(bookingId)
                    .orElseThrow(() -> new ResourceNotFoundException("Snapshot for booking", bookingId));
        }

        return BookingSnapshotResponse.builder()
                .snapshotId(snapshot.getId())
                .bookingId(snapshot.getBooking().getId())
                .snapshotData(snapshot.getSnapshotData())
                .snapshotType(snapshot.getSnapshotType())
                .createdBy(snapshot.getCreatedBy())
                .createdAt(snapshot.getCreatedAt())
                .build();
    }

    @Override
    @Transactional
    public void processExpiredBookings() {
        log.info("Processing expired bookings");

        LocalDateTime expiryTime = LocalDateTime.now().minusMinutes(bookingExpiryMinutes);
        List<Booking> expiredBookings = bookingRepository.findExpiredBookings(expiryTime);

        for (Booking booking : expiredBookings) {
            booking.setStatus(BookingStatus.EXPIRED);
            bookingRepository.save(booking);

            addTimelineEntry(booking, "EXPIRED", "Booking expired due to no action", "SYSTEM");

            log.info("Booking expired: {}", booking.getId());
        }
    }

    private Booking findBookingById(String bookingId) {
        return bookingRepository.findById(bookingId)
                .orElseThrow(() -> new ResourceNotFoundException("Booking", bookingId));
    }

    private void addTimelineEntry(Booking booking, String status, String notes, String changedBy) {
        BookingTimeline timeline = BookingTimeline.builder()
                .booking(booking)
                .status(status)
                .notes(notes)
                .changedBy(changedBy)
                .changedAt(LocalDateTime.now())
                .build();
        timelineRepository.save(timeline);
    }

    private String generateBookingNumber() {
        String timestamp = String.valueOf(System.currentTimeMillis()).substring(7);
        String random = UUID.randomUUID().toString().substring(0, 4).toUpperCase();
        return "BOK" + timestamp + random;
    }

    private double calculateTotalAmount(CreateBookingRequest request) {
        double amount = 0.0;

        // Calculate based on services
        if (request.getServices() != null) {
            for (var service : request.getServices()) {
                amount += service.getPrice() * (service.getQuantity() != null ? service.getQuantity() : 1);
            }
        }

        // Apply emergency multiplier
        if (Boolean.TRUE.equals(request.getIsEmergency())) {
            amount *= emergencyMultiplier;
        }

        // Apply petrol emergency multiplier
        if (request.getEmergencyType() != null &&
                request.getEmergencyType().name().equals("PETROL_EMERGENCY")) {
            amount *= petrolMultiplier;
        }

        return amount;
    }

    private boolean isUpfrontPaymentRequired(CreateBookingRequest request) {
        // Washing centers require upfront payment
        if (request.getProviderId().startsWith("wash")) {
            return true;
        }

        // Emergency bookings require upfront payment
        if (Boolean.TRUE.equals(request.getIsEmergency())) {
            return true;
        }

        return false;
    }
}
