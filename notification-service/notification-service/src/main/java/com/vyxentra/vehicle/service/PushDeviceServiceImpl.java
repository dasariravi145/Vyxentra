package com.vyxentra.vehicle.service;

import com.vyxentra.vehicle.dto.response.PushDeviceResponse;
import com.vyxentra.vehicle.entity.PushDevice;
import com.vyxentra.vehicle.exceptions.BusinessException;
import com.vyxentra.vehicle.exceptions.ErrorCode;
import com.vyxentra.vehicle.exceptions.ResourceNotFoundException;
import com.vyxentra.vehicle.mapper.PushDeviceMapper;
import com.vyxentra.vehicle.repository.PushDeviceRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class PushDeviceServiceImpl implements PushDeviceService {

    private final PushDeviceRepository pushDeviceRepository;
    private final PushDeviceMapper pushDeviceMapper;

    @Value("${notification.push.max-devices-per-user:5}")
    private int maxDevicesPerUser;

    @Override
    @Transactional
    public PushDeviceResponse registerDevice(String userId, String deviceToken, String platform,
                                             String deviceModel, String appVersion) {
        log.info("Registering device for user: {}, platform: {}, token: {}", userId, platform, deviceToken);

        // Check if device already registered
        return pushDeviceRepository.findByDeviceToken(deviceToken)
                .map(existingDevice -> updateExistingDevice(existingDevice, userId, platform, deviceModel, appVersion))
                .orElseGet(() -> createNewDevice(userId, deviceToken, platform, deviceModel, appVersion));
    }

    private PushDeviceResponse updateExistingDevice(PushDevice device, String userId, String platform,
                                                    String deviceModel, String appVersion) {
        log.info("Updating existing device: {}", device.getDeviceToken());

        // If device belongs to different user, check limit for new user
        if (!device.getUserId().equals(userId)) {
            long userDeviceCount = pushDeviceRepository.countByUserIdAndIsActiveTrue(userId);
            if (userDeviceCount >= maxDevicesPerUser) {
                throw new BusinessException(ErrorCode.MAX_DEVICES_EXCEEDED,
                        "Maximum " + maxDevicesPerUser + " devices allowed per user");
            }
        }

        device.setUserId(userId);
        device.setPlatform(platform);
        device.setDeviceModel(deviceModel);
        device.setAppVersion(appVersion);
        device.setIsActive(true);
        device.setLastUsedAt(LocalDateTime.now());

        PushDevice saved = pushDeviceRepository.save(device);
        log.info("Device updated successfully: {}", saved.getId());

        return pushDeviceMapper.toResponse(saved);
    }

    private PushDeviceResponse createNewDevice(String userId, String deviceToken, String platform,
                                               String deviceModel, String appVersion) {
        // Check device limit
        long deviceCount = pushDeviceRepository.countByUserIdAndIsActiveTrue(userId);
        if (deviceCount >= maxDevicesPerUser) {
            throw new BusinessException(ErrorCode.MAX_DEVICES_EXCEEDED,
                    "Maximum " + maxDevicesPerUser + " devices allowed per user");
        }

        PushDevice device = PushDevice.builder()
                .userId(userId)
                .deviceToken(deviceToken)
                .platform(platform)
                .deviceModel(deviceModel)
                .appVersion(appVersion)
                .isActive(true)
                .lastUsedAt(LocalDateTime.now())
                .build();

        PushDevice saved = pushDeviceRepository.save(device);
        log.info("New device registered with ID: {}", saved.getId());

        return pushDeviceMapper.toResponse(saved);
    }

    @Override
    @Transactional
    public void unregisterDevice(String userId, String deviceToken) {
        log.info("Unregistering device for user: {}, token: {}", userId, deviceToken);

        pushDeviceRepository.findByDeviceToken(deviceToken).ifPresent(device -> {
            if (device.getUserId().equals(userId)) {
                device.setIsActive(false);
                pushDeviceRepository.save(device);
                log.info("Device unregistered: {}", deviceToken);
            }
        });
    }

    @Override
    @Transactional(readOnly = true)
    public List<PushDeviceResponse> getUserDevices(String userId) {
        return pushDeviceRepository.findByUserIdAndIsActiveTrue(userId)
                .stream()
                .map(pushDeviceMapper::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public PushDeviceResponse getDevice(String deviceId, String userId) {
        PushDevice device = pushDeviceRepository.findByIdAndUserId(deviceId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("PushDevice", deviceId));
        return pushDeviceMapper.toResponse(device);
    }

    @Override
    @Transactional
    public void deactivateDevice(String deviceToken) {
        int updated = pushDeviceRepository.deactivateByToken(deviceToken);
        if (updated > 0) {
            log.info("Device deactivated: {}", deviceToken);
        } else {
            log.warn("No device found with token: {}", deviceToken);
        }
    }

    @Override
    @Transactional
    public void deactivateAllUserDevices(String userId) {
        int updated = pushDeviceRepository.deactivateAllForUser(userId);
        log.info("Deactivated {} devices for user: {}", updated, userId);
    }

    @Override
    @Transactional
    public void updateLastUsed(String deviceToken) {
        pushDeviceRepository.updateLastUsedByToken(deviceToken, LocalDateTime.now());
    }

    @Override
    @Transactional
    public void updateAppVersion(String deviceToken, String appVersion) {
        pushDeviceRepository.updateAppVersion(deviceToken, appVersion);
        log.info("Updated app version for device: {} to {}", deviceToken, appVersion);
    }

    @Override
    @Transactional
    public void cleanupInactiveDevices(int daysInactive) {
        LocalDateTime cutoff = LocalDateTime.now().minusDays(daysInactive);
        List<PushDevice> inactiveDevices = pushDeviceRepository.findInactiveDevices(cutoff);

        for (PushDevice device : inactiveDevices) {
            device.setIsActive(false);
            pushDeviceRepository.save(device);
        }

        log.info("Cleaned up {} inactive devices", inactiveDevices.size());
    }

    @Override
    public long countUserDevices(String userId) {
        return pushDeviceRepository.countByUserIdAndIsActiveTrue(userId);
    }

    @Override
    public boolean hasActiveDevices(String userId) {
        return pushDeviceRepository.hasActiveDevices(userId);
    }

    @Override
    public List<String> getActiveDeviceTokens(String userId) {
        return pushDeviceRepository.findByUserIdAndIsActiveTrue(userId)
                .stream()
                .map(PushDevice::getDeviceToken)
                .collect(Collectors.toList());
    }

    @Override
    public Map<String, Object> getDeviceStatistics() {
        Map<String, Object> stats = new HashMap<>();

        stats.put("totalActiveDevices", pushDeviceRepository.countActiveDevices());
        stats.put("devicesByPlatform", pushDeviceRepository.getDeviceCountByPlatform());
        stats.put("devicesByAppVersion", pushDeviceRepository.getDeviceCountByAppVersion());

        return stats;
    }

    @Override
    @Transactional
    public void bulkDeactivateDevices(List<String> deviceTokens) {
        int deactivated = pushDeviceRepository.bulkDeactivate(deviceTokens);
        log.info("Bulk deactivated {} devices", deactivated);
    }
}