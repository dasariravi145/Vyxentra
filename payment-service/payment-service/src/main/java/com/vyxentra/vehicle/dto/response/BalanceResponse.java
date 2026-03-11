package com.vyxentra.vehicle.dto.response;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BalanceResponse {

    private Double currentBalance;
    private Double pendingBalance;
    private Double availableBalance;
    private String currency;
    private String userId;
    private String walletId;
    private Instant lastUpdated;
}
