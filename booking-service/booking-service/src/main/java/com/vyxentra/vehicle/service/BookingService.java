package com.vyxentra.vehicle.service;

import com.vyxentra.vehicle.dto.request.BookingSearchRequest;
import com.vyxentra.vehicle.dto.request.CreateBookingRequest;
import com.vyxentra.vehicle.dto.response.*;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.List;

public interface BookingService {

    BookingResponse createBooking(String customerId, CreateBookingRequest request);

    BookingDetailResponse getBooking(String bookingId);

    BookingDetailResponse getBookingByNumber(String bookingNumber);

    List<BookingResponse> getCustomerBookings(String customerId, String status,
                                              LocalDateTime fromDate, LocalDateTime toDate);

    List<BookingResponse> getProviderBookings(String providerId, String status,
                                              LocalDateTime fromDate, LocalDateTime toDate);

    PageResponse<BookingResponse> searchBookings(BookingSearchRequest request, Pageable pageable);

    void cancelBooking(String bookingId, String reason, String userId);

    void confirmBooking(String bookingId, String userId);

    void assignEmployee(String bookingId, String employeeId, String providerId);

    void startService(String bookingId, String employeeId);

    void completeService(String bookingId, String employeeId);

    void rateBooking(String bookingId, Integer rating, String review, String customerId);

    List<BookingResponse> getUpcomingBookings(String userId, Integer hours);

    List<BookingTimelineResponse> getBookingTimeline(String bookingId);

    BookingSnapshotResponse getBookingSnapshot(String bookingId, String snapshotId);

    void processExpiredBookings();
}
