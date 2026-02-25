package com.vyxentra.vehicle.service;

import com.vyxentra.vehicle.constants.RedisKeys;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.Date;
import java.util.Set;

@Slf4j
@Service
@RequiredArgsConstructor
public class TokenBlacklistService {

    private final RedisTemplate<String, String> redisTemplate;

    public void blacklistToken(String token, Date expirationDate) {
        String key = RedisKeys.SESSION_TOKEN_PREFIX + token;
        long ttl = expirationDate.getTime() - System.currentTimeMillis();

        if (ttl > 0) {
            redisTemplate.opsForValue().set(key, "blacklisted", Duration.ofMillis(ttl));
            log.debug("Token blacklisted with TTL: {} ms", ttl);
        }
    }

    public boolean isBlacklisted(String token) {
        String key = RedisKeys.SESSION_TOKEN_PREFIX + token;
        return Boolean.TRUE.equals(redisTemplate.hasKey(key));
    }

    @Scheduled(cron = "0 0 */6 * * *") // Every 6 hours
    public void cleanupBlacklist() {
        log.info("Running token blacklist cleanup");
        // Redis handles TTL automatically
    }

    public void blacklistAllUserTokens(String userId) {
        String pattern = RedisKeys.SESSION_TOKEN_PREFIX + "*";
        Set<String> keys = redisTemplate.keys(pattern);

        if (keys != null) {
            redisTemplate.delete(keys);
            log.info("All tokens blacklisted for user: {}", userId);
        }
    }
}
