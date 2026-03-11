package com.vyxentra.vehicle.exception;


import com.vyxentra.vehicle.exceptions.ErrorCode;
import lombok.Getter;

@Getter
public class EmailException extends NotificationException {

    private final String recipient;
    private final String subject;

    public EmailException(String notificationId, String recipient, String subject, ErrorCode errorCode) {
        super(notificationId, "EMAIL", errorCode);
        this.recipient = recipient;
        this.subject = subject;
    }

    public EmailException(String notificationId, String recipient, String subject, ErrorCode errorCode, String... args) {
        super(notificationId, "EMAIL", errorCode, args);
        this.recipient = recipient;
        this.subject = subject;
    }

    public EmailException(String recipient, String subject, String message) {
        super(message);
        this.recipient = recipient;
        this.subject = subject;
    }

    public EmailException(String recipient, String subject, Throwable cause) {
        super("Failed to send email to: " + recipient + " with subject: " + subject);
        this.recipient = recipient;
        this.subject = subject;
        this.initCause(cause);
    }
}
