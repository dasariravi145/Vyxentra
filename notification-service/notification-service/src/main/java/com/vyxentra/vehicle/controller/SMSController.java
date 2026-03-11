package com.vyxentra.vehicle.controller;


import com.vyxentra.vehicle.dto.request.SendSMSRequest;
import com.vyxentra.vehicle.dto.response.ApiResponse;
import com.vyxentra.vehicle.dto.response.NotificationResponse;
import com.vyxentra.vehicle.entity.SMSLog;
import com.vyxentra.vehicle.service.SMSService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/api/v1/notifications/sms")
@RequiredArgsConstructor
public class SMSController {

    private final SMSService smsService;

    @PostMapping("/send")
    public ResponseEntity<ApiResponse<NotificationResponse>> sendSMS(
            @RequestHeader("X-User-ID") String userId,
            @Valid @RequestBody SendSMSRequest request) {
        log.info("Sending SMS to: {}", request.getTo());
        NotificationResponse response = smsService.sendSMS(userId, request);
        return ResponseEntity.ok(ApiResponse.success(response, "SMS sent"));
    }

    @PostMapping("/template/{templateName}")
    public ResponseEntity<ApiResponse<NotificationResponse>> sendTemplateSMS(
            @RequestHeader("X-User-ID") String userId,
            @PathVariable String templateName,
            @RequestParam String to,
            @RequestBody Object templateData) {
        log.info("Sending template SMS: {} to: {}", templateName, to);
        NotificationResponse response = smsService.sendTemplateSMS(userId, to, templateName, templateData);
        return ResponseEntity.ok(ApiResponse.success(response, "Template SMS sent"));
    }

    @PostMapping("/otp")
    public ResponseEntity<ApiResponse<Void>> sendOTP(
            @RequestParam String phoneNumber,
            @RequestParam String otp) {
        log.info("Sending OTP to: {}", phoneNumber);
        smsService.sendOTP(phoneNumber, otp);
        return ResponseEntity.ok(ApiResponse.success(null, "OTP sent"));
    }

    @GetMapping("/logs/{notificationId}")
    public ResponseEntity<ApiResponse<SMSLog>> getSMSLog(
            @PathVariable String notificationId) {
        log.info("Getting SMS log for notification: {}", notificationId);
        SMSLog log = smsService.getSMSLog(notificationId);
        return ResponseEntity.ok(ApiResponse.success(log));
    }
}
