package com.vyxentra.vehicle.service;

import com.vyxentra.vehicle.client.BookingServiceClient;
import com.vyxentra.vehicle.client.NotificationServiceClient;
import com.vyxentra.vehicle.dto.request.CreatePaymentRequest;
import com.vyxentra.vehicle.dto.request.ProcessPaymentRequest;
import com.vyxentra.vehicle.dto.response.PageResponse;
import com.vyxentra.vehicle.dto.response.PaymentDetailResponse;
import com.vyxentra.vehicle.dto.response.PaymentResponse;
import com.vyxentra.vehicle.dto.response.TransactionResponse;
import com.vyxentra.vehicle.entity.Payment;
import com.vyxentra.vehicle.entity.Wallet;
import com.vyxentra.vehicle.exceptions.BusinessException;
import com.vyxentra.vehicle.exceptions.ErrorCode;
import com.vyxentra.vehicle.exceptions.ResourceNotFoundException;
import com.vyxentra.vehicle.gateway.PaymentGateway;
import com.vyxentra.vehicle.gateway.PaymentGatewayFactory;
import com.vyxentra.vehicle.kafka.PaymentEventProducer;
import com.vyxentra.vehicle.mapper.PaymentMapper;
import com.vyxentra.vehicle.repository.PaymentRepository;
import com.vyxentra.vehicle.repository.WalletRepository;
import com.vyxentra.vehicle.validator.PaymentValidator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class PaymentServiceImpl implements PaymentService {

    private final PaymentRepository paymentRepository;
    private final WalletRepository walletRepository;
    private final PaymentMapper paymentMapper;
    private final PaymentValidator paymentValidator;
    private final PaymentGatewayFactory gatewayFactory;
    private final PaymentEventProducer eventProducer;
    private final CommissionService commissionService;

    private final BookingServiceClient bookingServiceClient;
    private final NotificationServiceClient notificationServiceClient;

    @Value("${payment.commission.percentage:15}")
    private int commissionPercentage;

    @Value("${payment.wallet.enabled:true}")
    private boolean walletEnabled;

    @Override
    @Transactional
    public PaymentResponse createPayment(String customerId, CreatePaymentRequest request) {
        log.info("Creating payment for booking: {} amount: {}", request.getBookingId(), request.getAmount());

        // Validate booking exists and is in correct state
        paymentValidator.validateBookingForPayment(request.getBookingId(), request.getAmount());

        // Check if payment already exists for this booking
        paymentRepository.findSuccessfulPaymentByBookingId(request.getBookingId())
                .ifPresent(p -> {
                    throw new BusinessException(ErrorCode.PAYMENT_ALREADY_PROCESSED,
                            "Payment already exists for this booking");
                });

        // Generate payment number
        String paymentNumber = generatePaymentNumber();

        // Calculate commission
        double commission = commissionService.calculateCommission(request.getAmount(), request.getBookingId());
        double providerAmount = request.getAmount() - commission;

        // Determine payment gateway
        String gateway = request.getPaymentGateway() != null ?
                request.getPaymentGateway() : getDefaultGateway();

        // Create payment record
        Payment payment = Payment.builder()
                .paymentNumber(paymentNumber)
                .bookingId(request.getBookingId())
                .customerId(customerId)
                .amount(request.getAmount())
                .commissionAmount(commission)
                .providerAmount(providerAmount)
                .paymentMethod(request.getPaymentMethod())
                .paymentGateway(gateway)
                .status("PENDING")
                .paymentType("BOOKING")
                .description(request.getDescription())
                .retryCount(0)
                .build();

        payment = paymentRepository.save(payment);

        // If wallet payment, process immediately
        if ("WALLET".equals(request.getPaymentMethod()) && walletEnabled) {
            processWalletPayment(payment, customerId);
        } else {
            // Create order in payment gateway
            PaymentGateway paymentGateway = gatewayFactory.getGateway(gateway);
            Map<String, Object> gatewayResponse = paymentGateway.createOrder(payment);

            payment.setGatewayOrderId((String) gatewayResponse.get("orderId"));
            payment.setMetadata(gatewayResponse);
            paymentRepository.save(payment);
        }

        log.info("Payment created with ID: {}, Number: {}", payment.getId(), paymentNumber);

        return paymentMapper.toResponse(payment);
    }

    @Override
    @Transactional
    public PaymentResponse processPayment(String paymentId, ProcessPaymentRequest request) {
        log.info("Processing payment: {}", paymentId);

        Payment payment = findPaymentById(paymentId);

        if (!"PENDING".equals(payment.getStatus())) {
            throw new BusinessException(ErrorCode.PAYMENT_ALREADY_PROCESSED,
                    "Payment already in " + payment.getStatus() + " state");
        }

        try {
            // Process via payment gateway
            PaymentGateway paymentGateway = gatewayFactory.getGateway(payment.getPaymentGateway());
            Map<String, Object> gatewayResponse = paymentGateway.processPayment(payment, request);

            boolean success = (boolean) gatewayResponse.getOrDefault("success", false);

            if (success) {
                // Update payment
                payment.setStatus("SUCCESS");
                payment.setGatewayPaymentId((String) gatewayResponse.get("paymentId"));
                payment.setMetadata(gatewayResponse);

                // Update booking payment status via Feign
                bookingServiceClient.updatePaymentStatus(payment.getBookingId(), "SUCCESS");

                // Publish event
                eventProducer.publishPaymentSuccess(payment);

                // Send notification
                notificationServiceClient.sendPaymentSuccessNotification(
                        payment.getCustomerId(), payment.getBookingId(), payment.getAmount());

                log.info("Payment processed successfully: {}", paymentId);
            } else {
                payment.setStatus("FAILED");
                payment.setErrorMessage((String) gatewayResponse.get("errorMessage"));
                payment.setErrorCode((String) gatewayResponse.get("errorCode"));

                log.warn("Payment failed: {} - {}", paymentId, gatewayResponse.get("errorMessage"));
            }

        } catch (Exception e) {
            log.error("Error processing payment: {}", e.getMessage());
            payment.setStatus("FAILED");
            payment.setErrorMessage(e.getMessage());
            payment.setErrorCode("GATEWAY_ERROR");
        }

        payment = paymentRepository.save(payment);

        return paymentMapper.toResponse(payment);
    }

    @Override
    @Transactional(readOnly = true)
    public PaymentDetailResponse getPayment(String paymentId) {
        Payment payment = findPaymentById(paymentId);
        return paymentMapper.toDetailResponse(payment);
    }

    @Override
    @Transactional(readOnly = true)
    public PaymentDetailResponse getPaymentByNumber(String paymentNumber) {
        Payment payment = paymentRepository.findByPaymentNumber(paymentNumber)
                .orElseThrow(() -> new ResourceNotFoundException("Payment", "number", paymentNumber));
        return paymentMapper.toDetailResponse(payment);
    }

    @Override
    @Transactional(readOnly = true)
    public List<PaymentResponse> getBookingPayments(String bookingId) {
        List<Payment> payments = paymentRepository.findByBookingIdOrderByCreatedAtDesc(bookingId);
        return paymentMapper.toResponseList(payments);
    }

    @Override
    @Transactional(readOnly = true)
    public List<PaymentResponse> getCustomerPayments(String customerId, LocalDateTime fromDate, LocalDateTime toDate) {
        List<Payment> payments = paymentRepository.findCustomerPayments(customerId, fromDate, toDate);
        return paymentMapper.toResponseList(payments);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean verifyPayment(String paymentId) {
        Payment payment = findPaymentById(paymentId);

        if (!"SUCCESS".equals(payment.getStatus())) {
            return false;
        }

        // Verify with gateway
        PaymentGateway paymentGateway = gatewayFactory.getGateway(payment.getPaymentGateway());
        return paymentGateway.verifyPayment(payment);
    }

    @Override
    @Transactional
    public void handleWebhook(String gateway, String payload, String signature) {
        log.info("Received webhook from gateway: {}", gateway);

        PaymentGateway paymentGateway = gatewayFactory.getGateway(gateway);
        Map<String, Object> webhookData = paymentGateway.processWebhook(payload, signature);

        String eventType = (String) webhookData.get("event");
        String paymentId = (String) webhookData.get("paymentId");

        if ("payment.success".equals(eventType) && paymentId != null) {
            paymentRepository.findByGatewayPaymentId(paymentId).ifPresent(payment -> {
                payment.setStatus("SUCCESS");
                paymentRepository.save(payment);

                // Update booking
                bookingServiceClient.updatePaymentStatus(payment.getBookingId(), "SUCCESS");

                // Publish event
                eventProducer.publishPaymentSuccess(payment);

                log.info("Payment verified via webhook: {}", paymentId);
            });
        }
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<TransactionResponse> getUserTransactions(String userId, Pageable pageable) {
        // This would combine payments and wallet transactions
        // For now, just return payments
        List<Payment> payments = paymentRepository.findByCustomerIdOrderByCreatedAtDesc(userId);

        List<TransactionResponse> transactions = payments.stream()
                .map(p -> TransactionResponse.builder()
                        .transactionId(p.getId())
                        .transactionNumber(p.getPaymentNumber())
                        .type("PAYMENT")
                        .amount(p.getAmount())
                        .status(p.getStatus())
                        .referenceId(p.getBookingId())
                        .createdAt(p.getCreatedAt())
                        .build())
                .toList();

        return PageResponse.<TransactionResponse>builder()
                .content(transactions)
                .pageNumber(pageable.getPageNumber())
                .pageSize(pageable.getPageSize())
                .totalElements(transactions.size())
                .totalPages(1)
                .first(true)
                .last(true)
                .hasNext(false)
                .hasPrevious(false)
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public String getPaymentStatus(String bookingId) {
        return paymentRepository.findSuccessfulPaymentByBookingId(bookingId)
                .map(Payment::getStatus)
                .orElse("PENDING");
    }

    @Override
    @Transactional
    public void processExpiredPayments() {
        log.info("Processing expired pending payments");

        LocalDateTime timeout = LocalDateTime.now().minusMinutes(30);
        List<Payment> expiredPayments = paymentRepository.findExpiredPendingPayments(timeout);

        for (Payment payment : expiredPayments) {
            payment.setStatus("EXPIRED");
            paymentRepository.save(payment);

            log.info("Payment expired: {}", payment.getId());
        }
    }

    private void processWalletPayment(Payment payment, String customerId) {
        Wallet wallet = walletRepository.findByUserId(customerId)
                .orElseThrow(() -> new BusinessException(ErrorCode.PAYMENT_FAILED,
                        "Wallet not found for user"));

        if (wallet.getBalance() < payment.getAmount()) {
            throw new BusinessException(ErrorCode.INSUFFICIENT_BALANCE,
                    "Insufficient wallet balance");
        }

        // Debit wallet
        int updated = walletRepository.debitBalance(wallet.getId(), payment.getAmount(), LocalDateTime.now());
        if (updated == 0) {
            throw new BusinessException(ErrorCode.INSUFFICIENT_BALANCE,
                    "Failed to debit wallet");
        }

        // Update payment
        payment.setStatus("SUCCESS");
        payment.setGatewayPaymentId("WALLET_" + UUID.randomUUID().toString().substring(0, 8));
        paymentRepository.save(payment);

        // Update booking
        bookingServiceClient.updatePaymentStatus(payment.getBookingId(), "SUCCESS");

        // Publish event
        eventProducer.publishPaymentSuccess(payment);

        log.info("Wallet payment processed successfully: {}", payment.getId());
    }

    private Payment findPaymentById(String paymentId) {
        return paymentRepository.findById(paymentId)
                .orElseThrow(() -> new ResourceNotFoundException("Payment", paymentId));
    }

    private String generatePaymentNumber() {
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmm"));
        String random = UUID.randomUUID().toString().substring(0, 4).toUpperCase();
        return "PAY" + timestamp + random;
    }

    private String getDefaultGateway() {
        return "RAZORPAY";
    }
}
