package com.vyxentra.vehicle.service;

import com.vyxentra.vehicle.dto.request.LoginRequest;
import com.vyxentra.vehicle.dto.request.OtpVerificationRequest;
import com.vyxentra.vehicle.dto.response.JwtResponse;

public interface AuthService {

    /**
     * Send OTP to user's mobile number
     */
    void sendOtp(LoginRequest request);

    /**
     * Verify OTP and generate JWT token
     */
    JwtResponse verifyOtp(OtpVerificationRequest request);

    /**
     * Refresh JWT token using refresh token
     */
    JwtResponse refreshToken(String refreshToken);

    /**
     * Logout user and invalidate tokens
     */
    void logout(String token);

    /**
     * Validate JWT token
     */
    boolean validateToken(String token);
}
