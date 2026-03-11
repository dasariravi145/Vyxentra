package com.vyxentra.vehicle.dto.request;

import com.vyxentra.vehicle.dto.PaymentMethodDetails;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreatePaymentRequest {

    @NotBlank(message = "Booking ID is required")
    private String bookingId;

    @NotNull(message = "Amount is required")
    @Positive(message = "Amount must be positive")
    private Double amount;

    private String paymentMethod; // CARD, UPI, WALLET, NETBANKING

    private String paymentGateway; // RAZORPAY, STRIPE, PAYU

    private String description;

    private Boolean useWallet;

    private PaymentMethodDetails paymentMethodDetails;

}
