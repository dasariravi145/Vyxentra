package com.vyxentra.vehicle.security;

import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.function.Predicate;

@Component
public class RouteValidator {

    private static final List<String> openApiEndpoints = List.of(
            "/api/v1/auth/login",
            "/api/v1/auth/verify-otp",
            "/api/v1/auth/refresh",
            "/api/v1/public/",
            "/actuator/health",
            "/actuator/info",
            "/fallback/",
            "/webjars/",
            "/swagger-ui",
            "/v3/api-docs"
    );

    public Predicate<ServerHttpRequest> isSecured =
            request -> openApiEndpoints
                    .stream()
                    .noneMatch(uri -> request.getURI().getPath().contains(uri));

    public boolean isPublicEndpoint(String path) {
        return openApiEndpoints.stream().anyMatch(path::startsWith);
    }

    public boolean isAdminEndpoint(String path) {
        return path.startsWith("/api/v1/admin/");
    }

    public boolean isProviderEndpoint(String path) {
        return path.startsWith("/api/v1/providers/") ||
                path.startsWith("/api/v1/employees/");
    }

    public boolean isCustomerEndpoint(String path) {
        return path.startsWith("/api/v1/users/") ||
                path.startsWith("/api/v1/bookings/");
    }
}
