package com.vyxentra.vehicle.client;


import com.vyxentra.vehicle.dto.response.ApiResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient(name = "booking-service", fallback = BookingServiceClientFallback.class)
public interface BookingServiceClient {

    @GetMapping("/api/v1/bookings/{bookingId}")
    ApiResponse<Object> getBooking(@PathVariable("bookingId") String bookingId);

    @PostMapping("/api/v1/bookings/{bookingId}/assign-employee")
    ApiResponse<Void> assignEmployee(@PathVariable("bookingId") String bookingId,
                                     @RequestParam("employeeId") String employeeId);

    @PostMapping("/api/v1/bookings/{bookingId}/status")
    ApiResponse<Void> updateBookingStatus(@PathVariable("bookingId") String bookingId,
                                          @RequestParam("status") String status);
}