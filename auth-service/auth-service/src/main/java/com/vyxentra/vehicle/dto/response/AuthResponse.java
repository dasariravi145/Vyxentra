package com.vyxentra.vehicle.dto.response;

import com.vyxentra.vehicle.enums.Role;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AuthResponse {
    private String userId;
    private String phoneNumber;
    private String email;
    private String fullName;
    private Role role;
    private boolean otpSent;
    private String message;
    private TokenResponse tokens;

    // Provider specific
    private String businessName;
    private ProviderStatus providerStatus;

    public enum ProviderStatus {
        PENDING_APPROVAL, ACTIVE, SUSPENDED
    }
}
