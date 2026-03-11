package com.vyxentra.vehicle.service;


import com.vyxentra.vehicle.dto.request.SendPushRequest;
import com.vyxentra.vehicle.dto.response.NotificationResponse;
import com.vyxentra.vehicle.entity.Notification;
import com.vyxentra.vehicle.entity.PushDevice;

import java.util.List;

public interface PushNotificationService {

    NotificationResponse sendPush(String userId, SendPushRequest request);

    void broadcastPush(SendPushRequest request, String userType);

    void registerDevice(String userId, String deviceToken, String platform, String deviceModel, String appVersion);

    void unregisterDevice(String userId, String deviceToken);

    List<PushDevice> getUserDevices(String userId);

    void retryPush(Notification notification);
}
