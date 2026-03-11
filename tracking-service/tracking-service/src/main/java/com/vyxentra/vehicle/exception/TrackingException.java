package com.vyxentra.vehicle.exception;


import com.vyxentra.vehicle.exceptions.BusinessException;
import com.vyxentra.vehicle.exceptions.ErrorCode;
import lombok.Getter;

@Getter
public class TrackingException extends BusinessException {

    private final String trackingSessionId;
    private final String bookingId;

    public TrackingException(String trackingSessionId, ErrorCode errorCode) {
        super(errorCode);
        this.trackingSessionId = trackingSessionId;
        this.bookingId = null;
    }

    public TrackingException(String trackingSessionId, ErrorCode errorCode, String... args) {
        super(errorCode, args);
        this.trackingSessionId = trackingSessionId;
        this.bookingId = null;
    }

    public TrackingException(String trackingSessionId, String bookingId, ErrorCode errorCode) {
        super(errorCode);
        this.trackingSessionId = trackingSessionId;
        this.bookingId = bookingId;
    }

    public TrackingException(String trackingSessionId, String bookingId, ErrorCode errorCode, String... args) {
        super(errorCode, args);
        this.trackingSessionId = trackingSessionId;
        this.bookingId = bookingId;
    }

    public TrackingException(String message) {
        super(ErrorCode.valueOf(message));
        this.trackingSessionId = null;
        this.bookingId = null;
    }
}
