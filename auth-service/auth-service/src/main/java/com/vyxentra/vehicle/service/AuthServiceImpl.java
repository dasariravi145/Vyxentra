package com.vyxentra.vehicle.service;

import com.vyxentra.vehicle.client.UserServiceClient;
import com.vyxentra.vehicle.constants.ServiceConstants;
import com.vyxentra.vehicle.dto.request.*;
import com.vyxentra.vehicle.dto.response.AuthResponse;
import com.vyxentra.vehicle.dto.response.TokenResponse;
import com.vyxentra.vehicle.entity.RefreshToken;
import com.vyxentra.vehicle.entity.User;
import com.vyxentra.vehicle.enums.Role;
import com.vyxentra.vehicle.exceptions.BusinessException;
import com.vyxentra.vehicle.exceptions.ErrorCode;
import com.vyxentra.vehicle.exceptions.ResourceNotFoundException;
import com.vyxentra.vehicle.repository.RefreshTokenRepository;
import com.vyxentra.vehicle.repository.UserRepository;
import com.vyxentra.vehicle.utils.CorrelationIdUtil;
import com.vyxentra.vehicle.utils.ValidationUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final RedisTemplate<String, Object> redisTemplate;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final OtpService otpService;
    private final AuthenticationManager authenticationManager;
    private final UserServiceClient userServiceClient;

    @Value("${otp.expiry-seconds:300}")
    private int otpExpirySeconds;

    @Value("${otp.max-attempts:3}")
    private int maxOtpAttempts;

    @Value("${jwt.refresh-expiration:604800000}")
    private long refreshExpirationMs;

    @Override
    @Transactional
    public AuthResponse register(RegisterRequest request) {
        log.info("Registering new user with phone: {}", request.getPhoneNumber());

        // Validate phone and email uniqueness
        if (userRepository.existsByPhoneNumber(request.getPhoneNumber())) {
            throw new BusinessException(ErrorCode.USER_ALREADY_EXISTS, "Phone number already registered");
        }

        if (userRepository.existsByEmail(request.getEmail())) {
            throw new BusinessException(ErrorCode.USER_ALREADY_EXISTS, "Email already registered");
        }

        // Validate provider rules
        if (request.getRole() == Role.PROVIDER) {
            validateProviderRegistration(request);
        }

        // Create user
        User user = User.builder()
                .phoneNumber(request.getPhoneNumber())
                .email(request.getEmail())
                .fullName(request.getFullName())
                .role(request.getRole())
                .enabled(true)
                .accountNonLocked(true)
                .failedAttempts(0)
                .build();

        // Set password if provided (for email/password login)
        if (request.getPassword() != null && !request.getPassword().isEmpty()) {
            user.setPassword(passwordEncoder.encode(request.getPassword()));
        }

        // Set provider fields
        if (request.getRole() == Role.PROVIDER) {
            user.setBusinessName(request.getBusinessName());
            user.setGstNumber(request.getGstNumber());
            user.setAddress(request.getAddress());
            user.setLatitude(request.getLatitude());
            user.setLongitude(request.getLongitude());
            user.setSupportsBike(request.getSupportsBike());
            user.setSupportsCar(request.getSupportsCar());
            user.setProviderStatus("PENDING_APPROVAL"); // Admin approval required
        }

        user = userRepository.save(user);

        // Generate and send OTP
        String otp = otpService.generateAndSendOtp(request.getPhoneNumber());

        log.info("User registered successfully with ID: {}", user.getId());

        return AuthResponse.builder()
                .userId(user.getId())
                .phoneNumber(user.getPhoneNumber())
                .email(user.getEmail())
                .fullName(user.getFullName())
                .role(user.getRole())
                .otpSent(true)
                .message("Registration successful. Please verify OTP.")
                .businessName(user.getBusinessName())
                .providerStatus(user.getProviderStatus() != null ?
                        AuthResponse.ProviderStatus.valueOf(user.getProviderStatus()) : null)
                .build();
    }

    @Override
    public AuthResponse login(LoginRequest request) {
        log.info("Processing login for phone: {}", request.getPhoneNumber());

        User user = userRepository.findByPhoneNumber(request.getPhoneNumber())
                .orElseThrow(() -> new ResourceNotFoundException("User", "phone", request.getPhoneNumber()));

        // Check if account is locked
        if (!user.isAccountNonLocked()) {
            if (user.getLockTime() != null &&
                    user.getLockTime()
                            .plus(30, java.time.temporal.ChronoUnit.MINUTES)
                            .isBefore(Instant.now())) {

                // Unlock account after 30 minutes
                userRepository.resetFailedAttempts(user.getPhoneNumber());

            } else {
                throw new BusinessException(
                        ErrorCode.UNAUTHORIZED,
                        "Account is locked. Try again after 30 minutes."
                );
            }
        }

        // Check provider status
        if (user.getRole() == Role.PROVIDER) {
            if ("SUSPENDED".equals(user.getProviderStatus())) {
                throw new BusinessException(ErrorCode.PROVIDER_SUSPENDED);
            }
            if (!"ACTIVE".equals(user.getProviderStatus())) {
                throw new BusinessException(ErrorCode.PROVIDER_NOT_APPROVED);
            }
        }

        // Verify OTP
        boolean isValid = otpService.verifyOtp(request.getPhoneNumber(), request.getOtp());

        if (!isValid) {
            // Increment failed attempts
            user.setFailedAttempts(user.getFailedAttempts() + 1);

            if (user.getFailedAttempts() >= maxOtpAttempts) {
                userRepository.lockUser(Instant.now(), user.getPhoneNumber());
                log.warn("User account locked due to multiple failed attempts: {}", user.getPhoneNumber());
            } else {
                userRepository.save(user);
            }

            throw new BusinessException(ErrorCode.OTP_INVALID);
        }

        // Reset failed attempts on successful login
        userRepository.resetFailedAttempts(user.getPhoneNumber());

        // Generate tokens
        TokenResponse tokens = generateTokens(user);

        // Update last login
        userRepository.updateLastLogin(user.getId(), Instant.now(),
                CorrelationIdUtil.getCurrentCorrelationId());

        log.info("User logged in successfully: {}", user.getId());

        return AuthResponse.builder()
                .userId(user.getId())
                .phoneNumber(user.getPhoneNumber())
                .email(user.getEmail())
                .fullName(user.getFullName())
                .role(user.getRole())
                .tokens(tokens)
                .businessName(user.getBusinessName())
                .providerStatus(user.getProviderStatus() != null ?
                        AuthResponse.ProviderStatus.valueOf(user.getProviderStatus()) : null)
                .build();
    }

    @Override
    @Transactional
    public TokenResponse verifyOtp(VerifyOtpRequest request) {
        log.info("Verifying OTP for phone: {}", request.getPhoneNumber());

        User user = userRepository.findByPhoneNumber(request.getPhoneNumber())
                .orElseThrow(() -> new ResourceNotFoundException("User", "phone", request.getPhoneNumber()));

        boolean isValid = otpService.verifyOtp(request.getPhoneNumber(), request.getOtp());

        if (!isValid) {
            throw new BusinessException(ErrorCode.OTP_INVALID);
        }

        // Generate tokens
        TokenResponse tokens = generateTokens(user);

        // Enable user if not enabled
        if (!user.isEnabled()) {
            user.setEnabled(true);
            userRepository.save(user);
        }

        log.info("OTP verified successfully for user: {}", user.getId());

        return tokens;
    }

    @Override
    public void resendOtp(ResendOtpRequest request) {
        log.info("Resending OTP for phone: {}", request.getPhoneNumber());

        // Check cooldown
        String cooldownKey = ServiceConstants.OTP_CACHE_PREFIX + "cooldown:" + request.getPhoneNumber();
        Boolean hasCooldown = redisTemplate.hasKey(cooldownKey);

        if (Boolean.TRUE.equals(hasCooldown)) {
            throw new BusinessException(ErrorCode.OTP_MAX_ATTEMPTS, "Please wait before requesting another OTP");
        }

        otpService.generateAndSendOtp(request.getPhoneNumber());

        // Set cooldown
        redisTemplate.opsForValue().set(cooldownKey, "1", 60, TimeUnit.SECONDS);

        log.info("OTP resent successfully for phone: {}", request.getPhoneNumber());
    }

    @Override
    @Transactional
    public TokenResponse refreshToken(RefreshTokenRequest request) {
        log.info("Refreshing token");

        RefreshToken refreshToken = refreshTokenRepository.findByToken(request.getRefreshToken())
                .orElseThrow(() -> new BusinessException(ErrorCode.UNAUTHORIZED, "Invalid refresh token"));

        if (refreshToken.isRevoked() || refreshToken.getExpiryDate().isBefore(Instant.now())) {
            throw new BusinessException(ErrorCode.UNAUTHORIZED, "Refresh token expired");
        }

        User user = userRepository.findById(refreshToken.getUserId())
                .orElseThrow(() -> new ResourceNotFoundException("User", refreshToken.getUserId()));

        // Generate new tokens
        String newAccessToken = jwtService.generateToken(user);
        String newRefreshToken = jwtService.generateRefreshToken(user);

        // Revoke old refresh token
        refreshToken.setRevoked(true);
        refreshToken.setRevokedAt(Instant.now());
        refreshTokenRepository.save(refreshToken);

        // Save new refresh token
        saveRefreshToken(user.getId(), newRefreshToken);

        log.info("Token refreshed successfully for user: {}", user.getId());

        return TokenResponse.builder()
                .accessToken(newAccessToken)
                .refreshToken(newRefreshToken)
                .tokenType("Bearer")
                .expiresIn(jwtService.getExpirationMs())
                .userId(user.getId())
                .role(user.getRole().name())
                .suspended("SUSPENDED".equals(user.getProviderStatus()))
                .build();
    }

    @Override
    @Transactional
    public void logout(String userId) {
        log.info("Logging out user: {}", userId);

        // Revoke all refresh tokens
        refreshTokenRepository.revokeAllUserTokens(userId);

        // Clear security context
        SecurityContextHolder.clearContext();

        log.info("User logged out successfully: {}", userId);
    }

    @Override
    public void forgotPassword(ForgotPasswordRequest request) {
        log.info("Processing forgot password for phone: {}", request.getPhoneNumber());

        User user = userRepository.findByPhoneNumber(request.getPhoneNumber())
                .orElseThrow(() -> new ResourceNotFoundException("User", "phone", request.getPhoneNumber()));

        // Generate and send OTP
        otpService.generateAndSendOtp(request.getPhoneNumber());

        log.info("Password reset OTP sent for user: {}", user.getId());
    }

    @Override
    @Transactional
    public void resetPassword(ResetPasswordRequest request) {
        log.info("Resetting password for phone: {}", request.getPhoneNumber());

        User user = userRepository.findByPhoneNumber(request.getPhoneNumber())
                .orElseThrow(() -> new ResourceNotFoundException("User", "phone", request.getPhoneNumber()));

        // Verify OTP
        boolean isValid = otpService.verifyOtp(request.getPhoneNumber(), request.getOtp());

        if (!isValid) {
            throw new BusinessException(ErrorCode.OTP_INVALID);
        }

        // Update password
        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(user);

        // Revoke all existing tokens
        refreshTokenRepository.revokeAllUserTokens(user.getId());

        log.info("Password reset successfully for user: {}", user.getId());
    }

    @Override
    public boolean validateToken(String token) {
        try {
            String userId = jwtService.extractUserId(token);
            if (userId == null) {
                return false;
            }

            UserDetails userDetails =(UserDetails) userServiceClient.loadUserByUserId(userId);
            return jwtService.isTokenValid(token, userDetails);
        } catch (Exception e) {
            log.error("Token validation failed: {}", e.getMessage());
            return false;
        }
    }

    private TokenResponse generateTokens(User user) {
        String accessToken = jwtService.generateToken(user);
        String refreshToken = jwtService.generateRefreshToken(user);

        // Save refresh token
        saveRefreshToken(user.getId(), refreshToken);

        return TokenResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .tokenType("Bearer")
                .expiresIn(jwtService.getExpirationMs())
                .userId(user.getId())
                .role(user.getRole().name())
                .suspended("SUSPENDED".equals(user.getProviderStatus()))
                .build();
    }

    private void saveRefreshToken(String userId, String token) {
        RefreshToken refreshToken = RefreshToken.builder()
                .token(token)
                .userId(userId)
                .expiryDate(Instant.now().plusMillis(refreshExpirationMs))
                .revoked(false)
                .createdAt(Instant.now())
                .build();

        refreshTokenRepository.save(refreshToken);
    }

    private void validateProviderRegistration(RegisterRequest request) {
        if (request.getBusinessName() == null || request.getBusinessName().trim().isEmpty()) {
            throw new BusinessException(ErrorCode.VALIDATION_ERROR, "Business name is required for providers");
        }

        if (request.getGstNumber() == null || request.getGstNumber().trim().isEmpty()) {
            throw new BusinessException(ErrorCode.VALIDATION_ERROR, "GST number is required for providers");
        }

        if (request.getAddress() == null || request.getAddress().trim().isEmpty()) {
            throw new BusinessException(ErrorCode.VALIDATION_ERROR, "Address is required for providers");
        }

        ValidationUtil.validateCoordinates(request.getLatitude(), request.getLongitude());

        // Must support at least one vehicle type
        if (!Boolean.TRUE.equals(request.getSupportsBike()) &&
                !Boolean.TRUE.equals(request.getSupportsCar())) {
            throw new BusinessException(ErrorCode.PROVIDER_INVALID_VEHICLE_SUPPORT);
        }
    }
}