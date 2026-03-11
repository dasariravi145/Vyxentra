package com.vyxentra.vehicle.service;


import com.vyxentra.vehicle.dto.response.PageResponse;
import com.vyxentra.vehicle.dto.response.TransactionLogResponse;
import com.vyxentra.vehicle.entity.Payment;
import com.vyxentra.vehicle.entity.Refund;
import com.vyxentra.vehicle.entity.WalletTransaction;
import org.springframework.data.domain.Pageable;

import java.time.Instant;
import java.util.Map;

public interface TransactionLogService {

    void logPaymentCreation(Payment payment, Map<String, Object> request);
    void logPaymentSuccess(Payment payment, Map<String, Object> response);
    void logPaymentFailure(Payment payment, String errorMessage, String errorCode);
    void logRefundCreation(Refund refund, Map<String, Object> request);
    void logRefundSuccess(Refund refund, Map<String, Object> response);
    void logWalletTransaction(WalletTransaction transaction);

    PageResponse<TransactionLogResponse> getTransactionLogs(String userId, Instant fromDate, Instant toDate,
                                                            String transactionType, Pageable pageable);

    TransactionLogResponse getTransactionLog(String logId);
    void cleanupOldLogs(int retentionDays);
}
