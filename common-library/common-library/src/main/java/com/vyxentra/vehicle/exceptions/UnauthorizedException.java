package com.vyxentra.vehicle.exceptions;

import org.springframework.http.HttpStatus;

public class UnauthorizedException extends BusinessException {

    public UnauthorizedException(String message) {
        super(message, "UNAUTHORIZED", HttpStatus.UNAUTHORIZED);
    }

    public UnauthorizedException() {
        super("Authentication required", "UNAUTHORIZED", HttpStatus.UNAUTHORIZED);
    }
}
