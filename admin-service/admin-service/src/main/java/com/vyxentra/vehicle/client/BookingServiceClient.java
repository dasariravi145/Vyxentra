package com.vyxentra.vehicle.client;


import com.vyxentra.vehicle.dto.response.ApiResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient(name = "booking-service", fallback = BookingServiceClientFallback.class)
public interface BookingServiceClient {

    @GetMapping("/api/v1/bookings/analytics/status-distribution")
    ApiResponse<Object> getBookingStatusDistribution();

    @GetMapping("/api/v1/bookings/analytics/daily")
    ApiResponse<Object> getDailyBookings(@RequestParam("date") String date);

    @GetMapping("/api/v1/bookings/analytics/range")
    ApiResponse<Object> getBookingsInRange(@RequestParam("fromDate") String fromDate,
                                           @RequestParam("toDate") String toDate);
}