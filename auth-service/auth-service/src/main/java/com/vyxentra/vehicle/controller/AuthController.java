package com.vyxentra.vehicle.controller;

import com.vyxentra.vehicle.dto.request.LoginRequest;
import com.vyxentra.vehicle.dto.request.OtpVerificationRequest;
import com.vyxentra.vehicle.dto.response.ApiResponse;
import com.vyxentra.vehicle.dto.response.JwtResponse;
import com.vyxentra.vehicle.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/login")
    public ResponseEntity<ApiResponse<Void>> login(@Valid @RequestBody LoginRequest request) {
        log.info("Login request received for mobile: {}", request.getMobileNumber());
        authService.sendOtp(request);
        return ResponseEntity.ok(ApiResponse.success("OTP sent successfully", null));
    }

    @PostMapping("/verify-otp")
    public ResponseEntity<ApiResponse<JwtResponse>> verifyOtp(
            @Valid @RequestBody OtpVerificationRequest request) {
        log.info("OTP verification request for mobile: {}", request.getMobileNumber());
        JwtResponse response = authService.verifyOtp(request);
        return ResponseEntity.ok(ApiResponse.success("OTP verified successfully", response));
    }

    @PostMapping("/refresh")
    public ResponseEntity<ApiResponse<JwtResponse>> refreshToken(
            @RequestHeader("Authorization") String refreshToken) {
        log.info("Token refresh request received");
        String token = refreshToken.substring(7);
        JwtResponse response = authService.refreshToken(token);
        return ResponseEntity.ok(ApiResponse.success("Token refreshed successfully", response));
    }

    @PostMapping("/logout")
    public ResponseEntity<ApiResponse<Void>> logout(@RequestHeader("Authorization") String token) {
        log.info("Logout request received");
        String jwtToken = token.substring(7);
        authService.logout(jwtToken);
        return ResponseEntity.ok(ApiResponse.success("Logged out successfully", null));
    }

    @PostMapping("/validate")
    public ResponseEntity<ApiResponse<Boolean>> validateToken(
            @RequestHeader("Authorization") String token) {
        String jwtToken = token.substring(7);
        boolean isValid = authService.validateToken(jwtToken);
        return ResponseEntity.ok(ApiResponse.success(isValid));
    }
}

