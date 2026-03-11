package com.vyxentra.vehicle.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class TransactionLogResponse {

    private String logId;
    private String transactionId;
    private String transactionType;
    private String action;
    private String userId;
    private Double amount;
    private String status;
    private String referenceId;
    private String referenceType;
    private String gatewayTransactionId;
    private String errorMessage;
    private Boolean success;
    private Instant createdAt;
}
