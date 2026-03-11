package com.vyxentra.vehicle.client;

import com.vyxentra.vehicle.dto.response.ApiResponse;
import com.vyxentra.vehicle.dto.response.ProviderResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

@Slf4j
@Component
public class ProviderServiceClientFallback implements ProviderServiceClient {

    private static final ProviderResponse FALLBACK_PROVIDER = ProviderResponse.builder()
            .providerId("FALLBACK")
            .businessName("Provider Service Unavailable")
            .status("UNKNOWN")
            .isAvailable(false)
            .build();

    @Override
    public ApiResponse<ProviderResponse> getProvider(String providerId) {
        log.error("Fallback: Unable to fetch provider: {}", providerId);
        return ApiResponse.error(null, "Provider service is currently unavailable");
    }

    @Override
    public ApiResponse<ProviderResponse> getProviderByUserId(String userId) {
        log.error("Fallback: Unable to fetch provider for user: {}", userId);
        return ApiResponse.error(null, "Provider service is currently unavailable");
    }

    @Override
    public ApiResponse<List<ProviderResponse>> getProvidersByIds(List<String> providerIds) {
        log.error("Fallback: Unable to fetch providers by IDs: {}", providerIds);
        return ApiResponse.success(List.of(FALLBACK_PROVIDER));
    }

    @Override
    public ApiResponse<Boolean> validateProvider(String providerId) {
        log.error("Fallback: Unable to validate provider: {}", providerId);
        return ApiResponse.success(false);
    }

    @Override
    public ApiResponse<Map<String, Object>> getProviderBankDetails(String providerId) {
        log.error("Fallback: Unable to fetch bank details for provider: {}", providerId);

        Map<String, Object> fallbackBankDetails = Map.of(
                "accountHolderName", "FALLBACK",
                "bankName", "FALLBACK BANK",
                "accountNumber", "XXXXXXXXXX",
                "ifscCode", "FBIN0001234",
                "isVerified", false
        );

        return ApiResponse.success(fallbackBankDetails);
    }

    @Override
    public ApiResponse<Boolean> validateBankAccount(String providerId, String accountNumber, String ifscCode) {
        log.error("Fallback: Unable to validate bank account for provider: {}", providerId);
        return ApiResponse.success(false);
    }

    @Override
    public ApiResponse<Map<String, Object>> getProviderEarnings(String providerId, String fromDate, String toDate) {
        log.error("Fallback: Unable to fetch earnings for provider: {}", providerId);

        Map<String, Object> fallbackEarnings = Map.of(
                "totalAmount", 0.0,
                "totalBookings", 0,
                "commission", 0.0,
                "netAmount", 0.0
        );

        return ApiResponse.success(fallbackEarnings);
    }

    @Override
    public ApiResponse<Double> getProviderCommission(String providerId) {
        log.error("Fallback: Unable to fetch commission for provider: {}", providerId);
        return ApiResponse.success(15.0); // Default commission rate
    }

    @Override
    public ApiResponse<Void> updatePayoutStatus(String providerId, String status, String payoutId) {
        log.error("Fallback: Unable to update payout status for provider: {}", providerId);
        return ApiResponse.error(null, "Provider service is currently unavailable");
    }

    @Override
    public ApiResponse<Boolean> isPayoutEligible(String providerId) {
        log.error("Fallback: Unable to check payout eligibility for provider: {}", providerId);
        return ApiResponse.success(false);
    }

    @Override
    public ApiResponse<String> getKycStatus(String providerId) {
        log.error("Fallback: Unable to fetch KYC status for provider: {}", providerId);
        return ApiResponse.success("UNKNOWN");
    }

    @Override
    public ApiResponse<Map<String, String>> getPanDetails(String providerId) {
        log.error("Fallback: Unable to fetch PAN details for provider: {}", providerId);
        return ApiResponse.success(Map.of("panNumber", "FALLBACK", "verified", "false"));
    }

    @Override
    public ApiResponse<Map<String, String>> getGstDetails(String providerId) {
        log.error("Fallback: Unable to fetch GST details for provider: {}", providerId);
        return ApiResponse.success(Map.of("gstNumber", "FALLBACK", "verified", "false"));
    }

    @Override
    public ApiResponse<Map<String, Object>> getBatchPayoutDetails(List<String> providerIds) {
        log.error("Fallback: Unable to fetch batch payout details for providers: {}", providerIds);

        Map<String, Object> fallbackDetails = Map.of(
                "status", "UNAVAILABLE",
                "count", providerIds.size(),
                "message", "Provider service unavailable"
        );

        return ApiResponse.success(fallbackDetails);
    }
}
