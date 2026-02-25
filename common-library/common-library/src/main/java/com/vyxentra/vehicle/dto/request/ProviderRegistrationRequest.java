package com.vyxentra.vehicle.dto.request;

import com.vyxentra.vehicle.dto.AddressDTO;
import com.vyxentra.vehicle.dto.GeoLocationDTO;
import com.vyxentra.vehicle.enums.ProviderType;
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

import java.math.BigDecimal;
import java.util.Map;
import java.util.Set;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProviderRegistrationRequest {

    @NotBlank(message = "Business name is required")
    private String businessName;

    @NotBlank(message = "Owner name is required")
    private String ownerName;

    @NotBlank(message = "Mobile number is required")
    @Pattern(regexp = "^[0-9]{10}$", message = "Mobile number must be 10 digits")
    private String mobileNumber;

    @Email(message = "Invalid email format")
    private String email;

    @NotBlank(message = "GST number is required")
    @Pattern(regexp = "^[0-9]{2}[A-Z]{5}[0-9]{4}[A-Z]{1}[1-9A-Z]{1}Z[0-9A-Z]{1}$",
            message = "Invalid GST format")
    private String gstNumber;

    @NotNull(message = "Provider type is required")
    private ProviderType providerType;

    @Valid
    @NotNull(message = "Address is required")
    private AddressDTO address;

    @Valid
    @NotNull(message = "Location coordinates are required")
    private GeoLocationDTO location;

    // Service capabilities
    private boolean supportsBike;
    private boolean supportsCar;

    // Pricing configuration
    private Map<VehicleType, BigDecimal> baseServiceCharge;
    private Map<VehicleType, BigDecimal> hourlyLaborRate;
    private Map<VehicleType, BigDecimal> emergencyMultiplier;

    // Business hours
    private String openingTime;
    private String closingTime;
    private Set<Integer> workingDays; // 1-7 (Monday-Sunday)

    // Documents
    private String panNumber;
    private String registrationCertificate;
    private String insuranceCertificate;
}
