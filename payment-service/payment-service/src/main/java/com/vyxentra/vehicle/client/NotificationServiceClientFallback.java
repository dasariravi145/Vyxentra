package com.vyxentra.vehicle.client;


import com.vyxentra.vehicle.dto.response.ApiResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class NotificationServiceClientFallback implements NotificationServiceClient {

    @Override
    public ApiResponse<Void> sendPaymentSuccessNotification(String userId, String bookingId, Double amount) {
        log.error("Fallback: Unable to send payment success notification to user: {}", userId);
        return ApiResponse.error(null, "Notification service is currently unavailable");
    }

    @Override
    public ApiResponse<Void> sendPaymentFailedNotification(String userId, String bookingId, String reason) {
        log.error("Fallback: Unable to send payment failed notification to user: {}", userId);
        return ApiResponse.error(null, "Notification service is currently unavailable");
    }

    @Override
    public ApiResponse<Void> sendRefundProcessedNotification(String userId, String paymentId, Double amount) {
        log.error("Fallback: Unable to send refund processed notification to user: {}", userId);
        return ApiResponse.error(null, "Notification service is currently unavailable");
    }

    @Override
    public ApiResponse<Void> sendRefundFailedNotification(String userId, String paymentId, Double amount, String reason) {
        log.error("Fallback: Unable to send refund failed notification to user: {}", userId);
        return ApiResponse.error(null, "Notification service is currently unavailable");
    }

    @Override
    public ApiResponse<Void> sendPayoutCreatedNotification(String providerId, Double amount, String payoutId) {
        log.error("Fallback: Unable to send payout created notification to provider: {}", providerId);
        return ApiResponse.error(null, "Notification service is currently unavailable");
    }

    @Override
    public ApiResponse<Void> sendPayoutProcessingNotification(String providerId, Double amount, String payoutId) {
        log.error("Fallback: Unable to send payout processing notification to provider: {}", providerId);
        return ApiResponse.error(null, "Notification service is currently unavailable");
    }

    @Override
    public ApiResponse<Void> sendPayoutSuccessNotification(String providerId, Double amount, String payoutId) {
        log.error("Fallback: Unable to send payout success notification to provider: {}", providerId);
        return ApiResponse.error(null, "Notification service is currently unavailable");
    }

    @Override
    public ApiResponse<Void> sendPayoutFailedNotification(String providerId, Double amount, String payoutId, String reason) {
        log.error("Fallback: Unable to send payout failed notification to provider: {}", providerId);
        return ApiResponse.error(null, "Notification service is currently unavailable");
    }

    @Override
    public ApiResponse<Void> sendPayoutCancelledNotification(String providerId, Double amount, String payoutId, String reason) {
        log.error("Fallback: Unable to send payout cancelled notification to provider: {}", providerId);
        return ApiResponse.error(null, "Notification service is currently unavailable");
    }

    @Override
    public ApiResponse<Void> sendWalletCreditNotification(String userId, Double amount, Double newBalance) {
        log.error("Fallback: Unable to send wallet credit notification to user: {}", userId);
        return ApiResponse.error(null, "Notification service is currently unavailable");
    }

    @Override
    public ApiResponse<Void> sendWalletDebitNotification(String userId, Double amount, Double newBalance) {
        log.error("Fallback: Unable to send wallet debit notification to user: {}", userId);
        return ApiResponse.error(null, "Notification service is currently unavailable");
    }

    @Override
    public ApiResponse<Void> sendWalletLowBalanceNotification(String userId, Double currentBalance) {
        log.error("Fallback: Unable to send wallet low balance notification to user: {}", userId);
        return ApiResponse.error(null, "Notification service is currently unavailable");
    }
}
