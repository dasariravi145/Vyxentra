package com.vyxentra.vehicle.service;


import com.vyxentra.vehicle.dto.CacheStats;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class ServiceCacheManager {

    private final CacheManager cacheManager;

    private static final String[] CACHE_NAMES = {
            "allServices",
            "servicesPaginated",
            "serviceDetails",
            "serviceByType",
            "popularServices",
            "recommendedServices",
            "servicesByVehicle",
            "categories",
            "category",
            "servicesByCategory",
            "addonsForService",
            "addon"
    };

    /**
     * Evict all service-related caches
     */
    public void evictAllServiceCaches() {
        log.info("Evicting all service caches");

        for (String cacheName : CACHE_NAMES) {
            evictCache(cacheName);
        }
    }

    /**
     * Evict a specific cache by name
     */
    public void evictCache(String cacheName) {
        Cache cache = cacheManager.getCache(cacheName);
        if (cache != null) {
            cache.clear();
            log.debug("Cleared cache: {}", cacheName);
        }
    }

    /**
     * Evict cache for a specific service
     */
    public void evictServiceCache(String serviceId) {
        log.info("Evicting cache for service: {}", serviceId);

        // Evict service details cache
        Cache detailsCache = cacheManager.getCache("serviceDetails");
        if (detailsCache != null) {
            detailsCache.evict(serviceId);
        }

        // Evict service by type cache (would need to know the type)
        // For simplicity, evict all related caches
        evictCache("serviceByType");
        evictCache("allServices");
        evictCache("servicesPaginated");
    }

    /**
     * Evict cache for a specific category
     */
    public void evictCategoryCache(String categoryId) {
        log.info("Evicting cache for category: {}", categoryId);

        Cache categoryCache = cacheManager.getCache("category");
        if (categoryCache != null) {
            categoryCache.evict(categoryId);
        }

        evictCache("categories");
        evictCache("servicesByCategory");
    }

    /**
     * Evict cache for a specific addon
     */
    public void evictAddonCache(String addonId, String serviceId) {
        log.info("Evicting cache for addon: {}", addonId);

        Cache addonCache = cacheManager.getCache("addon");
        if (addonCache != null) {
            addonCache.evict(addonId);
        }

        evictCache("addonsForService");
        if (serviceId != null) {
            evictServiceCache(serviceId);
        }
    }

    /**
     * Get cache statistics
     */
    public CacheStats getCacheStats() {
        CacheStats.CacheStatsBuilder builder = CacheStats.builder();

        for (String cacheName : CACHE_NAMES) {
            Cache cache = cacheManager.getCache(cacheName);
            if (cache != null) {
                // Note: Native Redis cache stats would require additional implementation
                builder.cacheInfo(cacheName, "configured");
            }
        }

        return builder.build();
    }


}
