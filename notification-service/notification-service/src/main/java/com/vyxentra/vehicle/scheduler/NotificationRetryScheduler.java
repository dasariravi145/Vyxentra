package com.vyxentra.vehicle.scheduler;


import com.vyxentra.vehicle.service.NotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class NotificationRetryScheduler {

    private final NotificationService notificationService;

    @Scheduled(fixedDelayString = "${notification.scheduler.retry-interval:60000}") // 1 minute
    public void retryFailedNotifications() {
        log.debug("Running scheduled job: retry failed notifications");
        try {
            notificationService.processPendingNotifications();
        } catch (Exception e) {
            log.error("Error processing pending notifications", e);
        }
    }

    @Scheduled(cron = "${notification.scheduler.cleanup-cron:0 0 3 * * *}") // 3 AM daily
    public void cleanupOldNotifications() {
        log.info("Running scheduled job: cleanup old notifications");
        // This would delete notifications older than retention period
    }

    @Scheduled(cron = "${notification.scheduler.preferences-cleanup:0 0 4 * * *}") // 4 AM daily
    public void cleanupInactiveDevices() {
        log.info("Running scheduled job: cleanup inactive push devices");
        // This would deactivate devices not used for > 90 days
    }
}
