package com.vyxentra.vehicle.controller;


import com.vyxentra.vehicle.dto.PayoutSummary;
import com.vyxentra.vehicle.dto.request.PayoutRequest;
import com.vyxentra.vehicle.dto.response.ApiResponse;
import com.vyxentra.vehicle.dto.response.PageResponse;
import com.vyxentra.vehicle.dto.response.PayoutResponse;
import com.vyxentra.vehicle.service.PayoutService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/v1/payments/payouts")
@RequiredArgsConstructor
public class PayoutController {

    private final PayoutService payoutService;

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<PayoutResponse>> createPayout(@Valid @RequestBody PayoutRequest request) {
        log.info("Creating payout for provider: {}", request.getProviderId());
        PayoutResponse response = payoutService.createPayout(request);
        return ResponseEntity.ok(ApiResponse.success(response, "Payout created successfully"));
    }

    @GetMapping("/{payoutId}")
    @PreAuthorize("hasAnyRole('PROVIDER', 'ADMIN')")
    public ResponseEntity<ApiResponse<PayoutResponse>> getPayout(@PathVariable String payoutId) {
        log.info("Getting payout: {}", payoutId);
        PayoutResponse response = payoutService.getPayout(payoutId);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/provider/{providerId}")
    @PreAuthorize("hasRole('PROVIDER') or hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<PageResponse<PayoutResponse>>> getProviderPayouts(
            @PathVariable String providerId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fromDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate toDate,
            @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
        log.info("Getting payouts for provider: {}", providerId);
        PageResponse<PayoutResponse> response = payoutService.getProviderPayouts(providerId, fromDate, toDate, pageable);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @PostMapping("/{payoutId}/process")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<PayoutResponse>> processPayout(@PathVariable String payoutId) {
        log.info("Processing payout: {}", payoutId);
        PayoutResponse response = payoutService.processPayout(payoutId);
        return ResponseEntity.ok(ApiResponse.success(response, "Payout processed successfully"));
    }

    @GetMapping("/pending")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<List<PayoutResponse>>> getPendingPayouts() {
        log.info("Getting pending payouts");
        List<PayoutResponse> responses = payoutService.getPendingPayouts();
        return ResponseEntity.ok(ApiResponse.success(responses));
    }

    @GetMapping("/summary/{providerId}")
    @PreAuthorize("hasRole('PROVIDER') or hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<PayoutSummary>> getPayoutSummary(@PathVariable String providerId) {
        log.info("Getting payout summary for provider: {}", providerId);
        PayoutSummary summary = payoutService.getPayoutSummary(providerId);
        return ResponseEntity.ok(ApiResponse.success(summary));
    }


}
