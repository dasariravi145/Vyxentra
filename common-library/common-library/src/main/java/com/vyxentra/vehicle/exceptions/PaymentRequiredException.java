package com.vyxentra.vehicle.exceptions;

import org.springframework.http.HttpStatus;

public class PaymentRequiredException extends BusinessException {

    public PaymentRequiredException(String message) {
        super(message, "PAYMENT_REQUIRED", HttpStatus.PAYMENT_REQUIRED);
    }

    public PaymentRequiredException() {
        super("Payment is required to proceed", "PAYMENT_REQUIRED", HttpStatus.PAYMENT_REQUIRED);
    }
}
