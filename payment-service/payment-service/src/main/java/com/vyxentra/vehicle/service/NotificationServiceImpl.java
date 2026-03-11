package com.vyxentra.vehicle.service;

import com.vyxentra.vehicle.client.NotificationServiceClient;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class NotificationServiceImpl implements NotificationService {

    private final NotificationServiceClient notificationClient;

    // ==================== Payment Notifications ====================

    @Override
    public void sendPaymentSuccessNotification(String userId, String bookingId, Double amount) {
        log.info("Sending payment success notification to user: {} for booking: {}, amount: {}",
                userId, bookingId, amount);

        try {
            notificationClient.sendPaymentSuccessNotification(userId, bookingId, amount);
        } catch (Exception e) {
            log.error("Failed to send payment success notification: {}", e.getMessage());
        }
    }

    @Override
    public void sendPaymentFailedNotification(String userId, String bookingId, String reason) {
        log.info("Sending payment failed notification to user: {} for booking: {}, reason: {}",
                userId, bookingId, reason);

        try {
            notificationClient.sendPaymentFailedNotification(userId, bookingId, reason);
        } catch (Exception e) {
            log.error("Failed to send payment failed notification: {}", e.getMessage());
        }
    }

    // ==================== Refund Notifications ====================

    @Override
    public void sendRefundProcessedNotification(String userId, String paymentId, Double amount) {
        log.info("Sending refund processed notification to user: {} for payment: {}, amount: {}",
                userId, paymentId, amount);

        try {
            notificationClient.sendRefundProcessedNotification(userId, paymentId, amount);
        } catch (Exception e) {
            log.error("Failed to send refund processed notification: {}", e.getMessage());
        }
    }

    @Override
    public void sendRefundFailedNotification(String userId, String paymentId, Double amount, String reason) {
        log.info("Sending refund failed notification to user: {} for payment: {}, amount: {}, reason: {}",
                userId, paymentId, amount, reason);

        try {
            notificationClient.sendRefundFailedNotification(userId, paymentId, amount, reason);
        } catch (Exception e) {
            log.error("Failed to send refund failed notification: {}", e.getMessage());
        }
    }

    // ==================== Payout Notifications ====================

    @Override
    public void sendPayoutCreatedNotification(String providerId, Double amount, String payoutId) {
        log.info("Sending payout created notification to provider: {} for payout: {}, amount: {}",
                providerId, payoutId, amount);

        try {
            notificationClient.sendPayoutCreatedNotification(providerId, amount, payoutId);
        } catch (Exception e) {
            log.error("Failed to send payout created notification: {}", e.getMessage());
        }
    }

    @Override
    public void sendPayoutProcessingNotification(String providerId, Double amount, String payoutId) {
        log.info("Sending payout processing notification to provider: {} for payout: {}, amount: {}",
                providerId, payoutId, amount);

        try {
            notificationClient.sendPayoutProcessingNotification(providerId, amount, payoutId);
        } catch (Exception e) {
            log.error("Failed to send payout processing notification: {}", e.getMessage());
        }
    }

    @Override
    public void sendPayoutSuccessNotification(String providerId, Double amount, String payoutId) {
        log.info("Sending payout success notification to provider: {} for payout: {}, amount: {}",
                providerId, payoutId, amount);

        try {
            notificationClient.sendPayoutSuccessNotification(providerId, amount, payoutId);
        } catch (Exception e) {
            log.error("Failed to send payout success notification: {}", e.getMessage());
        }
    }

    @Override
    public void sendPayoutFailedNotification(String providerId, Double amount, String payoutId, String reason) {
        log.info("Sending payout failed notification to provider: {} for payout: {}, amount: {}, reason: {}",
                providerId, payoutId, amount, reason);

        try {
            notificationClient.sendPayoutFailedNotification(providerId, amount, payoutId, reason);
        } catch (Exception e) {
            log.error("Failed to send payout failed notification: {}", e.getMessage());
        }
    }

    @Override
    public void sendPayoutCancelledNotification(String providerId, Double amount, String payoutId, String reason) {
        log.info("Sending payout cancelled notification to provider: {} for payout: {}, amount: {}, reason: {}",
                providerId, payoutId, amount, reason);

        try {
            notificationClient.sendPayoutCancelledNotification(providerId, amount, payoutId, reason);
        } catch (Exception e) {
            log.error("Failed to send payout cancelled notification: {}", e.getMessage());
        }
    }

    // ==================== Wallet Notifications ====================

    @Override
    public void sendWalletCreditNotification(String userId, Double amount, Double newBalance) {
        log.info("Sending wallet credit notification to user: {} amount: {}, new balance: {}",
                userId, amount, newBalance);

        try {
            notificationClient.sendWalletCreditNotification(userId, amount, newBalance);
        } catch (Exception e) {
            log.error("Failed to send wallet credit notification: {}", e.getMessage());
        }
    }

    @Override
    public void sendWalletDebitNotification(String userId, Double amount, Double newBalance) {
        log.info("Sending wallet debit notification to user: {} amount: {}, new balance: {}",
                userId, amount, newBalance);

        try {
            notificationClient.sendWalletDebitNotification(userId, amount, newBalance);
        } catch (Exception e) {
            log.error("Failed to send wallet debit notification: {}", e.getMessage());
        }
    }

    @Override
    public void sendWalletLowBalanceNotification(String userId, Double currentBalance) {
        log.info("Sending wallet low balance notification to user: {} current balance: {}",
                userId, currentBalance);

        try {
            notificationClient.sendWalletLowBalanceNotification(userId, currentBalance);
        } catch (Exception e) {
            log.error("Failed to send wallet low balance notification: {}", e.getMessage());
        }
    }
}