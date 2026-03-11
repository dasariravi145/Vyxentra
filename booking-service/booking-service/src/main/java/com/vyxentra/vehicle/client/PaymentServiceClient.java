package com.vyxentra.vehicle.client;

import com.vyxentra.vehicle.dto.response.ApiResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient(name = "payment-service", fallback = PaymentServiceClientFallback.class)
public interface PaymentServiceClient {

    @PostMapping("/api/v1/payments/create")
    ApiResponse<String> createPayment(@RequestParam("bookingId") String bookingId,
                                      @RequestParam("amount") Double amount);

    @GetMapping("/api/v1/payments/status/{bookingId}")
    ApiResponse<String> getPaymentStatus(@PathVariable("bookingId") String bookingId);

    @PostMapping("/api/v1/payments/refund/{bookingId}")
    ApiResponse<Void> initiateRefund(@PathVariable("bookingId") String bookingId);
}
