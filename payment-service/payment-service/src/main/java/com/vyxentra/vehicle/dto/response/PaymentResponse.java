package com.vyxentra.vehicle.dto.response;


import com.vyxentra.vehicle.enums.PaymentStatus;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PaymentResponse {

    private String paymentId;
    private String paymentNumber;
    private String bookingId;
    private String customerId;
    private Double amount;
    private Double commissionAmount;
    private Double providerAmount;
    private String paymentMethod;
    @Enumerated(EnumType.STRING)
    private PaymentStatus status;
    private String paymentType;
    private LocalDateTime createdAt;
}
