package com.vyxentra.vehicle.client;


import com.vyxentra.vehicle.dto.response.ApiResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class BookingServiceClientFallback implements BookingServiceClient {

    @Override
    public ApiResponse<Object> getBooking(String bookingId) {
        log.error("Fallback: Unable to fetch booking: {}", bookingId);
        return ApiResponse.error(null, "Booking service is currently unavailable");
    }

    @Override
    public ApiResponse<Void> assignEmployee(String bookingId, String employeeId) {
        log.error("Fallback: Unable to assign employee to booking: {}", bookingId);
        return ApiResponse.error(null, "Booking service is currently unavailable");
    }

    @Override
    public ApiResponse<Void> updateBookingStatus(String bookingId, String status) {
        log.error("Fallback: Unable to update booking status: {}", bookingId);
        return ApiResponse.error(null, "Booking service is currently unavailable");
    }
}
