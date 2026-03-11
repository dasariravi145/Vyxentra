package com.vyxentra.vehicle.service;


import com.vyxentra.vehicle.dto.request.NotificationPreferenceRequest;
import com.vyxentra.vehicle.dto.response.NotificationHistoryResponse;
import com.vyxentra.vehicle.dto.response.NotificationPreferenceResponse;
import com.vyxentra.vehicle.dto.response.PageResponse;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;

public interface NotificationService {

    PageResponse<NotificationHistoryResponse> getNotificationHistory(String userId, LocalDateTime fromDate,
                                                                     LocalDateTime toDate, String type,
                                                                     String status, Pageable pageable);

    Long getUnreadCount(String userId);

    void markAsRead(String notificationId, String userId);

    void markAllAsRead(String userId);

    void deleteNotification(String notificationId, String userId);

    NotificationPreferenceResponse getPreferences(String userId);

    NotificationPreferenceResponse updatePreferences(String userId, NotificationPreferenceRequest request);

    void setQuietHours(String userId, String startTime, String endTime, boolean enabled);

    void processPendingNotifications();
}
