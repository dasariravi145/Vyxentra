package com.vyxentra.vehicle.controller;

import com.vyxentra.vehicle.dto.response.ApiResponse;
import com.vyxentra.vehicle.dto.response.PageResponse;
import com.vyxentra.vehicle.dto.response.TransactionLogResponse;
import com.vyxentra.vehicle.service.TransactionLogService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;

@Slf4j
@RestController
@RequestMapping("/api/v1/payments/transactions")
@RequiredArgsConstructor
public class TransactionLogController {

    private final TransactionLogService transactionLogService;

    @GetMapping
    @PreAuthorize("hasAnyRole('CUSTOMER', 'PROVIDER')")
    public ResponseEntity<ApiResponse<PageResponse<TransactionLogResponse>>> getMyTransactions(
            @RequestHeader("X-User-ID") String userId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant fromDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant toDate,
            @RequestParam(required = false) String transactionType,
            @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
        log.info("Getting transactions for user: {}", userId);
        PageResponse<TransactionLogResponse> response = transactionLogService.getTransactionLogs(
                userId, fromDate, toDate, transactionType, pageable);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/{logId}")
    @PreAuthorize("hasAnyRole('CUSTOMER', 'PROVIDER', 'ADMIN')")
    public ResponseEntity<ApiResponse<TransactionLogResponse>> getTransactionLog(
            @PathVariable String logId) {
        log.info("Getting transaction log: {}", logId);
        TransactionLogResponse response = transactionLogService.getTransactionLog(logId);
        return ResponseEntity.ok(ApiResponse.success(response));
    }
}