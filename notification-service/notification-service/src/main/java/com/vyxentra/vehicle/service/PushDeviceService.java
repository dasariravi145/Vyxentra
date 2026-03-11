package com.vyxentra.vehicle.service;



import com.vyxentra.vehicle.dto.response.PushDeviceResponse;

import java.util.List;
import java.util.Map;

public interface PushDeviceService {

    PushDeviceResponse registerDevice(String userId, String deviceToken, String platform,
                                      String deviceModel, String appVersion);

    void unregisterDevice(String userId, String deviceToken);

    List<PushDeviceResponse> getUserDevices(String userId);

    PushDeviceResponse getDevice(String deviceId, String userId);

    void deactivateDevice(String deviceToken);

    void deactivateAllUserDevices(String userId);

    void updateLastUsed(String deviceToken);

    void updateAppVersion(String deviceToken, String appVersion);

    void cleanupInactiveDevices(int daysInactive);

    long countUserDevices(String userId);

    boolean hasActiveDevices(String userId);

    List<String> getActiveDeviceTokens(String userId);

    Map<String, Object> getDeviceStatistics();

    void bulkDeactivateDevices(List<String> deviceTokens);
}