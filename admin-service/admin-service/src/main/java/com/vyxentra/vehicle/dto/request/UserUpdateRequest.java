package com.vyxentra.vehicle.dto.request;


import com.vyxentra.vehicle.enums.Role;
import jakarta.validation.constraints.Email;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserUpdateRequest {

    private String fullName;

    @Email(message = "Invalid email format")
    private String email;

    private String phone;

    private Role role;

    private Boolean active;

    private Boolean emailVerified;

    private Boolean phoneVerified;

    private String notes;
}
