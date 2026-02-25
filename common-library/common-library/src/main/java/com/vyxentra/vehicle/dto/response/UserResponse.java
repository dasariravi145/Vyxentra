package com.vyxentra.vehicle.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.vyxentra.vehicle.dto.AddressDTO;
import com.vyxentra.vehicle.enums.UserRole;
import com.vyxentra.vehicle.enums.UserStatus;
import com.vyxentra.vehicle.enums.VehicleType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.Set;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class UserResponse {

    private String id;
    private String firstName;
    private String lastName;
    private String fullName;
    private String mobileNumber;
    private String countryCode;
    private String email;
    private AddressDTO address;
    private Set<UserRole> roles;
    private UserStatus status;
    private Set<VehicleType> ownedVehicles;
    private String emergencyContactName;
    private String emergencyContactNumber;
    private Instant createdAt;
    private Instant updatedAt;
    private Instant lastLoginAt;
    private boolean emailVerified;
    private boolean mobileVerified;

}
