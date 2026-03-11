package com.vyxentra.vehicle.event;


import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.UUID;

/**
 * Event class for user-related events consumed by notification service
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserEvent {

    private String eventId;
    private String eventType;
    private String correlationId;

    // User details
    private String userId;
    private String email;
    private String phoneNumber;
    private String fullName;
    private String role;

    // Address related
    private String addressId;
    private String addressType;
    private String city;
    private String state;
    private String postalCode;

    // Vehicle related
    private String vehicleId;
    private String vehicleType;
    private String registrationNumber;

    // Provider specific
    private String providerId;
    private String businessName;
    private String providerStatus;

    // Preferences
    private Boolean smsNotifications;
    private Boolean emailNotifications;
    private Boolean pushNotifications;
    private String preferredLanguage;

    // Metadata
    private String source;
    private String ipAddress;
    private String userAgent;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Instant timestamp;

    /**
     * Builder with automatic event ID and timestamp generation
     */
    public static class UserEventBuilder {
        private String eventId = UUID.randomUUID().toString();
        private Instant timestamp = Instant.now();

        public UserEventBuilder eventType(String eventType) {
            this.eventType = eventType;
            return this;
        }

        public UserEventBuilder correlationId(String correlationId) {
            this.correlationId = correlationId;
            return this;
        }

        public UserEventBuilder userId(String userId) {
            this.userId = userId;
            return this;
        }

        public UserEventBuilder email(String email) {
            this.email = email;
            return this;
        }

        public UserEventBuilder phoneNumber(String phoneNumber) {
            this.phoneNumber = phoneNumber;
            return this;
        }

        public UserEventBuilder fullName(String fullName) {
            this.fullName = fullName;
            return this;
        }

        public UserEventBuilder role(String role) {
            this.role = role;
            return this;
        }

        public UserEventBuilder addressId(String addressId) {
            this.addressId = addressId;
            return this;
        }

        public UserEventBuilder addressType(String addressType) {
            this.addressType = addressType;
            return this;
        }

        public UserEventBuilder city(String city) {
            this.city = city;
            return this;
        }

        public UserEventBuilder state(String state) {
            this.state = state;
            return this;
        }

        public UserEventBuilder postalCode(String postalCode) {
            this.postalCode = postalCode;
            return this;
        }

        public UserEventBuilder vehicleId(String vehicleId) {
            this.vehicleId = vehicleId;
            return this;
        }

        public UserEventBuilder vehicleType(String vehicleType) {
            this.vehicleType = vehicleType;
            return this;
        }

        public UserEventBuilder registrationNumber(String registrationNumber) {
            this.registrationNumber = registrationNumber;
            return this;
        }

        public UserEventBuilder providerId(String providerId) {
            this.providerId = providerId;
            return this;
        }

        public UserEventBuilder businessName(String businessName) {
            this.businessName = businessName;
            return this;
        }

        public UserEventBuilder providerStatus(String providerStatus) {
            this.providerStatus = providerStatus;
            return this;
        }

        public UserEventBuilder smsNotifications(Boolean smsNotifications) {
            this.smsNotifications = smsNotifications;
            return this;
        }

        public UserEventBuilder emailNotifications(Boolean emailNotifications) {
            this.emailNotifications = emailNotifications;
            return this;
        }

        public UserEventBuilder pushNotifications(Boolean pushNotifications) {
            this.pushNotifications = pushNotifications;
            return this;
        }

        public UserEventBuilder preferredLanguage(String preferredLanguage) {
            this.preferredLanguage = preferredLanguage;
            return this;
        }

        public UserEventBuilder source(String source) {
            this.source = source;
            return this;
        }

        public UserEventBuilder ipAddress(String ipAddress) {
            this.ipAddress = ipAddress;
            return this;
        }

        public UserEventBuilder userAgent(String userAgent) {
            this.userAgent = userAgent;
            return this;
        }

        public UserEvent build() {
            UserEvent event = new UserEvent();
            event.eventId = this.eventId;
            event.timestamp = this.timestamp;
            event.eventType = this.eventType;
            event.correlationId = this.correlationId;
            event.userId = this.userId;
            event.email = this.email;
            event.phoneNumber = this.phoneNumber;
            event.fullName = this.fullName;
            event.role = this.role;
            event.addressId = this.addressId;
            event.addressType = this.addressType;
            event.city = this.city;
            event.state = this.state;
            event.postalCode = this.postalCode;
            event.vehicleId = this.vehicleId;
            event.vehicleType = this.vehicleType;
            event.registrationNumber = this.registrationNumber;
            event.providerId = this.providerId;
            event.businessName = this.businessName;
            event.providerStatus = this.providerStatus;
            event.smsNotifications = this.smsNotifications;
            event.emailNotifications = this.emailNotifications;
            event.pushNotifications = this.pushNotifications;
            event.preferredLanguage = this.preferredLanguage;
            event.source = this.source;
            event.ipAddress = this.ipAddress;
            event.userAgent = this.userAgent;
            return event;
        }
    }

    // ==================== Event Type Constants ====================

    public static final String TYPE_USER_REGISTERED = "USER_REGISTERED";
    public static final String TYPE_USER_UPDATED = "USER_UPDATED";
    public static final String TYPE_USER_DEACTIVATED = "USER_DEACTIVATED";
    public static final String TYPE_USER_DELETED = "USER_DELETED";
    public static final String TYPE_USER_VERIFIED = "USER_VERIFIED";
    public static final String TYPE_USER_ROLE_CHANGED = "USER_ROLE_CHANGED";
    public static final String TYPE_USER_PREFERENCES_UPDATED = "USER_PREFERENCES_UPDATED";

    public static final String TYPE_ADDRESS_ADDED = "ADDRESS_ADDED";
    public static final String TYPE_ADDRESS_UPDATED = "ADDRESS_UPDATED";
    public static final String TYPE_ADDRESS_DELETED = "ADDRESS_DELETED";
    public static final String TYPE_ADDRESS_DEFAULT_CHANGED = "ADDRESS_DEFAULT_CHANGED";

    public static final String TYPE_VEHICLE_ADDED = "VEHICLE_ADDED";
    public static final String TYPE_VEHICLE_UPDATED = "VEHICLE_UPDATED";
    public static final String TYPE_VEHICLE_DELETED = "VEHICLE_DELETED";
    public static final String TYPE_VEHICLE_DEFAULT_CHANGED = "VEHICLE_DEFAULT_CHANGED";

    public static final String TYPE_PROVIDER_REGISTERED = "PROVIDER_REGISTERED";
    public static final String TYPE_PROVIDER_APPROVED = "PROVIDER_APPROVED";
    public static final String TYPE_PROVIDER_REJECTED = "PROVIDER_REJECTED";
    public static final String TYPE_PROVIDER_SUSPENDED = "PROVIDER_SUSPENDED";
    public static final String TYPE_PROVIDER_ACTIVATED = "PROVIDER_ACTIVATED";

    // ==================== Helper Methods ====================

    /**
     * Check if this is a user registration event
     */
    public boolean isUserRegistered() {
        return TYPE_USER_REGISTERED.equals(eventType);
    }

    /**
     * Check if this is a provider event
     */
    public boolean isProviderEvent() {
        return eventType != null && eventType.startsWith("PROVIDER_");
    }

    /**
     * Check if this is an address event
     */
    public boolean isAddressEvent() {
        return eventType != null && eventType.startsWith("ADDRESS_");
    }

    /**
     * Check if this is a vehicle event
     */
    public boolean isVehicleEvent() {
        return eventType != null && eventType.startsWith("VEHICLE_");
    }
}
