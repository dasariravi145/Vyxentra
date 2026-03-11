package com.vyxentra.vehicle.exception;


import com.vyxentra.vehicle.exceptions.ErrorCode;
import lombok.Getter;

@Getter
public class SMSException extends NotificationException {

    private final String phoneNumber;

    public SMSException(String notificationId, String phoneNumber, ErrorCode errorCode) {
        super(notificationId, "SMS", errorCode);
        this.phoneNumber = phoneNumber;
    }

    public SMSException(String notificationId, String phoneNumber, ErrorCode errorCode, String... args) {
        super(notificationId, "SMS", errorCode, args);
        this.phoneNumber = phoneNumber;
    }

    public SMSException(String phoneNumber, String message) {
        super(message);
        this.phoneNumber = phoneNumber;
    }

    public SMSException(String phoneNumber, Throwable cause) {
        super("Failed to send SMS to: " + phoneNumber);
        this.phoneNumber = phoneNumber;
        this.initCause(cause);
    }
}
