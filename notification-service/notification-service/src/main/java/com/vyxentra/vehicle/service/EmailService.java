package com.vyxentra.vehicle.service;


import com.vyxentra.vehicle.dto.request.SendEmailRequest;
import com.vyxentra.vehicle.dto.response.NotificationResponse;
import com.vyxentra.vehicle.entity.EmailLog;
import com.vyxentra.vehicle.entity.Notification;

import java.util.List;

public interface EmailService {

    NotificationResponse sendEmail(String userId, SendEmailRequest request);

    NotificationResponse sendTemplateEmail(String userId, String to, String templateName, Object templateData);

    void sendBatchEmails(List<SendEmailRequest> requests);

    EmailLog getEmailLog(String notificationId);

    void retryEmail(Notification notification);
}
