package com.vyxentra.vehicle.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.vyxentra.vehicle.enums.UserRole;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Set;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class JwtResponse {

    private String token;
    private String refreshToken;
    private String tokenType;
    private Long expiresIn;
    private String userId;
    private String mobileNumber;
    private Set<UserRole> roles;
    private boolean profileComplete;

    public static JwtResponseBuilder builder() {
        return new JwtResponseBuilder()
                .tokenType("Bearer");
    }
}
