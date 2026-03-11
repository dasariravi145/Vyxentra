package com.vyxentra.vehicle.exception;


import com.vyxentra.vehicle.exceptions.BusinessException;
import com.vyxentra.vehicle.exceptions.ErrorCode;
import lombok.Getter;

@Getter
public class PaymentException extends BusinessException {

    private final String paymentId;
    private final String transactionId;

    public PaymentException(String paymentId, ErrorCode errorCode) {
        super(errorCode);
        this.paymentId = paymentId;
        this.transactionId = null;
    }

    public PaymentException(String paymentId, ErrorCode errorCode, String... args) {
        super(errorCode, args);
        this.paymentId = paymentId;
        this.transactionId = null;
    }

    public PaymentException(String paymentId, String transactionId, ErrorCode errorCode) {
        super(errorCode);
        this.paymentId = paymentId;
        this.transactionId = transactionId;
    }

    public PaymentException(String message) {
        super(ErrorCode.valueOf(message));
        this.paymentId = null;
        this.transactionId = null;
    }
}
