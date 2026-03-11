package com.vyxentra.vehicle.controller;

import com.vyxentra.vehicle.dto.request.CreatePaymentRequest;
import com.vyxentra.vehicle.dto.request.ProcessPaymentRequest;
import com.vyxentra.vehicle.dto.response.*;
import com.vyxentra.vehicle.service.PaymentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/v1/payments")
@RequiredArgsConstructor
public class PaymentController {

    private final PaymentService paymentService;

    @PostMapping
    public ResponseEntity<ApiResponse<PaymentResponse>> createPayment(
            @RequestHeader("X-User-ID") String customerId,
            @Valid @RequestBody CreatePaymentRequest request) {
        log.info("Creating payment for booking: {} amount: {}", request.getBookingId(), request.getAmount());
        PaymentResponse response = paymentService.createPayment(customerId, request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(response, "Payment created successfully"));
    }

    @PostMapping("/{paymentId}/process")
    public ResponseEntity<ApiResponse<PaymentResponse>> processPayment(
            @PathVariable String paymentId,
            @Valid @RequestBody ProcessPaymentRequest request) {
        log.info("Processing payment: {}", paymentId);
        PaymentResponse response = paymentService.processPayment(paymentId, request);
        return ResponseEntity.ok(ApiResponse.success(response, "Payment processed"));
    }

    @GetMapping("/{paymentId}")
    public ResponseEntity<ApiResponse<PaymentDetailResponse>> getPayment(
            @PathVariable String paymentId) {
        log.info("Getting payment: {}", paymentId);
        PaymentDetailResponse response = paymentService.getPayment(paymentId);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/number/{paymentNumber}")
    public ResponseEntity<ApiResponse<PaymentDetailResponse>> getPaymentByNumber(
            @PathVariable String paymentNumber) {
        log.info("Getting payment by number: {}", paymentNumber);
        PaymentDetailResponse response = paymentService.getPaymentByNumber(paymentNumber);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/booking/{bookingId}")
    public ResponseEntity<ApiResponse<List<PaymentResponse>>> getBookingPayments(
            @PathVariable String bookingId) {
        log.info("Getting payments for booking: {}", bookingId);
        List<PaymentResponse> responses = paymentService.getBookingPayments(bookingId);
        return ResponseEntity.ok(ApiResponse.success(responses));
    }

    @GetMapping("/customer/{customerId}")
    public ResponseEntity<ApiResponse<List<PaymentResponse>>> getCustomerPayments(
            @PathVariable String customerId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime fromDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime toDate) {
        log.info("Getting payments for customer: {}", customerId);
        List<PaymentResponse> responses = paymentService.getCustomerPayments(customerId, fromDate, toDate);
        return ResponseEntity.ok(ApiResponse.success(responses));
    }

    @PostMapping("/{paymentId}/verify")
    public ResponseEntity<ApiResponse<Boolean>> verifyPayment(
            @PathVariable String paymentId) {
        log.info("Verifying payment: {}", paymentId);
        boolean isValid = paymentService.verifyPayment(paymentId);
        return ResponseEntity.ok(ApiResponse.success(isValid));
    }

    @PostMapping("/webhook/{gateway}")
    public ResponseEntity<String> handleWebhook(
            @PathVariable String gateway,
            @RequestBody String payload,
            @RequestHeader("X-Signature") String signature) {
        log.info("Received webhook from gateway: {}", gateway);
        paymentService.handleWebhook(gateway, payload, signature);
        return ResponseEntity.ok("Webhook received");
    }

    @GetMapping("/transactions")
    public ResponseEntity<ApiResponse<PageResponse<TransactionResponse>>> getTransactions(
            @RequestHeader("X-User-ID") String userId,
            @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
        log.info("Getting transactions for user: {}", userId);
        PageResponse<TransactionResponse> response = paymentService.getUserTransactions(userId, pageable);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/status/{bookingId}")
    public ResponseEntity<ApiResponse<String>> getPaymentStatus(
            @PathVariable String bookingId) {
        log.info("Getting payment status for booking: {}", bookingId);
        String status = paymentService.getPaymentStatus(bookingId);
        return ResponseEntity.ok(ApiResponse.success(status));
    }
}
