package com.vyxentra.vehicle.dto.request;

import com.vyxentra.vehicle.dto.AddressDTO;
import com.vyxentra.vehicle.enums.VehicleType;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Set;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserRegistrationRequest {

    @NotBlank(message = "First name is required")
    private String firstName;

    private String lastName;

    @NotBlank(message = "Mobile number is required")
    @Pattern(regexp = "^[0-9]{10}$", message = "Mobile number must be 10 digits")
    private String mobileNumber;

    @NotBlank(message = "Country code is required")
    @Pattern(regexp = "^\\+[1-9][0-9]{0,2}$", message = "Invalid country code format")
    private String countryCode;

    @Email(message = "Invalid email format")
    private String email;

    @Valid
    @NotNull(message = "Address is required")
    private AddressDTO address;

    private Set<VehicleType> ownedVehicles;

    private String emergencyContactName;

    @Pattern(regexp = "^[0-9]{10}$", message = "Emergency contact must be 10 digits")
    private String emergencyContactNumber;
}
