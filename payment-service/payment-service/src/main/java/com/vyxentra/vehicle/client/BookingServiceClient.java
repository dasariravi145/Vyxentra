package com.vyxentra.vehicle.client;


import com.vyxentra.vehicle.dto.response.ApiResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient(name = "booking-service", fallback = BookingServiceClientFallback.class)
public interface BookingServiceClient {

    @GetMapping("/api/v1/bookings/{bookingId}/amount")
    ApiResponse<Double> getBookingAmount(@PathVariable("bookingId") String bookingId);

    @PostMapping("/api/v1/bookings/{bookingId}/payment-status")
    ApiResponse<Void> updatePaymentStatus(@PathVariable("bookingId") String bookingId,
                                          @RequestParam("status") String status);

    @GetMapping("/api/v1/bookings/{bookingId}/provider-type")
    ApiResponse<String> getProviderType(@PathVariable("bookingId") String bookingId);
}
