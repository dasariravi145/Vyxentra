package com.vyxentra.vehicle.exception;


import com.vyxentra.vehicle.exceptions.BusinessException;
import com.vyxentra.vehicle.exceptions.ErrorCode;
import lombok.Getter;

@Getter
public class LocationException extends BusinessException {

    private final String entityId;
    private final String entityType;
    private final Double latitude;
    private final Double longitude;

    public LocationException(String entityId, String entityType, ErrorCode errorCode) {
        super(errorCode);
        this.entityId = entityId;
        this.entityType = entityType;
        this.latitude = null;
        this.longitude = null;
    }

    public LocationException(String entityId, String entityType, ErrorCode errorCode, Double latitude, Double longitude) {
        super(errorCode, String.valueOf(latitude), String.valueOf(longitude));
        this.entityId = entityId;
        this.entityType = entityType;
        this.latitude = latitude;
        this.longitude = longitude;
    }

    public LocationException(String message) {
        super(ErrorCode.valueOf(message));
        this.entityId = null;
        this.entityType = null;
        this.latitude = null;
        this.longitude = null;
    }
}
