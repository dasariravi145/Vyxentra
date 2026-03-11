package com.vyxentra.vehicle.service;

import com.vyxentra.vehicle.dto.response.PageResponse;
import com.vyxentra.vehicle.dto.response.TransactionLogResponse;
import com.vyxentra.vehicle.entity.Payment;
import com.vyxentra.vehicle.entity.Refund;
import com.vyxentra.vehicle.entity.TransactionLog;
import com.vyxentra.vehicle.entity.WalletTransaction;
import com.vyxentra.vehicle.exceptions.ResourceNotFoundException;
import com.vyxentra.vehicle.mapper.TransactionLogMapper;
import com.vyxentra.vehicle.repository.TransactionLogRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class TransactionLogServiceImpl implements TransactionLogService {

    private final TransactionLogRepository transactionLogRepository;
    private final TransactionLogMapper transactionLogMapper;

    @Override
    @Transactional
    public void logPaymentCreation(Payment payment, Map<String, Object> request) {
        TransactionLog log = createBaseLog(payment, "PAYMENT_CREATED");
        log.setRequestPayload(request);
        transactionLogRepository.save(log);
    }

    @Override
    @Transactional
    public void logPaymentSuccess(Payment payment, Map<String, Object> response) {
        TransactionLog log = createBaseLog(payment, "PAYMENT_SUCCESS");
        log.setSuccess(true);
        log.setResponsePayload(response);
        log.setGatewayTransactionId(payment.getGatewayPaymentId());
        transactionLogRepository.save(log);
    }

    @Override
    @Transactional
    public void logPaymentFailure(Payment payment, String errorMessage, String errorCode) {
        TransactionLog log = createBaseLog(payment, "PAYMENT_FAILED");
        log.setSuccess(false);
        log.setErrorMessage(errorMessage);
        log.setErrorCode(errorCode);
        transactionLogRepository.save(log);
    }

    @Override
    @Transactional
    public void logRefundCreation(Refund refund, Map<String, Object> request) {
        TransactionLog log = TransactionLog.builder()
                .transactionId(refund.getId())
                .transactionType("REFUND")
                .action("REFUND_CREATED")
                .userId(refund.getPayment().getCustomerId())
                .amount(refund.getAmount())
                .referenceId(refund.getPayment().getId())
                .referenceType("PAYMENT")
                .success(true)
                .requestPayload(request)
                .build();
        transactionLogRepository.save(log);
    }

    @Override
    @Transactional
    public void logRefundSuccess(Refund refund, Map<String, Object> response) {
        TransactionLog log = TransactionLog.builder()
                .transactionId(refund.getId())
                .transactionType("REFUND")
                .action("REFUND_SUCCESS")
                .userId(refund.getPayment().getCustomerId())
                .amount(refund.getAmount())
                .referenceId(refund.getPayment().getId())
                .referenceType("PAYMENT")
                .success(true)
                .responsePayload(response)
                .gatewayTransactionId(refund.getGatewayRefundId())
                .build();
        transactionLogRepository.save(log);
    }

    @Override
    @Transactional
    public void logWalletTransaction(WalletTransaction transaction) {
        TransactionLog log = TransactionLog.builder()
                .transactionId(transaction.getId())
                .transactionType("WALLET_" + transaction.getType())
                .action(transaction.getType())
                .userId(transaction.getWallet().getUserId())
                .amount(transaction.getAmount())
                .referenceId(transaction.getReferenceId())
                .referenceType(transaction.getReferenceType())
                .success(true)
                .build();
        transactionLogRepository.save(log);
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<TransactionLogResponse> getTransactionLogs(
            String userId,
            Instant fromDate,
            Instant toDate,
            String transactionType,
            Pageable pageable) {

        Instant start = fromDate != null ? fromDate : Instant.now().minus(30, ChronoUnit.DAYS);
        Instant end = toDate != null ? toDate : Instant.now();

        Page<TransactionLog> page = transactionLogRepository.findByUserIdAndDateRange(
                userId, start, end, pageable);

        List<TransactionLogResponse> responses =
                transactionLogMapper.toResponseList(page.getContent());

        return PageResponse.<TransactionLogResponse>builder()
                .content(responses)
                .pageNumber(page.getNumber())
                .pageSize(page.getSize())
                .totalElements(page.getTotalElements())
                .totalPages(page.getTotalPages())
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public TransactionLogResponse getTransactionLog(String logId) {
        TransactionLog log = transactionLogRepository.findById(logId)
                .orElseThrow(() -> new ResourceNotFoundException("TransactionLog", logId));
        return transactionLogMapper.toResponse(log);
    }

    @Override
    @Transactional
    public void cleanupOldLogs(int retentionDays) {
        Instant cutoff = Instant.now().minus(retentionDays, ChronoUnit.DAYS);
        int deleted = transactionLogRepository.deleteOlderThan(cutoff);
        log.info("Cleaned up {} transaction logs", deleted);
    }

    private TransactionLog createBaseLog(Payment payment, String action) {
        return TransactionLog.builder()
                .transactionId(payment.getId())
                .transactionType("PAYMENT")
                .action(action)
                .userId(payment.getCustomerId())
                .amount(payment.getAmount())
                .currency(payment.getCurrency())
                .status(payment.getStatus())
                .referenceId(payment.getBookingId())
                .referenceType("BOOKING")
                .paymentGateway(payment.getPaymentGateway())
                .build();
    }
}