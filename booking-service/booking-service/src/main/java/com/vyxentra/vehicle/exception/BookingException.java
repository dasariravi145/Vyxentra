package com.vyxentra.vehicle.exception;


import com.vyxentra.vehicle.exceptions.BusinessException;
import com.vyxentra.vehicle.exceptions.ErrorCode;
import lombok.Getter;

@Getter
public class BookingException extends BusinessException {

    private final String bookingId;
    private final String currentStatus;

    public BookingException(String bookingId, ErrorCode errorCode) {
        super(errorCode);
        this.bookingId = bookingId;
        this.currentStatus = null;
    }

    public BookingException(String bookingId, ErrorCode errorCode, String currentStatus) {
        super(errorCode, currentStatus);
        this.bookingId = bookingId;
        this.currentStatus = currentStatus;
    }

    public BookingException(String bookingId, String message) {
        super(ErrorCode.valueOf(message));
        this.bookingId = bookingId;
        this.currentStatus = null;
    }
}
