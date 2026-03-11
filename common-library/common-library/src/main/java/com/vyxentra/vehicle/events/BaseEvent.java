package com.vyxentra.vehicle.events;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.time.Instant;
import java.util.UUID;

@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public abstract class BaseEvent {
    private String eventId;
    private String eventType;
    private Instant timestamp;
    private String correlationId;
    private String userId;
    private String source;

    public BaseEvent(String correlationId, String userId, String source) {
        this.eventId = UUID.randomUUID().toString();
        this.timestamp = Instant.now();
        this.correlationId = correlationId;
        this.userId = userId;
        this.source = source;
        this.eventType = this.getClass().getSimpleName();
    }
}