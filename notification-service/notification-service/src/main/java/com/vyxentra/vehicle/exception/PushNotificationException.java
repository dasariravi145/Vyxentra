package com.vyxentra.vehicle.exception;


import com.vyxentra.vehicle.exceptions.ErrorCode;
import lombok.Getter;

@Getter
public class PushNotificationException extends NotificationException {

    private final String deviceToken;
    private final String platform;

    public PushNotificationException(String notificationId, String deviceToken, String platform, ErrorCode errorCode) {
        super(notificationId, "PUSH", errorCode);
        this.deviceToken = deviceToken;
        this.platform = platform;
    }

    public PushNotificationException(String notificationId, String deviceToken, String platform, ErrorCode errorCode, String... args) {
        super(notificationId, "PUSH", errorCode, args);
        this.deviceToken = deviceToken;
        this.platform = platform;
    }

    public PushNotificationException(String deviceToken, String platform, String message) {
        super(message);
        this.deviceToken = deviceToken;
        this.platform = platform;
    }

    public PushNotificationException(String deviceToken, String platform, Throwable cause) {
        super("Failed to send push notification to device: " + deviceToken + " on platform: " + platform);
        this.deviceToken = deviceToken;
        this.platform = platform;
        this.initCause(cause);
    }
}
