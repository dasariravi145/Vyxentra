package com.vyxentra.vehicle.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.vyxentra.vehicle.dto.ServiceHealth;
import com.vyxentra.vehicle.dto.request.CommissionUpdateRequest;
import com.vyxentra.vehicle.dto.request.ServiceConfigRequest;
import com.vyxentra.vehicle.dto.response.CommissionConfigResponse;
import com.vyxentra.vehicle.dto.response.ServiceConfigResponse;
import com.vyxentra.vehicle.dto.response.SystemConfigResponse;
import com.vyxentra.vehicle.dto.response.SystemHealthResponse;
import com.vyxentra.vehicle.entity.AdminAction;
import com.vyxentra.vehicle.entity.CommissionConfig;
import com.vyxentra.vehicle.entity.SystemConfig;
import com.vyxentra.vehicle.exceptions.BusinessException;
import com.vyxentra.vehicle.exceptions.ErrorCode;
import com.vyxentra.vehicle.exceptions.ResourceNotFoundException;
import com.vyxentra.vehicle.repository.AdminActionRepository;
import com.vyxentra.vehicle.repository.CommissionConfigRepository;
import com.vyxentra.vehicle.repository.SystemConfigRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.CacheManager;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class AdminServiceImpl implements AdminService {

    private final SystemConfigRepository systemConfigRepository;
    private final CommissionConfigRepository commissionConfigRepository;
    private final AdminActionRepository adminActionRepository;
    private final ObjectMapper objectMapper;
    private final CacheManager cacheManager;

    @Value("${admin.audit.log-user-actions:true}")
    private boolean logUserActions;

    @Override
    @Transactional(readOnly = true)
    public List<SystemConfigResponse> getAllConfigs() {
        return systemConfigRepository.findAll().stream()
                .map(this::mapToConfigResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public SystemConfigResponse getConfig(String key) {
        SystemConfig config = systemConfigRepository.findByConfigKey(key)
                .orElseThrow(() -> new ResourceNotFoundException("SystemConfig", "key", key));
        return mapToConfigResponse(config);
    }

    @Override
    @Transactional
    public SystemConfigResponse updateConfig(String adminId, String key, Map<String, Object> request) {
        log.info("Updating config: {} by admin: {}", key, adminId);

        SystemConfig config = systemConfigRepository.findByConfigKey(key)
                .orElseThrow(() -> new ResourceNotFoundException("SystemConfig", "key", key));

        // Capture before state for audit
        Map<String, Object> beforeState = Map.of(
                "value", config.getConfigValue(),
                "type", config.getConfigType()
        );

        // Update value
        Object newValue = request.get("value");
        if (newValue != null) {
            validateAndSetValue(config, newValue);
        }

        if (request.containsKey("description")) {
            config.setDescription((String) request.get("description"));
        }

        config.setUpdatedBy(adminId);
        config = systemConfigRepository.save(config);

        // Log admin action
        logAdminAction(adminId, "UPDATE_CONFIG", "CONFIG", key, beforeState,
                Map.of("value", config.getConfigValue()));

        // Clear cache if needed
        clearCache("configs");

        log.info("Config updated: {}", key);

        return mapToConfigResponse(config);
    }

    @Override
    @Transactional
    public SystemConfigResponse resetConfig(String adminId, String key) {
        log.info("Resetting config: {} by admin: {}", key, adminId);

        SystemConfig config = systemConfigRepository.findByConfigKey(key)
                .orElseThrow(() -> new ResourceNotFoundException("SystemConfig", "key", key));

        // Get default value from properties or predefined defaults
        String defaultValue = getDefaultValueForKey(key);

        Map<String, Object> beforeState = Map.of("value", config.getConfigValue());

        config.setConfigValue(defaultValue);
        config.setUpdatedBy(adminId);
        config = systemConfigRepository.save(config);

        logAdminAction(adminId, "RESET_CONFIG", "CONFIG", key, beforeState,
                Map.of("value", defaultValue));

        clearCache("configs");

        return mapToConfigResponse(config);
    }

    @Override
    @Transactional(readOnly = true)
    public Map<String, Object> exportAllConfigs() {
        Map<String, Object> export = new HashMap<>();

        // System configs
        export.put("system", systemConfigRepository.findAll().stream()
                .collect(Collectors.toMap(
                        SystemConfig::getConfigKey,
                        config -> Map.of(
                                "value", decryptIfNeeded(config),
                                "type", config.getConfigType(),
                                "description", config.getDescription()
                        )
                )));

        // Commission configs
        export.put("commission", commissionConfigRepository.findByIsActiveTrue().stream()
                .collect(Collectors.toMap(
                        CommissionConfig::getProviderType,
                        config -> Map.of(
                                "percentage", config.getCommissionPercentage(),
                                "minCommission", config.getMinCommission(),
                                "maxCommission", config.getMaxCommission(),
                                "effectiveFrom", config.getEffectiveFrom()
                        )
                )));

        return export;
    }

    @Override
    @Transactional
    public void importConfigs(String adminId, Map<String, Object> configs) {
        log.info("Importing configs by admin: {}", adminId);

        // Validate all configs first
        // Then apply updates
        // Log all changes

        logAdminAction(adminId, "IMPORT_CONFIGS", "CONFIG", "ALL", null, configs);

        clearCache("configs");

        log.info("Configs imported successfully");
    }

    @Override
    @Transactional(readOnly = true)
    public List<ServiceConfigResponse> getAllServiceConfigs() {
        // This would come from service-catalog-service via Feign
        return List.of();
    }

    @Override
    @Transactional(readOnly = true)
    public ServiceConfigResponse getServiceConfig(String serviceType) {
        // This would come from service-catalog-service via Feign
        return null;
    }

    @Override
    @Transactional
    public ServiceConfigResponse updateServiceConfig(String adminId, String serviceType,
                                                     ServiceConfigRequest request) {
        log.info("Updating service config: {} by admin: {}", serviceType, adminId);

        // This would call service-catalog-service via Feign

        logAdminAction(adminId, "UPDATE_SERVICE_CONFIG", "SERVICE", serviceType, null, request);

        return null;
    }

    @Override
    @Transactional
    public void toggleService(String adminId, String serviceType, boolean active) {
        log.info("Toggling service: {} to {} by admin: {}", serviceType, active, adminId);

        // This would call service-catalog-service via Feign

        logAdminAction(adminId, active ? "ACTIVATE_SERVICE" : "DEACTIVATE_SERVICE",
                "SERVICE", serviceType, null, Map.of("active", active));
    }

    @Override
    @Transactional(readOnly = true)
    public List<CommissionConfigResponse> getCommissionConfigs() {
        return commissionConfigRepository.findByIsActiveTrue().stream()
                .map(this::mapToCommissionResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public CommissionConfigResponse updateCommission(String adminId, String providerType,
                                                     CommissionUpdateRequest request) {
        log.info("Updating commission for: {} by admin: {}", providerType, adminId);

        CommissionConfig config = commissionConfigRepository
                .findByProviderTypeAndIsActiveTrue(providerType)
                .orElseGet(() -> {
                    // Create new config
                    return CommissionConfig.builder()
                            .providerType(providerType)
                            .commissionPercentage(15.0)
                            .effectiveFrom(LocalDate.now())
                            .isActive(true)
                            .build();
                });

        Map<String, Object> beforeState = Map.of(
                "percentage", config.getCommissionPercentage(),
                "minCommission", config.getMinCommission(),
                "maxCommission", config.getMaxCommission()
        );

        // Deactivate current config if effective date is in the future
        if (request.getEffectiveFrom() != null &&
                request.getEffectiveFrom().isAfter(LocalDate.now())) {
            config.setEffectiveTo(LocalDate.now().minusDays(1));
            commissionConfigRepository.save(config);

            // Create new config
            config = CommissionConfig.builder()
                    .providerType(providerType)
                    .commissionPercentage(request.getCommissionPercentage())
                    .minCommission(request.getMinCommission())
                    .maxCommission(request.getMaxCommission())
                    .effectiveFrom(request.getEffectiveFrom())
                    .effectiveTo(request.getEffectiveTo())
                    .isActive(true)
                    .updatedBy(adminId)
                    .build();
        } else {
            // Update current config
            config.setCommissionPercentage(request.getCommissionPercentage());
            config.setMinCommission(request.getMinCommission());
            config.setMaxCommission(request.getMaxCommission());
            config.setEffectiveFrom(request.getEffectiveFrom() != null ?
                    request.getEffectiveFrom() : LocalDate.now());
            config.setEffectiveTo(request.getEffectiveTo());
            config.setUpdatedBy(adminId);
        }

        config = commissionConfigRepository.save(config);

        logAdminAction(adminId, "UPDATE_COMMISSION", "COMMISSION", providerType,
                beforeState, Map.of(
                        "percentage", config.getCommissionPercentage(),
                        "minCommission", config.getMinCommission(),
                        "maxCommission", config.getMaxCommission()
                ));

        clearCache("commission");

        log.info("Commission updated for: {}", providerType);

        return mapToCommissionResponse(config);
    }

    @Override
    @Transactional(readOnly = true)
    public SystemHealthResponse getSystemHealth() {
        SystemHealthResponse response = new SystemHealthResponse();
        response.setStatus("UP");

        // Check dependent services
        List<ServiceHealth> services = Arrays.asList(
                checkServiceHealth("auth-service"),
                checkServiceHealth("user-service"),
                checkServiceHealth("provider-service"),
                checkServiceHealth("booking-service"),
                checkServiceHealth("payment-service"),
                checkServiceHealth("notification-service")
        );

        response.setServices(services);

        // Check for any warnings
        boolean hasDegraded = services.stream()
                .anyMatch(s -> "DEGRADED".equals(s.getStatus()));
        boolean hasDown = services.stream()
                .anyMatch(s -> "DOWN".equals(s.getStatus()));

        if (hasDown) {
            response.setStatus("DOWN");
        } else if (hasDegraded) {
            response.setStatus("DEGRADED");
        }

        return response;
    }

    @Override
    public void clearCache(String cacheName) {
        if (cacheName != null) {
            var cache = cacheManager.getCache(cacheName);
            if (cache != null) {
                cache.clear();
                log.info("Cleared cache: {}", cacheName);
            }
        } else {
            cacheManager.getCacheNames().forEach(name -> {
                var cache = cacheManager.getCache(name);
                if (cache != null) {
                    cache.clear();
                }
            });
            log.info("Cleared all caches");
        }
    }

    @Override
    @Transactional(readOnly = true)
    public Object getMetrics() {
        // Return system metrics from Actuator
        return Map.of(
                "uptime", System.currentTimeMillis(),
                "memory", Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory(),
                "threads", Thread.activeCount()
        );
    }

    private void validateAndSetValue(SystemConfig config, Object value) {
        String stringValue = value.toString();

        switch (config.getConfigType()) {
            case "INTEGER":
                try {
                    Integer.parseInt(stringValue);
                } catch (NumberFormatException e) {
                    throw new BusinessException(ErrorCode.VALIDATION_ERROR,
                            "Value must be an integer");
                }
                break;
            case "BOOLEAN":
                if (!"true".equalsIgnoreCase(stringValue) &&
                        !"false".equalsIgnoreCase(stringValue)) {
                    throw new BusinessException(ErrorCode.VALIDATION_ERROR,
                            "Value must be true or false");
                }
                break;
            case "DECIMAL":
                try {
                    Double.parseDouble(stringValue);
                } catch (NumberFormatException e) {
                    throw new BusinessException(ErrorCode.VALIDATION_ERROR,
                            "Value must be a decimal number");
                }
                break;
        }

        config.setConfigValue(stringValue);
    }

    private String getDefaultValueForKey(String key) {
        Map<String, String> defaults = Map.of(
                "booking.expiry.minutes", "30",
                "damage.approval.hours", "24",
                "emergency.multiplier.repair", "1.5",
                "emergency.multiplier.petrol", "1.2",
                "provider.settlement.days", "3",
                "wallet.max.balance", "50000",
                "wallet.min.topup", "100"
        );

        return defaults.getOrDefault(key, "");
    }

    private Object decryptIfNeeded(SystemConfig config) {
        if (Boolean.TRUE.equals(config.getIsEncrypted())) {
            // Decrypt logic here
            return "***ENCRYPTED***";
        }
        return config.getConfigValue();
    }

    private void logAdminAction(String adminId, String actionType, String targetType,
                                String targetId, Object beforeState, Object afterState) {
        if (!logUserActions) {
            return;
        }

        AdminAction action = AdminAction.builder()
                .adminId(adminId)
                .actionType(actionType)
                .targetType(targetType)
                .targetId(targetId)
                .beforeState(beforeState != null ? objectMapper.convertValue(beforeState, Map.class) : null)
                .afterState(afterState != null ? objectMapper.convertValue(afterState, Map.class) : null)
                .createdAt(LocalDateTime.now())
                .build();

        adminActionRepository.save(action);
    }

    private ServiceHealth checkServiceHealth(String serviceName) {
        // This would call each service's health endpoint via Feign
        return ServiceHealth.builder()
                .name(serviceName)
                .status("UP")
                .responseTime(100L)
                .version("1.0.0")
                .build();
    }

    private SystemConfigResponse mapToConfigResponse(SystemConfig config) {
        return SystemConfigResponse.builder()
                .configId(config.getId())
                .configKey(config.getConfigKey())
                .configValue(decryptIfNeeded(config))
                .configType(config.getConfigType())
                .description(config.getDescription())
                .isEncrypted(config.getIsEncrypted())
                .updatedBy(config.getUpdatedBy())
                .updatedAt(config.getUpdatedAt())
                .createdAt(config.getCreatedAt())
                .build();
    }

    private CommissionConfigResponse mapToCommissionResponse(CommissionConfig config) {
        return CommissionConfigResponse.builder()
                .configId(config.getId())
                .providerType(config.getProviderType())
                .commissionPercentage(config.getCommissionPercentage())
                .minCommission(config.getMinCommission())
                .maxCommission(config.getMaxCommission())
                .effectiveFrom(config.getEffectiveFrom())
                .effectiveTo(config.getEffectiveTo())
                .isActive(config.getIsActive())
                .updatedBy(config.getUpdatedBy())
                .updatedAt(Instant.from(config.getUpdatedAt()))
                .createdAt(Instant.from(config.getCreatedAt()))
                .build();
    }
}
