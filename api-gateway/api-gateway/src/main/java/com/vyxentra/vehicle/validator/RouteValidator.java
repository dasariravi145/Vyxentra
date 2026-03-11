package com.vyxentra.vehicle.validator;


import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;
import java.util.function.Predicate;

@Component
public class RouteValidator {

    private static final List<String> OPEN_API_ENDPOINTS = Arrays.asList(
            "/api/v1/auth/register",
            "/api/v1/auth/login",
            "/api/v1/auth/verify-otp",
            "/api/v1/auth/resend-otp",
            "/api/v1/auth/refresh-token",
            "/api/v1/auth/forgot-password",
            "/api/v1/auth/reset-password",
            "/actuator/health",
            "/actuator/info",
            "/fallback/**"
    );

    private static final Predicate<ServerHttpRequest> isOpenEndpoint =
            request -> OPEN_API_ENDPOINTS.stream()
                    .anyMatch(uri -> request.getURI().getPath().contains(uri));

    public boolean isOpenEndpoint(ServerHttpRequest request) {
        return isOpenEndpoint.test(request);
    }

    public List<String> getOpenApiEndpoints() {
        return OPEN_API_ENDPOINTS;
    }
}