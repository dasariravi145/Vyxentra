package com.vyxentra.vehicle.exception;


import com.vyxentra.vehicle.exceptions.BusinessException;
import com.vyxentra.vehicle.exceptions.ErrorCode;
import lombok.Getter;

@Getter
public class ProviderException extends BusinessException {

    private final String providerName;

    public ProviderException(String providerName, ErrorCode errorCode) {
        super(errorCode);
        this.providerName = providerName;
    }

    public ProviderException(String providerName, ErrorCode errorCode, String... args) {
        super(errorCode, args);
        this.providerName = providerName;
    }

    public ProviderException(String providerName, String message) {
        super(ErrorCode.valueOf(message));
        this.providerName = providerName;
    }
}