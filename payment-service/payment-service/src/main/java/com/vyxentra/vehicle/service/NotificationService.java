package com.vyxentra.vehicle.service;


public interface NotificationService {

    // Payment notifications
    void sendPaymentSuccessNotification(String userId, String bookingId, Double amount);

    void sendPaymentFailedNotification(String userId, String bookingId, String reason);

    // Refund notifications
    void sendRefundProcessedNotification(String userId, String paymentId, Double amount);

    void sendRefundFailedNotification(String userId, String paymentId, Double amount, String reason);

    // Payout notifications
    void sendPayoutCreatedNotification(String providerId, Double amount, String payoutId);

    void sendPayoutProcessingNotification(String providerId, Double amount, String payoutId);

    void sendPayoutSuccessNotification(String providerId, Double amount, String payoutId);

    void sendPayoutFailedNotification(String providerId, Double amount, String payoutId, String reason);

    void sendPayoutCancelledNotification(String providerId, Double amount, String payoutId, String reason);

    // Wallet notifications
    void sendWalletCreditNotification(String userId, Double amount, Double newBalance);

    void sendWalletDebitNotification(String userId, Double amount, Double newBalance);

    void sendWalletLowBalanceNotification(String userId, Double currentBalance);
}
