package com.vyxentra.vehicle.client;


import com.vyxentra.vehicle.dto.response.ApiResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class PaymentServiceClientFallback implements PaymentServiceClient {

    @Override
    public ApiResponse<String> createPayment(String bookingId, Double amount) {
        log.error("Fallback: Unable to create payment for booking: {}, amount: {}", bookingId, amount);
        return ApiResponse.error(null, "Payment service is currently unavailable");
    }

    @Override
    public ApiResponse<String> getPaymentStatus(String bookingId) {
        log.error("Fallback: Unable to get payment status for booking: {}", bookingId);
        return ApiResponse.error(null, "Payment service is currently unavailable");
    }

    @Override
    public ApiResponse<Void> initiateRefund(String bookingId) {
        log.error("Fallback: Unable to initiate refund for booking: {}", bookingId);
        return ApiResponse.error(null, "Payment service is currently unavailable");
    }
}
