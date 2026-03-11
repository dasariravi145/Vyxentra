package com.vyxentra.vehicle.exception;


import com.vyxentra.vehicle.exceptions.BusinessException;
import com.vyxentra.vehicle.exceptions.ErrorCode;
import lombok.Getter;

@Getter
public class DeviceException extends BusinessException {

    private final String deviceToken;
    private final String userId;

    public DeviceException(String deviceToken, String userId, ErrorCode errorCode) {
        super(errorCode);
        this.deviceToken = deviceToken;
        this.userId = userId;
    }

    public DeviceException(String deviceToken, String userId, ErrorCode errorCode, String... args) {
        super(errorCode, args);
        this.deviceToken = deviceToken;
        this.userId = userId;
    }

    public DeviceException(String deviceToken, String userId, String message) {
        super(ErrorCode.valueOf(message));
        this.deviceToken = deviceToken;
        this.userId = userId;
    }
}
