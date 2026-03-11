package com.vyxentra.vehicle.service;


import com.vyxentra.vehicle.client.BookingServiceClient;
import com.vyxentra.vehicle.client.PaymentServiceClient;
import com.vyxentra.vehicle.dto.TimeSeriesData;
import com.vyxentra.vehicle.dto.response.RevenueReportResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class AnalyticsServiceImpl implements AnalyticsService {

    private final BookingServiceClient bookingServiceClient;
    private final PaymentServiceClient paymentServiceClient;

    @Override
    public RevenueReportResponse getDailyRevenue(LocalDate date) {
        log.debug("Getting daily revenue for: {}", date);

        // This would aggregate from payment-service and booking-service

        return RevenueReportResponse.builder()
                .fromDate(date)
                .toDate(date)
                .interval("daily")
                .totalRevenue(125000.0)
                .totalCommission(18750.0)
                .totalProviderPayout(106250.0)
                .totalBookings(25L)
                .averageOrderValue(5000.0)
                .revenueByProviderType(Map.of(
                        "SERVICE_CENTER", 100000.0,
                        "WASHING_CENTER", 25000.0
                ))
                .revenueByPaymentMethod(Map.of(
                        "CARD", 75000.0,
                        "UPI", 40000.0,
                        "WALLET", 10000.0
                ))
                .timeSeries(List.of(
                        TimeSeriesData.builder()
                                .date(date)
                                .revenue(125000.0)
                                .commission(18750.0)
                                .bookings(25L)
                                .build()
                ))
                .build();
    }

    @Override
    public RevenueReportResponse getRevenueRange(LocalDate fromDate, LocalDate toDate, String interval) {
        log.debug("Getting revenue from {} to {} with interval: {}", fromDate, toDate, interval);

        // This would aggregate from payment-service

        return RevenueReportResponse.builder()
                .fromDate(fromDate)
                .toDate(toDate)
                .interval(interval)
                .totalRevenue(1250000.0)
                .totalCommission(187500.0)
                .totalProviderPayout(1062500.0)
                .totalBookings(250L)
                .averageOrderValue(5000.0)
                .build();
    }

    @Override
    public Map<String, Long> getBookingStatusDistribution() {
        // This would call booking-service
        return Map.of(
                "PENDING", 150L,
                "CONFIRMED", 300L,
                "IN_PROGRESS", 200L,
                "COMPLETED", 450L,
                "CANCELLED", 50L
        );
    }

    @Override
    public Object getTopProviders(int limit) {
        // This would call provider-service
        return List.of();
    }

    @Override
    public Long getActiveCustomers(int days) {
        // This would call user-service
        return 2500L;
    }

    @Override
    public Map<String, Object> getGrowthMetrics() {
        return Map.of(
                "userGrowth", 15.5,
                "bookingGrowth", 22.3,
                "revenueGrowth", 28.7,
                "providerGrowth", 12.1
        );
    }
}
