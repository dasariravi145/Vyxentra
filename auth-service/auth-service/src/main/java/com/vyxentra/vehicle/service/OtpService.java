package com.vyxentra.vehicle.service;

import com.vyxentra.vehicle.constants.ApiConstants;
import com.vyxentra.vehicle.constants.ErrorCodes;
import com.vyxentra.vehicle.constants.RedisKeys;
import com.vyxentra.vehicle.exceptions.BusinessException;
import com.vyxentra.vehicle.util.IdGenerator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;

@Slf4j
@Service
@RequiredArgsConstructor
public class OtpService {

    private final RedisTemplate<String, String> redisTemplate;

    public String generateAndStoreOtp(String mobileNumber) {
        // Check if user is blocked
        String blockKey = RedisKeys.getOtpBlockKey(mobileNumber);
        if (Boolean.TRUE.equals(redisTemplate.hasKey(blockKey))) {
            throw new BusinessException("Too many OTP attempts. Please try after some time.",
                    ErrorCodes.AUTH_BLOCKED);
        }

        // Check rate limiting
        String attemptKey = RedisKeys.getOtpAttemptKey(mobileNumber);
        String attemptsStr = redisTemplate.opsForValue().get(attemptKey);
        int attempts = attemptsStr != null ? Integer.parseInt(attemptsStr) : 0;

        if (attempts >= ApiConstants.OTP_MAX_ATTEMPTS) {
            // Block user for 30 minutes
            redisTemplate.opsForValue().set(blockKey, "blocked", Duration.ofMinutes(30));
            redisTemplate.delete(attemptKey);
            throw new BusinessException("Maximum OTP attempts exceeded. Please try after 30 minutes.",
                    ErrorCodes.AUTH_MAX_ATTEMPTS);
        }

        // Generate OTP
        String otp = IdGenerator.generateOTP();
        String otpKey = RedisKeys.getOtpKey(mobileNumber);

        // Store OTP with expiry
        redisTemplate.opsForValue().set(otpKey, otp, Duration.ofMinutes(ApiConstants.OTP_EXPIRY_MINUTES));

        // Increment attempts
        redisTemplate.opsForValue().increment(attemptKey);
        redisTemplate.expire(attemptKey, Duration.ofHours(1));

        log.debug("OTP generated for {}: {}", mobileNumber, otp);
        return otp;
    }

    public boolean verifyOtp(String mobileNumber, String otp) {
        String otpKey = RedisKeys.getOtpKey(mobileNumber);
        String storedOtp = redisTemplate.opsForValue().get(otpKey);

        if (storedOtp == null) {
            log.warn("OTP verification failed - no OTP found for: {}", mobileNumber);
            return false;
        }

        boolean isValid = storedOtp.equals(otp);

        if (isValid) {
            log.debug("OTP verified successfully for: {}", mobileNumber);
        } else {
            log.warn("Invalid OTP attempt for: {}", mobileNumber);
        }

        return isValid;
    }

    public void clearOtp(String mobileNumber) {
        String otpKey = RedisKeys.getOtpKey(mobileNumber);
        redisTemplate.delete(otpKey);

        String attemptKey = RedisKeys.getOtpAttemptKey(mobileNumber);
        redisTemplate.delete(attemptKey);

        log.debug("OTP data cleared for: {}", mobileNumber);
    }

    public boolean isBlocked(String mobileNumber) {
        String blockKey = RedisKeys.getOtpBlockKey(mobileNumber);
        return Boolean.TRUE.equals(redisTemplate.hasKey(blockKey));
    }
}

