package com.vyxentra.vehicle.events;

import com.vyxentra.vehicle.enums.PaymentType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.math.BigDecimal;
import java.time.Instant;

@Data
@EqualsAndHashCode(callSuper = true)
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class PaymentSuccessEvent extends BaseEvent {

    private String paymentId;
    private String bookingId;
    private String customerId;
    private String providerId;
    private BigDecimal amount;
    private BigDecimal platformFee;
    private BigDecimal providerEarnings;
    private BigDecimal taxAmount;
    private String currency;
    private PaymentType paymentType;
    private String transactionId;
    private String paymentMethod;
    private Instant paidAt;
    private boolean isEmergencySurchargeApplied;
}
