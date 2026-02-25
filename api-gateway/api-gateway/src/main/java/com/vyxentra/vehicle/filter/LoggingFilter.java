package com.vyxentra.vehicle.filter;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.atomic.AtomicLong;

@Slf4j
@Component
public class LoggingFilter extends AbstractGatewayFilterFactory<LoggingFilter.Config> {

    private static final AtomicLong requestCounter = new AtomicLong(0);

    public LoggingFilter() {
        super(Config.class);
    }

    @Override
    public GatewayFilter apply(Config config) {
        return (exchange, chain) -> {
            long requestNumber = requestCounter.incrementAndGet();
            ServerHttpRequest request = exchange.getRequest();
            long startTime = System.currentTimeMillis();

            // Log request if enabled
            if (config.isLogRequest()) {
                log.info("[Request #{}] {} {} - Headers: {}",
                        requestNumber,
                        request.getMethod(),
                        request.getPath(),
                        config.isLogHeaders() ? sanitizeHeaders(request.getHeaders()) : "[HIDDEN]");

                // Log request body if enabled (would require body capture)
                if (config.isLogRequestBody()) {
                    // Implementation for body logging would go here
                    log.debug("[Request #{}] Body logging enabled", requestNumber);
                }
            }

            // Add request ID if enabled
            if (config.isAddRequestId()) {
                exchange = exchange.mutate()
                        .request(r -> r.header(config.getRequestIdHeader(),
                                String.valueOf(requestNumber)))
                        .build();
            }

            org.springframework.web.server.ServerWebExchange finalExchange = exchange;
            return chain.filter(exchange)
                    .doFinally(signalType -> {
                        long duration = System.currentTimeMillis() - startTime;
                        ServerHttpResponse response = finalExchange.getResponse();

                        // Log response if enabled
                        if (config.isLogResponse()) {
                            log.info("[Request #{}] Completed with status: {} in {}ms",
                                    requestNumber,
                                    response.getStatusCode(),
                                    duration);

                            // Log response headers if enabled
                            if (config.isLogHeaders() && config.isLogResponseHeaders()) {
                                log.debug("[Request #{}] Response headers: {}",
                                        requestNumber, response.getHeaders());
                            }
                        }

                        // Log slow requests
                        if (duration > config.getSlowRequestThresholdMs()) {
                            log.warn("[Request #{}] Slow request detected: {}ms - {} {}",
                                    requestNumber, duration, request.getMethod(), request.getPath());
                        }
                    });
        };
    }

    private String sanitizeHeaders(org.springframework.http.HttpHeaders headers) {
        // Mask sensitive headers
        if (headers.containsKey("Authorization")) {
            headers = new org.springframework.http.HttpHeaders(headers);
            headers.set("Authorization", "Bearer [PROTECTED]");
        }
        return headers.toString();
    }

    @Data
    @EqualsAndHashCode(callSuper = false)
    public static class Config {

        // Basic logging configuration
        private boolean logRequest = true;
        private boolean logResponse = true;
        private boolean logHeaders = false;
        private boolean logRequestBody = false;
        private boolean logResponseBody = false;
        private boolean logResponseHeaders = false;

        // Request ID configuration
        private boolean addRequestId = true;
        private String requestIdHeader = "X-Request-ID";

        // Performance logging
        private long slowRequestThresholdMs = 5000; // 5 seconds

        // Sensitive headers to mask
        private Set<String> sensitiveHeaders = new HashSet<>(Arrays.asList(
                "Authorization",
                "Cookie",
                "Set-Cookie",
                "X-Auth-Token",
                "X-API-Key"
        ));

        // Sampling configuration (log only percentage of requests)
        private int samplingRate = 100; // 100% = log all, 50 = log 50%
        private boolean enableSampling = false;

        // Log level configuration
        private String requestLogLevel = "INFO";
        private String responseLogLevel = "INFO";
        private String errorLogLevel = "ERROR";

        // Payload size limits
        private int maxBodyLogSize = 1000; // characters
        private boolean truncateLargeBodies = true;

        // Color coding for console logs
        private boolean enableColorCoding = false;

        // Custom log format
        private String logFormat = "[Request #%d] %s %s - Status: %s, Duration: %dms";

        public static ConfigBuilder builder() {
            return new ConfigBuilder();
        }

        public static class ConfigBuilder {
            private boolean logRequest = true;
            private boolean logResponse = true;
            private boolean logHeaders = false;
            private boolean logRequestBody = false;
            private boolean logResponseBody = false;
            private boolean logResponseHeaders = false;
            private boolean addRequestId = true;
            private String requestIdHeader = "X-Request-ID";
            private long slowRequestThresholdMs = 5000;
            private Set<String> sensitiveHeaders = new HashSet<>(Arrays.asList(
                    "Authorization", "Cookie", "Set-Cookie", "X-Auth-Key"
            ));
            private int samplingRate = 100;
            private boolean enableSampling = false;
            private String requestLogLevel = "INFO";
            private String responseLogLevel = "INFO";
            private String errorLogLevel = "ERROR";
            private int maxBodyLogSize = 1000;
            private boolean truncateLargeBodies = true;
            private boolean enableColorCoding = false;
            private String logFormat = "[Request #%d] %s %s - Status: %s, Duration: %dms";

            public ConfigBuilder logRequest(boolean logRequest) {
                this.logRequest = logRequest;
                return this;
            }

            public ConfigBuilder logResponse(boolean logResponse) {
                this.logResponse = logResponse;
                return this;
            }

            public ConfigBuilder logHeaders(boolean logHeaders) {
                this.logHeaders = logHeaders;
                return this;
            }

            public ConfigBuilder logRequestBody(boolean logRequestBody) {
                this.logRequestBody = logRequestBody;
                return this;
            }

            public ConfigBuilder logResponseBody(boolean logResponseBody) {
                this.logResponseBody = logResponseBody;
                return this;
            }

            public ConfigBuilder addRequestId(boolean addRequestId) {
                this.addRequestId = addRequestId;
                return this;
            }

            public ConfigBuilder requestIdHeader(String requestIdHeader) {
                this.requestIdHeader = requestIdHeader;
                return this;
            }

            public ConfigBuilder slowRequestThresholdMs(long slowRequestThresholdMs) {
                this.slowRequestThresholdMs = slowRequestThresholdMs;
                return this;
            }

            public ConfigBuilder samplingRate(int samplingRate) {
                this.samplingRate = samplingRate;
                return this;
            }

            public Config build() {
                Config config = new Config();
                config.logRequest = this.logRequest;
                config.logResponse = this.logResponse;
                config.logHeaders = this.logHeaders;
                config.logRequestBody = this.logRequestBody;
                config.logResponseBody = this.logResponseBody;
                config.logResponseHeaders = this.logResponseHeaders;
                config.addRequestId = this.addRequestId;
                config.requestIdHeader = this.requestIdHeader;
                config.slowRequestThresholdMs = this.slowRequestThresholdMs;
                config.sensitiveHeaders = this.sensitiveHeaders;
                config.samplingRate = this.samplingRate;
                config.enableSampling = this.enableSampling;
                config.requestLogLevel = this.requestLogLevel;
                config.responseLogLevel = this.responseLogLevel;
                config.errorLogLevel = this.errorLogLevel;
                config.maxBodyLogSize = this.maxBodyLogSize;
                config.truncateLargeBodies = this.truncateLargeBodies;
                config.enableColorCoding = this.enableColorCoding;
                config.logFormat = this.logFormat;
                return config;
            }
        }
    }
}