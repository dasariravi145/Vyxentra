package com.vyxentra.vehicle.exceptions;

import org.springframework.http.HttpStatus;

public class DuplicateResourceException extends BusinessException {

    public DuplicateResourceException(String resourceType, String field, String value) {
        super(
                String.format("%s already exists with %s: %s", resourceType, field, value),
                "DUPLICATE_RESOURCE",
                HttpStatus.CONFLICT
        );
    }
}