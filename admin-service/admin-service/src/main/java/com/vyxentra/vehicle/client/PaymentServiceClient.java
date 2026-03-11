package com.vyxentra.vehicle.client;

import com.vyxentra.vehicle.dto.response.ApiResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient(name = "payment-service", fallback = PaymentServiceClientFallback.class)
public interface PaymentServiceClient {

    @GetMapping("/api/v1/payments/analytics/revenue/daily")
    ApiResponse<Object> getDailyRevenue(@RequestParam("date") String date);

    @GetMapping("/api/v1/payments/analytics/revenue/range")
    ApiResponse<Object> getRevenueInRange(@RequestParam("fromDate") String fromDate,
                                          @RequestParam("toDate") String toDate,
                                          @RequestParam("interval") String interval);
}
