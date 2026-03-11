package com.vyxentra.vehicle.exception;



import lombok.Getter;

@Getter
public class ProviderApprovalException extends BusinessException {

    private final String providerId;
    private final String currentStatus;

    public ProviderApprovalException(String providerId, String currentStatus) {
        super(ErrorCode.PROVIDER_INVALID_STATUS, "Provider is in " + currentStatus + " status");
        this.providerId = providerId;
        this.currentStatus = currentStatus;
    }

    public ProviderApprovalException(String providerId, String currentStatus, String message) {
        super(ErrorCode.PROVIDER_INVALID_STATUS, message);
        this.providerId = providerId;
        this.currentStatus = currentStatus;
    }
}
