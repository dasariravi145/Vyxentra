package com.vyxentra.vehicle.event;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.vyxentra.vehicle.events.BaseEvent;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserEventProducer extends BaseEvent {

    private String eventId;
    private String eventType;
    private String correlationId;
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
    private String businessName;
    private String providerStatus;

    // Metadata
    private String source;
    private String ipAddress;

    @JsonFormat(shape = JsonFormat.Shape.STRING)
    private Instant timestamp;

    public static UserEventBuilder builder() {
        return new UserEventBuilder();
    }

    public static class UserEventBuilder {

        private String eventId = UUID.randomUUID().toString();
        private Instant timestamp = Instant.now();

        private String eventType;
        private String correlationId;
        private String userId;
        private String email;
        private String phoneNumber;
        private String fullName;
        private String role;
        private String addressId;
        private String addressType;
        private String city;
        private String state;
        private String postalCode;
        private String vehicleId;
        private String vehicleType;
        private String registrationNumber;
        private String businessName;
        private String providerStatus;
        private String source;
        private String ipAddress;

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

        public UserEventBuilder businessName(String businessName) {
            this.businessName = businessName;
            return this;
        }

        public UserEventBuilder providerStatus(String providerStatus) {
            this.providerStatus = providerStatus;
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

        public UserEventProducer build() {
            UserEventProducer event = new UserEventProducer();
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
            event.businessName = this.businessName;
            event.providerStatus = this.providerStatus;
            event.source = this.source;
            event.ipAddress = this.ipAddress;
            return event;
        }
    }
}