package com.vyxentra.vehicle.mapper;
import com.vyxentra.vehicle.dto.request.PaymentMethodRequest;
import com.vyxentra.vehicle.dto.response.PaymentMethodResponse;
import com.vyxentra.vehicle.entity.PaymentMethod;
import org.mapstruct.*;

import java.time.YearMonth;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface PaymentMethodMapper {

    /**
     * Convert PaymentMethodRequest to PaymentMethod entity
     */
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "lastUsedAt", ignore = true)
    @Mapping(target = "isActive", constant = "true")
    @Mapping(target = "isVerified", constant = "false")
    @Mapping(target = "gatewayToken", ignore = true)
    @Mapping(target = "gatewayCustomerId", ignore = true)
    @Mapping(target = "gatewayPaymentMethodId", ignore = true)
    @Mapping(target = "metadata", ignore = true)
    @Mapping(target = "lastFour", source = "cardNumber", qualifiedByName = "extractLastFour")
    @Mapping(target = "cardType", source = "cardNumber", qualifiedByName = "determineCardType")
    @Mapping(target = "cardNetwork", source = "cardNumber", qualifiedByName = "determineCardNetwork")
    @Mapping(target = "displayName", source = ".", qualifiedByName = "generateDisplayName")
    PaymentMethod toEntity(PaymentMethodRequest request);

    /**
     * Convert PaymentMethod entity to PaymentMethodResponse
     */
    @Mapping(target = "methodId", source = "id")
    @Mapping(target = "isExpired", source = ".", qualifiedByName = "isCardExpired")
    @Mapping(target = "isInternational", ignore = true)
    @Mapping(target = "cardIssuer", ignore = true)
    @Mapping(target = "cardIssuerCountry", ignore = true)
    @Mapping(target = "riskLevel", ignore = true)
    @Mapping(target = "logoUrl", ignore = true)
    PaymentMethodResponse toResponse(PaymentMethod paymentMethod);

    /**
     * Convert list of PaymentMethod entities to list of PaymentMethodResponses
     */
    List<PaymentMethodResponse> toResponseList(List<PaymentMethod> paymentMethods);

    /**
     * Convert PaymentMethod entity to masked PaymentMethodResponse (sensitive data masked)
     */
    @Named("toMaskedResponse")
    default PaymentMethodResponse toMaskedResponse(PaymentMethod paymentMethod) {
        if (paymentMethod == null) {
            return null;
        }

        PaymentMethodResponse response = toResponse(paymentMethod);
        return maskSensitiveData(response);
    }

    /**
     * Convert list to masked responses
     */
    default List<PaymentMethodResponse> toMaskedResponseList(List<PaymentMethod> paymentMethods) {
        if (paymentMethods == null) {
            return List.of();
        }
        return paymentMethods.stream()
                .map(this::toMaskedResponse)
                .toList();
    }

    /**
     * Update existing PaymentMethod entity from request
     */
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "lastUsedAt", ignore = true)
    @Mapping(target = "userId", ignore = true)
    @Mapping(target = "userType", ignore = true)
    @Mapping(target = "gatewayToken", ignore = true)
    @Mapping(target = "gatewayCustomerId", ignore = true)
    @Mapping(target = "gatewayPaymentMethodId", ignore = true)
    @Mapping(target = "lastFour", source = "cardNumber", qualifiedByName = "extractLastFour")
    @Mapping(target = "cardType", source = "cardNumber", qualifiedByName = "determineCardType")
    @Mapping(target = "cardNetwork", source = "cardNumber", qualifiedByName = "determineCardNetwork")
    @Mapping(target = "displayName", source = ".", qualifiedByName = "generateDisplayName")
    void updateEntity(@MappingTarget PaymentMethod paymentMethod, PaymentMethodRequest request);

    // ==================== Custom Mapping Methods ====================

    /**
     * Extract last 4 digits from card number
     */
    @Named("extractLastFour")
    default String extractLastFour(String cardNumber) {
        if (cardNumber == null || cardNumber.length() < 4) {
            return null;
        }
        return cardNumber.substring(cardNumber.length() - 4);
    }

    /**
     * Determine card type (CREDIT/DEBIT) based on card number
     * Simplified logic - in production use BIN database
     */
    @Named("determineCardType")
    default String determineCardType(String cardNumber) {
        if (cardNumber == null || cardNumber.isEmpty()) {
            return "UNKNOWN";
        }

        // Check first digit for card type
        char firstDigit = cardNumber.charAt(0);
        if (firstDigit == '4' || firstDigit == '5' || firstDigit == '6') {
            // Most debit cards start with 4,5,6
            return "DEBIT";
        } else if (firstDigit == '3') {
            // American Express is usually credit
            return "CREDIT";
        }

        return "UNKNOWN";
    }

    /**
     * Determine card network (VISA, MASTERCARD, etc.) based on card number
     */
    @Named("determineCardNetwork")
    default String determineCardNetwork(String cardNumber) {
        if (cardNumber == null || cardNumber.isEmpty()) {
            return "UNKNOWN";
        }

        // Visa: starts with 4
        if (cardNumber.startsWith("4")) {
            return "VISA";
        }
        // Mastercard: starts with 51-55 or 2221-2720
        else if (cardNumber.matches("^(5[1-5]|22[2-9]|27[0-2]).*")) {
            return "MASTERCARD";
        }
        // American Express: starts with 34 or 37
        else if (cardNumber.startsWith("34") || cardNumber.startsWith("37")) {
            return "AMEX";
        }
        // RuPay: starts with 60, 65, 81, 82
        else if (cardNumber.matches("^(60|65|81|82).*")) {
            return "RUPAY";
        }
        // Discover: starts with 6011, 65, 644-649
        else if (cardNumber.matches("^(6011|65|64[4-9]).*")) {
            return "DISCOVER";
        }
        // Diners Club: starts with 300-305, 36, 38
        else if (cardNumber.matches("^(30[0-5]|36|38).*")) {
            return "DINERS";
        }
        // JCB: starts with 35
        else if (cardNumber.startsWith("35")) {
            return "JCB";
        }

        return "UNKNOWN";
    }

    /**
     * Generate display name for payment method
     */
    @Named("generateDisplayName")
    default String generateDisplayName(PaymentMethodRequest request) {
        if (request == null) {
            return null;
        }

        String methodType = request.getMethodType();
        if ("CARD".equals(methodType)) {
            String network = determineCardNetwork(request.getCardNumber());
            String lastFour = extractLastFour(request.getCardNumber());
            return network + " •••• " + lastFour;
        } else if ("UPI".equals(methodType)) {
            return request.getUpiId();
        } else if ("NETBANKING".equals(methodType)) {
            return request.getBankName();
        }

        return methodType;
    }

    /**
     * Check if card is expired
     */
    @Named("isCardExpired")
    default boolean isCardExpired(PaymentMethod paymentMethod) {
        if (paymentMethod == null ||
                paymentMethod.getExpiryMonth() == null ||
                paymentMethod.getExpiryYear() == null) {
            return false;
        }

        try {
            int expMonth = Integer.parseInt(paymentMethod.getExpiryMonth());
            int expYear = Integer.parseInt(paymentMethod.getExpiryYear());

            YearMonth expiry = YearMonth.of(expYear, expMonth);
            return expiry.isBefore(YearMonth.now());
        } catch (NumberFormatException e) {
            return false;
        }
    }

    /**
     * Mask sensitive data in response
     */
    @Named("maskSensitiveData")
    default PaymentMethodResponse maskSensitiveData(PaymentMethodResponse response) {
        if (response == null) {
            return null;
        }

        // Mask card number (already only lastFour is stored)
        if ("CARD".equals(response.getMethodType())) {
            // Card number is already stored as lastFour only
        }

        // Mask account number
        if (response.getAccountNumber() != null && response.getAccountNumber().length() > 4) {
            String accNo = response.getAccountNumber();
            response.setAccountNumber("XXXX" + accNo.substring(accNo.length() - 4));
        }

        // Mask UPI ID partially (show only domain)
        if (response.getVpa() != null && response.getVpa().contains("@")) {
            String[] parts = response.getVpa().split("@");
            if (parts.length > 1) {
                response.setVpa("xxxx@" + parts[1]);
            }
        }

        return response;
    }

    // ==================== Helper Methods for Gateway Responses ====================

    /**
     * Create response from gateway tokenization result
     */
    default PaymentMethodResponse fromGatewayResponse(String gatewayToken, String gatewayCustomerId,
                                                      String gatewayPaymentMethodId,
                                                      PaymentMethodRequest request) {
        PaymentMethod method = toEntity(request);
        method.setGatewayToken(gatewayToken);
        method.setGatewayCustomerId(gatewayCustomerId);
        method.setGatewayPaymentMethodId(gatewayPaymentMethodId);

        return toMaskedResponse(method);
    }

    /**
     * Merge gateway data into existing payment method
     */
    default void mergeGatewayData(PaymentMethod paymentMethod, String gatewayToken,
                                  String gatewayCustomerId, String gatewayPaymentMethodId) {
        if (paymentMethod != null) {
            paymentMethod.setGatewayToken(gatewayToken);
            paymentMethod.setGatewayCustomerId(gatewayCustomerId);
            paymentMethod.setGatewayPaymentMethodId(gatewayPaymentMethodId);
        }
    }

    // ==================== Card BIN Lookup Integration ====================

    /**
     * Enrich payment method with BIN lookup data
     * This would be called after getting BIN information from gateway
     */
    default void enrichWithBinData(PaymentMethod paymentMethod, Map<String, Object> binData) {

        if (paymentMethod == null || binData == null) {
            return;
        }

        if (binData.containsKey("cardType")) {
            paymentMethod.setCardType(binData.get("cardType").toString());
        }

        if (binData.containsKey("cardNetwork")) {
            paymentMethod.setCardNetwork(binData.get("cardNetwork").toString());
        }

        if (binData.containsKey("bankName")) {
            paymentMethod.setBankName(binData.get("bankName").toString());
        }

        if (paymentMethod.getLastFour() != null && paymentMethod.getCardNetwork() != null) {
            paymentMethod.setDisplayName(
                    paymentMethod.getCardNetwork() + " •••• " + paymentMethod.getLastFour()
            );
        }
    }

    // ==================== UPI App Detection ====================

    /**
     * Detect UPI app from VPA
     */
    @Named("detectUpiApp")
    default String detectUpiApp(String vpa) {
        if (vpa == null || !vpa.contains("@")) {
            return null;
        }

        String domain = vpa.split("@")[1].toLowerCase();

        if (domain.contains("okhdfcbank") || domain.contains("okicici") ||
                domain.contains("oksbi") || domain.contains("okaxis")) {
            return "Google Pay";
        } else if (domain.contains("ybl")) {
            return "PhonePe";
        } else if (domain.contains("paytm")) {
            return "Paytm";
        } else if (domain.contains("amazon")) {
            return "Amazon Pay";
        } else if (domain.contains("ibl")) {
            return "iMobile";
        } else if (domain.contains("freecharge")) {
            return "FreeCharge";
        } else if (domain.contains("mobikwik")) {
            return "MobiKwik";
        } else if (domain.contains("airtel")) {
            return "Airtel Payments Bank";
        }

        return "Other UPI App";
    }
}
