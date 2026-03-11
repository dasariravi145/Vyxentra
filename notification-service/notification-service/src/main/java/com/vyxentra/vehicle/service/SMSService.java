package com.vyxentra.vehicle.service;


import com.vyxentra.vehicle.dto.request.SendSMSRequest;
import com.vyxentra.vehicle.dto.response.NotificationResponse;
import com.vyxentra.vehicle.entity.Notification;
import com.vyxentra.vehicle.entity.SMSLog;

public interface SMSService {

    NotificationResponse sendSMS(String userId, SendSMSRequest request);

    NotificationResponse sendTemplateSMS(String userId, String to, String templateName, Object templateData);

    void sendOTP(String phoneNumber, String otp);

    SMSLog getSMSLog(String notificationId);

    void retrySMS(Notification notification);
}
