package com.vyxentra.vehicle.controller;

import com.vyxentra.vehicle.dto.request.BookingSearchRequest;
import com.vyxentra.vehicle.dto.request.CreateBookingRequest;
import com.vyxentra.vehicle.dto.request.UpdateBookingRequest;
import com.vyxentra.vehicle.dto.response.*;
import com.vyxentra.vehicle.service.BookingService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/v1/bookings")
@RequiredArgsConstructor
public class BookingController {

    private final BookingService bookingService;

    /**
     * Create a new booking
     */
    @PostMapping
    @PreAuthorize("hasRole('CUSTOMER')")
    public ResponseEntity<ApiResponse<BookingResponse>> createBooking(
            @RequestHeader("X-User-ID") String customerId,
            @Valid @RequestBody CreateBookingRequest request) {
        log.info("Creating booking for customer: {}", customerId);
        BookingResponse response = bookingService.createBooking(customerId, request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(response, "Booking created successfully"));
    }

    /**
     * Get booking by ID
     */
    @GetMapping("/{bookingId}")
    @PreAuthorize("hasAnyRole('CUSTOMER', 'PROVIDER', 'EMPLOYEE', 'ADMIN')")
    public ResponseEntity<ApiResponse<BookingDetailResponse>> getBooking(
            @PathVariable String bookingId) {
        log.info("Getting booking: {}", bookingId);
        BookingDetailResponse response = bookingService.getBooking(bookingId);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    /**
     * Get booking by booking number
     */
    @GetMapping("/number/{bookingNumber}")
    @PreAuthorize("hasAnyRole('CUSTOMER', 'PROVIDER', 'EMPLOYEE', 'ADMIN')")
    public ResponseEntity<ApiResponse<BookingDetailResponse>> getBookingByNumber(
            @PathVariable String bookingNumber) {
        log.info("Getting booking by number: {}", bookingNumber);
        BookingDetailResponse response = bookingService.getBookingByNumber(bookingNumber);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    /**
     * Get all bookings for authenticated customer
     */
    @GetMapping("/customer")
    @PreAuthorize("hasRole('CUSTOMER')")
    public ResponseEntity<ApiResponse<List<BookingResponse>>> getCustomerBookings(
            @RequestHeader("X-User-ID") String customerId,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime fromDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime toDate) {
        log.info("Getting bookings for customer: {}", customerId);
        List<BookingResponse> responses = bookingService.getCustomerBookings(customerId, status, fromDate, toDate);
        return ResponseEntity.ok(ApiResponse.success(responses));
    }

    /**
     * Get all bookings for authenticated provider
     */
    @GetMapping("/provider")
    @PreAuthorize("hasRole('PROVIDER')")
    public ResponseEntity<ApiResponse<List<BookingResponse>>> getProviderBookings(
            @RequestHeader("X-User-ID") String providerId,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime fromDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime toDate) {
        log.info("Getting bookings for provider: {}", providerId);
        List<BookingResponse> responses = bookingService.getProviderBookings(providerId, status, fromDate, toDate);
        return ResponseEntity.ok(ApiResponse.success(responses));
    }

    /**
     * Search bookings with filters (Admin only)
     */
    @PostMapping("/search")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<PageResponse<BookingResponse>>> searchBookings(
            @Valid @RequestBody BookingSearchRequest request,
            @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
        log.info("Searching bookings with filters");
        PageResponse<BookingResponse> response = bookingService.searchBookings(request, pageable);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    /**
     * Cancel a booking
     */
    @PatchMapping("/{bookingId}/cancel")
    @PreAuthorize("hasAnyRole('CUSTOMER', 'PROVIDER', 'ADMIN')")
    public ResponseEntity<ApiResponse<Void>> cancelBooking(
            @PathVariable String bookingId,
            @RequestParam String reason,
            @RequestHeader("X-User-ID") String userId) {
        log.info("Cancelling booking: {} by user: {}", bookingId, userId);
        bookingService.cancelBooking(bookingId, reason, userId);
        return ResponseEntity.ok(ApiResponse.success(null, "Booking cancelled successfully"));
    }

    /**
     * Confirm a booking (after payment if required)
     */
    @PatchMapping("/{bookingId}/confirm")
    @PreAuthorize("hasAnyRole('CUSTOMER', 'ADMIN')")
    public ResponseEntity<ApiResponse<Void>> confirmBooking(
            @PathVariable String bookingId,
            @RequestHeader("X-User-ID") String userId) {
        log.info("Confirming booking: {} by user: {}", bookingId, userId);
        bookingService.confirmBooking(bookingId, userId);
        return ResponseEntity.ok(ApiResponse.success(null, "Booking confirmed successfully"));
    }

    /**
     * Assign employee to booking (Provider only)
     */
    @PatchMapping("/{bookingId}/assign")
    @PreAuthorize("hasRole('PROVIDER')")
    public ResponseEntity<ApiResponse<Void>> assignEmployee(
            @PathVariable String bookingId,
            @RequestParam String employeeId,
            @RequestHeader("X-User-ID") String providerId) {
        log.info("Assigning employee {} to booking: {} by provider: {}", employeeId, bookingId, providerId);
        bookingService.assignEmployee(bookingId, employeeId, providerId);
        return ResponseEntity.ok(ApiResponse.success(null, "Employee assigned successfully"));
    }

    /**
     * Start service (Employee only)
     */
    @PatchMapping("/{bookingId}/start")
    @PreAuthorize("hasRole('EMPLOYEE')")
    public ResponseEntity<ApiResponse<Void>> startService(
            @PathVariable String bookingId,
            @RequestHeader("X-User-ID") String employeeId) {
        log.info("Starting service for booking: {} by employee: {}", bookingId, employeeId);
        bookingService.startService(bookingId, employeeId);
        return ResponseEntity.ok(ApiResponse.success(null, "Service started"));
    }

    /**
     * Complete service (Employee only)
     */
    @PatchMapping("/{bookingId}/complete")
    @PreAuthorize("hasRole('EMPLOYEE')")
    public ResponseEntity<ApiResponse<Void>> completeService(
            @PathVariable String bookingId,
            @RequestHeader("X-User-ID") String employeeId) {
        log.info("Completing service for booking: {} by employee: {}", bookingId, employeeId);
        bookingService.completeService(bookingId, employeeId);
        return ResponseEntity.ok(ApiResponse.success(null, "Service completed"));
    }

    /**
     * Rate a completed booking
     */
    @PostMapping("/{bookingId}/rate")
    @PreAuthorize("hasRole('CUSTOMER')")
    public ResponseEntity<ApiResponse<Void>> rateBooking(
            @PathVariable String bookingId,
            @RequestParam Integer rating,
            @RequestParam(required = false) String review,
            @RequestHeader("X-User-ID") String customerId) {
        log.info("Rating booking: {} with rating: {} by customer: {}", bookingId, rating, customerId);
        bookingService.rateBooking(bookingId, rating, review, customerId);
        return ResponseEntity.ok(ApiResponse.success(null, "Rating submitted successfully"));
    }

    /**
     * Get upcoming bookings for user
     */
    @GetMapping("/upcoming")
    @PreAuthorize("hasAnyRole('CUSTOMER', 'PROVIDER')")
    public ResponseEntity<ApiResponse<List<BookingResponse>>> getUpcomingBookings(
            @RequestHeader("X-User-ID") String userId,
            @RequestParam(required = false) Integer hours) {
        log.info("Getting upcoming bookings for user: {}", userId);
        List<BookingResponse> responses = bookingService.getUpcomingBookings(userId, hours);
        return ResponseEntity.ok(ApiResponse.success(responses));
    }

    /**
     * Get booking timeline
     */
    @GetMapping("/{bookingId}/timeline")
    @PreAuthorize("hasAnyRole('CUSTOMER', 'PROVIDER', 'EMPLOYEE', 'ADMIN')")
    public ResponseEntity<ApiResponse<List<BookingTimelineResponse>>> getBookingTimeline(
            @PathVariable String bookingId) {
        log.info("Getting timeline for booking: {}", bookingId);
        List<BookingTimelineResponse> responses = bookingService.getBookingTimeline(bookingId);
        return ResponseEntity.ok(ApiResponse.success(responses));
    }

    /**
     * Get booking snapshot
     */
    @GetMapping("/{bookingId}/snapshot")
    @PreAuthorize("hasAnyRole('CUSTOMER', 'PROVIDER', 'ADMIN')")
    public ResponseEntity<ApiResponse<BookingSnapshotResponse>> getBookingSnapshot(
            @PathVariable String bookingId,
            @RequestParam(required = false) String snapshotId) {
        log.info("Getting snapshot for booking: {}", bookingId);
        BookingSnapshotResponse response = bookingService.getBookingSnapshot(bookingId, snapshotId);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    /**
     * Update booking (Provider only - limited fields)
     */
    @PutMapping("/{bookingId}")
    @PreAuthorize("hasRole('PROVIDER')")
    public ResponseEntity<ApiResponse<BookingResponse>> updateBooking(
            @PathVariable String bookingId,
            @RequestHeader("X-User-ID") String providerId,
            @Valid @RequestBody UpdateBookingRequest request) {
        log.info("Updating booking: {} by provider: {}", bookingId, providerId);
        // This method would need to be implemented in service
        return ResponseEntity.ok(ApiResponse.success(null, "Booking updated successfully"));
    }
}
