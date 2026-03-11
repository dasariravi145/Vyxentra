package com.vyxentra.vehicle.service;
import com.vyxentra.vehicle.dto.request.PaymentMethodRequest;
import com.vyxentra.vehicle.dto.response.PaymentMethodResponse;
import com.vyxentra.vehicle.entity.PaymentMethod;
import com.vyxentra.vehicle.exceptions.BusinessException;
import com.vyxentra.vehicle.exceptions.ErrorCode;
import com.vyxentra.vehicle.exceptions.ResourceNotFoundException;
import com.vyxentra.vehicle.gateway.PaymentGateway;
import com.vyxentra.vehicle.gateway.PaymentGatewayFactory;
import com.vyxentra.vehicle.mapper.PaymentMethodMapper;
import com.vyxentra.vehicle.repository.PaymentMethodRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class PaymentMethodServiceImpl implements PaymentMethodService {

    private final PaymentMethodRepository paymentMethodRepository;
    private final PaymentMethodMapper paymentMethodMapper;
    private final PaymentGatewayFactory gatewayFactory;

    @Override
    @Transactional
    @CacheEvict(value = "paymentMethods", key = "#userId", allEntries = true)
    public PaymentMethodResponse addPaymentMethod(String userId, PaymentMethodRequest request) {
        log.info("Adding payment method for user: {}, type: {}", userId, request.getMethodType());

        // Check if user has reached max payment methods
        long methodCount = paymentMethodRepository.countByUserIdAndIsActiveTrue(userId);
        if (methodCount >= 10) {
            throw new BusinessException(ErrorCode.VALIDATION_ERROR, "Maximum 10 payment methods allowed per user");
        }

        // Create payment method entity
        PaymentMethod paymentMethod = paymentMethodMapper.toEntity(request);
        paymentMethod.setUserId(userId);
        paymentMethod.setUserType(determineUserType(userId));

        // Set default if this is the first method or if requested
        boolean isFirst = methodCount == 0;
        if (isFirst || Boolean.TRUE.equals(request.getIsDefault())) {
            paymentMethodRepository.resetDefaultForUser(userId);
            paymentMethod.setIsDefault(true);
        } else {
            paymentMethod.setIsDefault(false);
        }

        // Process based on method type
        switch (request.getMethodType()) {
            case "CARD":
                processCardDetails(paymentMethod, request);
                break;
            case "UPI":
                processUPIDetails(paymentMethod, request);
                break;
            case "NETBANKING":
                processNetbankingDetails(paymentMethod, request);
                break;
            default:
                throw new BusinessException(ErrorCode.VALIDATION_ERROR, "Unsupported payment method: " + request.getMethodType());
        }

        // Tokenize with gateway
        tokenizeWithGateway(paymentMethod, request);

        paymentMethod = paymentMethodRepository.save(paymentMethod);

        log.info("Payment method added successfully with ID: {}", paymentMethod.getId());

        return paymentMethodMapper.toMaskedResponse(paymentMethod);
    }

    @Override
    @Cacheable(value = "paymentMethods", key = "#userId")
    public List<PaymentMethodResponse> getUserPaymentMethods(String userId) {
        log.debug("Getting payment methods for user: {}", userId);

        return paymentMethodRepository.findByUserIdAndIsActiveTrue(userId)
                .stream()
                .map(paymentMethodMapper::toMaskedResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Cacheable(value = "paymentMethod", key = "#methodId", unless = "#result == null")
    public PaymentMethodResponse getPaymentMethod(String methodId, String userId) {
        log.debug("Getting payment method: {}", methodId);

        PaymentMethod paymentMethod = findPaymentMethodByIdAndUser(methodId, userId);
        return paymentMethodMapper.toMaskedResponse(paymentMethod);
    }

    @Override
    @Transactional
    @CacheEvict(value = {"paymentMethods", "paymentMethod"}, allEntries = true)
    public void deletePaymentMethod(String methodId, String userId) {
        log.info("Deleting payment method: {} for user: {}", methodId, userId);

        PaymentMethod paymentMethod = findPaymentMethodByIdAndUser(methodId, userId);

        // Soft delete
        paymentMethod.setIsActive(false);

        // If this was default, set another as default
        if (paymentMethod.getIsDefault()) {
            paymentMethodRepository.findByUserIdAndIsActiveTrue(userId)
                    .stream()
                    .findFirst()
                    .ifPresent(newDefault -> {
                        newDefault.setIsDefault(true);
                        paymentMethodRepository.save(newDefault);
                    });
        }

        paymentMethodRepository.save(paymentMethod);

        // Delete from gateway
        deleteFromGateway(paymentMethod);

        log.info("Payment method deleted: {}", methodId);
    }

    @Override
    @Transactional
    @CacheEvict(value = {"paymentMethods", "paymentMethod"}, allEntries = true)
    public void setDefaultPaymentMethod(String methodId, String userId) {
        log.info("Setting default payment method: {} for user: {}", methodId, userId);

        PaymentMethod paymentMethod = findPaymentMethodByIdAndUser(methodId, userId);

        if (paymentMethod.getIsDefault()) {
            log.debug("Payment method {} is already default", methodId);
            return;
        }

        // Reset all to non-default
        paymentMethodRepository.resetDefaultForUser(userId);

        // Set this as default
        paymentMethod.setIsDefault(true);
        paymentMethodRepository.save(paymentMethod);

        log.info("Default payment method set: {}", methodId);
    }

    @Override
    @Transactional
    public PaymentMethodResponse verifyPaymentMethod(String methodId, Map<String, Object> verificationData) {
        log.info("Verifying payment method: {}", methodId);

        PaymentMethod paymentMethod = paymentMethodRepository.findById(methodId)
                .orElseThrow(() -> new ResourceNotFoundException("PaymentMethod", methodId));

        // Implement verification logic (e.g., micro-deposit, OTP verification)
        paymentMethod.setIsVerified(true);
        paymentMethod.setVerifiedAt(Instant.now());

        paymentMethod = paymentMethodRepository.save(paymentMethod);

        return paymentMethodMapper.toMaskedResponse(paymentMethod);
    }

    @Override
    public boolean isPaymentMethodValid(String methodId) {
        PaymentMethod paymentMethod = paymentMethodRepository.findById(methodId)
                .orElseThrow(() -> new ResourceNotFoundException("PaymentMethod", methodId));

        if (!paymentMethod.getIsActive() || !paymentMethod.getIsVerified()) {
            return false;
        }

        // Check if card is expired
        if ("CARD".equals(paymentMethod.getMethodType()) &&
                paymentMethod.getExpiryYear() != null &&
                paymentMethod.getExpiryMonth() != null) {

            YearMonth expiry = YearMonth.of(
                    Integer.parseInt(paymentMethod.getExpiryYear()),
                    Integer.parseInt(paymentMethod.getExpiryMonth())
            );

            if (expiry.isBefore(YearMonth.now())) {
                return false;
            }
        }

        return true;
    }

    @Override
    public PaymentMethodResponse getDefaultPaymentMethod(String userId) {
        log.debug("Getting default payment method for user: {}", userId);

        return paymentMethodRepository.findByUserIdAndIsDefaultTrue(userId)
                .map(paymentMethodMapper::toMaskedResponse)
                .orElse(null);
    }

    @Override
    @Transactional
    public void updatePaymentMethodMetadata(String methodId, Map<String, Object> metadata) {
        log.debug("Updating metadata for payment method: {}", methodId);

        PaymentMethod paymentMethod = paymentMethodRepository.findById(methodId)
                .orElseThrow(() -> new ResourceNotFoundException("PaymentMethod", methodId));

        paymentMethod.setMetadata(metadata);
        paymentMethodRepository.save(paymentMethod);
    }

    @Override
    public List<PaymentMethodResponse> getExpiringMethods(int monthsThreshold) {
        log.debug("Getting payment methods expiring within {} months", monthsThreshold);

        YearMonth threshold = YearMonth.now().plusMonths(monthsThreshold);

        return paymentMethodRepository.findAll().stream()
                .filter(pm -> "CARD".equals(pm.getMethodType()))
                .filter(pm -> pm.getExpiryYear() != null && pm.getExpiryMonth() != null)
                .filter(pm -> {
                    YearMonth expiry = YearMonth.of(
                            Integer.parseInt(pm.getExpiryYear()),
                            Integer.parseInt(pm.getExpiryMonth())
                    );
                    return expiry.isBefore(threshold) && expiry.isAfter(YearMonth.now());
                })
                .map(paymentMethodMapper::toMaskedResponse)
                .collect(Collectors.toList());
    }

    private PaymentMethod findPaymentMethodByIdAndUser(String methodId, String userId) {
        return paymentMethodRepository.findById(methodId)
                .filter(pm -> pm.getUserId().equals(userId))
                .orElseThrow(() -> new ResourceNotFoundException("PaymentMethod", methodId));
    }

    private void processCardDetails(PaymentMethod paymentMethod, PaymentMethodRequest request) {
        String lastFour = request.getCardNumber().substring(request.getCardNumber().length() - 4);

        paymentMethod.setLastFour(lastFour);
        paymentMethod.setCardType(determineCardType(request.getCardNumber()));
        paymentMethod.setCardNetwork(determineCardNetwork(request.getCardNumber()));
        paymentMethod.setExpiryMonth(request.getExpiryMonth());
        paymentMethod.setExpiryYear(request.getExpiryYear());
        paymentMethod.setCardHolderName(request.getCardHolderName());
        paymentMethod.setDisplayName(paymentMethod.getCardNetwork() + " •••• " + lastFour);
    }

    private void processUPIDetails(PaymentMethod paymentMethod, PaymentMethodRequest request) {
        paymentMethod.setVpa(request.getUpiId());
        paymentMethod.setUpiId(request.getUpiId());
        paymentMethod.setDisplayName(request.getUpiId());

        // Extract UPI app name from VPA
        if (request.getUpiId() != null && request.getUpiId().contains("@")) {
            String[] parts = request.getUpiId().split("@");
            if (parts.length > 1) {
                paymentMethod.setUpiAppName(parts[1].split("\\.")[0]);
            }
        }
    }

    private void processNetbankingDetails(PaymentMethod paymentMethod, PaymentMethodRequest request) {
        paymentMethod.setBankName(request.getBankName());
        paymentMethod.setAccountNumber(maskAccountNumber(request.getAccountNumber()));
        paymentMethod.setIfscCode(request.getIfscCode());
        paymentMethod.setAccountType(request.getAccountType());
        paymentMethod.setDisplayName(request.getBankName());
    }

    private void tokenizeWithGateway(PaymentMethod paymentMethod, PaymentMethodRequest request) {
        // Implement tokenization with payment gateway
        PaymentGateway gateway = gatewayFactory.getGateway("DEFAULT");
        String token = gateway.tokenizePaymentMethod(paymentMethod, request);
        paymentMethod.setGatewayToken(token);
    }

    private void deleteFromGateway(PaymentMethod paymentMethod) {
        try {
            PaymentGateway gateway = gatewayFactory.getGateway("DEFAULT");
            gateway.deletePaymentMethod(paymentMethod.getGatewayToken());
        } catch (Exception e) {
            log.error("Failed to delete payment method from gateway: {}", e.getMessage());
        }
    }

    private String determineUserType(String userId) {
        if (userId.startsWith("cust")) return "CUSTOMER";
        if (userId.startsWith("prov")) return "PROVIDER";
        return "UNKNOWN";
    }

    private String determineCardType(String cardNumber) {
        // Simplified - in production, use proper BIN lookup
        if (cardNumber.startsWith("4")) return "VISA";
        if (cardNumber.startsWith("5")) return "MASTERCARD";
        if (cardNumber.startsWith("3")) return "AMEX";
        if (cardNumber.startsWith("6")) return "RUPAY";
        return "UNKNOWN";
    }

    private String determineCardNetwork(String cardNumber) {
        // Simplified
        return determineCardType(cardNumber);
    }

    private String maskAccountNumber(String accountNumber) {
        if (accountNumber == null || accountNumber.length() < 4) {
            return accountNumber;
        }
        return "XXXX" + accountNumber.substring(accountNumber.length() - 4);
    }
}
