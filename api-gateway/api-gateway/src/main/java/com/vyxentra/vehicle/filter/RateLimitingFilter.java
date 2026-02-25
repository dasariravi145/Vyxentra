package com.vyxentra.vehicle.filter;

import com.vyxentra.vehicle.constants.RedisKeys;

import com.vyxentra.vehicle.service.RateLimiterService;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

@Slf4j
@Component
public class RateLimitingFilter extends AbstractGatewayFilterFactory<RateLimitingFilter.Config> {

    @Autowired
    private RateLimiterService rateLimiterService;

    public RateLimitingFilter() {
        super(Config.class);
    }

    @Override
    public GatewayFilter apply(Config config) {
        return (exchange, chain) -> {
            String clientIp = exchange.getRequest().getRemoteAddress() != null ?
                    exchange.getRequest().getRemoteAddress().getAddress().getHostAddress() : "unknown";
            String path = exchange.getRequest().getPath().value();
            String userId = exchange.getRequest().getHeaders().getFirst("X-User-Id");
            String apiKey = exchange.getRequest().getHeaders().getFirst("X-API-Key");

            // Check whitelist/blacklist
            if (config.getWhitelistedIps().contains(clientIp)) {
                return chain.filter(exchange);
            }

            if (config.getBlacklistedIps().contains(clientIp)) {
                log.warn("Blacklisted IP attempted access: {}", clientIp);
                return onAccessDenied(exchange.getResponse(), "Access denied");
            }

            // Determine key based on configuration priority
            String key;
            int limit;
            Duration window;

            if (config.isUseUserBased() && userId != null) {
                if (config.getWhitelistedUsers().contains(userId)) {
                    return chain.filter(exchange);
                }
                key = RedisKeys.RATE_LIMIT_PREFIX + "user:" + userId;
                limit = config.getUserRateLimit();
                window = config.getUserWindow();
            } else if (config.isUseApiKey() && apiKey != null) {
                key = RedisKeys.RATE_LIMIT_PREFIX + "apikey:" + apiKey;
                limit = config.getApiKeyRateLimit();
                window = config.getApiKeyWindow();
            } else {
                key = RedisKeys.RATE_LIMIT_PREFIX + "ip:" + clientIp;
                limit = config.getIpRateLimit();
                window = config.getIpWindow();
            }

            // Apply path-specific limits if configured
            if (config.getPathLimits() != null && config.getPathLimits().containsKey(path)) {
                limit = config.getPathLimits().get(path);
            }

            // Apply endpoint cost if configured
            int cost = config.getDefaultCost();
            if (config.getEndpointCosts() != null && config.getEndpointCosts().containsKey(path)) {
                cost = config.getEndpointCosts().get(path);
            }

            // Apply burst multiplier
            if (config.isAllowBurst()) {
                limit = limit * config.getBurstMultiplier();
            }

            final int effectiveLimit = limit;

            return rateLimiterService.tryConsume(key, effectiveLimit, window)
                    .flatMap(result -> {
                        // Add rate limit headers
                        if (config.isIncludeHeaders()) {
                            exchange.getResponse().getHeaders().add("X-RateLimit-Limit", String.valueOf(effectiveLimit));
                            exchange.getResponse().getHeaders().add("X-RateLimit-Remaining", String.valueOf(result.getRemaining()));
                            exchange.getResponse().getHeaders().add("X-RateLimit-Reset", String.valueOf(result.getResetTime()));
                        }

                        if (!result.isAllowed()) {
                            log.warn("Rate limit exceeded - IP: {}, User: {}, Path: {}, Limit: {}",
                                    clientIp, userId, path, effectiveLimit);
                            return onRateLimitExceeded(exchange.getResponse(),
                                    config.getErrorMessage(), result.getRetryAfterSeconds());
                        }

                        // Log near limit warning
                        if (result.getRemaining() < effectiveLimit * 0.1) {
                            log.debug("Near rate limit - IP: {}, User: {}, Remaining: {}",
                                    clientIp, userId, result.getRemaining());
                        }

                        return chain.filter(exchange);
                    });
        };
    }

    private Mono<Void> onRateLimitExceeded(ServerHttpResponse response, String message, long retryAfterSeconds) {
        response.setStatusCode(HttpStatus.TOO_MANY_REQUESTS);
        response.getHeaders().setContentType(MediaType.APPLICATION_JSON);
        response.getHeaders().add("Retry-After", String.valueOf(retryAfterSeconds));

        String errorBody = String.format(
                "{\"timestamp\":\"%s\",\"status\":429,\"error\":\"RATE_LIMIT_EXCEEDED\",\"message\":\"%s\",\"retryAfter\":%d}",
                java.time.Instant.now().toString(),
                message,
                retryAfterSeconds
        );

        DataBuffer buffer = response.bufferFactory()
                .wrap(errorBody.getBytes(StandardCharsets.UTF_8));

        return response.writeWith(Mono.just(buffer));
    }

    private Mono<Void> onAccessDenied(ServerHttpResponse response, String message) {
        response.setStatusCode(HttpStatus.FORBIDDEN);
        response.getHeaders().setContentType(MediaType.APPLICATION_JSON);

        String errorBody = String.format(
                "{\"timestamp\":\"%s\",\"status\":403,\"error\":\"ACCESS_DENIED\",\"message\":\"%s\"}",
                java.time.Instant.now().toString(),
                message
        );

        DataBuffer buffer = response.bufferFactory()
                .wrap(errorBody.getBytes(StandardCharsets.UTF_8));

        return response.writeWith(Mono.just(buffer));
    }

    @Data
    @EqualsAndHashCode(callSuper = false)
    public static class Config {

        // Rate limiting strategies
        private boolean useUserBased = true;
        private boolean useIpBased = true;
        private boolean useApiKey = false;

        // Default limits
        private int ipRateLimit = 20; // requests per window
        private Duration ipWindow = Duration.ofMinutes(1);

        private int userRateLimit = 100; // requests per window
        private Duration userWindow = Duration.ofMinutes(1);

        private int apiKeyRateLimit = 1000; // requests per window
        private Duration apiKeyWindow = Duration.ofMinutes(1);

        // Path-specific limits (overrides)
        private Map<String, Integer> pathLimits = new HashMap<>();

        // Special endpoints with different limits
        private int authEndpointsLimit = 5; // login/register endpoints
        private int publicEndpointsLimit = 50;
        private int adminEndpointsLimit = 200;

        // Burst configuration
        private boolean allowBurst = true;
        private int burstMultiplier = 2; // Allow burst up to 2x limit

        // Response configuration
        private String errorMessage = "Rate limit exceeded. Please try again later.";
        private boolean includeHeaders = true;

        // Redis configuration
        private String rateLimitPrefix = "ratelimit:";
        private Duration defaultWindow = Duration.ofMinutes(1);

        // Whitelist/Blacklist
        private Set<String> whitelistedIps = new HashSet<>();
        private Set<String> blacklistedIps = new HashSet<>();
        private Set<String> whitelistedUsers = new HashSet<>();

        // Advanced configuration
        private boolean enableSlidingWindow = true;
        private boolean enableDistributedCounting = true;
        private int precision = 1000; // milliseconds precision

        // Rate limit by HTTP method
        private Map<String, Integer> methodLimits = new HashMap<>();

        // Cost per request (for weighted rate limiting)
        private int defaultCost = 1;
        private Map<String, Integer> endpointCosts = new HashMap<>();

        public static ConfigBuilder builder() {
            return new ConfigBuilder();
        }

        public static class ConfigBuilder {
            private boolean useUserBased = true;
            private boolean useIpBased = true;
            private boolean useApiKey = false;
            private int ipRateLimit = 20;
            private Duration ipWindow = Duration.ofMinutes(1);
            private int userRateLimit = 100;
            private Duration userWindow = Duration.ofMinutes(1);
            private int apiKeyRateLimit = 1000;
            private Duration apiKeyWindow = Duration.ofMinutes(1);
            private Map<String, Integer> pathLimits = new HashMap<>();
            private String errorMessage = "Rate limit exceeded. Please try again later.";
            private boolean allowBurst = true;
            private int burstMultiplier = 2;
            private Set<String> whitelistedIps = new HashSet<>();
            private Set<String> blacklistedIps = new HashSet<>();
            private Set<String> whitelistedUsers = new HashSet<>();
            private Map<String, Integer> endpointCosts = new HashMap<>();

            public ConfigBuilder useUserBased(boolean useUserBased) {
                this.useUserBased = useUserBased;
                return this;
            }

            public ConfigBuilder useIpBased(boolean useIpBased) {
                this.useIpBased = useIpBased;
                return this;
            }

            public ConfigBuilder ipRateLimit(int ipRateLimit) {
                this.ipRateLimit = ipRateLimit;
                return this;
            }

            public ConfigBuilder ipWindow(Duration ipWindow) {
                this.ipWindow = ipWindow;
                return this;
            }

            public ConfigBuilder userRateLimit(int userRateLimit) {
                this.userRateLimit = userRateLimit;
                return this;
            }

            public ConfigBuilder userWindow(Duration userWindow) {
                this.userWindow = userWindow;
                return this;
            }

            public ConfigBuilder apiKeyRateLimit(int apiKeyRateLimit) {
                this.apiKeyRateLimit = apiKeyRateLimit;
                return this;
            }

            public ConfigBuilder apiKeyWindow(Duration apiKeyWindow) {
                this.apiKeyWindow = apiKeyWindow;
                return this;
            }

            public ConfigBuilder addPathLimit(String path, int limit) {
                this.pathLimits.put(path, limit);
                return this;
            }

            public ConfigBuilder pathLimits(Map<String, Integer> pathLimits) {
                this.pathLimits = pathLimits;
                return this;
            }

            public ConfigBuilder errorMessage(String errorMessage) {
                this.errorMessage = errorMessage;
                return this;
            }

            public ConfigBuilder allowBurst(boolean allowBurst) {
                this.allowBurst = allowBurst;
                return this;
            }

            public ConfigBuilder burstMultiplier(int burstMultiplier) {
                this.burstMultiplier = burstMultiplier;
                return this;
            }

            public ConfigBuilder whitelistedIps(Set<String> whitelistedIps) {
                this.whitelistedIps = whitelistedIps;
                return this;
            }

            public ConfigBuilder blacklistedIps(Set<String> blacklistedIps) {
                this.blacklistedIps = blacklistedIps;
                return this;
            }

            public ConfigBuilder whitelistedUsers(Set<String> whitelistedUsers) {
                this.whitelistedUsers = whitelistedUsers;
                return this;
            }

            public ConfigBuilder addEndpointCost(String path, int cost) {
                this.endpointCosts.put(path, cost);
                return this;
            }

            public Config build() {
                Config config = new Config();
                config.setUseUserBased(this.useUserBased);
                config.setUseIpBased(this.useIpBased);
                config.setUseApiKey(this.useApiKey);
                config.setIpRateLimit(this.ipRateLimit);
                config.setIpWindow(this.ipWindow);
                config.setUserRateLimit(this.userRateLimit);
                config.setUserWindow(this.userWindow);
                config.setApiKeyRateLimit(this.apiKeyRateLimit);
                config.setApiKeyWindow(this.apiKeyWindow);
                config.setPathLimits(this.pathLimits);
                config.setErrorMessage(this.errorMessage);
                config.setAllowBurst(this.allowBurst);
                config.setBurstMultiplier(this.burstMultiplier);
                config.setWhitelistedIps(this.whitelistedIps);
                config.setBlacklistedIps(this.blacklistedIps);
                config.setWhitelistedUsers(this.whitelistedUsers);
                config.setEndpointCosts(this.endpointCosts);
                return config;
            }
        }
    }
}