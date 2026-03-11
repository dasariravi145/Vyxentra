package com.vyxentra.vehicle.controller;


import com.vyxentra.vehicle.dto.request.SendEmailRequest;
import com.vyxentra.vehicle.dto.response.ApiResponse;
import com.vyxentra.vehicle.dto.response.NotificationResponse;
import com.vyxentra.vehicle.entity.EmailLog;
import com.vyxentra.vehicle.service.EmailService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/v1/notifications/email")
@RequiredArgsConstructor
public class EmailController {

    private final EmailService emailService;

    @PostMapping("/send")
    public ResponseEntity<ApiResponse<NotificationResponse>> sendEmail(
            @RequestHeader("X-User-ID") String userId,
            @Valid @RequestBody SendEmailRequest request) {
        log.info("Sending email to: {}", request.getTo());
        NotificationResponse response = emailService.sendEmail(userId, request);
        return ResponseEntity.ok(ApiResponse.success(response, "Email sent"));
    }

    @PostMapping("/template/{templateName}")
    public ResponseEntity<ApiResponse<NotificationResponse>> sendTemplateEmail(
            @RequestHeader("X-User-ID") String userId,
            @PathVariable String templateName,
            @RequestParam String to,
            @RequestBody Object templateData) {
        log.info("Sending template email: {} to: {}", templateName, to);
        NotificationResponse response = emailService.sendTemplateEmail(userId, to, templateName, templateData);
        return ResponseEntity.ok(ApiResponse.success(response, "Template email sent"));
    }

    @PostMapping("/batch")
    public ResponseEntity<ApiResponse<Void>> sendBatchEmails(
            @RequestBody List<SendEmailRequest> requests) {
        log.info("Sending batch of {} emails", requests.size());
        emailService.sendBatchEmails(requests);
        return ResponseEntity.ok(ApiResponse.success(null, "Batch emails queued"));
    }

    @GetMapping("/logs/{notificationId}")
    public ResponseEntity<ApiResponse<EmailLog>> getEmailLog(
            @PathVariable String notificationId) {
        log.info("Getting email log for notification: {}", notificationId);
        EmailLog log = emailService.getEmailLog(notificationId);
        return ResponseEntity.ok(ApiResponse.success(log));
    }
}