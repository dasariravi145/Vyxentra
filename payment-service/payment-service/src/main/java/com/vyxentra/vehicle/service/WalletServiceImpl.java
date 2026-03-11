package com.vyxentra.vehicle.service;

import com.vyxentra.vehicle.dto.request.CreatePaymentRequest;
import com.vyxentra.vehicle.dto.request.WalletTopupRequest;
import com.vyxentra.vehicle.dto.response.PaymentResponse;
import com.vyxentra.vehicle.dto.response.WalletResponse;
import com.vyxentra.vehicle.dto.response.WalletTransactionResponse;
import com.vyxentra.vehicle.entity.Wallet;
import com.vyxentra.vehicle.entity.WalletTransaction;
import com.vyxentra.vehicle.exceptions.BusinessException;
import com.vyxentra.vehicle.exceptions.ErrorCode;
import com.vyxentra.vehicle.exceptions.ResourceNotFoundException;
import com.vyxentra.vehicle.kafka.PaymentEventProducer;
import com.vyxentra.vehicle.mapper.WalletMapper;
import com.vyxentra.vehicle.repository.WalletRepository;
import com.vyxentra.vehicle.repository.WalletTransactionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class WalletServiceImpl implements WalletService {

    private final WalletRepository walletRepository;
    private final WalletTransactionRepository transactionRepository;
    private final WalletMapper walletMapper;
    private final PaymentService paymentService;
    private final PaymentEventProducer eventProducer;

    @Value("${payment.wallet.max-balance:50000}")
    private double maxBalance;

    @Value("${payment.wallet.min-topup:100}")
    private double minTopup;

    @Override
    @Transactional
    public WalletResponse createWallet(String userId, String userType) {
        log.info("Creating wallet for user: {} type: {}", userId, userType);

        // Check if wallet already exists
        if (walletRepository.findByUserId(userId).isPresent()) {
            throw new BusinessException(ErrorCode.VALIDATION_ERROR, "Wallet already exists for this user");
        }

        Wallet wallet = Wallet.builder()
                .userId(userId)
                .userType(userType)
                .balance(0.0)
                .totalCredited(0.0)
                .totalDebited(0.0)
                .status("ACTIVE")
                .build();

        wallet = walletRepository.save(wallet);

        log.info("Wallet created with ID: {}", wallet.getId());

        return walletMapper.toResponse(wallet);
    }

    @Override
    @Transactional(readOnly = true)
    public WalletResponse getWallet(String walletId) {
        Wallet wallet = walletRepository.findById(walletId)
                .orElseThrow(() -> new ResourceNotFoundException("Wallet", walletId));
        return walletMapper.toResponse(wallet);
    }

    @Override
    @Transactional(readOnly = true)
    public WalletResponse getWalletByUserId(String userId) {
        Wallet wallet = walletRepository.findByUserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Wallet", "user", userId));
        return walletMapper.toResponse(wallet);
    }

    @Override
    @Transactional
    public WalletResponse topupWallet(String userId, WalletTopupRequest request) {
        log.info("Topping up wallet for user: {} amount: {}", userId, request.getAmount());

        if (request.getAmount() < minTopup) {
            throw new BusinessException(ErrorCode.VALIDATION_ERROR,
                    "Minimum topup amount is " + minTopup);
        }

        Wallet wallet = walletRepository.findByUserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Wallet", "user", userId));

        if (wallet.getBalance() + request.getAmount() > maxBalance) {
            throw new BusinessException(ErrorCode.VALIDATION_ERROR,
                    "Maximum wallet balance is " + maxBalance);
        }

        // Create payment for topup
        CreatePaymentRequest paymentRequest = CreatePaymentRequest.builder()
                .bookingId("WALLET_TOPUP")
                .amount(request.getAmount())
                .paymentMethod(request.getPaymentMethod())
                .paymentMethodDetails(request.getPaymentMethodDetails())
                .description("Wallet topup")
                .build();

        PaymentResponse payment = paymentService.createPayment(userId, paymentRequest);

        // Process payment
        // In real implementation, would wait for payment success webhook

        return walletMapper.toResponse(wallet);
    }

    @Override
    @Transactional
    public WalletResponse debitWallet(String walletId, Double amount, String referenceId,
                                      String referenceType, String description) {
        log.info("Debiting wallet: {} amount: {}", walletId, amount);

        Wallet wallet = walletRepository.findById(walletId)
                .orElseThrow(() -> new ResourceNotFoundException("Wallet", walletId));

        if (!"ACTIVE".equals(wallet.getStatus())) {
            throw new BusinessException(ErrorCode.PAYMENT_FAILED, "Wallet is " + wallet.getStatus());
        }

        if (wallet.getBalance() < amount) {
            throw new BusinessException(ErrorCode.INSUFFICIENT_BALANCE,
                    "Insufficient balance. Available: " + wallet.getBalance());
        }

        LocalDateTime now = LocalDateTime.now();

        // Debit balance
        int updated = walletRepository.debitBalance(walletId, amount, now);
        if (updated == 0) {
            throw new BusinessException(ErrorCode.INSUFFICIENT_BALANCE, "Failed to debit wallet");
        }

        // Create transaction record
        WalletTransaction transaction = WalletTransaction.builder()
                .wallet(wallet)
                .transactionNumber(generateTransactionNumber())
                .type("DEBIT")
                .amount(amount)
                .balanceAfter(wallet.getBalance() - amount)
                .referenceId(referenceId)
                .referenceType(referenceType)
                .description(description)
                .status("SUCCESS")
                .build();

        transactionRepository.save(transaction);

        // Refresh wallet data
        wallet = walletRepository.findById(walletId).get();

        return walletMapper.toResponse(wallet);
    }

    @Override
    @Transactional
    public WalletResponse creditWallet(String walletId, Double amount, String referenceId,
                                       String referenceType, String description) {
        log.info("Crediting wallet: {} amount: {}", walletId, amount);

        Wallet wallet = walletRepository.findById(walletId)
                .orElseThrow(() -> new ResourceNotFoundException("Wallet", walletId));

        if (!"ACTIVE".equals(wallet.getStatus())) {
            throw new BusinessException(ErrorCode.PAYMENT_FAILED, "Wallet is " + wallet.getStatus());
        }

        LocalDateTime now = LocalDateTime.now();

        // Credit balance
        walletRepository.creditBalance(walletId, amount, now);

        // Create transaction record
        WalletTransaction transaction = WalletTransaction.builder()
                .wallet(wallet)
                .transactionNumber(generateTransactionNumber())
                .type("CREDIT")
                .amount(amount)
                .balanceAfter(wallet.getBalance() + amount)
                .referenceId(referenceId)
                .referenceType(referenceType)
                .description(description)
                .status("SUCCESS")
                .build();

        transactionRepository.save(transaction);

        // Refresh wallet data
        wallet = walletRepository.findById(walletId).get();

        return walletMapper.toResponse(wallet);
    }

    @Override
    @Transactional(readOnly = true)
    public List<WalletTransactionResponse> getWalletTransactions(String userId, Pageable pageable) {
        Wallet wallet = walletRepository.findByUserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Wallet", "user", userId));

        return transactionRepository.findByWalletIdOrderByCreatedAtDesc(wallet.getId()).stream()
                .map(walletMapper::toTransactionResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public Double getWalletBalance(String userId) {
        return walletRepository.findByUserId(userId)
                .map(Wallet::getBalance)
                .orElse(0.0);
    }

    @Override
    @Transactional
    public void blockWallet(String walletId, String reason) {
        log.info("Blocking wallet: {} reason: {}", walletId, reason);

        Wallet wallet = walletRepository.findById(walletId)
                .orElseThrow(() -> new ResourceNotFoundException("Wallet", walletId));

        wallet.setStatus("BLOCKED");
        walletRepository.save(wallet);

        log.info("Wallet blocked: {}", walletId);
    }

    private String generateTransactionNumber() {
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmm"));
        String random = UUID.randomUUID().toString().substring(0, 4).toUpperCase();
        return "WLT" + timestamp + random;
    }
}
