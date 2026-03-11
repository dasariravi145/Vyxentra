package com.vyxentra.vehicle.mapper;
import com.vyxentra.vehicle.dto.BankAccountDetails;
import com.vyxentra.vehicle.dto.BookingSummary;
import com.vyxentra.vehicle.dto.PayoutSummary;
import com.vyxentra.vehicle.dto.TaxDetails;
import com.vyxentra.vehicle.dto.request.PayoutRequest;
import com.vyxentra.vehicle.dto.response.PayoutResponse;
import com.vyxentra.vehicle.entity.Payout;
import org.mapstruct.*;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface PayoutMapper {

    // ==================== Entity to Response ====================

    @Mapping(target = "payoutId", source = "id")
    @Mapping(target = "periodDisplay", expression = "java(formatPeriod(payout))")
    @Mapping(target = "isFailed", expression = "java(\"FAILED\".equals(payout.getStatus()))")
    @Mapping(target = "bookingCount", expression = "java(payout.getBookingIds() != null ? payout.getBookingIds().size() : 0)")
    @Mapping(target = "bankAccount", expression = "java(mapBankAccount(payout.getAccountDetails()))")
    @Mapping(target = "statusDescription", expression = "java(getStatusDescription(payout.getStatus()))")
    @Mapping(target = "formattedAmount", expression = "java(getFormattedAmount(payout))")
    @Mapping(target = "taxDetails", expression = "java(mapTaxDetails(payout))")
    @Mapping(target = "canRetry", expression = "java(canRetryPayout(payout))")
    PayoutResponse toResponse(Payout payout);

    List<PayoutResponse> toResponseList(List<Payout> payouts);

    // ==================== Request to Entity ====================

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "payoutNumber", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "requestedAt", ignore = true)
    @Mapping(target = "processedAt", ignore = true)
    @Mapping(target = "completedAt", ignore = true)
    @Mapping(target = "gatewayPayoutId", ignore = true)
    @Mapping(target = "gatewayReference", ignore = true)
    @Mapping(target = "failureReason", ignore = true)
    @Mapping(target = "failureCode", ignore = true)
    @Mapping(target = "retryCount", constant = "0")
    @Mapping(target = "settlementStatus", ignore = true)
    @Mapping(target = "settlementReference", ignore = true)
    @Mapping(target = "actualSettlementDate", ignore = true)
    @Mapping(target = "accountDetails", source = "bankAccount", qualifiedByName = "bankDetailsToMap")
    @Mapping(target = "upiDetails", source = "upiDetails", qualifiedByName = "upiDetailsToMap")
    @Mapping(target = "status", constant = "PENDING")
    Payout toEntity(PayoutRequest request);

    List<Payout> toEntityList(List<PayoutRequest> requests);

    // ==================== Update Methods ====================

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "payoutNumber", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "providerId", ignore = true)
    @Mapping(target = "periodStart", ignore = true)
    @Mapping(target = "periodEnd", ignore = true)
    @Mapping(target = "accountDetails", source = "bankAccount", qualifiedByName = "bankDetailsToMap")
    @Mapping(target = "upiDetails", source = "upiDetails", qualifiedByName = "upiDetailsToMap")
    void updateEntity(@MappingTarget Payout payout, PayoutRequest request);

    // ==================== Custom Mapping Methods ====================

    @Named("formatPeriod")
    default String formatPeriod(Payout payout) {
        if (payout.getPeriodStart() != null && payout.getPeriodEnd() != null) {
            return payout.getPeriodStart() + " to " + payout.getPeriodEnd();
        }
        return null;
    }

    @Named("mapBankAccount")
    default BankAccountDetails mapBankAccount(Map<String, Object> accountDetails) {
        if (accountDetails == null) return null;

        return BankAccountDetails.builder()
                .accountHolderName((String) accountDetails.get("accountHolderName"))
                .bankName((String) accountDetails.get("bankName"))
                .accountNumber((String) accountDetails.get("accountNumber"))
                .ifscCode((String) accountDetails.get("ifscCode"))
                .branchName((String) accountDetails.get("branchName"))
                .accountType((String) accountDetails.get("accountType"))
                //.upiId((String) accountDetails.get("upiId"))
                .isVerified(accountDetails.get("isVerified") != null ?
                        (Boolean) accountDetails.get("isVerified") : false)
                .build();
    }

    @Named("bankDetailsToMap")
    default Map<String, Object> bankDetailsToMap(BankAccountDetails bankDetails) {
        if (bankDetails == null) return null;

        return Map.of(
                "accountHolderName", bankDetails.getAccountHolderName(),
                "bankName", bankDetails.getBankName(),
                "accountNumber", maskAccountNumber(bankDetails.getAccountNumber()),
                "ifscCode", bankDetails.getIfscCode(),
                "branchName", bankDetails.getBranchName(),
                "accountType", bankDetails.getAccountType(),
                "routingNumber", bankDetails.getRoutingNumber(),
                "swiftCode", bankDetails.getSwiftCode(),
                "iban", bankDetails.getIban(),
                "isVerified", false
        );
    }

    @Named("upiDetailsToMap")
    default Map<String, Object> upiDetailsToMap(PayoutRequest.UpiDetails upiDetails) {
        if (upiDetails == null) return null;

        return Map.of(
                "upiId", upiDetails.getUpiId(),
                "vpa", upiDetails.getVpa(),
                "upiAppName", upiDetails.getUpiAppName(),
                "qrCodeUrl", upiDetails.getQrCodeUrl(),
                "isVerified", false
        );
    }

    @Named("mapTaxDetails")
    default TaxDetails mapTaxDetails(Payout payout) {
        if (payout.getMetadata() == null) return null;

        Map<String, Object> meta = payout.getMetadata();

        return TaxDetails.builder()
                .tdsAmount((Double) meta.getOrDefault("tdsAmount", 0.0))
                .tdsPercentage((Double) meta.getOrDefault("tdsPercentage", 0.0))
                .gstAmount((Double) meta.getOrDefault("gstAmount", 0.0))
                .gstPercentage((Double) meta.getOrDefault("gstPercentage", 0.0))
                .taxId((String) meta.get("taxId"))
                .panNumber((String) meta.get("panNumber"))
                .gstNumber((String) meta.get("gstNumber"))
                .build();
    }

    @Named("getStatusDescription")
    default String getStatusDescription(String status) {
        if (status == null) return "Unknown";

        switch (status) {
            case "PENDING": return "Pending processing";
            case "PROCESSING": return "Processing with bank";
            case "SUCCESS": return "Successfully completed";
            case "FAILED": return "Failed to process";
            case "CANCELLED": return "Cancelled";
            default: return status;
        }
    }

    @Named("getFormattedAmount")
    default String getFormattedAmount(Payout payout) {
        String currencySymbol = "INR".equals(payout.getCurrency()) ? "₹" : payout.getCurrency();
        Double amount = payout.getNetAmount() != null ? payout.getNetAmount() : payout.getTotalAmount();
        return currencySymbol + " " + String.format("%.2f", amount);
    }

    @Named("canRetryPayout")
    default boolean canRetryPayout(Payout payout) {
        return "FAILED".equals(payout.getStatus()) &&
                (payout.getRetryCount() == null || payout.getRetryCount() < 3);
    }

    @Named("maskAccountNumber")
    default String maskAccountNumber(String accountNumber) {
        if (accountNumber == null || accountNumber.length() < 4) {
            return accountNumber;
        }
        return "XXXX" + accountNumber.substring(accountNumber.length() - 4);
    }

    // ==================== Summary Methods ====================

    default PayoutSummary toSummary(Payout payout) {
        return PayoutSummary.builder()
                .totalPayouts(payout.getNetAmount())
                .totalTransactions(1L)
                .averagePayout(payout.getNetAmount())
                .minPayout(payout.getNetAmount())
                .maxPayout(payout.getNetAmount())
                .build();
    }

    default List<BookingSummary> toBookingSummary(List<String> bookingIds,
                                                  Map<String, Double> bookingAmounts) {
        if (bookingIds == null) return List.of();

        return bookingIds.stream()
                .map(id -> BookingSummary.builder()
                        .bookingId(id)
                        .amount(bookingAmounts != null ? bookingAmounts.getOrDefault(id, 0.0) : 0.0)
                        .build())
                .collect(Collectors.toList());
    }
}
