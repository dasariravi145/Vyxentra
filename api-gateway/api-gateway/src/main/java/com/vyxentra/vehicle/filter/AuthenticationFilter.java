package com.vyxentra.vehicle.filter;

import com.vyxentra.vehicle.constants.ServiceConstants;
import com.vyxentra.vehicle.enums.Role;
import com.vyxentra.vehicle.security.JwtUtil;
import com.vyxentra.vehicle.validator.RouteValidator;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.List;

@Slf4j
@Component
public class AuthenticationFilter implements GlobalFilter, Ordered {

    private final RouteValidator routeValidator;
    private final JwtUtil jwtUtil;

    public AuthenticationFilter(RouteValidator routeValidator, JwtUtil jwtUtil) {
        this.routeValidator = routeValidator;
        this.jwtUtil = jwtUtil;
    }

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        ServerHttpRequest request = exchange.getRequest();

        // Skip authentication for open endpoints
        if (routeValidator.isOpenEndpoint(request)) {
            log.debug("Skipping authentication for open endpoint: {}", request.getURI());
            return chain.filter(exchange);
        }

        // Check for authorization header
        if (!request.getHeaders().containsKey(ServiceConstants.AUTHORIZATION_HEADER)) {
            return handleUnauthorized(exchange, "Missing authorization header");
        }

        String authHeader = request.getHeaders().getFirst(ServiceConstants.AUTHORIZATION_HEADER);
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return handleUnauthorized(exchange, "Invalid authorization header format");
        }

        String token = authHeader.substring(7);

        try {
            // Validate JWT
            if (!jwtUtil.validateToken(token)) {
                return handleUnauthorized(exchange, "Invalid or expired token");
            }

            // Extract claims
            String userId = jwtUtil.getUserIdFromToken(token);
            String role = jwtUtil.getRoleFromToken(token);
            boolean isSuspended = jwtUtil.isSuspended(token);

            // Check if provider is suspended
            if (isSuspended) {
                return handleForbidden(exchange, "Provider account is suspended");
            }

            // Validate role-based access
            String path = request.getURI().getPath();
            if (!validateRoleAccess(role, path)) {
                return handleForbidden(exchange, "Insufficient permissions");
            }

            // Add user info to headers for downstream services
            ServerHttpRequest mutatedRequest = request.mutate()
                    .header(ServiceConstants.USER_ID_HEADER, userId)
                    .header(ServiceConstants.ROLE_HEADER, role)
                    .build();

            log.debug("Authentication successful for user: {}, role: {}", userId, role);
            return chain.filter(exchange.mutate().request(mutatedRequest).build());

        } catch (Exception e) {
            log.error("Authentication error: {}", e.getMessage());
            return handleUnauthorized(exchange, "Token validation failed");
        }
    }

    private boolean validateRoleAccess(String role, String path) {
        Role userRole = Role.valueOf(role);

        // Admin has access to all endpoints
        if (userRole == Role.ADMIN) {
            return true;
        }

        // Customer endpoints
        if (path.startsWith("/api/v1/users") && userRole != Role.CUSTOMER) {
            return false;
        }

        // Provider endpoints
        if (path.startsWith("/api/v1/providers") && userRole != Role.PROVIDER) {
            return false;
        }

        // Employee endpoints
        if (path.startsWith("/api/v1/employees") && userRole != Role.EMPLOYEE) {
            return false;
        }

        // Booking endpoints - customers and providers can access
        if (path.startsWith("/api/v1/bookings")) {
            return userRole == Role.CUSTOMER || userRole == Role.PROVIDER || userRole == Role.EMPLOYEE;
        }

        // Emergency endpoints - customers only
        if (path.startsWith("/api/v1/emergency") && userRole != Role.CUSTOMER) {
            return false;
        }

        // Payment endpoints
        if (path.startsWith("/api/v1/payments")) {
            return userRole == Role.CUSTOMER || userRole == Role.PROVIDER;
        }

        return true;
    }

    private Mono<Void> handleUnauthorized(ServerWebExchange exchange, String message) {
        log.warn("Unauthorized access attempt: {}", message);
        ServerHttpResponse response = exchange.getResponse();
        response.setStatusCode(HttpStatus.UNAUTHORIZED);
        return response.setComplete();
    }

    private Mono<Void> handleForbidden(ServerWebExchange exchange, String message) {
        log.warn("Forbidden access attempt: {}", message);
        ServerHttpResponse response = exchange.getResponse();
        response.setStatusCode(HttpStatus.FORBIDDEN);
        return response.setComplete();
    }

    @Override
    public int getOrder() {
        return -1;
    }
}