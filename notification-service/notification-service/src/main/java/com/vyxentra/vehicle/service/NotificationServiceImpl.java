package com.vyxentra.vehicle.service;

import com.vyxentra.vehicle.dto.request.NotificationPreferenceRequest;
import com.vyxentra.vehicle.dto.response.NotificationHistoryResponse;
import com.vyxentra.vehicle.dto.response.NotificationPreferenceResponse;
import com.vyxentra.vehicle.dto.response.PageResponse;
import com.vyxentra.vehicle.entity.Notification;
import com.vyxentra.vehicle.entity.NotificationPreference;
import com.vyxentra.vehicle.exceptions.BusinessException;
import com.vyxentra.vehicle.exceptions.ErrorCode;
import com.vyxentra.vehicle.exceptions.ResourceNotFoundException;
import com.vyxentra.vehicle.mapper.NotificationMapper;
import com.vyxentra.vehicle.repository.NotificationPreferenceRepository;
import com.vyxentra.vehicle.repository.NotificationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class NotificationServiceImpl implements NotificationService {

    private final NotificationRepository notificationRepository;
    private final NotificationPreferenceRepository preferenceRepository;
    private final NotificationMapper notificationMapper;
    private final EmailService emailService;
    private final SMSService smsService;
    private final PushNotificationService pushService;

    @Value("${notification.retry.max-attempts:3}")
    private int maxRetryAttempts;

    @Override
    @Transactional(readOnly = true)
    public PageResponse<NotificationHistoryResponse> getNotificationHistory(String userId, LocalDateTime fromDate,
                                                                            LocalDateTime toDate, String type,
                                                                            String status, Pageable pageable) {
        log.debug("Getting notification history for user: {}", userId);

        Page<Notification> page = notificationRepository.findUserNotifications(
                userId, fromDate, toDate, type, status, pageable);

        return PageResponse.<NotificationHistoryResponse>builder()
                .content(notificationMapper.toHistoryResponseList(page.getContent()))
                .pageNumber(page.getNumber())
                .pageSize(page.getSize())
                .totalElements(page.getTotalElements())
                .totalPages(page.getTotalPages())
                .first(page.isFirst())
                .last(page.isLast())
                .hasNext(page.hasNext())
                .hasPrevious(page.hasPrevious())
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public Long getUnreadCount(String userId) {
        return notificationRepository.countUnreadByUserId(userId);
    }

    @Override
    @Transactional
    public void markAsRead(String notificationId, String userId) {
        log.info("Marking notification {} as read for user: {}", notificationId, userId);

        Notification notification = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new ResourceNotFoundException("Notification", notificationId));

        if (!notification.getUserId().equals(userId)) {
            throw new BusinessException(ErrorCode.UNAUTHORIZED, "Not authorized to modify this notification");
        }

        notification.setStatus("READ");
        notification.setReadAt(LocalDateTime.now());
        notificationRepository.save(notification);
    }

    @Override
    @Transactional
    public void markAllAsRead(String userId) {
        log.info("Marking all notifications as read for user: {}", userId);
        int updated = notificationRepository.markAllAsRead(userId, LocalDateTime.now());
        log.info("Marked {} notifications as read", updated);
    }

    @Override
    @Transactional
    public void deleteNotification(String notificationId, String userId) {
        log.info("Deleting notification {} for user: {}", notificationId, userId);

        Notification notification = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new ResourceNotFoundException("Notification", notificationId));

        if (!notification.getUserId().equals(userId)) {
            throw new BusinessException(ErrorCode.UNAUTHORIZED, "Not authorized to delete this notification");
        }

        notificationRepository.delete(notification);
    }

    @Override
    @Transactional(readOnly = true)
    public NotificationPreferenceResponse getPreferences(String userId) {
        log.debug("Getting notification preferences for user: {}", userId);

        NotificationPreference preference = preferenceRepository.findByUserId(userId)
                .orElseGet(() -> createDefaultPreferences(userId));

        return notificationMapper.toPreferenceResponse(preference);
    }

    @Override
    @Transactional
    public NotificationPreferenceResponse updatePreferences(String userId, NotificationPreferenceRequest request) {
        log.info("Updating notification preferences for user: {}", userId);

        NotificationPreference preference = preferenceRepository.findByUserId(userId)
                .orElseGet(() -> createDefaultPreferences(userId));

        if (request.getEmailEnabled() != null) {
            preference.setEmailEnabled(request.getEmailEnabled());
        }
        if (request.getSmsEnabled() != null) {
            preference.setSmsEnabled(request.getSmsEnabled());
        }
        if (request.getPushEnabled() != null) {
            preference.setPushEnabled(request.getPushEnabled());
        }
        if (request.getMarketingEnabled() != null) {
            preference.setMarketingEnabled(request.getMarketingEnabled());
        }
        if (request.getBookingUpdates() != null) {
            preference.setBookingUpdates(request.getBookingUpdates());
        }
        if (request.getPaymentUpdates() != null) {
            preference.setPaymentUpdates(request.getPaymentUpdates());
        }
        if (request.getEmergencyAlerts() != null) {
            preference.setEmergencyAlerts(request.getEmergencyAlerts());
        }
        if (request.getPromotionalOffers() != null) {
            preference.setPromotionalOffers(request.getPromotionalOffers());
        }

        preference = preferenceRepository.save(preference);

        return notificationMapper.toPreferenceResponse(preference);
    }

    @Override
    @Transactional
    public void setQuietHours(String userId, String startTime, String endTime, boolean enabled) {
        log.info("Setting quiet hours for user: {} - {} to {}", userId, startTime, endTime);

        NotificationPreference preference = preferenceRepository.findByUserId(userId)
                .orElseGet(() -> createDefaultPreferences(userId));

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm");
        preference.setQuietHoursStart(LocalTime.parse(startTime, formatter));
        preference.setQuietHoursEnd(LocalTime.parse(endTime, formatter));
        preference.setQuietHoursEnabled(enabled);

        preferenceRepository.save(preference);
    }

    @Override
    @Transactional
    public void processPendingNotifications() {
        log.info("Processing pending notifications");

        List<Notification> pendingNotifications = notificationRepository.findPendingNotifications(maxRetryAttempts);

        for (Notification notification : pendingNotifications) {
            try {
                switch (notification.getType()) {
                    case "EMAIL":
                        emailService.retryEmail(notification);
                        break;
                    case "SMS":
                        smsService.retrySMS(notification);
                        break;
                    case "PUSH":
                        pushService.retryPush(notification);
                        break;
                }
            } catch (Exception e) {
                log.error("Failed to process pending notification {}: {}", notification.getId(), e.getMessage());
            }
        }
    }

    private NotificationPreference createDefaultPreferences(String userId) {
        NotificationPreference preference = NotificationPreference.builder()
                .userId(userId)
                .emailEnabled(true)
                .smsEnabled(true)
                .pushEnabled(true)
                .marketingEnabled(false)
                .bookingUpdates(true)
                .paymentUpdates(true)
                .emergencyAlerts(true)
                .promotionalOffers(false)
                .quietHoursEnabled(false)
                .build();

        return preferenceRepository.save(preference);
    }
}