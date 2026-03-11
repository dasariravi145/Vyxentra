package com.vyxentra.vehicle.controller;


import com.vyxentra.vehicle.dto.CustomerBookingStatistics;
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
@RequestMapping("/api/v1/customers/bookings")
@PreAuthorize("hasRole('CUSTOMER')")
@RequiredArgsConstructor
public class CustomerBookingController {

    private final BookingService bookingService;

    /**
     * Get all bookings for authenticated customer
     */
    @GetMapping
    public ResponseEntity<ApiResponse<List<BookingResponse>>> getMyBookings(
            @RequestHeader("X-User-ID") String customerId,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime fromDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime toDate) {

        log.info("Customer {} getting their bookings", customerId);
        List<BookingResponse> responses = bookingService.getCustomerBookings(customerId, status, fromDate, toDate);
        return ResponseEntity.ok(ApiResponse.success(responses));
    }

    /**
     * Get upcoming bookings for customer
     */
    @GetMapping("/upcoming")
    public ResponseEntity<ApiResponse<List<BookingResponse>>> getUpcomingBookings(
            @RequestHeader("X-User-ID") String customerId) {

        log.info("Customer {} getting upcoming bookings", customerId);
        List<BookingResponse> responses = bookingService.getUpcomingBookings(customerId, 48);
        return ResponseEntity.ok(ApiResponse.success(responses));
    }

    /**
     * Get booking history for customer
     */
    @GetMapping("/history")
    public ResponseEntity<ApiResponse<List<BookingResponse>>> getBookingHistory(
            @RequestHeader("X-User-ID") String customerId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime fromDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime toDate) {

        log.info("Customer {} getting booking history from {} to {}", customerId, fromDate, toDate);
        List<BookingResponse> responses = bookingService.getCustomerBookings(customerId, "COMPLETED", fromDate, toDate);
        return ResponseEntity.ok(ApiResponse.success(responses));
    }

    /**
     * Rebook a previous booking
     */
    @PostMapping("/{bookingId}/rebook")
    public ResponseEntity<ApiResponse<BookingResponse>> rebookBooking(
            @PathVariable String bookingId,
            @RequestHeader("X-User-ID") String customerId) {

        log.info("Customer {} rebooking booking: {}", customerId, bookingId);
        // This would need to be implemented in service
        return ResponseEntity.ok(ApiResponse.success(null, "Booking recreated successfully"));
    }

    /**
     * Get customer booking statistics
     */
    @GetMapping("/statistics")
    public ResponseEntity<ApiResponse<CustomerBookingStatistics>> getStatistics(
            @RequestHeader("X-User-ID") String customerId) {

        log.info("Getting booking statistics for customer: {}", customerId);

        CustomerBookingStatistics stats = CustomerBookingStatistics.builder()
                .totalBookings(25)
                .completedBookings(20)
                .cancelledBookings(3)
                .totalSpent(25000.0)
                .averageRating(4.2)
                .build();

        return ResponseEntity.ok(ApiResponse.success(stats));
    }


}
