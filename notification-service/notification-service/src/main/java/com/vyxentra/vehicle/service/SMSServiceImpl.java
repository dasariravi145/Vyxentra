package com.vyxentra.vehicle.service;


import com.vyxentra.vehicle.dto.request.SendSMSRequest;
import com.vyxentra.vehicle.dto.response.NotificationResponse;
import com.vyxentra.vehicle.entity.Notification;
import com.vyxentra.vehicle.entity.NotificationTemplate;
import com.vyxentra.vehicle.entity.SMSLog;
import com.vyxentra.vehicle.exceptions.BusinessException;
import com.vyxentra.vehicle.exceptions.ErrorCode;
import com.vyxentra.vehicle.exceptions.ResourceNotFoundException;
import com.vyxentra.vehicle.provider.SMSProvider;
import com.vyxentra.vehicle.provider.SMSProviderFactory;
import com.vyxentra.vehicle.repository.NotificationRepository;
import com.vyxentra.vehicle.repository.NotificationTemplateRepository;
import com.vyxentra.vehicle.repository.SMSLogRepository;
import com.vyxentra.vehicle.template.TemplateEngine;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Map;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class SMSServiceImpl implements SMSService {

    private final NotificationRepository notificationRepository;
    private final SMSLogRepository smsLogRepository;
    private final NotificationTemplateRepository templateRepository;
    private final TemplateEngine templateEngine;
    private final SMSProviderFactory providerFactory;

    @Value("${twilio.phone-number}")
    private String fromNumber;

    @Override
    @Transactional
    public NotificationResponse sendSMS(String userId, SendSMSRequest request) {
        log.info("Sending SMS to: {}", request.getTo());

        // Create notification record
        Notification notification = createNotification(userId, request);
        notification = notificationRepository.save(notification);

        try {
            // Get SMS provider
            SMSProvider provider = providerFactory.getProvider();

            // Send SMS
            Map<String, Object> providerResponse = provider.sendSMS(
                    fromNumber,
                    request.getTo(),
                    request.getMessage()
            );

            // Create SMS log
            SMSLog smsLog = SMSLog.builder()
                    .notification(notification)
                    .fromNumber(fromNumber)
                    .toNumber(request.getTo())
                    .message(request.getMessage())
                    .provider(provider.getProviderName())
                    .providerSid((String) providerResponse.get("sid"))
                    .providerResponse(providerResponse)
                    .status("SENT")
                    .build();

            smsLogRepository.save(smsLog);

            // Update notification
            notification.setStatus("SENT");
            notification.setSentAt(LocalDateTime.now());
            notificationRepository.save(notification);

            log.info("SMS sent successfully to: {}", request.getTo());

        } catch (Exception e) {
            log.error("Failed to send SMS to: {} - {}", request.getTo(), e.getMessage());

            notification.setStatus("FAILED");
            notification.setErrorMessage(e.getMessage());
            notification.setRetryCount(0);
            notificationRepository.save(notification);

            throw new BusinessException(ErrorCode.INTERNAL_SERVER_ERROR, "Failed to send SMS: " + e.getMessage());
        }

        return mapToResponse(notification);
    }

    @Override
    @Transactional
    public NotificationResponse sendTemplateSMS(String userId, String to, String templateName, Object templateData) {
        log.info("Sending template SMS: {} to: {}", templateName, to);

        // Get template
        NotificationTemplate template = templateRepository.findByNameAndChannelAndIsActiveTrue(templateName, "SMS")
                .orElseThrow(() -> new BusinessException(ErrorCode.VALIDATION_ERROR,
                        "SMS template not found: " + templateName));

        // Process template
        String message = templateEngine.processTemplate(template.getTemplateContent(), templateData);

        // Create request
        SendSMSRequest request = SendSMSRequest.builder()
                .to(to)
                .message(message)
                .templateName(templateName)
                .templateData((Map<String, Object>) templateData)
                .build();

        return sendSMS(userId, request);
    }

    @Override
    public void sendOTP(String phoneNumber, String otp) {
        log.info("Sending OTP to: {}", phoneNumber);

        Map<String, Object> templateData = Map.of("otp", otp);

        SendSMSRequest request = SendSMSRequest.builder()
                .to(phoneNumber)
                .templateName("otp-verification")
                .templateData(templateData)
                .referenceType("OTP")
                .build();

        sendSMS("SYSTEM", request);
    }

    @Override
    @Transactional(readOnly = true)
    public SMSLog getSMSLog(String notificationId) {
        return smsLogRepository.findByNotificationId(notificationId)
                .orElseThrow(() -> new ResourceNotFoundException("SMSLog", "notification", notificationId));
    }

    @Override
    @Transactional
    public void retrySMS(Notification notification) {
        log.info("Retrying SMS notification: {}", notification.getId());

        SMSLog smsLog = smsLogRepository.findByNotificationId(notification.getId())
                .orElseThrow(() -> new ResourceNotFoundException("SMSLog", "notification", notification.getId()));

        try {
            SMSProvider provider = providerFactory.getProvider();

            Map<String, Object> providerResponse = provider.sendSMS(
                    smsLog.getFromNumber(),
                    smsLog.getToNumber(),
                    smsLog.getMessage()
            );

            smsLog.setStatus("SENT");
            smsLog.setProviderResponse(providerResponse);
            smsLogRepository.save(smsLog);

            notification.setStatus("SENT");
            notification.setSentAt(LocalDateTime.now());
            notification.setRetryCount(notification.getRetryCount() + 1);
            notificationRepository.save(notification);

            log.info("SMS retry successful for notification: {}", notification.getId());

        } catch (Exception e) {
            log.error("SMS retry failed for notification: {} - {}", notification.getId(), e.getMessage());

            notification.setRetryCount(notification.getRetryCount() + 1);
            notificationRepository.save(notification);
        }
    }

    private Notification createNotification(String userId, SendSMSRequest request) {
        String notificationNumber = generateNotificationNumber();

        return Notification.builder()
                .notificationNumber(notificationNumber)
                .userId(userId)
                .userType(determineUserType(userId))
                .type("SMS")
                .channel("TRANSACTIONAL")
                .content(request.getMessage())
                .templateName(request.getTemplateName())
                .templateData(request.getTemplateData())
                .status("PENDING")
                .priority("NORMAL")
                .referenceId(request.getReferenceId())
                .referenceType(request.getReferenceType())
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

    private NotificationResponse mapToResponse(Notification notification) {
        return NotificationResponse.builder()
                .notificationId(notification.getId())
                .notificationNumber(notification.getNotificationNumber())
                .userId(notification.getUserId())
                .type(notification.getType())
                .channel(notification.getChannel())
                .content(notification.getContent())
                .status(notification.getStatus())
                .sentAt(notification.getSentAt())
                .deliveredAt(notification.getDeliveredAt())
                .readAt(notification.getReadAt())
                .build();
    }
}
