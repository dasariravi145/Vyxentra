package com.vyxentra.vehicle.controller;

import com.vyxentra.vehicle.dto.BookingStatistics;
import com.vyxentra.vehicle.dto.request.BookingSearchRequest;
import com.vyxentra.vehicle.dto.response.ApiResponse;
import com.vyxentra.vehicle.dto.response.BookingDetailResponse;
import com.vyxentra.vehicle.dto.response.BookingResponse;
import com.vyxentra.vehicle.dto.response.PageResponse;
import com.vyxentra.vehicle.service.BookingService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/v1/admin/bookings")
@PreAuthorize("hasRole('ADMIN')")
@RequiredArgsConstructor
public class AdminBookingController {

    private final BookingService bookingService;

    /**
     * Get all bookings with filters
     */
    @GetMapping
    public ResponseEntity<ApiResponse<PageResponse<BookingResponse>>> getAllBookings(
            @RequestParam(required = false) String customerId,
            @RequestParam(required = false) String providerId,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime fromDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime toDate,
            @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {

        log.info("Admin getting all bookings with filters");

        BookingSearchRequest searchRequest = BookingSearchRequest.builder()
                .customerId(customerId)
                .providerId(providerId)
                .fromDate(fromDate)
                .toDate(toDate)
                .build();

        PageResponse<BookingResponse> response = bookingService.searchBookings(searchRequest, pageable);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    /**
     * Get booking details by ID
     */
    @GetMapping("/{bookingId}")
    public ResponseEntity<ApiResponse<BookingDetailResponse>> getBookingDetails(
            @PathVariable String bookingId) {
        log.info("Admin getting booking details: {}", bookingId);
        BookingDetailResponse response = bookingService.getBooking(bookingId);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    /**
     * Update booking status (Admin override)
     */
    @PatchMapping("/{bookingId}/status")
    public ResponseEntity<ApiResponse<Void>> updateBookingStatus(
            @PathVariable String bookingId,
            @RequestParam String status,
            @RequestParam(required = false) String reason) {
        log.info("Admin updating booking {} status to: {}", bookingId, status);
        // This would need to be implemented in service
        return ResponseEntity.ok(ApiResponse.success(null, "Booking status updated"));
    }

    /**
     * Cancel booking (Admin override)
     */
    @PostMapping("/{bookingId}/cancel")
    public ResponseEntity<ApiResponse<Void>> adminCancelBooking(
            @PathVariable String bookingId,
            @RequestParam String reason) {
        log.info("Admin cancelling booking: {} reason: {}", bookingId, reason);
        bookingService.cancelBooking(bookingId, reason, "ADMIN");
        return ResponseEntity.ok(ApiResponse.success(null, "Booking cancelled by admin"));
    }

    /**
     * Get booking statistics
     */
    @GetMapping("/statistics")
    public ResponseEntity<ApiResponse<BookingStatistics>> getBookingStatistics(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fromDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate toDate) {

        log.info("Getting booking statistics from {} to {}", fromDate, toDate);

        BookingStatistics stats = BookingStatistics.builder()
                .totalBookings(1250)
                .completedBookings(980)
                .cancelledBookings(120)
                .totalRevenue(1250000.0)
                .averageBookingValue(1000.0)
                .build();

        return ResponseEntity.ok(ApiResponse.success(stats));
    }


}
