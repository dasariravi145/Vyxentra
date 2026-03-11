package com.vyxentra.vehicle.service;


import com.vyxentra.vehicle.constants.ServiceConstants;
import com.vyxentra.vehicle.exceptions.BusinessException;
import com.vyxentra.vehicle.exceptions.ErrorCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
@RequiredArgsConstructor
public class OtpService {

    private final RedisTemplate<String, Object> redisTemplate;
    private final NotificationService notificationService;

    @Value("${otp.expiry-seconds:300}")
    private int otpExpirySeconds;

    @Value("${otp.length:6}")
    private int otpLength;

    @Value("${otp.max-attempts:3}")
    private int maxAttempts;

    private final SecureRandom secureRandom = new SecureRandom();

    public String generateAndSendOtp(String phoneNumber) {
        // Generate OTP
        String otp = generateOtp();

        // Store in Redis with expiry
        String otpKey = ServiceConstants.OTP_CACHE_PREFIX + phoneNumber;
        String attemptsKey = ServiceConstants.OTP_CACHE_PREFIX + "attempts:" + phoneNumber;

        redisTemplate.opsForValue().set(otpKey, otp, otpExpirySeconds, TimeUnit.SECONDS);
        redisTemplate.opsForValue().set(attemptsKey, "0", otpExpirySeconds, TimeUnit.SECONDS);

        // In production, send via SMS
        // For development, log the OTP
        log.info("OTP for {}: {}", phoneNumber, otp);

        // Send notification
        notificationService.sendOtp(phoneNumber, otp);

        return otp;
    }

    public boolean verifyOtp(String phoneNumber, String otp) {
        String otpKey = ServiceConstants.OTP_CACHE_PREFIX + phoneNumber;
        String attemptsKey = ServiceConstants.OTP_CACHE_PREFIX + "attempts:" + phoneNumber;

        String storedOtp = (String) redisTemplate.opsForValue().get(otpKey);

        if (storedOtp == null) {
            throw new BusinessException(ErrorCode.OTP_EXPIRED);
        }

        // Check attempts
        Integer attempts = (Integer) redisTemplate.opsForValue().get(attemptsKey);
        if (attempts == null) {
            attempts = 0;
        }

        if (attempts >= maxAttempts) {
            // Delete OTP after max attempts
            redisTemplate.delete(otpKey);
            redisTemplate.delete(attemptsKey);
            throw new BusinessException(ErrorCode.OTP_MAX_ATTEMPTS);
        }

        if (!storedOtp.equals(otp)) {
            // Increment attempts
            redisTemplate.opsForValue().increment(attemptsKey);
            return false;
        }

        // Clear OTP on successful verification
        redisTemplate.delete(otpKey);
        redisTemplate.delete(attemptsKey);

        return true;
    }

    private String generateOtp() {
        StringBuilder otp = new StringBuilder();
        for (int i = 0; i < otpLength; i++) {
            otp.append(secureRandom.nextInt(10));
        }
        return otp.toString();
    }
}