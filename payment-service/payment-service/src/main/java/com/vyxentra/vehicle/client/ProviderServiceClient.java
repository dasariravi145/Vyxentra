package com.vyxentra.vehicle.client;

import com.vyxentra.vehicle.dto.response.ApiResponse;
import com.vyxentra.vehicle.dto.response.ProviderResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@FeignClient(name = "provider-service", url = "${services.provider-service.url:http://localhost:8083}",
        fallback = ProviderServiceClientFallback.class)
public interface ProviderServiceClient {

    // ==================== Provider Details ====================

    /**
     * Get provider by ID
     */
    @GetMapping("/api/v1/providers/{providerId}")
    ApiResponse<ProviderResponse> getProvider(@PathVariable("providerId") String providerId);

    /**
     * Get provider by user ID
     */
    @GetMapping("/api/v1/providers/user/{userId}")
    ApiResponse<ProviderResponse> getProviderByUserId(@PathVariable("userId") String userId);

    /**
     * Get multiple providers by IDs
     */
    @GetMapping("/api/v1/providers/batch")
    ApiResponse<List<ProviderResponse>> getProvidersByIds(@RequestParam("ids") List<String> providerIds);

    /**
     * Validate provider exists and is active
     */
    @GetMapping("/api/v1/providers/{providerId}/validate")
    ApiResponse<Boolean> validateProvider(@PathVariable("providerId") String providerId);

    // ==================== Bank Details ====================

    /**
     * Get provider bank details for payout
     */
    @GetMapping("/api/v1/providers/{providerId}/bank-details")
    ApiResponse<Map<String, Object>> getProviderBankDetails(@PathVariable("providerId") String providerId);

    /**
     * Validate provider bank account
     */
    @PostMapping("/api/v1/providers/{providerId}/bank-details/validate")
    ApiResponse<Boolean> validateBankAccount(@PathVariable("providerId") String providerId,
                                             @RequestParam("accountNumber") String accountNumber,
                                             @RequestParam("ifscCode") String ifscCode);

    // ==================== Provider Stats ====================

    /**
     * Get provider earnings summary
     */
    @GetMapping("/api/v1/providers/{providerId}/earnings")
    ApiResponse<Map<String, Object>> getProviderEarnings(@PathVariable("providerId") String providerId,
                                                         @RequestParam("fromDate") String fromDate,
                                                         @RequestParam("toDate") String toDate);

    /**
     * Get provider commission rate
     */
    @GetMapping("/api/v1/providers/{providerId}/commission")
    ApiResponse<Double> getProviderCommission(@PathVariable("providerId") String providerId);

    /**
     * Update provider payout status
     */
    @PostMapping("/api/v1/providers/{providerId}/payout-status")
    ApiResponse<Void> updatePayoutStatus(@PathVariable("providerId") String providerId,
                                         @RequestParam("status") String status,
                                         @RequestParam("payoutId") String payoutId);

    // ==================== Provider Verification ====================

    /**
     * Check if provider is verified for payout
     */
    @GetMapping("/api/v1/providers/{providerId}/payout-eligible")
    ApiResponse<Boolean> isPayoutEligible(@PathVariable("providerId") String providerId);

    /**
     * Get provider KYC status
     */
    @GetMapping("/api/v1/providers/{providerId}/kyc-status")
    ApiResponse<String> getKycStatus(@PathVariable("providerId") String providerId);

    // ==================== Provider Documents ====================

    /**
     * Get provider PAN details
     */
    @GetMapping("/api/v1/providers/{providerId}/documents/pan")
    ApiResponse<Map<String, String>> getPanDetails(@PathVariable("providerId") String providerId);

    /**
     * Get provider GST details
     */
    @GetMapping("/api/v1/providers/{providerId}/documents/gst")
    ApiResponse<Map<String, String>> getGstDetails(@PathVariable("providerId") String providerId);

    // ==================== Bulk Operations ====================

    /**
     * Get multiple providers' payout details
     */
    @PostMapping("/api/v1/providers/batch/payout-details")
    ApiResponse<Map<String, Object>> getBatchPayoutDetails(@RequestBody List<String> providerIds);
}
