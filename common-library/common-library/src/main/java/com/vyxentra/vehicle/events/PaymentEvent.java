package com.vyxentra.vehicle.events;


import com.vyxentra.vehicle.enums.PaymentStatus;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.math.BigDecimal;

@Data
@SuperBuilder
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class PaymentEvent extends BaseEvent {
    private String paymentId;
    private String bookingId;
    private String customerId;
    private String providerId;
    private BigDecimal amount;
    private BigDecimal commission;
    private BigDecimal providerAmount;
    private PaymentStatus status;
    private String paymentMethod;
    private String transactionId;
    private String failureReason;
}
