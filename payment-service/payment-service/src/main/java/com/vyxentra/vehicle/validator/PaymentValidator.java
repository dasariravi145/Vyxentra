package com.vyxentra.vehicle.validator;
import com.vyxentra.vehicle.entity.Payment;
import com.vyxentra.vehicle.exceptions.BusinessException;
import com.vyxentra.vehicle.exceptions.ErrorCode;
import com.vyxentra.vehicle.repository.PaymentRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class PaymentValidator {

    private final PaymentRepository paymentRepository;

    public void validateBookingForPayment(String bookingId, Double amount) {
        // This would call booking service to validate
        // For now, just log
        log.debug("Validating booking {} for payment amount {}", bookingId, amount);
    }

    public void validateRefund(Payment payment, Double refundAmount) {
        if (!"SUCCESS".equals(payment.getStatus())) {
            throw new BusinessException(ErrorCode.PAYMENT_NOT_FOUND,
                    "Cannot refund payment in " + payment.getStatus() + " state");
        }

        if (refundAmount <= 0) {
            throw new BusinessException(ErrorCode.VALIDATION_ERROR,
                    "Refund amount must be positive");
        }

        if (refundAmount > payment.getAmount()) {
            throw new BusinessException(ErrorCode.VALIDATION_ERROR,
                    "Refund amount cannot exceed payment amount");
        }
    }

    public void validateWalletTransaction(Double amount, Double balance) {
        if (amount <= 0) {
            throw new BusinessException(ErrorCode.VALIDATION_ERROR,
                    "Transaction amount must be positive");
        }

        if (balance < amount) {
            throw new BusinessException(ErrorCode.INSUFFICIENT_BALANCE,
                    "Insufficient wallet balance");
        }
    }
}
