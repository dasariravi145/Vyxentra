package com.vyxentra.vehicle.event;


import com.fasterxml.jackson.annotation.JsonFormat;
import com.vyxentra.vehicle.events.BaseEvent;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.UUID;

/**
 * Event class for user-related events published by auth-service
 * Consumed by other services (notification, user, admin, etc.)
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserEvent extends BaseEvent {

    private String eventId;
    private String eventType;
    private String correlationId;

    // User details
    private String userId;
    private String phoneNumber;
    private String email;
    private String fullName;
    private String role;

    // Authentication details
    private String loginMethod; // OTP, PASSWORD, SOCIAL
    private String ipAddress;
    private String userAgent;
    private String deviceId;
    private Boolean isNewDevice;

    // OTP related
    private String otpType; // LOGIN, REGISTRATION, PASSWORD_RESET
    private Boolean otpVerified;
    private Integer otpAttempts;

    // Account status
    private Boolean isActive;
    private Boolean isLocked;
    private String lockReason;
    private Integer failedAttempts;
    private Instant lockedUntil;

    // Provider specific (if user is also a provider)
    private String providerId;
    private String businessName;
    private String providerStatus;

    // Token details
    private String tokenType; // ACCESS, REFRESH
    private Instant tokenIssuedAt;
    private Instant tokenExpiresAt;

    // Session details
    private String sessionId;
    private Boolean isNewSession;

    // Metadata
    private String source;
    private String serviceVersion;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Instant timestamp;

    // ==================== Event Type Constants ====================

    public static final String TYPE_USER_REGISTERED = "USER_REGISTERED";
    public static final String TYPE_USER_LOGGED_IN = "USER_LOGGED_IN";
    public static final String TYPE_USER_LOGGED_OUT = "USER_LOGGED_OUT";
    public static final String TYPE_USER_UPDATED = "USER_UPDATED";
    public static final String TYPE_USER_DEACTIVATED = "USER_DEACTIVATED";
    public static final String TYPE_USER_ACTIVATED = "USER_ACTIVATED";
    public static final String TYPE_USER_LOCKED = "USER_LOCKED";
    public static final String TYPE_USER_UNLOCKED = "USER_UNLOCKED";

    public static final String TYPE_OTP_SENT = "OTP_SENT";
    public static final String TYPE_OTP_VERIFIED = "OTP_VERIFIED";
    public static final String TYPE_OTP_FAILED = "OTP_FAILED";
    public static final String TYPE_OTP_MAX_ATTEMPTS = "OTP_MAX_ATTEMPTS";

    public static final String TYPE_PASSWORD_RESET_REQUESTED = "PASSWORD_RESET_REQUESTED";
    public static final String TYPE_PASSWORD_RESET_COMPLETED = "PASSWORD_RESET_COMPLETED";
    public static final String TYPE_PASSWORD_CHANGED = "PASSWORD_CHANGED";

    public static final String TYPE_TOKEN_REFRESHED = "TOKEN_REFRESHED";
    public static final String TYPE_TOKEN_REVOKED = "TOKEN_REVOKED";

    public static final String TYPE_PROVIDER_REGISTERED = "PROVIDER_REGISTERED";
    public static final String TYPE_PROVIDER_APPROVED = "PROVIDER_APPROVED";
    public static final String TYPE_PROVIDER_REJECTED = "PROVIDER_REJECTED";

    // ==================== Builder with defaults ====================

    public static class UserEventBuilder {
        private String eventId = UUID.randomUUID().toString();
        private Instant timestamp = Instant.now();
        private String source = "auth-service";
        private String serviceVersion = "1.0.0";

        public UserEventBuilder withUserDetails(String userId, String phoneNumber,
                                                String email, String fullName, String role) {
            this.userId = userId;
            this.phoneNumber = phoneNumber;
            this.email = email;
            this.fullName = fullName;
            this.role = role;
            return this;
        }

        public UserEventBuilder withLoginDetails(String loginMethod, String ipAddress,
                                                 String userAgent, String deviceId) {
            this.loginMethod = loginMethod;
            this.ipAddress = ipAddress;
            this.userAgent = userAgent;
            this.deviceId = deviceId;
            return this;
        }

        public UserEventBuilder withOtpDetails(String otpType, Boolean otpVerified, Integer otpAttempts) {
            this.otpType = otpType;
            this.otpVerified = otpVerified;
            this.otpAttempts = otpAttempts;
            return this;
        }

        public UserEventBuilder withAccountStatus(Boolean isActive, Boolean isLocked,
                                                  Integer failedAttempts, String lockReason) {
            this.isActive = isActive;
            this.isLocked = isLocked;
            this.failedAttempts = failedAttempts;
            this.lockReason = lockReason;
            return this;
        }

        public UserEventBuilder withTokenDetails(String tokenType, Instant issuedAt, Instant expiresAt) {
            this.tokenType = tokenType;
            this.tokenIssuedAt = issuedAt;
            this.tokenExpiresAt = expiresAt;
            return this;
        }

        public UserEventBuilder withSessionDetails(String sessionId, Boolean isNewSession) {
            this.sessionId = sessionId;
            this.isNewSession = isNewSession;
            return this;
        }

        public UserEventBuilder withProviderDetails(String providerId, String businessName, String providerStatus) {
            this.providerId = providerId;
            this.businessName = businessName;
            this.providerStatus = providerStatus;
            return this;
        }

        public UserEvent build() {
            UserEvent event = new UserEvent();
            event.eventId = this.eventId;
            event.timestamp = this.timestamp;
            event.source = this.source;
            event.serviceVersion = this.serviceVersion;
            event.eventType = this.eventType;
            event.correlationId = this.correlationId;
            event.userId = this.userId;
            event.phoneNumber = this.phoneNumber;
            event.email = this.email;
            event.fullName = this.fullName;
            event.role = this.role;
            event.loginMethod = this.loginMethod;
            event.ipAddress = this.ipAddress;
            event.userAgent = this.userAgent;
            event.deviceId = this.deviceId;
            event.isNewDevice = this.isNewDevice;
            event.otpType = this.otpType;
            event.otpVerified = this.otpVerified;
            event.otpAttempts = this.otpAttempts;
            event.isActive = this.isActive;
            event.isLocked = this.isLocked;
            event.lockReason = this.lockReason;
            event.failedAttempts = this.failedAttempts;
            event.lockedUntil = this.lockedUntil;
            event.providerId = this.providerId;
            event.businessName = this.businessName;
            event.providerStatus = this.providerStatus;
            event.tokenType = this.tokenType;
            event.tokenIssuedAt = this.tokenIssuedAt;
            event.tokenExpiresAt = this.tokenExpiresAt;
            event.sessionId = this.sessionId;
            event.isNewSession = this.isNewSession;
            return event;
        }
    }
}