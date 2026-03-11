package com.vyxentra.vehicle.scheduler;


import com.vyxentra.vehicle.service.DashboardCacheService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class DashboardCacheScheduler {

    private final DashboardCacheService cacheService;

    /**
     * Clean up expired cache entries every hour
     */
    @Scheduled(fixedDelayString = "${admin.scheduler.cache-cleanup-interval:3600000}") // 1 hour
    public void cleanupExpiredCache() {
        log.info("Running scheduled cleanup of expired dashboard caches");
        try {
            int deleted = cacheService.cleanupExpiredCaches();
            if (deleted > 0) {
                log.info("Cleaned up {} expired cache entries", deleted);
            }
        } catch (Exception e) {
            log.error("Error during cache cleanup: {}", e.getMessage());
        }
    }

    /**
     * Log cache statistics daily
     */
    @Scheduled(cron = "${admin.scheduler.cache-stats-cron:0 0 2 * * *}") // 2 AM daily
    public void logCacheStatistics() {
        log.info("Logging dashboard cache statistics");
        try {
            var stats = cacheService.getCacheStatistics();
            log.info("Cache Statistics - Total: {}, Valid: {}, Expired: {}, Max: {}",
                    stats.getTotalEntries(), stats.getValidEntries(),
                    stats.getExpiredEntries(), stats.getMaxEntries());
        } catch (Exception e) {
            log.error("Error logging cache statistics: {}", e.getMessage());
        }
    }
}
