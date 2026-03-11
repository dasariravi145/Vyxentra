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
public class RefundResponse {

    private String refundId;
    private String refundNumber;
    private String paymentId;
    private String bookingId;
    private Double amount;
    private String reason;
    private String status;
    private String gatewayRefundId;
    private String processedBy;
    private LocalDateTime processedAt;
    private String approvedBy;
    private LocalDateTime approvedAt;
    private LocalDateTime createdAt;
}
