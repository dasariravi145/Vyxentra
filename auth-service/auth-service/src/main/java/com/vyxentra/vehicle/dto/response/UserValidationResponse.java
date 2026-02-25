package com.vyxentra.vehicle.dto.response;

import com.vyxentra.vehicle.enums.UserRole;
import com.vyxentra.vehicle.enums.UserStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Set;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserValidationResponse {

    private String userId;
    private String mobileNumber;
    private Set<UserRole> roles;
    private UserStatus status;
    private boolean exists;
    private boolean active;
}