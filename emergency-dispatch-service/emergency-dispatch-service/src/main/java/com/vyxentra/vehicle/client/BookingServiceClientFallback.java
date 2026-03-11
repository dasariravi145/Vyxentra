package com.vyxentra.vehicle.client;


import com.vyxentra.vehicle.dto.response.ApiResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Map;

@Slf4j
@Component
public class BookingServiceClientFallback implements BookingServiceClient {

    @Override
    public ApiResponse<String> createEmergencyBooking(Map<String, Object> bookingData) {
        log.error("Fallback: Unable to create emergency booking");
        return ApiResponse.error(null, "Booking service is currently unavailable");
    }

    @Override
    public ApiResponse<Void> updateBookingStatus(String bookingId, String status) {
        log.error("Fallback: Unable to update booking status: {}", bookingId);
        return ApiResponse.error(null, "Booking service is currently unavailable");
    }
}
