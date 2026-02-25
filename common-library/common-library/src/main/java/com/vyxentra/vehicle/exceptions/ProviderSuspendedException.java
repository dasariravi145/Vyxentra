package com.vyxentra.vehicle.exceptions;

import org.springframework.http.HttpStatus;

public class ProviderSuspendedException extends BusinessException {

    public ProviderSuspendedException(String providerId) {
        super(
                String.format("Provider %s is currently suspended", providerId),
                "PROVIDER_SUSPENDED",
                HttpStatus.FORBIDDEN
        );
    }

    public ProviderSuspendedException(String providerId, String reason) {
        super(
                String.format("Provider %s is suspended. Reason: %s", providerId, reason),
                "PROVIDER_SUSPENDED",
                HttpStatus.FORBIDDEN
        );
    }
}
