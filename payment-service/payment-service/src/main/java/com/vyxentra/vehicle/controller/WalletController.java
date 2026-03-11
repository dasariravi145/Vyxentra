package com.vyxentra.vehicle.controller;

import com.vyxentra.vehicle.dto.request.WalletTopupRequest;
import com.vyxentra.vehicle.dto.response.ApiResponse;
import com.vyxentra.vehicle.dto.response.WalletResponse;
import com.vyxentra.vehicle.dto.response.WalletTransactionResponse;
import com.vyxentra.vehicle.service.WalletService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/v1/payments/wallet")
@RequiredArgsConstructor
public class WalletController {

    private final WalletService walletService;

    @PostMapping("/create")
    public ResponseEntity<ApiResponse<WalletResponse>> createWallet(
            @RequestHeader("X-User-ID") String userId,
            @RequestParam String userType) {
        log.info("Creating wallet for user: {} type: {}", userId, userType);
        WalletResponse response = walletService.createWallet(userId, userType);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(response, "Wallet created successfully"));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<WalletResponse>> getMyWallet(
            @RequestHeader("X-User-ID") String userId) {
        log.info("Getting wallet for user: {}", userId);
        WalletResponse response = walletService.getWalletByUserId(userId);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/{walletId}")
    public ResponseEntity<ApiResponse<WalletResponse>> getWallet(
            @PathVariable String walletId) {
        log.info("Getting wallet: {}", walletId);
        WalletResponse response = walletService.getWallet(walletId);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @PostMapping("/topup")
    public ResponseEntity<ApiResponse<WalletResponse>> topupWallet(
            @RequestHeader("X-User-ID") String userId,
            @Valid @RequestBody WalletTopupRequest request) {
        log.info("Topping up wallet for user: {} amount: {}", userId, request.getAmount());
        WalletResponse response = walletService.topupWallet(userId, request);
        return ResponseEntity.ok(ApiResponse.success(response, "Wallet topped up successfully"));
    }

    @PostMapping("/{walletId}/debit")
    public ResponseEntity<ApiResponse<WalletResponse>> debitWallet(
            @PathVariable String walletId,
            @RequestParam Double amount,
            @RequestParam String referenceId,
            @RequestParam String referenceType,
            @RequestParam String description) {
        log.info("Debiting wallet: {} amount: {}", walletId, amount);
        WalletResponse response = walletService.debitWallet(walletId, amount, referenceId, referenceType, description);
        return ResponseEntity.ok(ApiResponse.success(response, "Wallet debited successfully"));
    }

    @PostMapping("/{walletId}/credit")
    public ResponseEntity<ApiResponse<WalletResponse>> creditWallet(
            @PathVariable String walletId,
            @RequestParam Double amount,
            @RequestParam String referenceId,
            @RequestParam String referenceType,
            @RequestParam String description) {
        log.info("Crediting wallet: {} amount: {}", walletId, amount);
        WalletResponse response = walletService.creditWallet(walletId, amount, referenceId, referenceType, description);
        return ResponseEntity.ok(ApiResponse.success(response, "Wallet credited successfully"));
    }

    @GetMapping("/transactions")
    public ResponseEntity<ApiResponse<List<WalletTransactionResponse>>> getWalletTransactions(
            @RequestHeader("X-User-ID") String userId,
            @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
        log.info("Getting wallet transactions for user: {}", userId);
        List<WalletTransactionResponse> responses = walletService.getWalletTransactions(userId, pageable);
        return ResponseEntity.ok(ApiResponse.success(responses));
    }

    @GetMapping("/balance")
    public ResponseEntity<ApiResponse<Double>> getWalletBalance(
            @RequestHeader("X-User-ID") String userId) {
        log.info("Getting wallet balance for user: {}", userId);
        Double balance = walletService.getWalletBalance(userId);
        return ResponseEntity.ok(ApiResponse.success(balance));
    }

    @PostMapping("/{walletId}/block")
    public ResponseEntity<ApiResponse<Void>> blockWallet(
            @PathVariable String walletId,
            @RequestParam String reason) {
        log.info("Blocking wallet: {} reason: {}", walletId, reason);
        walletService.blockWallet(walletId, reason);
        return ResponseEntity.ok(ApiResponse.success(null, "Wallet blocked"));
    }
}
