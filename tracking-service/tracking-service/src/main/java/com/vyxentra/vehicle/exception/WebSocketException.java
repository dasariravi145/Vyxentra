package com.vyxentra.vehicle.exception;


import com.vyxentra.vehicle.exceptions.BusinessException;
import com.vyxentra.vehicle.exceptions.ErrorCode;
import lombok.Getter;

@Getter
public class WebSocketException extends BusinessException {

    private final String sessionId;
    private final String userId;

    public WebSocketException(String sessionId, ErrorCode errorCode) {
        super(errorCode);
        this.sessionId = sessionId;
        this.userId = null;
    }

    public WebSocketException(String sessionId, String userId, ErrorCode errorCode) {
        super(errorCode);
        this.sessionId = sessionId;
        this.userId = userId;
    }

    public WebSocketException(String sessionId, ErrorCode errorCode, String... args) {
        super(errorCode, args);
        this.sessionId = sessionId;
        this.userId = null;
    }

    public WebSocketException(String message) {
        super(ErrorCode.valueOf(message));
        this.sessionId = null;
        this.userId = null;
    }
}
