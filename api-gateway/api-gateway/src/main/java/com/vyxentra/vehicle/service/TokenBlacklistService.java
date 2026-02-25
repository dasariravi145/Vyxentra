package com.vyxentra.vehicle.service;

import com.vyxentra.vehicle.constants.RedisKeys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.Date;

@Slf4j
@Service
public class TokenBlacklistService {

    @Autowired
    private ReactiveRedisTemplate<String, String> redisTemplate;

    public Mono<Boolean> blacklistToken(String token, Date expirationDate) {
        String key = RedisKeys.SESSION_TOKEN_PREFIX + token;
        long ttl = expirationDate.getTime() - System.currentTimeMillis();

        log.info("Blacklisting token with TTL: {} ms", ttl);

        return redisTemplate.opsForValue()
                .set(key, "blacklisted", Duration.ofMillis(ttl))
                .doOnSuccess(success -> {
                    if (success) {
                        log.debug("Token blacklisted successfully");
                    }
                });
    }

    public Mono<Boolean> isBlacklisted(String token) {
        String key = RedisKeys.SESSION_TOKEN_PREFIX + token;
        return redisTemplate.hasKey(key);
    }

    @Scheduled(cron = "0 0 */6 * * *") // Run every 6 hours
    public void cleanupBlacklist() {
        log.info("Running scheduled cleanup of token blacklist");
        // Redis automatically handles TTL-based cleanup
        // This method is for monitoring purposes
    }
}
