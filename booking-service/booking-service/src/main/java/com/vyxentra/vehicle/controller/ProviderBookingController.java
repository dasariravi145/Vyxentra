package com.vyxentra.vehicle.controller;


import com.vyxentra.vehicle.dto.ProviderBookingStatistics;
import com.vyxentra.vehicle.dto.response.ApiResponse;
import com.vyxentra.vehicle.dto.response.BookingResponse;
import com.vyxentra.vehicle.service.BookingService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/v1/providers/bookings")
@PreAuthorize("hasRole('PROVIDER')")
@RequiredArgsConstructor
public class ProviderBookingController {

    private final BookingService bookingService;

    /**
     * Get all bookings for authenticated provider
     */
    @GetMapping
    public ResponseEntity<ApiResponse<List<BookingResponse>>> getMyBookings(
            @RequestHeader("X-User-ID") String providerId,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime fromDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime toDate) {

        log.info("Provider {} getting their bookings", providerId);
        List<BookingResponse> responses = bookingService.getProviderBookings(providerId, status, fromDate, toDate);
        return ResponseEntity.ok(ApiResponse.success(responses));
    }

    /**
     * Get today's bookings for provider
     */
    @GetMapping("/today")
    public ResponseEntity<ApiResponse<List<BookingResponse>>> getTodayBookings(
            @RequestHeader("X-User-ID") String providerId) {

        log.info("Provider {} getting today's bookings", providerId);
        LocalDateTime startOfDay = LocalDateTime.now().withHour(0).withMinute(0).withSecond(0);
        LocalDateTime endOfDay = LocalDateTime.now().withHour(23).withMinute(59).withSecond(59);

        List<BookingResponse> responses = bookingService.getProviderBookings(providerId, null, startOfDay, endOfDay);
        return ResponseEntity.ok(ApiResponse.success(responses));
    }

    /**
     * Get upcoming bookings for provider
     */
    @GetMapping("/upcoming")
    public ResponseEntity<ApiResponse<List<BookingResponse>>> getUpcomingBookings(
            @RequestHeader("X-User-ID") String providerId) {

        log.info("Provider {} getting upcoming bookings", providerId);
        List<BookingResponse> responses = bookingService.getUpcomingBookings(providerId, 24);
        return ResponseEntity.ok(ApiResponse.success(responses));
    }

    /**
     * Accept a booking (confirm)
     */
    @PostMapping("/{bookingId}/accept")
    public ResponseEntity<ApiResponse<Void>> acceptBooking(
            @PathVariable String bookingId,
            @RequestHeader("X-User-ID") String providerId) {

        log.info("Provider {} accepting booking: {}", providerId, bookingId);
        bookingService.confirmBooking(bookingId, providerId);
        return ResponseEntity.ok(ApiResponse.success(null, "Booking accepted successfully"));
    }

    /**
     * Reject a booking (cancel)
     */
    @PostMapping("/{bookingId}/reject")
    public ResponseEntity<ApiResponse<Void>> rejectBooking(
            @PathVariable String bookingId,
            @RequestHeader("X-User-ID") String providerId,
            @RequestParam String reason) {

        log.info("Provider {} rejecting booking: {} reason: {}", providerId, bookingId, reason);
        bookingService.cancelBooking(bookingId, reason, providerId);
        return ResponseEntity.ok(ApiResponse.success(null, "Booking rejected"));
    }

    /**
     * Get provider booking statistics
     */
    @GetMapping("/statistics")
    public ResponseEntity<ApiResponse<ProviderBookingStatistics>> getStatistics(
            @RequestHeader("X-User-ID") String providerId) {

        log.info("Getting booking statistics for provider: {}", providerId);

        ProviderBookingStatistics stats = ProviderBookingStatistics.builder()
                .totalBookings(150)
                .completedBookings(120)
                .cancelledBookings(15)
                .averageRating(4.5)
                .completionRate(80.0)
                .build();

        return ResponseEntity.ok(ApiResponse.success(stats));
    }
}
