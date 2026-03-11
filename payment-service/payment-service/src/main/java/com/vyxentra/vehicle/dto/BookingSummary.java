package com.vyxentra.vehicle.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public  class BookingSummary {
    private String bookingId;
    private String bookingNumber;
    private Double amount;
    private Double commission;
    private Double netAmount;
    private Instant completedAt;
    private String serviceType;
    private String customerName;
}
