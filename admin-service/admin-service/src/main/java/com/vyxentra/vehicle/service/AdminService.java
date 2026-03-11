package com.vyxentra.vehicle.service;



import com.vyxentra.vehicle.dto.request.CommissionUpdateRequest;
import com.vyxentra.vehicle.dto.request.ServiceConfigRequest;
import com.vyxentra.vehicle.dto.response.CommissionConfigResponse;
import com.vyxentra.vehicle.dto.response.ServiceConfigResponse;
import com.vyxentra.vehicle.dto.response.SystemConfigResponse;
import com.vyxentra.vehicle.dto.response.SystemHealthResponse;

import java.util.List;
import java.util.Map;

public interface AdminService {

    // System Configuration
    List<SystemConfigResponse> getAllConfigs();

    SystemConfigResponse getConfig(String key);

    SystemConfigResponse updateConfig(String adminId, String key, Map<String, Object> request);

    SystemConfigResponse resetConfig(String adminId, String key);

    Map<String, Object> exportAllConfigs();

    void importConfigs(String adminId, Map<String, Object> configs);

    // Service Configuration
    List<ServiceConfigResponse> getAllServiceConfigs();

    ServiceConfigResponse getServiceConfig(String serviceType);

    ServiceConfigResponse updateServiceConfig(String adminId, String serviceType, ServiceConfigRequest request);

    void toggleService(String adminId, String serviceType, boolean active);

    // Commission Configuration
    List<CommissionConfigResponse> getCommissionConfigs();

    CommissionConfigResponse updateCommission(String adminId, String providerType, CommissionUpdateRequest request);

    // System Health
    SystemHealthResponse getSystemHealth();

    // Cache Management
    void clearCache(String cacheName);

    // Metrics
    Object getMetrics();
}
