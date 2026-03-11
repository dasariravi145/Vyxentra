package com.vyxentra.vehicle.client;

import com.vyxentra.vehicle.dto.response.ApiResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class BookingServiceClientFallback implements BookingServiceClient {

    @Override
    public ApiResponse<Double> getBookingAmount(String bookingId) {
        log.error("Fallback: Unable to fetch booking amount: {}", bookingId);
        return ApiResponse.error(null, "Booking service is currently unavailable");
    }

    @Override
    public ApiResponse<Void> updatePaymentStatus(String bookingId, String status) {
        log.error("Fallback: Unable to update payment status for booking: {}", bookingId);
        return ApiResponse.error(null, "Booking service is currently unavailable");
    }

    @Override
    public ApiResponse<String> getProviderType(String bookingId) {
        log.error("Fallback: Unable to fetch provider type for booking: {}", bookingId);
        return ApiResponse.success("SERVICE_CENTER");
    }
}
