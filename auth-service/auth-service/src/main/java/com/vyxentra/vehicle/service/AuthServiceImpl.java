package com.vyxentra.vehicle.service;

import com.vyxentra.vehicle.client.UserServiceClient;
import com.vyxentra.vehicle.constants.ErrorCodes;
import com.vyxentra.vehicle.dto.request.LoginRequest;
import com.vyxentra.vehicle.dto.request.OtpVerificationRequest;
import com.vyxentra.vehicle.dto.request.UserRegistrationRequest;
import com.vyxentra.vehicle.dto.response.JwtResponse;
import com.vyxentra.vehicle.dto.response.UserResponse;
import com.vyxentra.vehicle.enums.UserRole;
import com.vyxentra.vehicle.enums.UserStatus;
import com.vyxentra.vehicle.event.OtpSentEvent;
import com.vyxentra.vehicle.events.BaseEvent;
import com.vyxentra.vehicle.exceptions.BusinessException;
import com.vyxentra.vehicle.exceptions.UnauthorizedException;
import com.vyxentra.vehicle.util.IdGenerator;
import com.vyxentra.vehicle.util.ValidationUtil;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import lombok.Builder;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.Date;
import java.util.Set;

@Slf4j
@Service
@Builder
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final OtpService otpService;
    private final JwtService jwtService;
    private final TokenBlacklistService tokenBlacklistService;
    private final UserServiceClient userServiceClient;
    private final KafkaTemplate<String, Object> kafkaTemplate;

    @Override
    public void sendOtp(LoginRequest request) {
        ValidationUtil.validateMobileNumber(request.getMobileNumber());

        // Check if user exists and is active
        try {
            UserResponse user = userServiceClient.getUserByMobile(
                    request.getCountryCode(), request.getMobileNumber());

            if (user.getStatus() != UserStatus.ACTIVE) {
                log.warn("Inactive user attempted login: {}", request.getMobileNumber());
                throw new BusinessException("Account is not active", ErrorCodes.USER_INACTIVE);
            }
        } catch (Exception e) {
            // User not found - will be created during registration
            log.info("New user registration flow for: {}", request.getMobileNumber());
        }

        // Generate and store OTP
        String otp = otpService.generateAndStoreOtp(request.getMobileNumber());

        // In production, integrate with SMS service
        log.info("OTP for {}: {}", request.getMobileNumber(), otp);

        // Send notification event
        sendNotificationEvent(request.getMobileNumber(), otp);
    }

    @Override
    @Transactional
    @CircuitBreaker(name = "userService", fallbackMethod = "verifyOtpFallback")
    @Retry(name = "userService")
    public JwtResponse verifyOtp(OtpVerificationRequest request) {
        ValidationUtil.validateMobileNumber(request.getMobileNumber());
        ValidationUtil.validateOtp(request.getOtp());

        // Verify OTP
        boolean isValid = otpService.verifyOtp(request.getMobileNumber(), request.getOtp());
        if (!isValid) {
            throw new BusinessException("Invalid OTP", ErrorCodes.AUTH_INVALID_OTP);
        }

        // Get or create user
        UserResponse user;
        try {
            user = userServiceClient.getUserByMobile(request.getCountryCode(), request.getMobileNumber());
        } catch (Exception e) {
            // Create new user
            user = userServiceClient.createUser(UserRegistrationRequest.builder()
                    .mobileNumber(request.getMobileNumber())
                    .countryCode(request.getCountryCode())
                    .build());
        }

        // Check user status
        if (user.getStatus() != UserStatus.ACTIVE) {
            throw new BusinessException("Account is not active", ErrorCodes.USER_INACTIVE);
        }

        // Generate tokens
        Set<UserRole> roles = user.getRoles();
        String token = jwtService.generateToken(user.getId(), user.getMobileNumber(), roles);
        String refreshToken = jwtService.generateRefreshToken(user.getId(), user.getMobileNumber());

        // Update device info
        if (request.getDeviceId() != null) {
            userServiceClient.updateDeviceInfo(user.getId(), request.getDeviceId(), request.getFcmToken());
        }

        // Clear OTP after successful verification
        otpService.clearOtp(request.getMobileNumber());

        log.info("User authenticated successfully: {}", user.getId());

        return JwtResponse.builder()
                .token(token)
                .refreshToken(refreshToken)
                .expiresIn(jwtService.getExpirationTime())
                .userId(user.getId())
                .mobileNumber(user.getMobileNumber())
                .roles(roles)
                .profileComplete(user.isEmailVerified()) // Simplified check
                .build();
    }

    @Override
    @Transactional
    public JwtResponse refreshToken(String refreshToken) {
        if (!jwtService.validateToken(refreshToken)) {
            throw new UnauthorizedException("Invalid refresh token");
        }

        String userId = jwtService.extractUserId(refreshToken);
        String mobileNumber = jwtService.extractMobileNumber(refreshToken);

        // Check if token is blacklisted
        if (tokenBlacklistService.isBlacklisted(refreshToken)) {
            throw new UnauthorizedException("Token has been revoked");
        }

        // Get user to verify roles haven't changed
        UserResponse user = userServiceClient.getUserById(userId);

        // Generate new tokens
        String newToken = jwtService.generateToken(userId, mobileNumber, user.getRoles());
        String newRefreshToken = jwtService.generateRefreshToken(userId, mobileNumber);

        // Blacklist old refresh token
        tokenBlacklistService.blacklistToken(refreshToken, jwtService.getExpirationDate(refreshToken));

        log.info("Token refreshed for user: {}", userId);

        return JwtResponse.builder()
                .token(newToken)
                .refreshToken(newRefreshToken)
                .expiresIn(jwtService.getExpirationTime())
                .userId(userId)
                .mobileNumber(mobileNumber)
                .roles(user.getRoles())
                .profileComplete(user.isEmailVerified())
                .build();
    }

    @Override
    @Transactional
    public void logout(String token) {
        if (token != null && !token.isEmpty()) {
            Date expiration = jwtService.getExpirationDate(token);
            tokenBlacklistService.blacklistToken(token, expiration);

            String userId = jwtService.extractUserId(token);
            log.info("User logged out: {}", userId);
        }
    }

    @Override
    public boolean validateToken(String token) {
        if (token == null || token.isEmpty()) {
            return false;
        }

        if (tokenBlacklistService.isBlacklisted(token)) {
            return false;
        }

        return jwtService.validateToken(token);
    }

    private void sendNotificationEvent(String mobileNumber, String otp) {
        try {
            OtpSentEvent event = OtpSentEvent.builder()
                    .eventId(IdGenerator.generateEventId())
                    .eventType("OTP_SENT")
                    .timestamp(Instant.now())
                    .build();

            kafkaTemplate.send("notification-events", mobileNumber, event);
        } catch (Exception e) {
            log.error("Failed to send notification event", e);
        }
    }

    private JwtResponse verifyOtpFallback(OtpVerificationRequest request, Exception e) {
        log.error("Fallback for verifyOtp: {}", e.getMessage());
        throw new BusinessException("Authentication service is temporarily unavailable. Please try again later.");
    }
}

