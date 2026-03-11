package com.vyxentra.vehicle.client;


import com.vyxentra.vehicle.dto.response.ApiResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class PaymentServiceClientFallback implements PaymentServiceClient {

    @Override
    public ApiResponse<Object> getDailyRevenue(String date) {
        log.error("Fallback: Unable to fetch daily revenue for: {}", date);
        return ApiResponse.error(null, "Payment service is currently unavailable");
    }

    @Override
    public ApiResponse<Object> getRevenueInRange(String fromDate, String toDate, String interval) {
        log.error("Fallback: Unable to fetch revenue in range: {} - {}", fromDate, toDate);
        return ApiResponse.error(null, "Payment service is currently unavailable");
    }
}