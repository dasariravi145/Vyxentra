package com.vyxentra.vehicle.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "booking-service", path = "/api/v1/bookings", fallback = BookingServiceClientFallback.class)
public interface BookingServiceClient {

    @GetMapping("/user/{userId}/exists")
    Boolean hasActiveBookings(@PathVariable String userId);
}
