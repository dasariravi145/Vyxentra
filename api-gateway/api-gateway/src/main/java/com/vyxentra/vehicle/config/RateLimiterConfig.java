package com.vyxentra.vehicle.config;

import org.springframework.cloud.gateway.filter.ratelimit.KeyResolver;
import org.springframework.cloud.gateway.filter.ratelimit.RedisRateLimiter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import reactor.core.publisher.Mono;

import java.security.Principal;

@Configuration
public class RateLimiterConfig {

    // =========================
    // Key Resolvers
    // =========================

    @Bean
    @Primary
    public KeyResolver apiKeyResolver() {
        return exchange -> {
            if (exchange.getRequest().getRemoteAddress() != null) {
                return Mono.just(
                        exchange.getRequest()
                                .getRemoteAddress()
                                .getAddress()
                                .getHostAddress()
                );
            }
            return Mono.just("unknown");
        };
    }

    @Bean
    public KeyResolver userKeyResolver() {
        return exchange -> exchange.getPrincipal()
                .map(Principal::getName)
                .defaultIfEmpty("anonymous");
    }

    @Bean
    public KeyResolver combinedKeyResolver() {
        return exchange -> exchange.getPrincipal()
                .map(Principal::getName)
                .switchIfEmpty(
                        Mono.justOrEmpty(exchange.getRequest().getRemoteAddress())
                                .map(addr -> addr.getAddress().getHostAddress())
                                .defaultIfEmpty("unknown")
                );
    }

    // =========================
    // Rate Limiters
    // =========================

    @Bean
    @Primary   // 👈 IMPORTANT: Gateway needs exactly one default RateLimiter
    public RedisRateLimiter redisRateLimiter() {
        return new RedisRateLimiter(10, 20, 1);
    }

    @Bean
    public RedisRateLimiter strictRedisRateLimiter() {
        return new RedisRateLimiter(5, 10, 1);
    }
}