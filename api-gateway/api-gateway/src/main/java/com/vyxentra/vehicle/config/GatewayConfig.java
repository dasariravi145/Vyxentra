package com.vyxentra.vehicle.config;

import com.vyxentra.vehicle.filter.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;

import java.time.Duration;

@Configuration
public class GatewayConfig {

    @Autowired
    private AuthenticationFilter authenticationFilter;

    @Autowired
    private LoggingFilter loggingFilter;

    @Autowired
    private RateLimitingFilter rateLimitingFilter;

    @Autowired
    private RequestTrackingFilter requestTrackingFilter;

    @Autowired
    private ResponseWrapperFilter responseWrapperFilter;

    @Bean
    public RouteLocator customRouteLocator(RouteLocatorBuilder builder) {
        return builder.routes()
                // Auth Service Routes (public)
                .route("auth-service", r -> r
                        .path("/api/v1/auth/**")
                        .filters(f -> f
                                .filter(requestTrackingFilter.apply(new RequestTrackingFilter.Config()))
                                .filter(loggingFilter.apply(new LoggingFilter.Config()))
                                .filter(responseWrapperFilter.apply(new ResponseWrapperFilter.Config()))
                                .circuitBreaker(config -> config
                                        .setName("authServiceCB")
                                        .setFallbackUri("forward:/fallback/auth"))
                                .retry(config -> config
                                        .setRetries(3)
                                        .setStatuses(HttpStatus.SERVICE_UNAVAILABLE)
                                        .setBackoff(Duration.ofMillis(100), Duration.ofMillis(1000), 2, true)))
                        .uri("lb://auth-service"))

                // Public endpoints (no auth required)
                .route("public-service", r -> r
                        .path("/api/v1/public/**")
                        .filters(f -> f
                                .filter(requestTrackingFilter.apply(new RequestTrackingFilter.Config()))
                                .filter(loggingFilter.apply(new LoggingFilter.Config()))
                                .filter(rateLimitingFilter.apply(new RateLimitingFilter.Config())))
                        .uri("lb://auth-service"))

                // User Service Routes (authenticated)
                .route("user-service", r -> r
                        .path("/api/v1/users/**")
                        .filters(f -> f
                                .filter(requestTrackingFilter.apply(new RequestTrackingFilter.Config()))
                                .filter(authenticationFilter.apply(new AuthenticationFilter.Config()))
                                .filter(rateLimitingFilter.apply(new RateLimitingFilter.Config()))
                                .filter(loggingFilter.apply(new LoggingFilter.Config()))
                                .filter(responseWrapperFilter.apply(new ResponseWrapperFilter.Config()))
                                .circuitBreaker(config -> config
                                        .setName("userServiceCB")
                                        .setFallbackUri("forward:/fallback/user")))
                        .uri("lb://user-service"))

                // Provider Service Routes (authenticated)
                .route("provider-service", r -> r
                        .path("/api/v1/providers/**")
                        .filters(f -> f
                                .filter(requestTrackingFilter.apply(new RequestTrackingFilter.Config()))
                                .filter(authenticationFilter.apply(new AuthenticationFilter.Config()))
                                .filter(rateLimitingFilter.apply(new RateLimitingFilter.Config()))
                                .filter(loggingFilter.apply(new LoggingFilter.Config()))
                                .filter(responseWrapperFilter.apply(new ResponseWrapperFilter.Config()))
                                .circuitBreaker(config -> config
                                        .setName("providerServiceCB")
                                        .setFallbackUri("forward:/fallback/provider")))
                        .uri("lb://provider-service"))

                // Employee Service Routes (authenticated, provider employees only)
                .route("employee-service", r -> r
                        .path("/api/v1/employees/**")
                        .filters(f -> f
                                .filter(requestTrackingFilter.apply(new RequestTrackingFilter.Config()))
                                .filter(authenticationFilter.apply(new AuthenticationFilter.Config()))
                                .filter(rateLimitingFilter.apply(new RateLimitingFilter.Config()))
                                .filter(loggingFilter.apply(new LoggingFilter.Config()))
                                .filter(responseWrapperFilter.apply(new ResponseWrapperFilter.Config()))
                                .circuitBreaker(config -> config
                                        .setName("employeeServiceCB")
                                        .setFallbackUri("forward:/fallback/employee")))
                        .uri("lb://employee-service"))

                // Booking Service Routes (authenticated)
                .route("booking-service", r -> r
                        .path("/api/v1/bookings/**")
                        .filters(f -> f
                                .filter(requestTrackingFilter.apply(new RequestTrackingFilter.Config()))
                                .filter(authenticationFilter.apply(new AuthenticationFilter.Config()))
                                .filter(rateLimitingFilter.apply(new RateLimitingFilter.Config()))
                                .filter(loggingFilter.apply(new LoggingFilter.Config()))
                                .filter(responseWrapperFilter.apply(new ResponseWrapperFilter.Config()))
                                .circuitBreaker(config -> config
                                        .setName("bookingServiceCB")
                                        .setFallbackUri("forward:/fallback/booking")))
                        .uri("lb://booking-service"))

                // Payment Service Routes (authenticated)
                .route("payment-service", r -> r
                        .path("/api/v1/payments/**")
                        .filters(f -> f
                                .filter(requestTrackingFilter.apply(new RequestTrackingFilter.Config()))
                                .filter(authenticationFilter.apply(new AuthenticationFilter.Config()))
                                .filter(rateLimitingFilter.apply(new RateLimitingFilter.Config()))
                                .filter(loggingFilter.apply(new LoggingFilter.Config()))
                                .filter(responseWrapperFilter.apply(new ResponseWrapperFilter.Config()))
                                .circuitBreaker(config -> config
                                        .setName("paymentServiceCB")
                                        .setFallbackUri("forward:/fallback/payment")))
                        .uri("lb://payment-service"))

                // Tracking Service Routes (authenticated)
                .route("tracking-service", r -> r
                        .path("/api/v1/tracking/**")
                        .filters(f -> f
                                .filter(requestTrackingFilter.apply(new RequestTrackingFilter.Config()))
                                .filter(authenticationFilter.apply(new AuthenticationFilter.Config()))
                                .filter(rateLimitingFilter.apply(new RateLimitingFilter.Config()))
                                .filter(loggingFilter.apply(new LoggingFilter.Config()))
                                .filter(responseWrapperFilter.apply(new ResponseWrapperFilter.Config()))
                                .circuitBreaker(config -> config
                                        .setName("trackingServiceCB")
                                        .setFallbackUri("forward:/fallback/tracking")))
                        .uri("lb://tracking-service"))

                // Notification Service Routes (authenticated)
                .route("notification-service", r -> r
                        .path("/api/v1/notifications/**")
                        .filters(f -> f
                                .filter(requestTrackingFilter.apply(new RequestTrackingFilter.Config()))
                                .filter(authenticationFilter.apply(new AuthenticationFilter.Config()))
                                .filter(rateLimitingFilter.apply(new RateLimitingFilter.Config()))
                                .filter(loggingFilter.apply(new LoggingFilter.Config()))
                                .filter(responseWrapperFilter.apply(new ResponseWrapperFilter.Config()))
                                .circuitBreaker(config -> config
                                        .setName("notificationServiceCB")
                                        .setFallbackUri("forward:/fallback/notification")))
                        .uri("lb://notification-service"))

                // Admin Service Routes (admin only)
                .route("admin-service", r -> r
                        .path("/api/v1/admin/**")
                        .filters(f -> f
                                .filter(requestTrackingFilter.apply(new RequestTrackingFilter.Config()))
                                .filter(authenticationFilter.apply(new AuthenticationFilter.Config()))
                                .filter(rateLimitingFilter.apply(new RateLimitingFilter.Config()))
                                .filter(loggingFilter.apply(new LoggingFilter.Config()))
                                .filter(responseWrapperFilter.apply(new ResponseWrapperFilter.Config()))
                                .circuitBreaker(config -> config
                                        .setName("adminServiceCB")
                                        .setFallbackUri("forward:/fallback/admin")))
                        .uri("lb://admin-service"))

                // WebSocket Routes for live tracking
                .route("websocket-tracking", r -> r
                        .path("/ws/tracking/**")
                        .filters(f -> f
                                .filter(requestTrackingFilter.apply(new RequestTrackingFilter.Config()))
                                .filter(authenticationFilter.apply(new AuthenticationFilter.Config())))
                        .uri("lb://tracking-service"))

                // WebSocket Routes for notifications
                .route("websocket-notifications", r -> r
                        .path("/ws/notifications/**")
                        .filters(f -> f
                                .filter(requestTrackingFilter.apply(new RequestTrackingFilter.Config()))
                                .filter(authenticationFilter.apply(new AuthenticationFilter.Config())))
                        .uri("lb://notification-service"))

                .build();
    }
}
