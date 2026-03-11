package com.vyxentra.vehicle.controller;



import com.vyxentra.vehicle.dto.response.ApiResponse;
import com.vyxentra.vehicle.dto.response.ErrorResponse;
import com.vyxentra.vehicle.exceptions.ErrorCode;
import com.vyxentra.vehicle.utils.CorrelationIdUtil;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

import java.time.Instant;

@RestController
@RequestMapping("/fallback")
public class FallbackController {

    @GetMapping("/auth")
    public Mono<ResponseEntity<ApiResponse<Void>>> authFallback() {
        return Mono.just(createFallbackResponse("Auth service is temporarily unavailable"));
    }

    @GetMapping("/users")
    public Mono<ResponseEntity<ApiResponse<Void>>> userFallback() {
        return Mono.just(createFallbackResponse("User service is temporarily unavailable"));
    }

    @GetMapping("/providers")
    public Mono<ResponseEntity<ApiResponse<Void>>> providerFallback() {
        return Mono.just(createFallbackResponse("Provider service is temporarily unavailable"));
    }

    @GetMapping("/employees")
    public Mono<ResponseEntity<ApiResponse<Void>>> employeeFallback() {
        return Mono.just(createFallbackResponse("Employee service is temporarily unavailable"));
    }

    @GetMapping("/catalog")
    public Mono<ResponseEntity<ApiResponse<Void>>> catalogFallback() {
        return Mono.just(createFallbackResponse("Service catalog is temporarily unavailable"));
    }

    @GetMapping("/bookings")
    public Mono<ResponseEntity<ApiResponse<Void>>> bookingFallback() {
        return Mono.just(createFallbackResponse("Booking service is temporarily unavailable"));
    }

    @GetMapping("/emergency")
    public Mono<ResponseEntity<ApiResponse<Void>>> emergencyFallback() {
        return Mono.just(createFallbackResponse("Emergency dispatch service is temporarily unavailable"));
    }

    @GetMapping("/tracking")
    public Mono<ResponseEntity<ApiResponse<Void>>> trackingFallback() {
        return Mono.just(createFallbackResponse("Tracking service is temporarily unavailable"));
    }

    @GetMapping("/payments")
    public Mono<ResponseEntity<ApiResponse<Void>>> paymentFallback() {
        return Mono.just(createFallbackResponse("Payment service is temporarily unavailable"));
    }

    @GetMapping("/notifications")
    public Mono<ResponseEntity<ApiResponse<Void>>> notificationFallback() {
        return Mono.just(createFallbackResponse("Notification service is temporarily unavailable"));
    }

    @GetMapping("/admin")
    public Mono<ResponseEntity<ApiResponse<Void>>> adminFallback() {
        return Mono.just(createFallbackResponse("Admin service is temporarily unavailable"));
    }

    private ResponseEntity<ApiResponse<Void>> createFallbackResponse(String message) {
        ErrorResponse error = ErrorResponse.builder()
                .code(ErrorCode.INTERNAL_SERVER_ERROR.getCode())
                .message(message)
                .timestamp(Instant.now())
                .correlationId(CorrelationIdUtil.getCurrentCorrelationId())
                .build();

        return ResponseEntity
                .status(HttpStatus.SERVICE_UNAVAILABLE)
                .body(ApiResponse.error(error));
    }
}