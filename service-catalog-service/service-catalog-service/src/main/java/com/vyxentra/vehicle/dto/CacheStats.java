package com.vyxentra.vehicle.dto;

import java.util.Map;

@lombok.Data
@lombok.Builder
@lombok.NoArgsConstructor
@lombok.AllArgsConstructor
public  class CacheStats {
    private Map<String, String> cacheInfo;
    private int totalCaches;
    private long estimatedSize;

    public static class CacheStatsBuilder {
        private Map<String, String> cacheInfo = new java.util.HashMap<>();
        private int totalCaches = 0;

        public CacheStatsBuilder cacheInfo(String cacheName, String status) {
            cacheInfo.put(cacheName, status);
            totalCaches++;
            return this;
        }

        public CacheStats build() {
            CacheStats stats = new CacheStats();
            stats.cacheInfo = this.cacheInfo;
            stats.totalCaches = this.totalCaches;
            stats.estimatedSize = this.cacheInfo.size();
            return stats;
        }
    }
}