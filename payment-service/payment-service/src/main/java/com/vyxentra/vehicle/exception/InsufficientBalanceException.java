package com.vyxentra.vehicle.exception;


import com.vyxentra.vehicle.exceptions.BusinessException;
import com.vyxentra.vehicle.exceptions.ErrorCode;
import lombok.Getter;

@Getter
public class InsufficientBalanceException extends BusinessException {

    private final String walletId;
    private final Double requestedAmount;
    private final Double availableBalance;

    public InsufficientBalanceException(String walletId, Double requestedAmount, Double availableBalance) {
        super(ErrorCode.INSUFFICIENT_BALANCE,
                String.format("Requested: %.2f, Available: %.2f", requestedAmount, availableBalance));
        this.walletId = walletId;
        this.requestedAmount = requestedAmount;
        this.availableBalance = availableBalance;
    }
}
