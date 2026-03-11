package com.vyxentra.vehicle.dto.request;

import com.vyxentra.vehicle.enums.AddressType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AddAddressRequest {

    @NotBlank(message = "Address line 1 is required")
    private String addressLine1;

    private String addressLine2;

    @NotBlank(message = "City is required")
    private String city;

    @NotBlank(message = "State is required")
    private String state;

    @NotBlank(message = "Postal code is required")
    @Pattern(regexp = "^[1-9][0-9]{5}$", message = "Invalid postal code")
    private String postalCode;

    @NotBlank(message = "Country is required")
    private String country;

    private AddressType addressType;

    private Double latitude;
    private Double longitude;

    private String landmark;

    private boolean isDefault;

    private String contactName;
    private String contactPhone;
}