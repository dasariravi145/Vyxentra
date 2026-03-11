package com.vyxentra.vehicle.client;

import com.vyxentra.vehicle.dto.response.ApiResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient(name = "notification-service", url = "${services.notification-service.url:http://localhost:8090}",
        fallback = NotificationServiceClientFallback.class)
public interface NotificationServiceClient {

    // Payment notifications
    @PostMapping("/api/v1/notifications/payment/success")
    ApiResponse<Void> sendPaymentSuccessNotification(@RequestParam("userId") String userId,
                                                     @RequestParam("bookingId") String bookingId,
                                                     @RequestParam("amount") Double amount);

    @PostMapping("/api/v1/notifications/payment/failed")
    ApiResponse<Void> sendPaymentFailedNotification(@RequestParam("userId") String userId,
                                                    @RequestParam("bookingId") String bookingId,
                                                    @RequestParam("reason") String reason);

    // Refund notifications
    @PostMapping("/api/v1/notifications/refund/processed")
    ApiResponse<Void> sendRefundProcessedNotification(@RequestParam("userId") String userId,
                                                      @RequestParam("paymentId") String paymentId,
                                                      @RequestParam("amount") Double amount);

    @PostMapping("/api/v1/notifications/refund/failed")
    ApiResponse<Void> sendRefundFailedNotification(@RequestParam("userId") String userId,
                                                   @RequestParam("paymentId") String paymentId,
                                                   @RequestParam("amount") Double amount,
                                                   @RequestParam("reason") String reason);

    // Payout notifications
    @PostMapping("/api/v1/notifications/payout/created")
    ApiResponse<Void> sendPayoutCreatedNotification(@RequestParam("providerId") String providerId,
                                                    @RequestParam("amount") Double amount,
                                                    @RequestParam("payoutId") String payoutId);

    @PostMapping("/api/v1/notifications/payout/processing")
    ApiResponse<Void> sendPayoutProcessingNotification(@RequestParam("providerId") String providerId,
                                                       @RequestParam("amount") Double amount,
                                                       @RequestParam("payoutId") String payoutId);

    @PostMapping("/api/v1/notifications/payout/success")
    ApiResponse<Void> sendPayoutSuccessNotification(@RequestParam("providerId") String providerId,
                                                    @RequestParam("amount") Double amount,
                                                    @RequestParam("payoutId") String payoutId);

    @PostMapping("/api/v1/notifications/payout/failed")
    ApiResponse<Void> sendPayoutFailedNotification(@RequestParam("providerId") String providerId,
                                                   @RequestParam("amount") Double amount,
                                                   @RequestParam("payoutId") String payoutId,
                                                   @RequestParam("reason") String reason);

    @PostMapping("/api/v1/notifications/payout/cancelled")
    ApiResponse<Void> sendPayoutCancelledNotification(@RequestParam("providerId") String providerId,
                                                      @RequestParam("amount") Double amount,
                                                      @RequestParam("payoutId") String payoutId,
                                                      @RequestParam("reason") String reason);

    // Wallet notifications
    @PostMapping("/api/v1/notifications/wallet/credit")
    ApiResponse<Void> sendWalletCreditNotification(@RequestParam("userId") String userId,
                                                   @RequestParam("amount") Double amount,
                                                   @RequestParam("newBalance") Double newBalance);

    @PostMapping("/api/v1/notifications/wallet/debit")
    ApiResponse<Void> sendWalletDebitNotification(@RequestParam("userId") String userId,
                                                  @RequestParam("amount") Double amount,
                                                  @RequestParam("newBalance") Double newBalance);

    @PostMapping("/api/v1/notifications/wallet/low-balance")
    ApiResponse<Void> sendWalletLowBalanceNotification(@RequestParam("userId") String userId,
                                                       @RequestParam("currentBalance") Double currentBalance);
}
