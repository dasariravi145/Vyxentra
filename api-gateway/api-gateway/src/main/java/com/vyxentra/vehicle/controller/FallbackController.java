package com.vyxentra.vehicle.controller;


import com.vyxentra.vehicle.dto.response.ApiResponse;
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
        return Mono.just(ResponseEntity
                .status(HttpStatus.SERVICE_UNAVAILABLE)
                .body(ApiResponse.<Void>builder()
                        .success(false)
                        .message("Auth service is temporarily unavailable. Please try again later.")
                        .timestamp(Instant.now())
                        .build()));
    }

    @GetMapping("/user")
    public Mono<ResponseEntity<ApiResponse<Void>>> userFallback() {
        return Mono.just(ResponseEntity
                .status(HttpStatus.SERVICE_UNAVAILABLE)
                .body(ApiResponse.<Void>builder()
                        .success(false)
                        .message("User service is temporarily unavailable. Please try again later.")
                        .timestamp(Instant.now())
                        .build()));
    }

    @GetMapping("/provider")
    public Mono<ResponseEntity<ApiResponse<Void>>> providerFallback() {
        return Mono.just(ResponseEntity
                .status(HttpStatus.SERVICE_UNAVAILABLE)
                .body(ApiResponse.<Void>builder()
                        .success(false)
                        .message("Provider service is temporarily unavailable. Please try again later.")
                        .timestamp(Instant.now())
                        .build()));
    }

    @GetMapping("/booking")
    public Mono<ResponseEntity<ApiResponse<Void>>> bookingFallback() {
        return Mono.just(ResponseEntity
                .status(HttpStatus.SERVICE_UNAVAILABLE)
                .body(ApiResponse.<Void>builder()
                        .success(false)
                        .message("Booking service is temporarily unavailable. Please try again later.")
                        .timestamp(Instant.now())
                        .build()));
    }

    @GetMapping("/payment")
    public Mono<ResponseEntity<ApiResponse<Void>>> paymentFallback() {
        return Mono.just(ResponseEntity
                .status(HttpStatus.SERVICE_UNAVAILABLE)
                .body(ApiResponse.<Void>builder()
                        .success(false)
                        .message("Payment service is temporarily unavailable. Please try again later.")
                        .timestamp(Instant.now())
                        .build()));
    }

    @GetMapping("/tracking")
    public Mono<ResponseEntity<ApiResponse<Void>>> trackingFallback() {
        return Mono.just(ResponseEntity
                .status(HttpStatus.SERVICE_UNAVAILABLE)
                .body(ApiResponse.<Void>builder()
                        .success(false)
                        .message("Tracking service is temporarily unavailable. Please try again later.")
                        .timestamp(Instant.now())
                        .build()));
    }

    @GetMapping("/notification")
    public Mono<ResponseEntity<ApiResponse<Void>>> notificationFallback() {
        return Mono.just(ResponseEntity
                .status(HttpStatus.SERVICE_UNAVAILABLE)
                .body(ApiResponse.<Void>builder()
                        .success(false)
                        .message("Notification service is temporarily unavailable. Please try again later.")
                        .timestamp(Instant.now())
                        .build()));
    }

    @GetMapping("/admin")
    public Mono<ResponseEntity<ApiResponse<Void>>> adminFallback() {
        return Mono.just(ResponseEntity
                .status(HttpStatus.SERVICE_UNAVAILABLE)
                .body(ApiResponse.<Void>builder()
                        .success(false)
                        .message("Admin service is temporarily unavailable. Please try again later.")
                        .timestamp(Instant.now())
                        .build()));
    }
}
