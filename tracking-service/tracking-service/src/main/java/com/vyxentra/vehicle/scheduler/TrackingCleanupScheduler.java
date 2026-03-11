package com.vyxentra.vehicle.scheduler;


import com.vyxentra.vehicle.service.LocationService;
import com.vyxentra.vehicle.service.TrackingService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class TrackingCleanupScheduler {

    private final TrackingService trackingService;
    private final LocationService locationService;

    @Scheduled(fixedDelayString = "${tracking.scheduler.stale-session-interval:300000}") // 5 minutes
    public void processStaleSessions() {
        log.info("Running scheduled job: process stale tracking sessions");
        try {
            trackingService.processStaleSessions();
        } catch (Exception e) {
            log.error("Error processing stale sessions", e);
        }
    }

    @Scheduled(cron = "${tracking.scheduler.history-cleanup-cron:0 0 2 * * *}") // 2 AM daily
    public void cleanupOldLocationHistory() {
        log.info("Running scheduled job: cleanup old location history");
        try {
            locationService.cleanupOldLocations();
        } catch (Exception e) {
            log.error("Error cleaning up location history", e);
        }
    }

    @Scheduled(fixedDelayString = "${tracking.scheduler.heartbeat-cleanup-interval:60000}") // 1 minute
    public void cleanupStaleHeartbeats() {
        log.debug("Running scheduled job: cleanup stale heartbeats");
        // This would clean up stale WebSocket connections
    }
}