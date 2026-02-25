package com.vyxentra.vehicle.events;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import lombok.*;
import lombok.experimental.SuperBuilder;

import java.io.Serializable;
import java.time.Instant;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.PROPERTY, property = "eventType")
@JsonSubTypes({
        @JsonSubTypes.Type(value = BookingCreatedEvent.class, name = "BOOKING_CREATED"),
        @JsonSubTypes.Type(value = EmergencyTriggeredEvent.class, name = "EMERGENCY_TRIGGERED"),
        @JsonSubTypes.Type(value = DamageReportedEvent.class, name = "DAMAGE_REPORTED"),
        @JsonSubTypes.Type(value = DamageApprovedEvent.class, name = "DAMAGE_APPROVED"),
        @JsonSubTypes.Type(value = DamageRejectedEvent.class, name = "DAMAGE_REJECTED"),
        @JsonSubTypes.Type(value = RepairDelayedEvent.class, name = "REPAIR_DELAYED"),
        @JsonSubTypes.Type(value = ServiceStartedEvent.class, name = "SERVICE_STARTED"),
        @JsonSubTypes.Type(value = ServiceCompletedEvent.class, name = "SERVICE_COMPLETED"),
        @JsonSubTypes.Type(value = PaymentSuccessEvent.class, name = "PAYMENT_SUCCESS"),
        @JsonSubTypes.Type(value = ProviderApprovedEvent.class, name = "PROVIDER_APPROVED")
})
@JsonInclude(JsonInclude.Include.NON_NULL)
@SuperBuilder
public abstract class BaseEvent implements Serializable {

    private String eventId;
    private String eventType;
    private String correlationId;
    private String causationId;
    private String userId;
    private String serviceId;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", timezone = "UTC")
    private Instant timestamp;

    private String source;
    private String version = "1.0";

    public void generateEventId() {
        this.eventId = UUID.randomUUID().toString();
    }

    public void setTimestamps() {
        if (this.timestamp == null) {
            this.timestamp = Instant.now();
        }
        if (this.correlationId == null) {
            this.correlationId = UUID.randomUUID().toString();
        }
    }
}
