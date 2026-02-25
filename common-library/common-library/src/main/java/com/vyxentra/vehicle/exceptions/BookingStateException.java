package com.vyxentra.vehicle.exceptions;

import org.springframework.http.HttpStatus;

public class BookingStateException extends BusinessException {

    public BookingStateException(String message) {
        super(message, "INVALID_BOOKING_STATE", HttpStatus.BAD_REQUEST);
    }

    public BookingStateException(String currentState, String expectedState) {
        super(
                String.format("Invalid booking state. Current: %s, Expected: %s", currentState, expectedState),
                "INVALID_BOOKING_STATE",
                HttpStatus.BAD_REQUEST
        );
    }
}
