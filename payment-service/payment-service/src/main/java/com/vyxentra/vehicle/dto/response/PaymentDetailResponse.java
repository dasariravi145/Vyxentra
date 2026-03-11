package com.vyxentra.vehicle.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PaymentDetailResponse {

    private String paymentId;
    private String paymentNumber;
    private String bookingId;
    private String customerId;
    private String providerId;
    private Double amount;
    private Double commissionAmount;
    private Double providerAmount;
    private String paymentMethod;
    private String paymentGateway;
    private String gatewayPaymentId;
    private String gatewayOrderId;
    private String status;
    private String paymentType;
    private String description;
    private Map<String, Object> metadata;
    private String errorMessage;
    private Integer retryCount;

    private List<RefundResponse> refunds;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
