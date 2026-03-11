package com.vyxentra.vehicle.client;


import com.vyxentra.vehicle.dto.response.ApiResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class BookingServiceClientFallback implements BookingServiceClient {

    @Override
    public ApiResponse<Object> getBookingStatusDistribution() {
        log.error("Fallback: Unable to fetch booking status distribution");
        return ApiResponse.error(null, "Booking service is currently unavailable");
    }

    @Override
    public ApiResponse<Object> getDailyBookings(String date) {
        log.error("Fallback: Unable to fetch daily bookings for: {}", date);
        return ApiResponse.error(null, "Booking service is currently unavailable");
    }

    @Override
    public ApiResponse<Object> getBookingsInRange(String fromDate, String toDate) {
        log.error("Fallback: Unable to fetch bookings in range: {} - {}", fromDate, toDate);
        return ApiResponse.error(null, "Booking service is currently unavailable");
    }
}