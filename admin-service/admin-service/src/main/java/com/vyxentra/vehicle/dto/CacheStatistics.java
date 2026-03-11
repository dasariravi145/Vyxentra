package com.vyxentra.vehicle.dto;

import java.util.List;

@lombok.Data
@lombok.Builder
@lombok.NoArgsConstructor
@lombok.AllArgsConstructor
public  class CacheStatistics {
    private long totalEntries;
    private long validEntries;
    private long expiredEntries;
    private List<Object[]> cacheSizeByType;
    private int maxEntries;
    private int defaultTtlMinutes;
    private double hitRate;
    private double missRate;
}
