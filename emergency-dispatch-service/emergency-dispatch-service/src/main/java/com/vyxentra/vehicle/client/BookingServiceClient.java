package com.vyxentra.vehicle.client;


import com.vyxentra.vehicle.dto.response.ApiResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.Map;

@FeignClient(name = "booking-service", fallback = BookingServiceClientFallback.class)
public interface BookingServiceClient {

    @PostMapping("/api/v1/bookings/emergency")
    ApiResponse<String> createEmergencyBooking(@RequestBody Map<String, Object> bookingData);

    @PostMapping("/api/v1/bookings/{bookingId}/status")
    ApiResponse<Void> updateBookingStatus(@RequestParam("bookingId") String bookingId,
                                          @RequestParam("status") String status);
}