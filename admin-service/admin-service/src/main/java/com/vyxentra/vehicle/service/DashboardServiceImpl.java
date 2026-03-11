package com.vyxentra.vehicle.service;

import com.vyxentra.vehicle.dto.Activity;
import com.vyxentra.vehicle.dto.PendingApproval;
import com.vyxentra.vehicle.dto.response.DashboardResponse;
import com.vyxentra.vehicle.entity.DashboardCache;
import com.vyxentra.vehicle.repository.DashboardCacheRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class DashboardServiceImpl implements DashboardService {

    private final DashboardCacheRepository dashboardCacheRepository;

    @Value("${admin.dashboard.cache-minutes:5}")
    private int cacheMinutes;

    @Override
    @Cacheable(value = "dashboard", key = "'main'", unless = "#result == null")
    public DashboardResponse getDashboard() {
        log.debug("Building dashboard");

        // Check cache first
        String cacheKey = "dashboard:main";
        DashboardCache cached = dashboardCacheRepository.findByCacheKey(cacheKey)
                .filter(c -> c.getExpiresAt().isAfter(LocalDateTime.now()))
                .orElse(null);

        if (cached != null) {
            return cached.getCacheData();
        }

        // Build fresh dashboard
        DashboardResponse dashboard = buildDashboard();

        // Cache it
        DashboardCache cache = DashboardCache.builder()
                .id(UUID.randomUUID().toString())
                .cacheKey(cacheKey)
                .cacheData(dashboard)
                .expiresAt(LocalDateTime.now().plusMinutes(cacheMinutes))
                .createdAt(Instant.from(LocalDateTime.now()))
                .build();

        dashboardCacheRepository.save(cache);

        return dashboard;
    }

    @Override
    @CacheEvict(value = "dashboard", allEntries = true)
    public DashboardResponse refreshDashboard() {
        log.info("Refreshing dashboard cache");

        // Clear cache
        dashboardCacheRepository.deleteByCacheKeyStartsWith("dashboard:");

        // Build fresh
        return buildDashboard();
    }

    private DashboardResponse buildDashboard() {
        // This would aggregate data from various services
        // For now, return sample data

        return DashboardResponse.builder()
                .totalUsers(15000L)
                .totalProviders(500L)
                .totalBookings(25000L)
                .totalRevenue(12500000L)
                .pendingApprovals(25L)
                .activeEmergencies(3L)
                .todayBookings(120L)
                .todayRevenue(600000L)
                .todayNewUsers(45L)
                .todayCompletedServices(98L)
                .bookingStatusDistribution(Map.of(
                        "PENDING", 150L,
                        "CONFIRMED", 300L,
                        "IN_PROGRESS", 200L,
                        "COMPLETED", 450L
                ))
                .userTypeDistribution(Map.of(
                        "CUSTOMER", 14000L,
                        "PROVIDER", 500L,
                        "EMPLOYEE", 450L,
                        "ADMIN", 50L
                ))
                .recentActivities(List.of(
                        Activity.builder()
                                .id("act_1")
                                .type("PROVIDER_APPROVED")
                                .description("New service center approved in Bangalore")
                                .timestamp(LocalDateTime.now().minusHours(2))
                                .build()
                ))
                .pendingProviderApprovals(List.of(
                        PendingApproval.builder()
                                .providerId("prov_1")
                                .businessName("Quick Fix Garage")
                                .registeredAt(LocalDateTime.now().minusDays(2))
                                .pendingDocuments(2L)
                                .build()
                ))
                .lastUpdated(LocalDateTime.now())
                .build();
    }
}
