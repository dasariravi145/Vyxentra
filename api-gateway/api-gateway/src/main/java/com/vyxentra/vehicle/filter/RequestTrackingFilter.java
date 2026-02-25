package com.vyxentra.vehicle.filter;

import com.vyxentra.vehicle.util.IdGenerator;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.function.Supplier;

@Slf4j
@Component
public class RequestTrackingFilter extends AbstractGatewayFilterFactory<RequestTrackingFilter.Config> {

    private static final String CORRELATION_ID_HEADER = "X-Correlation-ID";
    private static final String REQUEST_ID_HEADER = "X-Request-ID";
    private static final String SPAN_ID_HEADER = "X-Span-ID";
    private static final String TRACE_ID_HEADER = "X-Trace-ID";

    public RequestTrackingFilter() {
        super(Config.class);
    }

    @Override
    public GatewayFilter apply(Config config) {
        return (exchange, chain) -> {
            ServerHttpRequest request = exchange.getRequest();

            // Generate or extract correlation ID
            String correlationId = request.getHeaders().getFirst(config.getCorrelationIdHeader());
            if (correlationId == null || correlationId.isEmpty()) {
                correlationId = config.isUseCustomGenerator() ?
                        config.getCorrelationIdGenerator().get() :
                        IdGenerator.generateCorrelationId();
            }
            final String finalCorrelationId = correlationId;

            // Generate request ID
            String requestId = request.getHeaders().getFirst(config.getRequestIdHeader());
            if (requestId == null || requestId.isEmpty()) {
                requestId = IdGenerator.generateRequestId();
            }
            final String finalRequestId = requestId;

            // Generate span ID for distributed tracing
            String spanId = request.getHeaders().getFirst(config.getSpanIdHeader());
            if (spanId == null && config.isGenerateSpanId()) {
                spanId = UUID.randomUUID().toString().substring(0, 8);
            }
            final String finalSpanId = spanId;

            // Add headers to request
            ServerHttpRequest.Builder requestBuilder = request.mutate();

            if (config.isAddCorrelationId()) {
                requestBuilder.header(config.getCorrelationIdHeader(), finalCorrelationId);
            }

            if (config.isAddRequestId()) {
                requestBuilder.header(config.getRequestIdHeader(), finalRequestId);
            }

            if (config.isAddSpanId() && finalSpanId != null) {
                requestBuilder.header(config.getSpanIdHeader(), finalSpanId);
            }

            // Add trace ID if configured
            if (config.isAddTraceId()) {
                String traceId = request.getHeaders().getFirst(config.getTraceIdHeader());
                if (traceId == null) {
                    traceId = finalCorrelationId;
                }
                requestBuilder.header(config.getTraceIdHeader(), traceId);
            }

            // Add client info if configured
            if (config.isAddClientInfo()) {
                String clientIp = request.getRemoteAddress() != null ?
                        request.getRemoteAddress().getAddress().getHostAddress() : "unknown";
                requestBuilder.header("X-Client-IP", clientIp);
                if (request.getRemoteAddress() != null) {
                    requestBuilder.header("X-Client-Port",
                            String.valueOf(request.getRemoteAddress().getPort()));
                }
            }

            // Add static metadata if configured
            if (config.getStaticMetadata() != null && !config.getStaticMetadata().isEmpty()) {
                config.getStaticMetadata().forEach(requestBuilder::header);
            }

            ServerHttpRequest mutatedRequest = requestBuilder.build();

            // Add to MDC for logging with configured fields
            if (config.isEnableMdc()) {
                MDC.put(config.getMdcCorrelationIdKey(), finalCorrelationId);
                MDC.put(config.getMdcRequestIdKey(), finalRequestId);
                MDC.put(config.getMdcPathKey(), mutatedRequest.getPath().value());
                MDC.put(config.getMdcMethodKey(), mutatedRequest.getMethod().toString());

                if (finalSpanId != null) {
                    MDC.put(config.getMdcSpanIdKey(), finalSpanId);
                }

                // Extract user ID if configured
                if (config.isExtractUserId()) {
                    String userId = mutatedRequest.getHeaders().getFirst(config.getUserIdHeader());
                    if (userId != null) {
                        MDC.put(config.getMdcUserIdKey(), userId);
                    }
                }
            }

            // Log request if configured
            if (config.isLogRequest()) {
                // Check sampling
                if (!config.isEnableSampling() || shouldLog(config.getSamplingRate())) {
                    log.info("Request tracking - Method: {}, Path: {}, Client: {}, CorrelationID: {}",
                            mutatedRequest.getMethod(),
                            mutatedRequest.getPath(),
                            mutatedRequest.getRemoteAddress(),
                            finalCorrelationId);
                }
            }

            long startTime = config.isMeasureLatency() ? System.currentTimeMillis() : 0;
            final long finalStartTime = startTime;

            return chain.filter(exchange.mutate().request(mutatedRequest).build())
                    .doFinally(signalType -> {
                        if (config.isMeasureLatency()) {
                            long duration = System.currentTimeMillis() - finalStartTime;
                            MDC.put("requestDuration", String.valueOf(duration));

                            if (duration > config.getSlowRequestThresholdMs()) {
                                log.warn("Slow request detected: {} ms for path: {}",
                                        duration, mutatedRequest.getPath());
                            }
                        }

                        // Clear MDC after request completion
                        if (config.isEnableMdc()) {
                            MDC.clear();
                        }
                    })
                    .doOnSuccess(aVoid -> {
                        if (config.isLogSuccess()) {
                            log.info("Request completed successfully - Path: {}, CorrelationID: {}",
                                    mutatedRequest.getPath(), finalCorrelationId);
                        }
                    })
                    .doOnError(error -> {
                        if (config.isLogError()) {
                            log.error("Request failed - Path: {}, CorrelationID: {}, Error: {}",
                                    mutatedRequest.getPath(), finalCorrelationId, error.getMessage());
                        }
                    });
        };
    }

    private boolean shouldLog(int samplingRate) {
        return samplingRate >= 100 || Math.random() * 100 < samplingRate;
    }

    @Data
    @EqualsAndHashCode(callSuper = false)
    public static class Config {

        // Header configuration
        private String correlationIdHeader = CORRELATION_ID_HEADER;
        private String requestIdHeader = REQUEST_ID_HEADER;
        private String spanIdHeader = SPAN_ID_HEADER;
        private String traceIdHeader = TRACE_ID_HEADER;

        // Header addition flags
        private boolean addCorrelationId = true;
        private boolean addRequestId = true;
        private boolean addSpanId = true;
        private boolean addTraceId = false;
        private boolean addClientInfo = true;

        // ID generation
        private boolean useCustomGenerator = false;
        private Supplier<String> correlationIdGenerator = IdGenerator::generateCorrelationId;
        private boolean generateSpanId = true;

        // MDC (Mapped Diagnostic Context) configuration
        private boolean enableMdc = true;
        private String mdcCorrelationIdKey = "correlationId";
        private String mdcRequestIdKey = "requestId";
        private String mdcSpanIdKey = "spanId";
        private String mdcPathKey = "path";
        private String mdcMethodKey = "method";
        private String mdcUserIdKey = "userId";

        // Logging configuration
        private boolean logRequest = true;
        private boolean logSuccess = true;
        private boolean logError = true;
        private boolean logHeaders = false;

        // Performance monitoring
        private boolean measureLatency = true;
        private long slowRequestThresholdMs = 5000; // 5 seconds

        // Sampling configuration
        private boolean enableSampling = false;
        private int samplingRate = 100; // percentage

        // Forward headers to downstream services
        private boolean forwardCorrelationId = true;
        private boolean forwardRequestId = true;
        private boolean forwardSpanId = true;

        // Timeout configuration
        private long requestTimeoutMs = 30000; // 30 seconds

        // Metadata to add
        private Map<String, String> staticMetadata = new HashMap<>();

        // User identification
        private boolean extractUserId = true;
        private String userIdHeader = "X-User-Id";

        public static ConfigBuilder builder() {
            return new ConfigBuilder();
        }

        public static class ConfigBuilder {
            private String correlationIdHeader = CORRELATION_ID_HEADER;
            private String requestIdHeader = REQUEST_ID_HEADER;
            private String spanIdHeader = SPAN_ID_HEADER;
            private String traceIdHeader = TRACE_ID_HEADER;
            private boolean addCorrelationId = true;
            private boolean addRequestId = true;
            private boolean addSpanId = true;
            private boolean addTraceId = false;
            private boolean addClientInfo = true;
            private boolean useCustomGenerator = false;
            private Supplier<String> correlationIdGenerator = IdGenerator::generateCorrelationId;
            private boolean generateSpanId = true;
            private boolean enableMdc = true;
            private String mdcCorrelationIdKey = "correlationId";
            private String mdcRequestIdKey = "requestId";
            private String mdcSpanIdKey = "spanId";
            private String mdcPathKey = "path";
            private String mdcMethodKey = "method";
            private String mdcUserIdKey = "userId";
            private boolean logRequest = true;
            private boolean logSuccess = true;
            private boolean logError = true;
            private boolean logHeaders = false;
            private boolean measureLatency = true;
            private long slowRequestThresholdMs = 5000;
            private boolean enableSampling = false;
            private int samplingRate = 100;
            private boolean forwardCorrelationId = true;
            private boolean forwardRequestId = true;
            private boolean forwardSpanId = true;
            private long requestTimeoutMs = 30000;
            private Map<String, String> staticMetadata = new HashMap<>();
            private boolean extractUserId = true;
            private String userIdHeader = "X-User-Id";

            public ConfigBuilder correlationIdHeader(String correlationIdHeader) {
                this.correlationIdHeader = correlationIdHeader;
                return this;
            }

            public ConfigBuilder requestIdHeader(String requestIdHeader) {
                this.requestIdHeader = requestIdHeader;
                return this;
            }

            public ConfigBuilder spanIdHeader(String spanIdHeader) {
                this.spanIdHeader = spanIdHeader;
                return this;
            }

            public ConfigBuilder traceIdHeader(String traceIdHeader) {
                this.traceIdHeader = traceIdHeader;
                return this;
            }

            public ConfigBuilder addCorrelationId(boolean addCorrelationId) {
                this.addCorrelationId = addCorrelationId;
                return this;
            }

            public ConfigBuilder addRequestId(boolean addRequestId) {
                this.addRequestId = addRequestId;
                return this;
            }

            public ConfigBuilder addSpanId(boolean addSpanId) {
                this.addSpanId = addSpanId;
                return this;
            }

            public ConfigBuilder addTraceId(boolean addTraceId) {
                this.addTraceId = addTraceId;
                return this;
            }

            public ConfigBuilder addClientInfo(boolean addClientInfo) {
                this.addClientInfo = addClientInfo;
                return this;
            }

            public ConfigBuilder useCustomGenerator(boolean useCustomGenerator) {
                this.useCustomGenerator = useCustomGenerator;
                return this;
            }

            public ConfigBuilder correlationIdGenerator(Supplier<String> correlationIdGenerator) {
                this.correlationIdGenerator = correlationIdGenerator;
                return this;
            }

            public ConfigBuilder generateSpanId(boolean generateSpanId) {
                this.generateSpanId = generateSpanId;
                return this;
            }

            public ConfigBuilder enableMdc(boolean enableMdc) {
                this.enableMdc = enableMdc;
                return this;
            }

            public ConfigBuilder mdcCorrelationIdKey(String mdcCorrelationIdKey) {
                this.mdcCorrelationIdKey = mdcCorrelationIdKey;
                return this;
            }

            public ConfigBuilder mdcRequestIdKey(String mdcRequestIdKey) {
                this.mdcRequestIdKey = mdcRequestIdKey;
                return this;
            }

            public ConfigBuilder mdcSpanIdKey(String mdcSpanIdKey) {
                this.mdcSpanIdKey = mdcSpanIdKey;
                return this;
            }

            public ConfigBuilder mdcPathKey(String mdcPathKey) {
                this.mdcPathKey = mdcPathKey;
                return this;
            }

            public ConfigBuilder mdcMethodKey(String mdcMethodKey) {
                this.mdcMethodKey = mdcMethodKey;
                return this;
            }

            public ConfigBuilder mdcUserIdKey(String mdcUserIdKey) {
                this.mdcUserIdKey = mdcUserIdKey;
                return this;
            }

            public ConfigBuilder logRequest(boolean logRequest) {
                this.logRequest = logRequest;
                return this;
            }

            public ConfigBuilder logSuccess(boolean logSuccess) {
                this.logSuccess = logSuccess;
                return this;
            }

            public ConfigBuilder logError(boolean logError) {
                this.logError = logError;
                return this;
            }

            public ConfigBuilder measureLatency(boolean measureLatency) {
                this.measureLatency = measureLatency;
                return this;
            }

            public ConfigBuilder slowRequestThresholdMs(long slowRequestThresholdMs) {
                this.slowRequestThresholdMs = slowRequestThresholdMs;
                return this;
            }

            public ConfigBuilder enableSampling(boolean enableSampling) {
                this.enableSampling = enableSampling;
                return this;
            }

            public ConfigBuilder samplingRate(int samplingRate) {
                this.samplingRate = samplingRate;
                return this;
            }

            public ConfigBuilder extractUserId(boolean extractUserId) {
                this.extractUserId = extractUserId;
                return this;
            }

            public ConfigBuilder userIdHeader(String userIdHeader) {
                this.userIdHeader = userIdHeader;
                return this;
            }

            public ConfigBuilder addStaticMetadata(String key, String value) {
                this.staticMetadata.put(key, value);
                return this;
            }

            public Config build() {
                Config config = new Config();
                config.setCorrelationIdHeader(this.correlationIdHeader);
                config.setRequestIdHeader(this.requestIdHeader);
                config.setSpanIdHeader(this.spanIdHeader);
                config.setTraceIdHeader(this.traceIdHeader);
                config.setAddCorrelationId(this.addCorrelationId);
                config.setAddRequestId(this.addRequestId);
                config.setAddSpanId(this.addSpanId);
                config.setAddTraceId(this.addTraceId);
                config.setAddClientInfo(this.addClientInfo);
                config.setUseCustomGenerator(this.useCustomGenerator);
                config.setCorrelationIdGenerator(this.correlationIdGenerator);
                config.setGenerateSpanId(this.generateSpanId);
                config.setEnableMdc(this.enableMdc);
                config.setMdcCorrelationIdKey(this.mdcCorrelationIdKey);
                config.setMdcRequestIdKey(this.mdcRequestIdKey);
                config.setMdcSpanIdKey(this.mdcSpanIdKey);
                config.setMdcPathKey(this.mdcPathKey);
                config.setMdcMethodKey(this.mdcMethodKey);
                config.setMdcUserIdKey(this.mdcUserIdKey);
                config.setLogRequest(this.logRequest);
                config.setLogSuccess(this.logSuccess);
                config.setLogError(this.logError);
                config.setLogHeaders(this.logHeaders);
                config.setMeasureLatency(this.measureLatency);
                config.setSlowRequestThresholdMs(this.slowRequestThresholdMs);
                config.setEnableSampling(this.enableSampling);
                config.setSamplingRate(this.samplingRate);
                config.setForwardCorrelationId(this.forwardCorrelationId);
                config.setForwardRequestId(this.forwardRequestId);
                config.setForwardSpanId(this.forwardSpanId);
                config.setRequestTimeoutMs(this.requestTimeoutMs);
                config.setStaticMetadata(this.staticMetadata);
                config.setExtractUserId(this.extractUserId);
                config.setUserIdHeader(this.userIdHeader);
                return config;
            }
        }
    }
}