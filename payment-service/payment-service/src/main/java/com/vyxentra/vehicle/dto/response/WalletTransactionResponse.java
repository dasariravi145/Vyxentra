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
public class WalletTransactionResponse {

    private String transactionId;
    private String transactionNumber;
    private String type; // CREDIT, DEBIT
    private Double amount;
    private Double balanceAfter;
    private String referenceId;
    private String referenceType;
    private String description;
    private LocalDateTime createdAt;
}