package com.vyxentra.vehicle.service;

import com.vyxentra.vehicle.dto.request.SendPushRequest;
import com.vyxentra.vehicle.dto.response.NotificationResponse;
import com.vyxentra.vehicle.entity.Notification;
import com.vyxentra.vehicle.entity.PushDevice;
import com.vyxentra.vehicle.exceptions.BusinessException;
import com.vyxentra.vehicle.exceptions.ErrorCode;
import com.vyxentra.vehicle.provider.PushProvider;
import com.vyxentra.vehicle.provider.PushProviderFactory;
import com.vyxentra.vehicle.repository.NotificationRepository;
import com.vyxentra.vehicle.repository.NotificationTemplateRepository;
import com.vyxentra.vehicle.repository.PushDeviceRepository;
import com.vyxentra.vehicle.template.TemplateEngine;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class PushNotificationServiceImpl implements PushNotificationService {

    private final NotificationRepository notificationRepository;
    private final PushDeviceRepository pushDeviceRepository;
    private final NotificationTemplateRepository templateRepository;
    private final TemplateEngine templateEngine;
    private final PushProviderFactory providerFactory;

    @Override
    @Transactional
    public NotificationResponse sendPush(String userId, SendPushRequest request) {
        log.info("Sending push notification to user: {}", userId);

        // Get user's active devices
        List<PushDevice> devices = pushDeviceRepository.findByUserIdAndIsActiveTrue(userId);

        if (devices.isEmpty()) {
            throw new BusinessException(ErrorCode.VALIDATION_ERROR, "No active devices found for user");
        }

        // Create notification record
        Notification notification = createNotification(userId, request);
        notification = notificationRepository.save(notification);

        int successCount = 0;
        PushProvider provider = providerFactory.getProvider();

        for (PushDevice device : devices) {
            try {
                // Send push to device
                Map<String, Object> providerResponse = provider.sendPush(
                        device.getDeviceToken(),
                        request.getTitle(),
                        request.getBody(),
                        request.getData(),
                        request.getImageUrl(),
                        request.getClickAction()
                );

                // Update device last used
                pushDeviceRepository.updateLastUsed(device.getId(), LocalDateTime.now());

                successCount++;

                log.debug("Push sent to device: {}", device.getDeviceToken());

            } catch (Exception e) {
                log.error("Failed to send push to device {}: {}", device.getDeviceToken(), e.getMessage());

                // Deactivate device if token is invalid
                if (e.getMessage() != null && e.getMessage().contains("InvalidRegistration")) {
                    device.setIsActive(false);
                    pushDeviceRepository.save(device);
                }
            }
        }

        // Update notification status
        if (successCount > 0) {
            notification.setStatus("SENT");
            notification.setSentAt(LocalDateTime.now());
        } else {
            notification.setStatus("FAILED");
            notification.setErrorMessage("Failed to send to any device");
        }

        notificationRepository.save(notification);

        log.info("Push notification sent to {} of {} devices for user: {}", successCount, devices.size(), userId);

        return mapToResponse(notification);
    }

    @Override
    @Transactional
    public void broadcastPush(SendPushRequest request, String userType) {
        log.info("Broadcasting push notification to {} users", userType != null ? userType : "all");

        List<PushDevice> devices;
        if (userType != null) {
            devices = pushDeviceRepository.findByUserType(userType);
        } else {
            devices = pushDeviceRepository.findAll();
        }

        // Create notification record for broadcast
        Notification notification = createNotification("BROADCAST", request);
        notification = notificationRepository.save(notification);

        PushProvider provider = providerFactory.getProvider();
        int successCount = 0;

        for (PushDevice device : devices) {
            try {
                provider.sendPush(
                        device.getDeviceToken(),
                        request.getTitle(),
                        request.getBody(),
                        request.getData(),
                        request.getImageUrl(),
                        request.getClickAction()
                );
                successCount++;
            } catch (Exception e) {
                log.error("Failed to broadcast to device {}: {}", device.getDeviceToken(), e.getMessage());
            }
        }

        log.info("Push broadcast sent to {} of {} devices", successCount, devices.size());
    }

    @Override
    @Transactional
    public void registerDevice(String userId, String deviceToken, String platform,
                               String deviceModel, String appVersion) {
        log.info("Registering device for user: {}", userId);

        // Check if device already registered
        pushDeviceRepository.findByDeviceToken(deviceToken).ifPresentOrElse(
                device -> {
                    // Update existing device
                    device.setUserId(userId);
                    device.setPlatform(platform);
                    device.setDeviceModel(deviceModel);
                    device.setAppVersion(appVersion);
                    device.setIsActive(true);
                    device.setLastUsedAt(LocalDateTime.now());
                    pushDeviceRepository.save(device);
                    log.info("Device updated: {}", deviceToken);
                },
                () -> {
                    // Create new device
                    PushDevice device = PushDevice.builder()
                            .userId(userId)
                            .deviceToken(deviceToken)
                            .platform(platform)
                            .deviceModel(deviceModel)
                            .appVersion(appVersion)
                            .isActive(true)
                            .lastUsedAt(LocalDateTime.now())
                            .build();
                    pushDeviceRepository.save(device);
                    log.info("New device registered: {}", deviceToken);
                }
        );
    }

    @Override
    @Transactional
    public void unregisterDevice(String userId, String deviceToken) {
        log.info("Unregistering device for user: {}");
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
    public List<PushDevice> getUserDevices(String userId) {
        return pushDeviceRepository.findByUserIdAndIsActiveTrue(userId);
    }

    @Override
    @Transactional
    public void retryPush(Notification notification) {
        log.info("Retrying push notification: {}", notification.getId());

        SendPushRequest request = mapToRequest(notification);

        try {
            NotificationResponse response = sendPush(notification.getUserId(), request);

            if ("SENT".equals(response.getStatus())) {
                notification.setStatus("SENT");
                notification.setSentAt(LocalDateTime.now());
                notification.setRetryCount(notification.getRetryCount() + 1);
                notificationRepository.save(notification);

                log.info("Push retry successful for notification: {}", notification.getId());
            }
        } catch (Exception e) {
            log.error("Push retry failed for notification: {} - {}", notification.getId(), e.getMessage());

            notification.setRetryCount(notification.getRetryCount() + 1);
            notificationRepository.save(notification);
        }
    }

    private Notification createNotification(String userId, SendPushRequest request) {
        String notificationNumber = generateNotificationNumber();

        return Notification.builder()
                .notificationNumber(notificationNumber)
                .userId(userId)
                .userType(determineUserType(userId))
                .type("PUSH")
                .channel("TRANSACTIONAL")
                .title(request.getTitle())
                .content(request.getBody())
                .status("PENDING")
                .priority(determinePriority(request.getPriority()))
                .referenceId(request.getReferenceId())
                .referenceType(request.getReferenceType())
                .metadata(Map.of(
                        "data", request.getData(),
                        "imageUrl", request.getImageUrl(),
                        "clickAction", request.getClickAction(),
                        "sound", request.getSound()
                ))
                .retryCount(0)
                .build();
    }

    private String generateNotificationNumber() {
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmm"));
        String random = UUID.randomUUID().toString().substring(0, 4).toUpperCase();
        return "NOT" + timestamp + random;
    }

    private String determineUserType(String userId) {
        if (userId.startsWith("cust")) return "CUSTOMER";
        if (userId.startsWith("prov")) return "PROVIDER";
        if (userId.startsWith("emp")) return "EMPLOYEE";
        if (userId.startsWith("admin")) return "ADMIN";
        return "SYSTEM";
    }

    private String determinePriority(Integer priority) {
        if (priority == null) return "NORMAL";
        if (priority >= 8) return "HIGH";
        if (priority <= 3) return "LOW";
        return "NORMAL";
    }

    private SendPushRequest mapToRequest(Notification notification) {
        Map<String, Object> metadata = notification.getMetadata();

        return SendPushRequest.builder()
                .title(notification.getTitle())
                .body(notification.getContent())
                .data(metadata != null ? (Map<String, String>) metadata.get("data") : null)
                .imageUrl(metadata != null ? (String) metadata.get("imageUrl") : null)
                .clickAction(metadata != null ? (String) metadata.get("clickAction") : null)
                .sound(metadata != null ? (String) metadata.get("sound") : null)
                .referenceId(notification.getReferenceId())
                .referenceType(notification.getReferenceType())
                .build();
    }

    private NotificationResponse mapToResponse(Notification notification) {
        return NotificationResponse.builder()
                .notificationId(notification.getId())
                .notificationNumber(notification.getNotificationNumber())
                .userId(notification.getUserId())
                .type(notification.getType())
                .channel(notification.getChannel())
                .title(notification.getTitle())
                .content(notification.getContent())
                .status(notification.getStatus())
                .sentAt(notification.getSentAt())
                .deliveredAt(notification.getDeliveredAt())
                .readAt(notification.getReadAt())
                .build();
    }
}