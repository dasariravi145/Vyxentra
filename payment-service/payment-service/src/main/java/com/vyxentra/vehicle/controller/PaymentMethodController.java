package com.vyxentra.vehicle.controller;

import com.vyxentra.vehicle.dto.request.PaymentMethodRequest;
import com.vyxentra.vehicle.dto.response.ApiResponse;
import com.vyxentra.vehicle.dto.response.PaymentMethodResponse;
import com.vyxentra.vehicle.service.PaymentMethodService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/v1/payments/methods")
@RequiredArgsConstructor
public class PaymentMethodController {

    private final PaymentMethodService paymentMethodService;

    @PostMapping
    @PreAuthorize("hasAnyRole('CUSTOMER', 'PROVIDER')")
    public ResponseEntity<ApiResponse<PaymentMethodResponse>> addPaymentMethod(
            @RequestHeader("X-User-ID") String userId,
            @Valid @RequestBody PaymentMethodRequest request) {
        log.info("Adding payment method for user: {}", userId);
        PaymentMethodResponse response = paymentMethodService.addPaymentMethod(userId, request);
        return ResponseEntity.ok(ApiResponse.success(response, "Payment method added successfully"));
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('CUSTOMER', 'PROVIDER')")
    public ResponseEntity<ApiResponse<List<PaymentMethodResponse>>> getUserPaymentMethods(
            @RequestHeader("X-User-ID") String userId) {
        log.info("Getting payment methods for user: {}", userId);
        List<PaymentMethodResponse> responses = paymentMethodService.getUserPaymentMethods(userId);
        return ResponseEntity.ok(ApiResponse.success(responses));
    }

    @GetMapping("/{methodId}")
    @PreAuthorize("hasAnyRole('CUSTOMER', 'PROVIDER')")
    public ResponseEntity<ApiResponse<PaymentMethodResponse>> getPaymentMethod(
            @PathVariable String methodId,
            @RequestHeader("X-User-ID") String userId) {
        log.info("Getting payment method: {}", methodId);
        PaymentMethodResponse response = paymentMethodService.getPaymentMethod(methodId, userId);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @DeleteMapping("/{methodId}")
    @PreAuthorize("hasAnyRole('CUSTOMER', 'PROVIDER')")
    public ResponseEntity<ApiResponse<Void>> deletePaymentMethod(
            @PathVariable String methodId,
            @RequestHeader("X-User-ID") String userId) {
        log.info("Deleting payment method: {}", methodId);
        paymentMethodService.deletePaymentMethod(methodId, userId);
        return ResponseEntity.ok(ApiResponse.success(null, "Payment method deleted successfully"));
    }

    @PutMapping("/{methodId}/default")
    @PreAuthorize("hasAnyRole('CUSTOMER', 'PROVIDER')")
    public ResponseEntity<ApiResponse<Void>> setDefaultPaymentMethod(
            @PathVariable String methodId,
            @RequestHeader("X-User-ID") String userId) {
        log.info("Setting default payment method: {} for user: {}", methodId, userId);
        paymentMethodService.setDefaultPaymentMethod(methodId, userId);
        return ResponseEntity.ok(ApiResponse.success(null, "Default payment method updated"));
    }
}
