package com.vyxentra.vehicle.service;



import com.vyxentra.vehicle.dto.response.RevenueReportResponse;

import java.time.LocalDate;
import java.util.Map;

public interface AnalyticsService {

    RevenueReportResponse getDailyRevenue(LocalDate date);

    RevenueReportResponse getRevenueRange(LocalDate fromDate, LocalDate toDate, String interval);

    Map<String, Long> getBookingStatusDistribution();

    Object getTopProviders(int limit);

    Long getActiveCustomers(int days);

    Map<String, Object> getGrowthMetrics();

}