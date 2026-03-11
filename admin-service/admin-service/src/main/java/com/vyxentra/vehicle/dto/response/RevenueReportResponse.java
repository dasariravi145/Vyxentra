package com.vyxentra.vehicle.dto.response;


import com.vyxentra.vehicle.dto.TimeSeriesData;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RevenueReportResponse {

    private LocalDate fromDate;
    private LocalDate toDate;
    private String interval;

    // Summary
    private Double totalRevenue;
    private Double totalCommission;
    private Double totalProviderPayout;
    private Long totalBookings;
    private Double averageOrderValue;

    // Breakdowns
    private Map<String, Double> revenueByProviderType;
    private Map<String, Double> revenueByPaymentMethod;
    private Map<String, Double> revenueByServiceType;

    // Time series
    private List<TimeSeriesData> timeSeries;

}
