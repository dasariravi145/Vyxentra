package com.vyxentra.vehicle.client;


import com.vyxentra.vehicle.dto.response.ApiResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class PaymentServiceClientFallback implements PaymentServiceClient {

    @Override
    public ApiResponse<String> createEmergencyPayment(String bookingId, Double amount, String customerId) {
        log.error("Fallback: Unable to create emergency payment for booking: {}", bookingId);
        return ApiResponse.error(null, "Payment service is currently unavailable");
    }

    @Override
    public ApiResponse<Boolean> verifyPayment(String paymentId) {
        log.error("Fallback: Unable to verify payment: {}", paymentId);
        return ApiResponse.success(false);
    }
}