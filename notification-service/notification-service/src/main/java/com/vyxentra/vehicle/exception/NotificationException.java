package com.vyxentra.vehicle.exception;



import com.vyxentra.vehicle.exceptions.BusinessException;
import com.vyxentra.vehicle.exceptions.ErrorCode;
import lombok.Getter;

@Getter
public class NotificationException extends BusinessException {

    private final String notificationId;
    private final String notificationType;

    public NotificationException(String notificationId, ErrorCode errorCode) {
        super(errorCode);
        this.notificationId = notificationId;
        this.notificationType = null;
    }

    public NotificationException(String notificationId, ErrorCode errorCode, String... args) {
        super(errorCode, args);
        this.notificationId = notificationId;
        this.notificationType = null;
    }

    public NotificationException(String notificationId, String notificationType, ErrorCode errorCode) {
        super(errorCode);
        this.notificationId = notificationId;
        this.notificationType = notificationType;
    }

    public NotificationException(String notificationId, String notificationType, ErrorCode errorCode, String... args) {
        super(errorCode, args);
        this.notificationId = notificationId;
        this.notificationType = notificationType;
    }

    public NotificationException(String message) {
        super(ErrorCode.valueOf(message));
        this.notificationId = null;
        this.notificationType = null;
    }
}