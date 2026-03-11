package com.vyxentra.vehicle.service;

import com.vyxentra.vehicle.dto.request.SendEmailRequest;
import com.vyxentra.vehicle.dto.response.NotificationResponse;
import com.vyxentra.vehicle.entity.EmailLog;
import com.vyxentra.vehicle.entity.Notification;
import com.vyxentra.vehicle.entity.NotificationTemplate;
import com.vyxentra.vehicle.exceptions.BusinessException;
import com.vyxentra.vehicle.exceptions.ErrorCode;
import com.vyxentra.vehicle.exceptions.ResourceNotFoundException;
import com.vyxentra.vehicle.provider.EmailProvider;
import com.vyxentra.vehicle.provider.EmailProviderFactory;
import com.vyxentra.vehicle.repository.EmailLogRepository;
import com.vyxentra.vehicle.repository.NotificationRepository;
import com.vyxentra.vehicle.repository.NotificationTemplateRepository;
import com.vyxentra.vehicle.template.TemplateEngine;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
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
public class EmailServiceImpl implements EmailService {

    private final NotificationRepository notificationRepository;
    private final EmailLogRepository emailLogRepository;
    private final NotificationTemplateRepository templateRepository;
    private final TemplateEngine templateEngine;
    private final EmailProviderFactory providerFactory;

    @Value("${spring.mail.username}")
    private String fromEmail;

    @Value("${sendgrid.from-name}")
    private String fromName;

    @Override
    @Transactional
    public NotificationResponse sendEmail(String userId, SendEmailRequest request) {
        log.info("Sending email to: {}", request.getTo());

        // Create notification record
        Notification notification = createNotification(userId, request);
        notification = notificationRepository.save(notification);

        try {
            // Get email provider
            EmailProvider provider = providerFactory.getProvider();

            // Prepare email
            String from = fromName != null ? fromName + " <" + fromEmail + ">" : fromEmail;

            // Send email
            Map<String, Object> providerResponse = provider.sendEmail(
                    from,
                    request.getTo(),
                    request.getSubject(),
                    request.getContent(),
                    request.getTextContent(),
                    request.getAttachments()
            );

            // Create email log
            EmailLog emailLog = EmailLog.builder()
                    .notification(notification)
                    .fromEmail(fromEmail)
                    .toEmail(request.getTo())
                    .subject(request.getSubject())
                    .htmlContent(request.getContent())
                    .textContent(request.getTextContent())
                    .provider(provider.getProviderName())
                    .providerMessageId((String) providerResponse.get("messageId"))
                    .providerResponse(providerResponse)
                    .status("SENT")
                    .build();

            emailLogRepository.save(emailLog);

            // Update notification
            notification.setStatus("SENT");
            notification.setSentAt(LocalDateTime.now());
            notificationRepository.save(notification);

            log.info("Email sent successfully to: {}", request.getTo());

        } catch (Exception e) {
            log.error("Failed to send email to: {} - {}", request.getTo(), e.getMessage());

            notification.setStatus("FAILED");
            notification.setErrorMessage(e.getMessage());
            notification.setRetryCount(0);
            notificationRepository.save(notification);

            throw new BusinessException(ErrorCode.INTERNAL_SERVER_ERROR, "Failed to send email: " + e.getMessage());
        }

        return mapToResponse(notification);
    }

    @Override
    @Transactional
    public NotificationResponse sendTemplateEmail(String userId, String to, String templateName, Object templateData) {
        log.info("Sending template email: {} to: {}", templateName, to);

        // Get template
        NotificationTemplate template = templateRepository.findByNameAndChannelAndIsActiveTrue(templateName, "EMAIL")
                .orElseThrow(() -> new BusinessException(ErrorCode.VALIDATION_ERROR,
                        "Email template not found: " + templateName));

        // Process template
        String subject = templateEngine.processTemplate(template.getSubject(), templateData);
        String content = templateEngine.processTemplate(template.getTemplateContent(), templateData);

        // Create request
        SendEmailRequest request = SendEmailRequest.builder()
                .to(to)
                .subject(subject)
                .content(content)
                .templateName(templateName)
                .templateData((Map<String, Object>) templateData)
                .build();

        return sendEmail(userId, request);
    }

    @Override
    @Async
    public void sendBatchEmails(List<SendEmailRequest> requests) {
        log.info("Processing batch of {} emails", requests.size());

        for (SendEmailRequest request : requests) {
            try {
                sendEmail("SYSTEM", request);
            } catch (Exception e) {
                log.error("Failed to send batch email to: {} - {}", request.getTo(), e.getMessage());
            }
        }
    }

    @Override
    @Transactional(readOnly = true)
    public EmailLog getEmailLog(String notificationId) {
        return emailLogRepository.findByNotificationId(notificationId)
                .orElseThrow(() -> new ResourceNotFoundException("EmailLog", "notification", notificationId));
    }

    @Override
    @Transactional
    public void retryEmail(Notification notification) {
        log.info("Retrying email notification: {}", notification.getId());

        EmailLog emailLog = emailLogRepository.findByNotificationId(notification.getId())
                .orElseThrow(() -> new ResourceNotFoundException("EmailLog", "notification", notification.getId()));

        try {
            EmailProvider provider = providerFactory.getProvider();

            Map<String, Object> providerResponse = provider.sendEmail(
                    emailLog.getFromEmail(),
                    emailLog.getToEmail(),
                    emailLog.getSubject(),
                    emailLog.getHtmlContent(),
                    emailLog.getTextContent(),
                    null // attachments not stored for retry
            );

            emailLog.setStatus("SENT");
            emailLog.setProviderResponse(providerResponse);
            emailLogRepository.save(emailLog);

            notification.setStatus("SENT");
            notification.setSentAt(LocalDateTime.now());
            notification.setRetryCount(notification.getRetryCount() + 1);
            notificationRepository.save(notification);

            log.info("Email retry successful for notification: {}", notification.getId());

        } catch (Exception e) {
            log.error("Email retry failed for notification: {} - {}", notification.getId(), e.getMessage());

            notification.setRetryCount(notification.getRetryCount() + 1);
            notificationRepository.save(notification);
        }
    }

    private Notification createNotification(String userId, SendEmailRequest request) {
        String notificationNumber = generateNotificationNumber();

        return Notification.builder()
                .notificationNumber(notificationNumber)
                .userId(userId)
                .userType(determineUserType(userId))
                .type("EMAIL")
                .channel(determineChannel(request))
                .title(request.getSubject())
                .content(request.getContent())
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

    private String determineChannel(SendEmailRequest request) {
        if (request.getTemplateName() != null &&
                (request.getTemplateName().contains("promo") || request.getTemplateName().contains("offer"))) {
            return "PROMOTIONAL";
        }
        return "TRANSACTIONAL";
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
