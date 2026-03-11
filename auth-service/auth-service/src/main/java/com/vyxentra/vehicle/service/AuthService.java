package com.vyxentra.vehicle.service;


import com.vyxentra.vehicle.dto.request.*;
import com.vyxentra.vehicle.dto.response.AuthResponse;
import com.vyxentra.vehicle.dto.response.TokenResponse;

public interface AuthService {

    AuthResponse register(RegisterRequest request);

    AuthResponse login(LoginRequest request);

    TokenResponse verifyOtp(VerifyOtpRequest request);

    void resendOtp(ResendOtpRequest request);

    TokenResponse refreshToken(RefreshTokenRequest request);

    void logout(String userId);

    void forgotPassword(ForgotPasswordRequest request);

    void resetPassword(ResetPasswordRequest request);

    boolean validateToken(String token);
}
