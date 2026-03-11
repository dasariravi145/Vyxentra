package com.vyxentra.vehicle.exception;


import lombok.Getter;
import java.time.Instant;

@Getter
public class ProviderSuspendedException extends BusinessException {

    private final String providerId;
    private final String suspensionReason;
    private final String suspendedBy;
    private final Instant suspensionDate;
    private final boolean canAppeal;
    private final String appealUrl;

    public ProviderSuspendedException(String providerId) {
        super(ErrorCode.PROVIDER_SUSPENDED);
        this.providerId = providerId;
        this.suspensionReason = "No reason provided";
        this.suspendedBy = null;
        this.suspensionDate = Instant.now();
        this.canAppeal = true;
        this.appealUrl = "/api/v1/providers/" + providerId + "/appeal";
    }

    public ProviderSuspendedException(String providerId, String reason) {
        super(ErrorCode.PROVIDER_SUSPENDED, reason);
        this.providerId = providerId;
        this.suspensionReason = reason;
        this.suspendedBy = null;
        this.suspensionDate = Instant.now();
        this.canAppeal = true;
        this.appealUrl = "/api/v1/providers/" + providerId + "/appeal";
    }

    public ProviderSuspendedException(String providerId, String reason, String suspendedBy) {
        super(ErrorCode.PROVIDER_SUSPENDED, reason);
        this.providerId = providerId;
        this.suspensionReason = reason;
        this.suspendedBy = suspendedBy;
        this.suspensionDate = Instant.now();
        this.canAppeal = true;
        this.appealUrl = "/api/v1/providers/" + providerId + "/appeal";
    }
}
