package com.vyxentra.vehicle.client;

import com.vyxentra.vehicle.dto.response.ApiResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class BookingServiceClientFallback implements BookingServiceClient {

    @Override
    public ApiResponse<Object> getBookingDetails(String bookingId) {
        log.error("Fallback: Unable to fetch booking details: {}", bookingId);
        return ApiResponse.error(null, "Booking service is currently unavailable");
    }
}
