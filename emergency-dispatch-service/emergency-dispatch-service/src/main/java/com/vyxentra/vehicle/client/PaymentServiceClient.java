package com.vyxentra.vehicle.client;


import com.vyxentra.vehicle.dto.response.ApiResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient(name = "payment-service", fallback = PaymentServiceClientFallback.class)
public interface PaymentServiceClient {

    @PostMapping("/api/v1/payments/emergency")
    ApiResponse<String> createEmergencyPayment(@RequestParam("bookingId") String bookingId,
                                               @RequestParam("amount") Double amount,
                                               @RequestParam("customerId") String customerId);

    @PostMapping("/api/v1/payments/verify/{paymentId}")
    ApiResponse<Boolean> verifyPayment(@RequestParam("paymentId") String paymentId);
}
