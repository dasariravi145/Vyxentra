package com.vyxentra.vehicle.exception;

import com.vyxentra.vehicle.exceptions.BusinessException;
import com.vyxentra.vehicle.exceptions.ErrorCode;
import lombok.Getter;

@Getter
public class RateLimitException extends BusinessException {

    private final String userId;
    private final String notificationType;
    private final int retryAfterSeconds;

    public RateLimitException(String userId, String notificationType, int retryAfterSeconds) {
        super(ErrorCode.RATE_LIMIT_EXCEEDED,
                "Rate limit exceeded for user: " + userId + ". Retry after " + retryAfterSeconds + " seconds");
        this.userId = userId;
        this.notificationType = notificationType;
        this.retryAfterSeconds = retryAfterSeconds;
    }

    public RateLimitException(String userId, String notificationType, String message, int retryAfterSeconds) {
        super(ErrorCode.valueOf(message));
        this.userId = userId;
        this.notificationType = notificationType;
        this.retryAfterSeconds = retryAfterSeconds;
    }
}