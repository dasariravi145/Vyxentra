package com.vyxentra.vehicle.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WebSocketMessage {

    private String type;
    private Object data;
    private LocalDateTime timestamp;
    private String messageId;
    private String correlationId;

    // Message types
    public static final String TYPE_LOCATION_UPDATE = "LOCATION_UPDATE";
    public static final String TYPE_ETA_UPDATE = "ETA_UPDATE";
    public static final String TYPE_TRACKING_PAUSED = "TRACKING_PAUSED";
    public static final String TYPE_TRACKING_RESUMED = "TRACKING_RESUMED";
    public static final String TYPE_TRACKING_ENDED = "TRACKING_ENDED";
    public static final String TYPE_SUBSCRIBE = "SUBSCRIBE";
    public static final String TYPE_UNSUBSCRIBE = "UNSUBSCRIBE";
    public static final String TYPE_HEARTBEAT = "HEARTBEAT";
    public static final String TYPE_HEARTBEAT_ACK = "HEARTBEAT_ACK";
    public static final String TYPE_ERROR = "ERROR";
    public static final String TYPE_CONNECTED = "CONNECTED";
}
