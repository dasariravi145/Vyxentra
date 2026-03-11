package com.vyxentra.vehicle.scheduler;

import com.vyxentra.vehicle.service.EmergencyDispatchService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class EmergencyExpiryScheduler {

    private final EmergencyDispatchService emergencyDispatchService;

    @Scheduled(fixedDelayString = "${emergency.scheduler.expiry-interval:30000}") // 30 seconds
    public void processExpiredRequests() {
        log.debug("Running scheduled job: process expired emergency requests");
        try {
            emergencyDispatchService.processExpiredRequests();
        } catch (Exception e) {
            log.error("Error processing expired emergency requests", e);
        }
    }

    @Scheduled(fixedDelayString = "${emergency.scheduler.radius-expansion-interval:60000}") // 1 minute
    public void expandSearchRadius() {
        log.debug("Running scheduled job: expand search radius");
        try {
            emergencyDispatchService.expandSearchRadius();
        } catch (Exception e) {
            log.error("Error expanding search radius", e);
        }
    }

    @Scheduled(cron = "${emergency.scheduler.provider-timeout-cleanup:0 */5 * * * *}") // Every 5 minutes
    public void cleanupProviderTimeouts() {
        log.debug("Running scheduled job: cleanup provider timeouts");
        // This would clean up stale provider timeout records
    }
}
