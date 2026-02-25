package com.vyxentra.vehicle.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RateLimitResult {
    private boolean allowed;
    private long remaining;
    private long limit;
    private long resetTime;
    private long retryAfterSeconds;

    public static RateLimitResult allowed(long remaining, long limit, long resetTime) {
        return RateLimitResult.builder()
                .allowed(true)
                .remaining(remaining)
                .limit(limit)
                .resetTime(resetTime)
                .retryAfterSeconds(0)
                .build();
    }

    public static RateLimitResult denied(long limit, long retryAfterSeconds) {
        return RateLimitResult.builder()
                .allowed(false)
                .remaining(0)
                .limit(limit)
                .resetTime(System.currentTimeMillis() + (retryAfterSeconds * 1000))
                .retryAfterSeconds(retryAfterSeconds)
                .build();
    }
}