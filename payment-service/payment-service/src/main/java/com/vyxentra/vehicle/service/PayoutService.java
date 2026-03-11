package com.vyxentra.vehicle.service;


import com.vyxentra.vehicle.dto.BankAccountDetails;
import com.vyxentra.vehicle.dto.PayoutStatistics;
import com.vyxentra.vehicle.dto.PayoutSummary;
import com.vyxentra.vehicle.dto.request.PayoutRequest;
import com.vyxentra.vehicle.dto.response.PageResponse;
import com.vyxentra.vehicle.dto.response.PayoutResponse;
import org.springframework.data.domain.Pageable;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

public interface PayoutService {

    // ==================== Create Payout ====================

    /**
     * Create a new payout
     *
     * @param request Payout request details
     * @return Created payout response
     */
    PayoutResponse createPayout(PayoutRequest request);

    /**
     * Bulk create payouts for multiple providers
     *
     * @param requests List of payout requests
     * @return List of created payout responses
     */
    List<PayoutResponse> bulkCreatePayouts(List<PayoutRequest> requests);

    /**
     * Auto-generate payout for a provider based on completed bookings
     *
     * @param providerId Provider ID
     * @param fromDate Start date
     * @param toDate End date
     * @return Created payout response
     */
    PayoutResponse generatePayoutForProvider(String providerId, LocalDate fromDate, LocalDate toDate);

    // ==================== Get Payout ====================

    /**
     * Get payout by ID
     *
     * @param payoutId Payout ID
     * @return Payout response
     */
    PayoutResponse getPayout(String payoutId);

    /**
     * Get payout by payout number
     *
     * @param payoutNumber Payout number
     * @return Payout response
     */
    PayoutResponse getPayoutByNumber(String payoutNumber);

    /**
     * Get all payouts for a provider
     *
     * @param providerId Provider ID
     * @param fromDate Optional start date filter
     * @param toDate Optional end date filter
     * @param pageable Pagination information
     * @return Paginated list of payouts
     */
    PageResponse<PayoutResponse> getProviderPayouts(String providerId, LocalDate fromDate,
                                                    LocalDate toDate, Pageable pageable);

    /**
     * Get all payouts across all providers (Admin only)
     *
     * @param fromDate Optional start date filter
     * @param toDate Optional end date filter
     * @param status Optional status filter
     * @param pageable Pagination information
     * @return Paginated list of payouts
     */
    PageResponse<PayoutResponse> getAllPayouts(LocalDate fromDate, LocalDate toDate,
                                               String status, Pageable pageable);

    // ==================== Process Payout ====================

    /**
     * Process a pending payout
     *
     * @param payoutId Payout ID
     * @return Updated payout response
     */
    PayoutResponse processPayout(String payoutId);

    /**
     * Batch process multiple payouts
     *
     * @param payoutIds List of payout IDs
     * @return List of processed payout responses
     */
    List<PayoutResponse> batchProcessPayouts(List<String> payoutIds);

    /**
     * Process all pending payouts (scheduled job)
     *
     * @return Number of payouts processed
     */
    int processAllPendingPayouts();

    // ==================== Pending Payouts ====================

    /**
     * Get all pending payouts
     *
     * @return List of pending payout responses
     */
    List<PayoutResponse> getPendingPayouts();

    /**
     * Get pending payouts for a specific provider
     *
     * @param providerId Provider ID
     * @return List of pending payout responses
     */
    List<PayoutResponse> getPendingPayoutsForProvider(String providerId);

    /**
     * Get count of pending payouts
     *
     * @return Count of pending payouts
     */
    long getPendingPayoutsCount();

    // ==================== Update Payout Status ====================

    /**
     * Update payout status
     *
     * @param payoutId Payout ID
     * @param status New status
     * @param gatewayReference Gateway reference if applicable
     * @return Updated payout response
     */
    PayoutResponse updatePayoutStatus(String payoutId, String status, String gatewayReference);

    /**
     * Mark payout as successful
     *
     * @param payoutId Payout ID
     * @param gatewayReference Gateway reference
     * @return Updated payout response
     */
    PayoutResponse markAsSuccessful(String payoutId, String gatewayReference);

    /**
     * Mark payout as failed
     *
     * @param payoutId Payout ID
     * @param reason Failure reason
     * @param failureCode Failure code
     * @return Updated payout response
     */
    PayoutResponse markAsFailed(String payoutId, String reason, String failureCode);

    /**
     * Mark payout as processing
     *
     * @param payoutId Payout ID
     * @param gatewayPayoutId Gateway payout ID
     * @return Updated payout response
     */
    PayoutResponse markAsProcessing(String payoutId, String gatewayPayoutId);

    /**
     * Mark payout as cancelled
     *
     * @param payoutId Payout ID
     * @param reason Cancellation reason
     * @return Updated payout response
     */
    PayoutResponse cancelPayout(String payoutId, String reason);

    // ==================== Retry Payout ====================

    /**
     * Retry a failed payout
     *
     * @param payoutId Payout ID
     * @return Updated payout response
     */
    PayoutResponse retryPayout(String payoutId);

    /**
     * Retry multiple failed payouts
     *
     * @param payoutIds List of payout IDs
     * @return List of retried payout responses
     */
    List<PayoutResponse> bulkRetryPayouts(List<String> payoutIds);

    // ==================== Summary and Statistics ====================

    /**
     * Get payout summary for a provider
     *
     * @param providerId Provider ID
     * @return Payout summary
     */
    PayoutSummary getPayoutSummary(String providerId);

    /**
     * Get global payout statistics (Admin only)
     *
     * @param fromDate Start date
     * @param toDate End date
     * @return Payout statistics
     */
    PayoutStatistics getPayoutStatistics(LocalDate fromDate, LocalDate toDate);

    /**
     * Get monthly payout breakdown for a provider
     *
     * @param providerId Provider ID
     * @param year Year
     * @return Monthly breakdown map
     */
    Map<String, Double> getMonthlyPayoutBreakdown(String providerId, int year);

    // ==================== Reports ====================

    /**
     * Generate payout report
     *
     * @param providerId Provider ID (optional, null for all)
     * @param fromDate Start date
     * @param toDate End date
     * @param format Report format (PDF, EXCEL, CSV)
     * @return Report as byte array
     */
    byte[] generatePayoutReport(String providerId, LocalDate fromDate, LocalDate toDate, String format);

    /**
     * Export payouts to CSV
     *
     * @param payoutIds List of payout IDs
     * @return CSV data as byte array
     */
    byte[] exportPayoutsToCsv(List<String> payoutIds);

    // ==================== Validation ====================

    /**
     * Validate bank account details
     *
     * @param accountDetails Bank account details
     * @return true if valid
     */
    boolean validateBankAccount(BankAccountDetails accountDetails);

    /**
     * Validate UPI details
     *
     * @param upiDetails UPI details
     * @return true if valid
     */
    boolean validateUpiDetails(PayoutRequest.UpiDetails upiDetails);

    // ==================== Settlement ====================

    /**
     * Mark payout as settled
     *
     * @param payoutId Payout ID
     * @param settlementReference Settlement reference
     * @return Updated payout response
     */
    PayoutResponse markAsSettled(String payoutId, String settlementReference);

    /**
     * Get unsettled payouts
     *
     * @return List of unsettled payouts
     */
    List<PayoutResponse> getUnsettledPayouts();

    // ==================== Search ====================

    /**
     * Search payouts by various criteria
     *
     * @param providerId Provider ID
     * @param status Status
     * @param fromDate Start date
     * @param toDate End date
     * @param minAmount Minimum amount
     * @param maxAmount Maximum amount
     * @param pageable Pagination
     * @return Paginated results
     */
    PageResponse<PayoutResponse> searchPayouts(String providerId, String status,
                                               LocalDate fromDate, LocalDate toDate,
                                               Double minAmount, Double maxAmount,
                                               Pageable pageable);

}
