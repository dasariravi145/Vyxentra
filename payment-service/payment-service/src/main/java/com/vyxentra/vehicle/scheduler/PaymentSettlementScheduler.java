package com.vyxentra.vehicle.scheduler;

import com.vyxentra.vehicle.service.PaymentService;
import com.vyxentra.vehicle.service.RefundService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class PaymentSettlementScheduler {

    private final PaymentService paymentService;
    private final RefundService refundService;

    @Scheduled(fixedDelayString = "${payment.scheduler.expired-payments:60000}") // 1 minute
    public void processExpiredPayments() {
        log.info("Running scheduled job: process expired payments");
        try {
            paymentService.processExpiredPayments();
        } catch (Exception e) {
            log.error("Error processing expired payments", e);
        }
    }

    @Scheduled(fixedDelayString = "${payment.scheduler.auto-refunds:300000}") // 5 minutes
    public void processAutoRefunds() {
        log.info("Running scheduled job: process auto-refunds");
        try {
            refundService.processAutoRefunds();
        } catch (Exception e) {
            log.error("Error processing auto-refunds", e);
        }
    }

    @Scheduled(cron = "${payment.scheduler.daily-settlement-cron:0 0 1 * * *}") // 1 AM daily
    public void processDailySettlement() {
        log.info("Running scheduled job: process daily settlement");
        // This would process daily settlements for providers
    }
}
