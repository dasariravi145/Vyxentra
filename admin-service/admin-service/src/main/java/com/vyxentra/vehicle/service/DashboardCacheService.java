package com.vyxentra.vehicle.service;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.vyxentra.vehicle.dto.CacheStatistics;
import com.vyxentra.vehicle.dto.response.DashboardResponse;
import com.vyxentra.vehicle.entity.DashboardCache;
import com.vyxentra.vehicle.repository.DashboardCacheRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class DashboardCacheService {

    private final DashboardCacheRepository cacheRepository;
    private final ObjectMapper objectMapper;

    @Value("${admin.dashboard.cache.default-ttl-minutes:5}")
    private int defaultTtlMinutes;

    @Value("${admin.dashboard.cache.max-entries:100}")
    private int maxCacheEntries;

    /**
     * Get cached dashboard data
     */
    @Cacheable(value = "dashboard", key = "#cacheKey", unless = "#result == null")
    public Optional<DashboardResponse> getCachedDashboard(String cacheKey) {
        log.debug("Fetching cached dashboard for key: {}", cacheKey);

        LocalDateTime now = LocalDateTime.now();
        Optional<DashboardCache> cached = cacheRepository.findValidByCacheKey(cacheKey, now);

        cached.ifPresent(cache -> {
            cache.incrementAccessCount();
            cacheRepository.save(cache);
            log.debug("Cache hit for key: {}, access count: {}", cacheKey, cache.getAccessCount());
        });

        return cached.map(DashboardCache::getCacheData);
    }

    /**
     * Cache dashboard data
     */
    @Transactional
    public void cacheDashboard(String cacheKey, DashboardResponse data, int ttlMinutes) {
        log.info("Caching dashboard data for key: {}, TTL: {} minutes", cacheKey, ttlMinutes);

        // Check cache size limit
        long cacheSize = cacheRepository.count();
        if (cacheSize >= maxCacheEntries) {
            // Remove oldest entry
            List<DashboardCache> oldest = cacheRepository.findOldest(1);
            if (!oldest.isEmpty()) {
                cacheRepository.delete(oldest.get(0));
                log.info("Removed oldest cache entry to maintain size limit");
            }
        }

        LocalDateTime expiresAt = LocalDateTime.now().plusMinutes(ttlMinutes);

        DashboardCache cache = DashboardCache.builder()
                .cacheKey(cacheKey)
                .cacheData(data)
                .expiresAt(expiresAt)
                .createdAt(java.time.Instant.now())
                .version(1)
                .accessCount(0L)
                .build();

        cacheRepository.save(cache);

        log.info("Dashboard cached successfully with key: {}, expires at: {}", cacheKey, expiresAt);
    }

    /**
     * Cache dashboard data with default TTL
     */
    public void cacheDashboard(String cacheKey, DashboardResponse data) {
        cacheDashboard(cacheKey, data, defaultTtlMinutes);
    }

    /**
     * Update existing cache
     */
    @Transactional
    @CacheEvict(value = "dashboard", key = "#cacheKey")
    public void updateCache(String cacheKey, DashboardResponse data) {
        log.info("Updating cache for key: {}", cacheKey);

        cacheRepository.findByCacheKey(cacheKey).ifPresentOrElse(
                existing -> {
                    existing.setCacheData(data);
                    existing.setExpiresAt(LocalDateTime.now().plusMinutes(defaultTtlMinutes));
                    existing.setVersion(existing.getVersion() + 1);
                    cacheRepository.save(existing);
                    log.info("Cache updated for key: {}, new version: {}", cacheKey, existing.getVersion());
                },
                () -> cacheDashboard(cacheKey, data)
        );
    }

    /**
     * Invalidate cache by key
     */
    @Transactional
    @CacheEvict(value = "dashboard", key = "#cacheKey")
    public void invalidateCache(String cacheKey) {
        log.info("Invalidating cache for key: {}", cacheKey);
        cacheRepository.deleteByCacheKey(cacheKey);
    }

    /**
     * Invalidate all caches with pattern
     */
    @Transactional
    @CacheEvict(value = "dashboard", allEntries = true)
    public void invalidateCachePattern(String pattern) {
        log.info("Invalidating all caches with pattern: {}", pattern);
        cacheRepository.deleteByCacheKeyPattern(pattern);
    }

    /**
     * Invalidate all caches
     */
    @Transactional
    @CacheEvict(value = "dashboard", allEntries = true)
    public void invalidateAllCaches() {
        log.info("Invalidating all dashboard caches");
        cacheRepository.deleteAll();
    }

    /**
     * Clean up expired caches
     */
    @Transactional
    public int cleanupExpiredCaches() {
        log.info("Cleaning up expired dashboard caches");
        LocalDateTime now = LocalDateTime.now();
        int deleted = cacheRepository.deleteAllExpired(now);
        log.info("Deleted {} expired cache entries", deleted);
        return deleted;
    }

    /**
     * Refresh cache with new data
     */
    @Transactional
    public DashboardResponse refreshCache(String cacheKey, DashboardResponse newData) {
        log.info("Refreshing cache for key: {}", cacheKey);
        cacheDashboard(cacheKey, newData);
        return newData;
    }

    /**
     * Get cache statistics
     */
    public CacheStatistics getCacheStatistics() {
        LocalDateTime now = LocalDateTime.now();

        long totalEntries = cacheRepository.count();
        long validEntries = cacheRepository.countValid(now);
        long expiredEntries = cacheRepository.countExpired(now);

        List<Object[]> sizeByType = cacheRepository.getCacheSizeByType();

        return CacheStatistics.builder()
                .totalEntries(totalEntries)
                .validEntries(validEntries)
                .expiredEntries(expiredEntries)
                .cacheSizeByType(sizeByType)
                .maxEntries(maxCacheEntries)
                .defaultTtlMinutes(defaultTtlMinutes)
                .build();
    }
}
