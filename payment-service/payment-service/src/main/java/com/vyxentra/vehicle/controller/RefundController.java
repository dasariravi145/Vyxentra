package com.vyxentra.vehicle.controller;


import com.vyxentra.vehicle.dto.request.RefundRequest;
import com.vyxentra.vehicle.dto.response.ApiResponse;
import com.vyxentra.vehicle.dto.response.RefundResponse;
import com.vyxentra.vehicle.service.RefundService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/v1/payments/refunds")
@RequiredArgsConstructor
public class RefundController {

    private final RefundService refundService;

    @PostMapping
    public ResponseEntity<ApiResponse<RefundResponse>> initiateRefund(
            @RequestHeader("X-User-ID") String userId,
            @Valid @RequestBody RefundRequest request) {
        log.info("Initiating refund for payment: {} amount: {}", request.getPaymentId(), request.getAmount());
        RefundResponse response = refundService.initiateRefund(userId, request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(response, "Refund initiated"));
    }

    @GetMapping("/{refundId}")
    public ResponseEntity<ApiResponse<RefundResponse>> getRefund(
            @PathVariable String refundId) {
        log.info("Getting refund: {}", refundId);
        RefundResponse response = refundService.getRefund(refundId);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/payment/{paymentId}")
    public ResponseEntity<ApiResponse<List<RefundResponse>>> getPaymentRefunds(
            @PathVariable String paymentId) {
        log.info("Getting refunds for payment: {}", paymentId);
        List<RefundResponse> responses = refundService.getPaymentRefunds(paymentId);
        return ResponseEntity.ok(ApiResponse.success(responses));
    }

    @PostMapping("/{refundId}/process")
    public ResponseEntity<ApiResponse<RefundResponse>> processRefund(
            @PathVariable String refundId) {
        log.info("Processing refund: {}", refundId);
        RefundResponse response = refundService.processRefund(refundId);
        return ResponseEntity.ok(ApiResponse.success(response, "Refund processed"));
    }

    @PostMapping("/{refundId}/cancel")
    public ResponseEntity<ApiResponse<Void>> cancelRefund(
            @PathVariable String refundId,
            @RequestHeader("X-User-ID") String userId) {
        log.info("Cancelling refund: {} by user: {}", refundId, userId);
        refundService.cancelRefund(refundId, userId);
        return ResponseEntity.ok(ApiResponse.success(null, "Refund cancelled"));
    }

    @GetMapping("/pending")
    public ResponseEntity<ApiResponse<List<RefundResponse>>> getPendingRefunds(
            @RequestParam(required = false) String paymentId) {
        log.info("Getting pending refunds");
        List<RefundResponse> responses = refundService.getPendingRefunds(paymentId);
        return ResponseEntity.ok(ApiResponse.success(responses));
    }
}
