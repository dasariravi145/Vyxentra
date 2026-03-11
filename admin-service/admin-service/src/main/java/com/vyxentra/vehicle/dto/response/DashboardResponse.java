package com.vyxentra.vehicle.dto.response;


import com.vyxentra.vehicle.dto.Activity;
import com.vyxentra.vehicle.dto.Alert;
import com.vyxentra.vehicle.dto.ChartData;
import com.vyxentra.vehicle.dto.PendingApproval;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DashboardResponse {

    // Key Metrics
    private Long totalUsers;
    private Long totalProviders;
    private Long totalBookings;
    private Long totalRevenue;
    private Long pendingApprovals;
    private Long activeEmergencies;

    // Today's Metrics
    private Long todayBookings;
    private Long todayRevenue;
    private Long todayNewUsers;
    private Long todayCompletedServices;

    // Charts Data
    private List<ChartData> revenueChart;
    private List<ChartData> bookingsChart;
    private Map<String, Long> bookingStatusDistribution;
    private Map<String, Long> userTypeDistribution;

    // Recent Activities
    private List<Activity> recentActivities;
    private List<PendingApproval> pendingProviderApprovals;
    private List<Alert> alerts;

    private LocalDateTime lastUpdated;

}