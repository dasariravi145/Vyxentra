package com.vyxentra.vehicle.exceptions;

import org.springframework.http.HttpStatus;

public class DistributedLockException extends BusinessException {

    public DistributedLockException(String resourceType, String resourceId) {
        super(
                String.format("Unable to acquire lock for %s: %s", resourceType, resourceId),
                "LOCK_ACQUISITION_FAILED",
                HttpStatus.CONFLICT
        );
    }

    public DistributedLockException(String message) {
        super(message, "LOCK_ACQUISITION_FAILED", HttpStatus.CONFLICT);
    }
}
