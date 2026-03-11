package com.vyxentra.vehicle.exception;


import com.vyxentra.vehicle.exceptions.BusinessException;
import com.vyxentra.vehicle.exceptions.ErrorCode;
import lombok.Getter;

@Getter
public class ETACalculationException extends BusinessException {

    private final String trackingSessionId;
    private final Double currentLat;
    private final Double currentLng;
    private final Double destLat;
    private final Double destLng;

    public ETACalculationException(String trackingSessionId, ErrorCode errorCode) {
        super(errorCode);
        this.trackingSessionId = trackingSessionId;
        this.currentLat = null;
        this.currentLng = null;
        this.destLat = null;
        this.destLng = null;
    }

    public ETACalculationException(String trackingSessionId, Double currentLat, Double currentLng,
                                   Double destLat, Double destLng, ErrorCode errorCode) {
        super(errorCode, String.valueOf(currentLat), String.valueOf(currentLng),
                String.valueOf(destLat), String.valueOf(destLng));
        this.trackingSessionId = trackingSessionId;
        this.currentLat = currentLat;
        this.currentLng = currentLng;
        this.destLat = destLat;
        this.destLng = destLng;
    }

    public ETACalculationException(String message) {
        super(ErrorCode.valueOf(message));
        this.trackingSessionId = null;
        this.currentLat = null;
        this.currentLng = null;
        this.destLat = null;
        this.destLng = null;
    }
}
