package com.vyxentra.vehicle.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TransactionResponse {

    private String transactionId;
    private String transactionNumber;
    private String type; // PAYMENT, REFUND, TOPUP
    private Double amount;
    private String status;
    private String referenceId; // booking_id, payment_id
    private LocalDateTime createdAt;
}
