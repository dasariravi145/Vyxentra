package com.vyxentra.vehicle.exception;


import com.vyxentra.vehicle.exceptions.BusinessException;
import com.vyxentra.vehicle.exceptions.ErrorCode;
import lombok.Getter;

@Getter
public class EmergencyException extends BusinessException {

    private final String emergencyId;
    private final String currentStatus;

    public EmergencyException(String emergencyId, ErrorCode errorCode) {
        super(errorCode);
        this.emergencyId = emergencyId;
        this.currentStatus = null;
    }

    public EmergencyException(String emergencyId, ErrorCode errorCode, String currentStatus) {
        super(errorCode, currentStatus);
        this.emergencyId = emergencyId;
        this.currentStatus = currentStatus;
    }

    public EmergencyException(String emergencyId, String message) {
        super(ErrorCode.valueOf(message));
        this.emergencyId = emergencyId;
        this.currentStatus = null;
    }
}
