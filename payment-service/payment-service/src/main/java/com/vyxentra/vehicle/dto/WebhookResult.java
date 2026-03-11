package com.vyxentra.vehicle.dto;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WebhookResult {

    private boolean success;

    private String eventType;

    private String paymentId;

    private String transactionId;

    private String status;

    private String message;

    private Map<String, Object> data;
}