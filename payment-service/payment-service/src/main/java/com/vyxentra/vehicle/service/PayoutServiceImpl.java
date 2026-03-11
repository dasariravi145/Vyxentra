package com.vyxentra.vehicle.service;

import com.vyxentra.vehicle.client.ProviderServiceClient;
import com.vyxentra.vehicle.dto.BankAccountDetails;
import com.vyxentra.vehicle.dto.PayoutStatistics;
import com.vyxentra.vehicle.dto.PayoutSummary;
import com.vyxentra.vehicle.dto.request.PayoutRequest;
import com.vyxentra.vehicle.dto.response.PageResponse;
import com.vyxentra.vehicle.dto.response.PayoutResponse;
import com.vyxentra.vehicle.entity.Payout;
import com.vyxentra.vehicle.exceptions.BusinessException;
import com.vyxentra.vehicle.exceptions.ErrorCode;
import com.vyxentra.vehicle.exceptions.ResourceNotFoundException;
import com.vyxentra.vehicle.gateway.PaymentGateway;
import com.vyxentra.vehicle.gateway.PaymentGatewayFactory;
import com.vyxentra.vehicle.kafka.PaymentEventProducer;
import com.vyxentra.vehicle.mapper.PayoutMapper;
import com.vyxentra.vehicle.repository.PayoutRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class PayoutServiceImpl implements PayoutService {

    private final PayoutRepository payoutRepository;
    private final PayoutMapper payoutMapper;
    private final PaymentGatewayFactory gatewayFactory;
    private final PaymentEventProducer eventProducer;
    private final NotificationService notificationService;
    private final ProviderServiceClient providerServiceClient;

    // ==================== Create Payout ====================

    @Override
    @Transactional
    public PayoutResponse createPayout(PayoutRequest request) {
        log.info("Creating payout for provider: {}, amount: {}, period: {} to {}",
                request.getProviderId(), request.getNetAmount(),
                request.getPeriodStart(), request.getPeriodEnd());

        // Validate bank account or UPI
        if (request.getBankAccount() != null) {
            if (!validateBankAccount(request.getBankAccount())) {
                throw new BusinessException(ErrorCode.VALIDATION_ERROR, "Invalid bank account details");
            }
        } else if (request.getUpiDetails() != null) {
            if (!validateUpiDetails(request.getUpiDetails())) {
                throw new BusinessException(ErrorCode.VALIDATION_ERROR, "Invalid UPI details");
            }
        } else {
            throw new BusinessException(ErrorCode.VALIDATION_ERROR,
                    "Either bank account or UPI details must be provided");
        }

        // Check if payout already exists for this period
        if (payoutRepository.existsByProviderIdAndPeriodStartAndPeriodEnd(
                request.getProviderId(), request.getPeriodStart(), request.getPeriodEnd())) {
            throw new BusinessException(ErrorCode.VALIDATION_ERROR,
                    "Payout already exists for this provider and period");
        }

        // Validate net amount calculation
        if (!isValidNetAmount(request)) {
            throw new BusinessException(ErrorCode.VALIDATION_ERROR,
                    "Net amount must equal total amount minus deductions");
        }

        // Generate payout number
        String payoutNumber = generatePayoutNumber();

        // Create payout entity
        Payout payout = Payout.builder()
                .payoutNumber(payoutNumber)
                .providerId(request.getProviderId())
                .providerName(request.getProviderName())
                .totalAmount(request.getTotalAmount())
                .commissionDeducted(request.getCommissionDeducted())
                .taxDeducted(request.getTaxDeducted())
                .processingFee(request.getProcessingFee())
                .gatewayFee(request.getGatewayFee())
                .netAmount(request.getNetAmount())
                .currency(request.getCurrency() != null ? request.getCurrency() : "INR")
                .periodStart(request.getPeriodStart())
                .periodEnd(request.getPeriodEnd())
                .bookingIds((List<String>) request.getBookingIds())
                .status("PENDING")
                .paymentMethod(request.getPaymentMethod())
                .accountDetails(convertBankDetails(request.getBankAccount()))
                .upiDetails(convertUpiDetails(request.getUpiDetails()))
                .notes(request.getNotes())
                .metadata(request.getMetadata())
                .priority(request.getPriority() != null ? request.getPriority() : 3)
                .retryCount(0)
                .build();

        payout = payoutRepository.save(payout);

        log.info("Payout created with ID: {}, Number: {}", payout.getId(), payoutNumber);

        // Auto-process if requested
        if (Boolean.TRUE.equals(request.getAutoProcess())) {
            return processPayout(payout.getId());
        }

        // Send notification
        if (Boolean.TRUE.equals(request.getSendNotification())) {
            notificationService.sendPayoutCreatedNotification(
                    payout.getProviderId(),
                    payout.getNetAmount(),
                    payout.getId()
            );
        }

        return payoutMapper.toResponse(payout);
    }

    @Override
    @Transactional
    public List<PayoutResponse> bulkCreatePayouts(List<PayoutRequest> requests) {
        log.info("Bulk creating {} payouts", requests.size());

        return requests.stream()
                .map(this::createPayout)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public PayoutResponse generatePayoutForProvider(String providerId, LocalDate fromDate, LocalDate toDate) {
        log.info("Auto-generating payout for provider: {} from {} to {}", providerId, fromDate, toDate);

        // Fetch completed bookings from booking service
        // This would call bookingServiceClient.getCompletedBookings(providerId, fromDate, toDate)

        // For demo, create a sample payout
        PayoutRequest request = PayoutRequest.builder()
                .providerId(providerId)
                .periodStart(fromDate)
                .periodEnd(toDate)
                .totalAmount(15000.0)
                .commissionDeducted(2250.0)
                .taxDeducted(500.0)
                .netAmount(12250.0)
                .paymentMethod("BANK_TRANSFER")
                .withBankAccount("Provider Name", "Bank Name", "1234567890", "IFSC0001234")
                .autoProcess(true)
                .build();

        return createPayout(request);
    }

    // ==================== Get Payout ====================

    @Override
    @Transactional(readOnly = true)
    public PayoutResponse getPayout(String payoutId) {
        Payout payout = findPayoutById(payoutId);
        return payoutMapper.toResponse(payout);
    }

    @Override
    @Transactional(readOnly = true)
    public PayoutResponse getPayoutByNumber(String payoutNumber) {
        Payout payout = payoutRepository.findByPayoutNumber(payoutNumber)
                .orElseThrow(() -> new ResourceNotFoundException("Payout", "number", payoutNumber));
        return payoutMapper.toResponse(payout);
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<PayoutResponse> getProviderPayouts(String providerId, LocalDate fromDate,
                                                           LocalDate toDate, Pageable pageable) {

        Page<Payout> page;
        if (fromDate != null && toDate != null) {
            page = payoutRepository.findByProviderIdAndPeriodBetween(providerId, fromDate, toDate, pageable);
        } else {
            page = payoutRepository.findByProviderIdOrderByCreatedAtDesc(providerId, pageable);
        }

        return PageResponse.<PayoutResponse>builder()
                .content(payoutMapper.toResponseList(page.getContent()))
                .pageNumber(page.getNumber())
                .pageSize(page.getSize())
                .totalElements(page.getTotalElements())
                .totalPages(page.getTotalPages())
                .first(page.isFirst())
                .last(page.isLast())
                .hasNext(page.hasNext())
                .hasPrevious(page.hasPrevious())
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<PayoutResponse> getAllPayouts(LocalDate fromDate, LocalDate toDate,
                                                      String status, Pageable pageable) {
        // This would use specifications for complex queries
        Page<Payout> page = payoutRepository.findAll(pageable);

        return PageResponse.<PayoutResponse>builder()
                .content(payoutMapper.toResponseList(page.getContent()))
                .pageNumber(page.getNumber())
                .pageSize(page.getSize())
                .totalElements(page.getTotalElements())
                .totalPages(page.getTotalPages())
                .first(page.isFirst())
                .last(page.isLast())
                .hasNext(page.hasNext())
                .hasPrevious(page.hasPrevious())
                .build();
    }

    // ==================== Process Payout ====================

    @Override
    @Transactional
    public PayoutResponse processPayout(String payoutId) {
        log.info("Processing payout: {}", payoutId);

        Payout payout = findPayoutById(payoutId);

        if (!"PENDING".equals(payout.getStatus())) {
            throw new BusinessException(ErrorCode.PAYMENT_INVALID_STATUS,
                    "Payout is not in PENDING state. Current state: " + payout.getStatus());
        }

        try {
            // Get payment gateway
            PaymentGateway gateway = gatewayFactory.getGateway("DEFAULT");

            // Prepare bank details for gateway
            Map<String, Object> bankDetails = prepareBankDetailsForGateway(payout);

            // Create payout in gateway
            String gatewayPayoutId = gateway.createPayout(
                    payout.getProviderId(),
                    payout.getNetAmount(),
                    bankDetails
            );

            payout.setStatus("PROCESSING");
            payout.setGatewayPayoutId(gatewayPayoutId);
            payout.setRequestedAt(Instant.now());
            payout = payoutRepository.save(payout);

            log.info("Payout {} sent to gateway with ID: {}", payoutId, gatewayPayoutId);

            // Publish event
            eventProducer.publishPayoutProcessing(payout);

        } catch (Exception e) {
            log.error("Failed to process payout {}: {}", payoutId, e.getMessage());
            return markAsFailed(payoutId, e.getMessage(), "GATEWAY_ERROR");
        }

        return payoutMapper.toResponse(payout);
    }

    @Override
    @Transactional
    public List<PayoutResponse> batchProcessPayouts(List<String> payoutIds) {
        log.info("Batch processing {} payouts", payoutIds.size());

        return payoutIds.stream()
                .map(this::processPayout)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public int processAllPendingPayouts() {
        log.info("Processing all pending payouts");

        List<Payout> pendingPayouts = payoutRepository.findPendingPayouts(LocalDate.now());

        int successCount = 0;
        for (Payout payout : pendingPayouts) {
            try {
                processPayout(payout.getId());
                successCount++;
            } catch (Exception e) {
                log.error("Failed to process payout {}: {}", payout.getId(), e.getMessage());
            }
        }

        log.info("Processed {} out of {} pending payouts", successCount, pendingPayouts.size());
        return successCount;
    }

    // ==================== Pending Payouts ====================

    @Override
    @Transactional(readOnly = true)
    public List<PayoutResponse> getPendingPayouts() {
        return payoutRepository.findPendingPayouts(LocalDate.now())
                .stream()
                .map(payoutMapper::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<PayoutResponse> getPendingPayoutsForProvider(String providerId) {
        return payoutRepository.findByProviderIdAndStatus(providerId, "PENDING")
                .stream()
                .map(payoutMapper::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public long getPendingPayoutsCount() {
        return payoutRepository.countByStatus("PENDING");
    }

    // ==================== Update Payout Status ====================

    @Override
    @Transactional
    public PayoutResponse updatePayoutStatus(String payoutId, String status, String gatewayReference) {
        log.info("Updating payout {} status to: {}", payoutId, status);

        Payout payout = findPayoutById(payoutId);

        payout.setStatus(status);
        payout.setGatewayReference(gatewayReference);

        if ("SUCCESS".equals(status)) {
            payout.setProcessedAt(Instant.now());
            payout.setCompletedAt(Instant.now());

            // Send notification
            notificationService.sendPayoutSuccessNotification(
                    payout.getProviderId(),
                    payout.getNetAmount(),
                    payout.getId()
            );

            // Publish event
            eventProducer.publishPayoutSuccess(payout);

        } else if ("FAILED".equals(status)) {
            payout.setCompletedAt(Instant.now());

            // Send notification
            notificationService.sendPayoutFailedNotification(
                    payout.getProviderId(),
                    payout.getNetAmount(),
                    payout.getId(),
                    payout.getFailureReason()
            );
        }

        payout = payoutRepository.save(payout);

        return payoutMapper.toResponse(payout);
    }

    @Override
    @Transactional
    public PayoutResponse markAsSuccessful(String payoutId, String gatewayReference) {
        return updatePayoutStatus(payoutId, "SUCCESS", gatewayReference);
    }

    @Override
    @Transactional
    public PayoutResponse markAsFailed(String payoutId, String reason, String failureCode) {
        log.info("Marking payout {} as failed. Reason: {}", payoutId, reason);

        Payout payout = findPayoutById(payoutId);

        payout.setStatus("FAILED");
        payout.setFailureReason(reason);
        payout.setFailureCode(failureCode);
        payout.setCompletedAt(Instant.now());

        payout = payoutRepository.save(payout);

        // Send notification
        notificationService.sendPayoutFailedNotification(
                payout.getProviderId(),
                payout.getNetAmount(),
                payout.getId(),
                reason
        );

        // Publish event
        eventProducer.publishPayoutFailed(payout, reason, failureCode);

        return payoutMapper.toResponse(payout);
    }

    @Override
    @Transactional
    public PayoutResponse markAsProcessing(String payoutId, String gatewayPayoutId) {
        log.info("Marking payout {} as processing with gateway ID: {}", payoutId, gatewayPayoutId);

        Payout payout = findPayoutById(payoutId);

        payout.setStatus("PROCESSING");
        payout.setGatewayPayoutId(gatewayPayoutId);
        payout.setRequestedAt(Instant.now());

        payout = payoutRepository.save(payout);

        return payoutMapper.toResponse(payout);
    }

    @Override
    @Transactional
    public PayoutResponse cancelPayout(String payoutId, String reason) {
        log.info("Cancelling payout {}: {}", payoutId, reason);

        Payout payout = findPayoutById(payoutId);

        if (!"PENDING".equals(payout.getStatus())) {
            throw new BusinessException(ErrorCode.PAYMENT_INVALID_STATUS,
                    "Only pending payouts can be cancelled");
        }

        payout.setStatus("CANCELLED");
        payout.setFailureReason(reason);
        payout.setCompletedAt(Instant.now());

        payout = payoutRepository.save(payout);

        // Send notification
        notificationService.sendPayoutCancelledNotification(
                payout.getProviderId(),
                payout.getNetAmount(),
                payout.getId(),
                reason
        );

        return payoutMapper.toResponse(payout);
    }

    // ==================== Retry Payout ====================

    @Override
    @Transactional
    public PayoutResponse retryPayout(String payoutId) {
        log.info("Retrying failed payout: {}", payoutId);

        Payout payout = findPayoutById(payoutId);

        if (!"FAILED".equals(payout.getStatus())) {
            throw new BusinessException(ErrorCode.PAYMENT_INVALID_STATUS,
                    "Only failed payouts can be retried");
        }

        // Increment retry count
        payout.setRetryCount(payout.getRetryCount() + 1);
        payout.setStatus("PENDING");
        payout.setFailureReason(null);
        payout.setFailureCode(null);
        payout.setGatewayPayoutId(null);
        payout.setGatewayReference(null);
        payoutRepository.save(payout);

        return processPayout(payoutId);
    }

    @Override
    @Transactional
    public List<PayoutResponse> bulkRetryPayouts(List<String> payoutIds) {
        log.info("Bulk retrying {} payouts", payoutIds.size());

        return payoutIds.stream()
                .map(this::retryPayout)
                .collect(Collectors.toList());
    }

    // ==================== Summary and Statistics ====================

    @Override
    @Transactional(readOnly = true)
    public PayoutSummary getPayoutSummary(String providerId) {
        List<Payout> payouts = payoutRepository.findByProviderIdOrderByCreatedAtDesc(
                providerId, Pageable.unpaged()).getContent();

        double totalEarned = payouts.stream()
                .filter(p -> "SUCCESS".equals(p.getStatus()))
                .mapToDouble(Payout::getNetAmount)
                .sum();

        double totalPaid = payouts.stream()
                .filter(p -> "SUCCESS".equals(p.getStatus()) || "SETTLED".equals(p.getStatus()))
                .mapToDouble(Payout::getNetAmount)
                .sum();

        double pendingAmount = payouts.stream()
                .filter(p -> "PENDING".equals(p.getStatus()) || "PROCESSING".equals(p.getStatus()))
                .mapToDouble(Payout::getNetAmount)
                .sum();

        long totalPayouts = payouts.size();
        long successfulPayouts = payouts.stream()
                .filter(p -> "SUCCESS".equals(p.getStatus()))
                .count();
        long failedPayouts = payouts.stream()
                .filter(p -> "FAILED".equals(p.getStatus()))
                .count();
        long pendingPayouts = payouts.stream()
                .filter(p -> "PENDING".equals(p.getStatus()))
                .count();

        Optional<Payout> lastPayout = payouts.stream()
                .filter(p -> "SUCCESS".equals(p.getStatus()))
                .findFirst();

        Double averagePayoutAmount = successfulPayouts > 0 ? totalEarned / successfulPayouts : 0.0;

        return PayoutSummary.builder()
                .totalEarned(totalEarned)
                .totalPaid(totalPaid)
                .pendingAmount(pendingAmount)
                .totalPayouts((double) totalPayouts)
                .successfulPayouts(successfulPayouts)
                .failedPayouts(failedPayouts)
                .pendingPayouts(pendingPayouts)
                .lastPayoutDate(lastPayout.map(p -> p.getCreatedAt().atZone(java.time.ZoneId.systemDefault()).toLocalDate()).orElse(null))
                .lastPayoutAmount(lastPayout.map(Payout::getNetAmount).orElse(0.0))
                .averagePayoutAmount(averagePayoutAmount)
                .monthlyBreakdown(getMonthlyPayoutBreakdown(providerId, LocalDate.now().getYear()))
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public PayoutStatistics getPayoutStatistics(LocalDate fromDate, LocalDate toDate) {
        List<Payout> payouts = payoutRepository.findAll(); // In production, filter by date range

        long totalPayouts = payouts.size();
        double totalAmount = payouts.stream()
                .mapToDouble(Payout::getNetAmount)
                .sum();
        double totalCommission = payouts.stream()
                .mapToDouble(p -> p.getCommissionDeducted() != null ? p.getCommissionDeducted() : 0.0)
                .sum();
        double totalTax = payouts.stream()
                .mapToDouble(p -> p.getTaxDeducted() != null ? p.getTaxDeducted() : 0.0)
                .sum();

        long uniqueProviders = payouts.stream()
                .map(Payout::getProviderId)
                .distinct()
                .count();

        Map<String, Long> statusDistribution = payouts.stream()
                .collect(Collectors.groupingBy(Payout::getStatus, Collectors.counting()));

        return PayoutStatistics.builder()
                .totalPayouts(totalPayouts)
                .totalAmount(totalAmount)
                .totalCommission(totalCommission)
                .totalTax(totalTax)
                .uniqueProviders(uniqueProviders)
                .statusDistribution(statusDistribution)
                .averagePayoutPerProvider(uniqueProviders > 0 ? totalAmount / uniqueProviders : 0.0)
                .minPayout(payouts.stream().mapToDouble(Payout::getNetAmount).min().orElse(0.0))
                .maxPayout(payouts.stream().mapToDouble(Payout::getNetAmount).max().orElse(0.0))
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public Map<String, Double> getMonthlyPayoutBreakdown(String providerId, int year) {
        List<Payout> payouts = payoutRepository.findByProviderIdOrderByCreatedAtDesc(
                providerId, Pageable.unpaged()).getContent();

        Map<String, Double> monthlyBreakdown = new LinkedHashMap<>();

        for (int month = 1; month <= 12; month++) {

            final int currentMonth = month;   // FIX

            String monthKey = String.format("%02d", currentMonth);

            double monthlyTotal = payouts.stream()
                    .filter(p -> "SUCCESS".equals(p.getStatus()))
                    .filter(p -> p.getCreatedAt().atZone(java.time.ZoneId.systemDefault()).getYear() == year)
                    .filter(p -> p.getCreatedAt().atZone(java.time.ZoneId.systemDefault()).getMonthValue() == currentMonth)
                    .mapToDouble(Payout::getNetAmount)
                    .sum();

            monthlyBreakdown.put(monthKey, monthlyTotal);
        }

        return monthlyBreakdown;
    }

    // ==================== Reports ====================

    @Override
    public byte[] generatePayoutReport(String providerId, LocalDate fromDate, LocalDate toDate, String format) {
        // Implementation for report generation
        log.info("Generating payout report for provider: {}, from: {}, to: {}, format: {}",
                providerId, fromDate, toDate, format);
        return new byte[0];
    }

    @Override
    public byte[] exportPayoutsToCsv(List<String> payoutIds) {
        // Implementation for CSV export
        log.info("Exporting {} payouts to CSV", payoutIds.size());
        return new byte[0];
    }

    // ==================== Validation ====================

    @Override
    public boolean validateBankAccount(BankAccountDetails accountDetails) {
        if (accountDetails == null) return false;

        // Basic validation
        if (accountDetails.getAccountNumber() == null ||
                accountDetails.getAccountNumber().length() < 9 ||
                accountDetails.getAccountNumber().length() > 18) {
            return false;
        }

        if (accountDetails.getIfscCode() == null ||
                !accountDetails.getIfscCode().matches("^[A-Z]{4}0[A-Z0-9]{6}$")) {
            return false;
        }

        return true;
    }

    @Override
    public boolean validateUpiDetails(PayoutRequest.UpiDetails upiDetails) {
        if (upiDetails == null || upiDetails.getUpiId() == null) return false;

        return upiDetails.getUpiId().matches("^[a-zA-Z0-9._-]+@[a-zA-Z0-9]+$");
    }

    // ==================== Settlement ====================

    @Override
    @Transactional
    public PayoutResponse markAsSettled(String payoutId, String settlementReference) {
        log.info("Marking payout {} as settled. Reference: {}", payoutId, settlementReference);

        Payout payout = findPayoutById(payoutId);

        payout.setSettlementStatus("SETTLED");
        payout.setSettlementReference(settlementReference);
        payout.setActualSettlementDate(LocalDate.from(Instant.now()));

        payout = payoutRepository.save(payout);

        return payoutMapper.toResponse(payout);
    }

    @Override
    @Transactional(readOnly = true)
    public List<PayoutResponse> getUnsettledPayouts() {
        return payoutRepository.findBySettlementStatusNot("SETTLED")
                .stream()
                .map(payoutMapper::toResponse)
                .collect(Collectors.toList());
    }

    // ==================== Search ====================

    @Override
    @Transactional(readOnly = true)
    public PageResponse<PayoutResponse> searchPayouts(String providerId, String status,
                                                      LocalDate fromDate, LocalDate toDate,
                                                      Double minAmount, Double maxAmount,
                                                      Pageable pageable) {
        // This would use specifications for complex queries
        Page<Payout> page = payoutRepository.findAll(pageable);

        return PageResponse.<PayoutResponse>builder()
                .content(payoutMapper.toResponseList(page.getContent()))
                .pageNumber(page.getNumber())
                .pageSize(page.getSize())
                .totalElements(page.getTotalElements())
                .totalPages(page.getTotalPages())
                .first(page.isFirst())
                .last(page.isLast())
                .hasNext(page.hasNext())
                .hasPrevious(page.hasPrevious())
                .build();
    }

    // ==================== Private Helper Methods ====================

    private Payout findPayoutById(String payoutId) {
        return payoutRepository.findById(payoutId)
                .orElseThrow(() -> new ResourceNotFoundException("Payout", payoutId));
    }

    private String generatePayoutNumber() {
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmm"));
        String random = UUID.randomUUID().toString().substring(0, 4).toUpperCase();
        return "PO" + timestamp + random;
    }

    private boolean isValidNetAmount(PayoutRequest request) {
        if (request.getTotalAmount() == null || request.getNetAmount() == null) return true;

        double deductions = (request.getCommissionDeducted() != null ? request.getCommissionDeducted() : 0) +
                (request.getTaxDeducted() != null ? request.getTaxDeducted() : 0) +
                (request.getProcessingFee() != null ? request.getProcessingFee() : 0) +
                (request.getGatewayFee() != null ? request.getGatewayFee() : 0);

        double calculatedNet = request.getTotalAmount() - deductions;
        return Math.abs(calculatedNet - request.getNetAmount()) < 0.01;
    }

    private Map<String, Object> convertBankDetails(BankAccountDetails bankDetails) {
        if (bankDetails == null) return null;

        Map<String, Object> details = new HashMap<>();
        details.put("accountHolderName", bankDetails.getAccountHolderName());
        details.put("bankName", bankDetails.getBankName());
        details.put("accountNumber", maskAccountNumber(bankDetails.getAccountNumber()));
        details.put("ifscCode", bankDetails.getIfscCode());
        details.put("branchName", bankDetails.getBranchName());
        details.put("accountType", bankDetails.getAccountType());
        details.put("routingNumber", bankDetails.getRoutingNumber());
        details.put("swiftCode", bankDetails.getSwiftCode());
        details.put("iban", bankDetails.getIban());
        return details;
    }

    private Map<String, Object> convertUpiDetails(PayoutRequest.UpiDetails upiDetails) {
        if (upiDetails == null) return null;

        Map<String, Object> details = new HashMap<>();
        details.put("upiId", upiDetails.getUpiId());
        details.put("vpa", upiDetails.getVpa());
        details.put("upiAppName", upiDetails.getUpiAppName());
        details.put("qrCodeUrl", upiDetails.getQrCodeUrl());
        return details;
    }

    private Map<String, Object> prepareBankDetailsForGateway(Payout payout) {
        Map<String, Object> bankDetails = new HashMap<>();

        if (payout.getAccountDetails() != null) {
            bankDetails.putAll(payout.getAccountDetails());
        } else if (payout.getUpiDetails() != null) {
            bankDetails.putAll((Map<? extends String, ?>) payout.getUpiDetails());
        }

        bankDetails.put("amount", payout.getNetAmount());
        bankDetails.put("currency", payout.getCurrency());
        bankDetails.put("providerId", payout.getProviderId());

        return bankDetails;
    }

    private String maskAccountNumber(String accountNumber) {
        if (accountNumber == null || accountNumber.length() < 4) {
            return accountNumber;
        }
        return "XXXX" + accountNumber.substring(accountNumber.length() - 4);
    }
}