package com.vyxentra.vehicle.service;

import com.vyxentra.vehicle.dto.RateLimitResult;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.time.Instant;

@Slf4j
@Service
public class RateLimiterService {

    @Autowired
    private ReactiveRedisTemplate<String, String> redisTemplate;

    public Mono<RateLimitResult> tryConsume(String key, int limit, Duration window) {
        Instant now = Instant.now();
        long windowSeconds = window.getSeconds();
        long currentWindow = now.getEpochSecond() / windowSeconds;
        String windowKey = key + ":" + currentWindow;

        return redisTemplate.opsForValue()
                .increment(windowKey)
                .flatMap(count -> {
                    if (count == 1) {
                        return redisTemplate.expire(windowKey, window)
                                .thenReturn(count);
                    }
                    return Mono.just(count);
                })
                .map(count -> {
                    long remaining = Math.max(0, limit - count);
                    long resetTime = (currentWindow + 1) * windowSeconds * 1000;

                    if (count <= limit) {
                        return RateLimitResult.allowed(remaining, limit, resetTime);
                    } else {
                        long retryAfter = (count - limit) * windowSeconds; // Approximate
                        return RateLimitResult.denied(limit, retryAfter);
                    }
                });
    }

    public Mono<Long> getCurrentCount(String key) {
        Instant now = Instant.now();
        long windowSeconds = 60; // Default 1 minute window
        long currentWindow = now.getEpochSecond() / windowSeconds;
        String windowKey = key + ":" + currentWindow;

        return redisTemplate.opsForValue()
                .get(windowKey)
                .map(Long::parseLong)
                .defaultIfEmpty(0L);
    }

    public Mono<Boolean> resetLimit(String key) {
        Instant now = Instant.now();
        long windowSeconds = 60;
        long currentWindow = now.getEpochSecond() / windowSeconds;
        String windowKey = key + ":" + currentWindow;

        return redisTemplate.delete(windowKey)
                .map(deleted -> deleted > 0);
    }
}