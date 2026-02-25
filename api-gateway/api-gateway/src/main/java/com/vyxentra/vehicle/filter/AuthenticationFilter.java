package com.vyxentra.vehicle.filter;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.vyxentra.vehicle.constants.ErrorCodes;
import com.vyxentra.vehicle.dto.response.ErrorResponse;
import com.vyxentra.vehicle.security.RouteValidator;
import com.vyxentra.vehicle.service.TokenBlacklistService;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.time.Instant;
import java.util.Arrays;
import java.util.List;

@Slf4j
@Component
public class AuthenticationFilter extends AbstractGatewayFilterFactory<AuthenticationFilter.Config> {

    @Autowired
    private RouteValidator routeValidator;

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private TokenBlacklistService tokenBlacklistService;

    @Autowired
    private ObjectMapper objectMapper;

    public AuthenticationFilter() {
        super(Config.class);
    }

    @Override
    public GatewayFilter apply(Config config) {
        return (exchange, chain) -> {
            ServerHttpRequest request = exchange.getRequest();

            // Skip authentication for public endpoints
            if (routeValidator.isSecured.test(request)) {
                // Check if authorization header is present
                if (!request.getHeaders().containsKey(HttpHeaders.AUTHORIZATION)) {
                    return onError(exchange, "Missing authorization header",
                            HttpStatus.UNAUTHORIZED, ErrorCodes.AUTH_UNAUTHORIZED);
                }

                String authHeader = request.getHeaders().get(HttpHeaders.AUTHORIZATION).get(0);
                if (authHeader != null && authHeader.startsWith("Bearer ")) {
                    String token = authHeader.substring(7);

                    try {
                        // Check if token is blacklisted
                        if (tokenBlacklistService.isBlacklisted(token).block()) {
                            log.warn("Blacklisted token attempt: {}", token.substring(0, 20) + "...");
                            return onError(exchange, "Token has been revoked",
                                    HttpStatus.UNAUTHORIZED, ErrorCodes.AUTH_INVALID_TOKEN);
                        }

                        // Validate token
                        if (!jwtUtil.validateToken(token)) {
                            return onError(exchange, "Invalid token",
                                    HttpStatus.UNAUTHORIZED, ErrorCodes.AUTH_INVALID_TOKEN);
                        }

                        // Extract claims and add to headers for downstream services
                        String userId = jwtUtil.extractUserId(token);
                        String roles = String.join(",", jwtUtil.extractRoles(token));

                        ServerHttpRequest mutatedRequest = request.mutate()
                                .header(config.getUserIdHeader(), userId)
                                .header(config.getRolesHeader(), roles)
                                .header(config.getRequestIdHeader(), request.getHeaders().getFirst(config.getRequestIdHeader()))
                                .build();

                        log.debug("Authenticated request - User: {}, Roles: {}, Path: {}",
                                userId, roles, request.getPath());

                        return chain.filter(exchange.mutate().request(mutatedRequest).build());

                    } catch (Exception e) {
                        log.error("Token validation error: {}", e.getMessage());
                        return onError(exchange, "Token validation failed",
                                HttpStatus.UNAUTHORIZED, ErrorCodes.AUTH_INVALID_TOKEN);
                    }
                } else {
                    return onError(exchange, "Invalid authorization header format",
                            HttpStatus.UNAUTHORIZED, ErrorCodes.AUTH_INVALID_TOKEN);
                }
            }

            return chain.filter(exchange);
        };
    }

    private Mono<Void> onError(org.springframework.web.server.ServerWebExchange exchange,
                               String message, HttpStatus status, String errorCode) {
        ServerHttpResponse response = exchange.getResponse();
        response.setStatusCode(status);
        response.getHeaders().setContentType(MediaType.APPLICATION_JSON);

        ErrorResponse errorResponse = ErrorResponse.builder()
                .timestamp(Instant.now().toString())
                .status(status.value())
                .error(errorCode)
                .message(message)
                .path(exchange.getRequest().getPath().value())
                .build();

        try {
            byte[] bytes = objectMapper.writeValueAsBytes(errorResponse);
            DataBuffer buffer = response.bufferFactory().wrap(bytes);
            return response.writeWith(Mono.just(buffer));
        } catch (JsonProcessingException e) {
            log.error("Error creating error response", e);
            return response.setComplete();
        }
    }

    @Data
    @EqualsAndHashCode(callSuper = false)
    public static class Config {

        // Header names for passing user information to downstream services
        private String userIdHeader = "X-User-Id";
        private String rolesHeader = "X-User-Roles";
        private String requestIdHeader = "X-Request-ID";

        // Token validation configuration
        private boolean validateToken = true;
        private boolean checkBlacklist = true;

        // List of paths to exclude from authentication (overrides RouteValidator)
        private List<String> excludePaths = Arrays.asList(
                "/api/v1/auth/login",
                "/api/v1/auth/verify-otp",
                "/api/v1/auth/refresh",
                "/actuator/health"
        );

        // Response configuration
        private boolean includeErrorDetails = true;
        private String errorMessagePrefix = "Authentication failed: ";

        // Token extraction configuration
        private String tokenPrefix = "Bearer ";
        private String tokenHeader = HttpHeaders.AUTHORIZATION;

        // Cache configuration
        private boolean cacheUserInfo = true;
        private int cacheExpirySeconds = 300;

        // Allow custom configuration through builder pattern
        public static ConfigBuilder builder() {
            return new ConfigBuilder();
        }

        public static class ConfigBuilder {
            private String userIdHeader = "X-User-Id";
            private String rolesHeader = "X-User-Roles";
            private String requestIdHeader = "X-Request-ID";
            private boolean validateToken = true;
            private boolean checkBlacklist = true;
            private List<String> excludePaths;
            private boolean includeErrorDetails = true;
            private String errorMessagePrefix = "Authentication failed: ";
            private String tokenPrefix = "Bearer ";
            private String tokenHeader = HttpHeaders.AUTHORIZATION;
            private boolean cacheUserInfo = true;
            private int cacheExpirySeconds = 300;

            public ConfigBuilder userIdHeader(String userIdHeader) {
                this.userIdHeader = userIdHeader;
                return this;
            }

            public ConfigBuilder rolesHeader(String rolesHeader) {
                this.rolesHeader = rolesHeader;
                return this;
            }

            public ConfigBuilder requestIdHeader(String requestIdHeader) {
                this.requestIdHeader = requestIdHeader;
                return this;
            }

            public ConfigBuilder validateToken(boolean validateToken) {
                this.validateToken = validateToken;
                return this;
            }

            public ConfigBuilder checkBlacklist(boolean checkBlacklist) {
                this.checkBlacklist = checkBlacklist;
                return this;
            }

            public ConfigBuilder excludePaths(List<String> excludePaths) {
                this.excludePaths = excludePaths;
                return this;
            }

            public ConfigBuilder includeErrorDetails(boolean includeErrorDetails) {
                this.includeErrorDetails = includeErrorDetails;
                return this;
            }

            public ConfigBuilder errorMessagePrefix(String errorMessagePrefix) {
                this.errorMessagePrefix = errorMessagePrefix;
                return this;
            }

            public ConfigBuilder tokenPrefix(String tokenPrefix) {
                this.tokenPrefix = tokenPrefix;
                return this;
            }

            public ConfigBuilder tokenHeader(String tokenHeader) {
                this.tokenHeader = tokenHeader;
                return this;
            }

            public ConfigBuilder cacheUserInfo(boolean cacheUserInfo) {
                this.cacheUserInfo = cacheUserInfo;
                return this;
            }

            public ConfigBuilder cacheExpirySeconds(int cacheExpirySeconds) {
                this.cacheExpirySeconds = cacheExpirySeconds;
                return this;
            }

            public Config build() {
                Config config = new Config();
                config.userIdHeader = this.userIdHeader;
                config.rolesHeader = this.rolesHeader;
                config.requestIdHeader = this.requestIdHeader;
                config.validateToken = this.validateToken;
                config.checkBlacklist = this.checkBlacklist;
                config.excludePaths = this.excludePaths;
                config.includeErrorDetails = this.includeErrorDetails;
                config.errorMessagePrefix = this.errorMessagePrefix;
                config.tokenPrefix = this.tokenPrefix;
                config.tokenHeader = this.tokenHeader;
                config.cacheUserInfo = this.cacheUserInfo;
                config.cacheExpirySeconds = this.cacheExpirySeconds;
                return config;
            }
        }
    }
}