package com.vyxentra.vehicle.scheduler;


import com.vyxentra.vehicle.service.AuditService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class AuditCleanupScheduler {

    private final AuditService auditService;

    @Scheduled(cron = "${admin.scheduler.audit-cleanup-cron:0 0 4 * * *}") // 4 AM daily
    public void cleanupAuditLogs() {
        log.info("Running scheduled job: cleanup old audit logs");
        try {
            auditService.cleanupOldAuditLogs();
        } catch (Exception e) {
            log.error("Error cleaning up audit logs", e);
        }
    }

    @Scheduled(cron = "${admin.scheduler.dashboard-cache-cron:0 */5 * * * *}") // Every 5 minutes
    public void refreshDashboardCache() {
        log.debug("Running scheduled job: refresh dashboard cache");
        // This would refresh the dashboard cache
    }
}