package com.vyxentra.vehicle.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class NotificationService {

    public void sendOtp(String phoneNumber, String otp) {
        // In production, integrate with SMS gateway
        // For development, just log
        log.info("Sending OTP {} to phone number: {}", otp, phoneNumber);

        // TODO: Implement actual SMS sending
        // SMSGateway.send(phoneNumber, "Your OTP is: " + otp);
    }

    public void sendWelcomeMessage(String phoneNumber, String name) {
        log.info("Sending welcome message to {}: {}", phoneNumber, name);
        // TODO: Implement welcome SMS/email
    }

    public void sendAccountLockedAlert(String phoneNumber) {
        log.info("Sending account locked alert to: {}", phoneNumber);
        // TODO: Implement account locked notification
    }
}
